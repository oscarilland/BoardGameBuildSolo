package netzerobelfast.sim;

import netzerobelfast.config.GameConfig;
import netzerobelfast.config.Rules.CharacterDef;
import netzerobelfast.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Plays many simulated games and summarises the balance (plan 006): how often Belfast reaches net
 * zero, how often each character and strategy wins against its fair share, and how often
 * bankruptcy and disasters happen. Used by {@code --simulate} and to re-tune after playtests (Q-10).
 *
 * <p>It's a guide, not a playtest: computer players aren't people.
 *
 * @param games             games played
 * @param netZeroRate       share of games where Belfast reached net zero
 * @param netZeroByMix      net zero rate for games that were mostly green, mixed or mostly polluting
 * @param byCharacter       wins and fair share by character name
 * @param byStrategy        wins and fair share by strategy name
 * @param bankruptcyGames   share of games with at least one bankruptcy
 * @param bankruptciesPerGame average bankruptcies per game
 * @param disastersPerGame  average disasters per game
 */
public record BalanceReport(
        int games,
        double netZeroRate,
        Map<String, Double> netZeroByMix,
        Map<String, Rate> byCharacter,
        Map<String, Rate> byStrategy,
        double bankruptcyGames,
        double bankruptciesPerGame,
        double disastersPerGame) {

    /**
     * How often something won.
     *
     * @param fairShare the win rate it would have if every player were equally likely to win
     */
    public record Rate(int wins, int played, double fairShare) {
        public double winRate() {
            return played == 0 ? 0 : (double) wins / played;
        }
    }

    private static final String[] NAMES = {"Ann", "Bo", "Cal", "Di", "Ed", "Fi"};

    /** Plays {@code games} games, each with 4-6 players, random characters and a random mix of strategies. */
    public static BalanceReport run(GameConfig config, int games, long seed) {
        Random setup = new Random(seed);
        int minPlayers = config.rules().game().minPlayers();
        int maxPlayers = config.rules().game().maxPlayers();
        List<String> characterIds = new ArrayList<>();
        Map<String, String> characterNames = new LinkedHashMap<>();
        for (CharacterDef character : config.rules().characters()) {
            characterIds.add(character.id());
            characterNames.put(character.id(), character.name());
        }

        int netZero = 0;
        Map<String, int[]> mix = new LinkedHashMap<>(); // mix -> {games, net zero}
        for (String m : new String[] {"mostly green", "mixed", "mostly polluting"}) {
            mix.put(m, new int[2]);
        }
        Map<String, double[]> characters = new LinkedHashMap<>(); // wins, played, sum of fair shares
        Map<String, double[]> strategies = new LinkedHashMap<>();
        int gamesWithBankruptcy = 0;
        int bankruptcies = 0;
        int disasters = 0;

        for (int g = 0; g < games; g++) {
            int count = minPlayers + setup.nextInt(maxPlayers - minPlayers + 1);
            List<String> ids = new ArrayList<>(characterIds);
            Collections.shuffle(ids, setup);
            List<Simulator.Seat> seats = new ArrayList<>();
            int green = 0;
            int polluting = 0;
            for (int i = 0; i < count; i++) {
                Strategy strategy = switch (setup.nextInt(3)) {
                    case 0 -> Strategies.green();
                    case 1 -> Strategies.polluter();
                    default -> Strategies.random(Math.min(0.95, Math.max(0.05, 0.6 + setup.nextGaussian() * 0.2)));
                };
                green += strategy.name().equals("green") ? 1 : 0;
                polluting += strategy.name().equals("polluter") ? 1 : 0;
                seats.add(new Simulator.Seat(NAMES[i], ids.get(i), strategy));
            }

            Simulator.Outcome outcome = Simulator.play(config, setup.nextLong(), seats);
            boolean reached = outcome.result().netZero();
            netZero += reached ? 1 : 0;
            String kind = green * 2 > count ? "mostly green" : polluting * 2 > count ? "mostly polluting" : "mixed";
            mix.get(kind)[0]++;
            mix.get(kind)[1] += reached ? 1 : 0;
            bankruptcies += outcome.bankruptcies();
            gamesWithBankruptcy += outcome.bankruptcies() > 0 ? 1 : 0;
            disasters += outcome.disasters();

            Player winner = outcome.result().winner();
            for (Map.Entry<Player, Strategy> seat : outcome.strategies().entrySet()) {
                boolean won = seat.getKey() == winner;
                tally(characters, characterNames.get(seat.getKey().character().id()), won, 1.0 / count);
                tally(strategies, seat.getValue().name(), won, 1.0 / count);
            }
        }

        Map<String, Double> netZeroByMix = new LinkedHashMap<>();
        mix.forEach((kind, counts) -> netZeroByMix.put(kind, counts[0] == 0 ? 0 : (double) counts[1] / counts[0]));
        return new BalanceReport(games, games == 0 ? 0 : (double) netZero / games, netZeroByMix,
                rates(characters), rates(strategies),
                games == 0 ? 0 : (double) gamesWithBankruptcy / games,
                games == 0 ? 0 : (double) bankruptcies / games,
                games == 0 ? 0 : (double) disasters / games);
    }

    private static void tally(Map<String, double[]> table, String key, boolean won, double fairShare) {
        double[] row = table.computeIfAbsent(key, k -> new double[3]);
        row[0] += won ? 1 : 0;
        row[1]++;
        row[2] += fairShare;
    }

    private static Map<String, Rate> rates(Map<String, double[]> table) {
        Map<String, Rate> rates = new LinkedHashMap<>();
        table.forEach((key, row) -> rates.put(key, new Rate((int) row[0], (int) row[1], row[2] / row[1])));
        return rates;
    }

    /** The report as text for the console. */
    public String format() {
        StringBuilder out = new StringBuilder();
        out.append(String.format(Locale.ROOT, "Net Zero Belfast balance report: %d simulated games%n", games));
        out.append(String.format(Locale.ROOT, "Belfast reached net zero in %s of games%n", percent(netZeroRate)));
        netZeroByMix.forEach((kind, rate) -> out.append(String.format(Locale.ROOT, "  %-17s %s%n", kind, percent(rate))));
        out.append("Win rate by character (fair share in brackets):\n");
        byCharacter.forEach((name, rate) -> out.append(line(name, rate)));
        out.append("Win rate by strategy:\n");
        byStrategy.forEach((name, rate) -> out.append(line(name, rate)));
        out.append(String.format(Locale.ROOT, "Bankruptcy: %s of games, %.2f per game%n", percent(bankruptcyGames), bankruptciesPerGame));
        out.append(String.format(Locale.ROOT, "Disasters: %.1f per game%n", disastersPerGame));
        out.append("A guide, not a playtest: computer players aren't people.\n");
        return out.toString();
    }

    private static String line(String name, Rate rate) {
        return String.format(Locale.ROOT, "  %-19s %s (%s), %d games%n", name, percent(rate.winRate()), percent(rate.fairShare()), rate.played());
    }

    private static String percent(double share) {
        return String.format(Locale.ROOT, "%.0f%%", share * 100);
    }
}
