package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.model.GameResult.MeterLine;
import netzerobelfast.model.GameResult.PlayerScore;
import netzerobelfast.model.GameResult.TieBreak;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scores a finished game (FR-14) and gathers the end-of-game report data (FR-16).
 *
 * <ul>
 *   <li>Net zero reached: Legacy = GP + (money − loan repayments) ÷ 5.</li>
 *   <li>Net zero missed: money is worthless; Legacy = GP − 5 × Polluting squares still owned.</li>
 * </ul>
 *
 * <p>The winner has the highest score; ties go to the most Green Projects, then the fewest Polluting
 * squares, then a die roll-off that re-rolls ties (Q-01), so there is always exactly one winner.
 */
final class Scoring {

    private Scoring() {}

    static GameResult score(Game game) {
        Rules rules = game.rules();
        boolean netZero = game.meter().netZero();
        int divisor = rules.scoring().moneyDivisor();
        int penalty = rules.scoring().missedPollutingPenaltyGP();

        List<PlayerScore> scores = new ArrayList<>();
        for (Player player : game.players()) {
            int polluting = 0;
            int green = 0;
            for (Square square : game.board().squares()) {
                if (square instanceof PollutingSquare p && p.owner() == player) {
                    polluting++;
                } else if (square instanceof GreenProjectSquare g && g.owner() == player) {
                    green++;
                }
            }
            int scaled = netZero
                    ? divisor * player.greenPoints() + (player.money() - player.loanRepayments())
                    : divisor * (player.greenPoints() - penalty * polluting);
            scores.add(new PlayerScore(player, player.greenPoints(), player.money(), player.loanRepayments(),
                    polluting, green, new LegacyScore(scaled, divisor)));
        }

        // Best first: score, then more Green Projects, then fewer Polluting squares. The sort is
        // stable, so anyone still level stays in seat order until the roll-off below.
        Comparator<PlayerScore> best = Comparator.comparing(PlayerScore::score).reversed()
                .thenComparing(Comparator.comparingInt(PlayerScore::greenOwned).reversed())
                .thenComparingInt(PlayerScore::pollutingOwned);
        scores.sort(best);

        TieBreak decidedBy = TieBreak.SCORE;
        Map<Player, List<Integer>> rollOff = new LinkedHashMap<>();
        if (scores.size() > 1) {
            PlayerScore first = scores.get(0);
            PlayerScore second = scores.get(1);
            if (first.score().compareTo(second.score()) == 0) {
                decidedBy = first.greenOwned() != second.greenOwned() ? TieBreak.GREEN_PROJECTS
                        : first.pollutingOwned() != second.pollutingOwned() ? TieBreak.FEWER_POLLUTING
                        : TieBreak.ROLL_OFF;
            }
            if (decidedBy == TieBreak.ROLL_OFF) {
                List<PlayerScore> tied = new ArrayList<>();
                for (PlayerScore s : scores) {
                    if (best.compare(s, first) == 0) {
                        tied.add(s);
                    }
                }
                PlayerScore winner = rollOff(tied, game.dice(), rollOff);
                scores.remove(winner);
                scores.add(0, winner);
            }
        }

        return new GameResult(netZero, game.meter().value(), List.copyOf(scores), scores.get(0).player(),
                decidedBy, rollOff, game.meter().history(), meterLines(game), pollutingShares(game));
    }

    /** Every tied player rolls one die; the highest wins, and anyone level on the highest rolls again. */
    private static PlayerScore rollOff(List<PlayerScore> tied, Dice dice, Map<Player, List<Integer>> rolls) {
        List<PlayerScore> rolling = tied;
        while (rolling.size() > 1) {
            int highest = 0;
            Map<PlayerScore, Integer> thisRound = new LinkedHashMap<>();
            for (PlayerScore s : rolling) {
                int roll = dice.rollOne();
                thisRound.put(s, roll);
                rolls.computeIfAbsent(s.player(), p -> new ArrayList<>()).add(roll);
                highest = Math.max(highest, roll);
            }
            List<PlayerScore> stillLevel = new ArrayList<>();
            for (Map.Entry<PlayerScore, Integer> entry : thisRound.entrySet()) {
                if (entry.getValue() == highest) {
                    stillLevel.add(entry.getKey());
                }
            }
            rolling = stillLevel;
        }
        rolls.replaceAll((player, list) -> List.copyOf(list));
        return rolling.get(0);
    }

    /** The interim targets and the net zero line, for the meter graph. */
    private static List<MeterLine> meterLines(Game game) {
        Rules.Meter meter = game.rules().meter();
        List<MeterLine> lines = new ArrayList<>();
        for (Rules.InterimTarget target : meter.interimTargets()) {
            lines.add(new MeterLine(target.year() + " target", target.year(), target.maxMeter()));
        }
        lines.add(new MeterLine("Net zero", null, meter.netZeroAtOrBelow()));
        return List.copyOf(lines);
    }

    /** Each player's polluting-square-rounds as a percentage of the city's, to one decimal place; all 0 if none. */
    private static Map<Player, BigDecimal> pollutingShares(Game game) {
        int total = 0;
        for (Player player : game.players()) {
            total += player.pollutingSquareRounds();
        }
        Map<Player, BigDecimal> shares = new LinkedHashMap<>();
        for (Player player : game.players()) {
            BigDecimal share = total == 0 ? BigDecimal.ZERO.setScale(1)
                    : BigDecimal.valueOf(player.pollutingSquareRounds() * 100L)
                            .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
            shares.put(player, share);
        }
        return shares;
    }
}
