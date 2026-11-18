package netzerobelfast.config;

import netzerobelfast.config.BoardDef.SquareDef;
import netzerobelfast.config.BoardDef.SquareType;
import netzerobelfast.config.CardDef.Option;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static netzerobelfast.config.GameConfig.BOARD_FILE;
import static netzerobelfast.config.GameConfig.CARDS_FILE;
import static netzerobelfast.config.GameConfig.CONFIG_FILE;

/**
 * Checks that the loaded values make sense together, after {@link ConfigParser} has checked
 * that every key exists with the right type. The first problem found stops loading.
 *
 * <p>These are structural checks (ids unique, prices inside their range, every square on a
 * route), not the board's design numbers. How many squares or cards there are is data, so it
 * isn't checked here; the tests check the real files' counts instead (NFR-07).
 */
final class ConfigValidator {

    private ConfigValidator() {}

    static void validate(Rules rules, BoardDef board, List<CardDef> cards) {
        validateRules(rules);
        validateBoard(rules, board);
        validateCards(cards);
    }

    // ---- config.json --------------------------------------------------------

    private static void validateRules(Rules rules) {
        Rules.Game game = rules.game();
        check(game.minPlayers() >= 1, CONFIG_FILE, "game.minPlayers", "is " + game.minPlayers() + ", expected at least 1");
        check(game.minPlayers() <= game.maxPlayers(), CONFIG_FILE, "game.minPlayers",
                "is " + game.minPlayers() + ", but game.maxPlayers is " + game.maxPlayers());
        check(game.maxNameLength() >= 1, CONFIG_FILE, "game.maxNameLength", "is " + game.maxNameLength() + ", expected at least 1");
        check(game.rounds() >= 1, CONFIG_FILE, "game.rounds", "is " + game.rounds() + ", expected at least 1");
        check(game.yearsPerRound() >= 1, CONFIG_FILE, "game.yearsPerRound", "is " + game.yearsPerRound() + ", expected at least 1");
        int expectedEnd = game.startYear() + game.rounds() * game.yearsPerRound();
        check(game.endYear() == expectedEnd, CONFIG_FILE, "game.endYear",
                "is " + game.endYear() + ", but startYear + rounds × yearsPerRound is " + expectedEnd);

        List<Rules.CharacterDef> characters = rules.characters();
        check(characters.size() >= game.maxPlayers(), CONFIG_FILE, "characters",
                "has " + characters.size() + ", but each of up to " + game.maxPlayers() + " players needs a different one");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < characters.size(); i++) {
            Rules.CharacterDef character = characters.get(i);
            check(ids.add(character.id()), CONFIG_FILE, "characters[" + i + "].id", "'" + character.id() + "' is used twice");
            check(character.startMoney() >= 0, CONFIG_FILE, "characters[" + i + "].startMoney", "is negative");
        }

        checkRange(rules.polluting().minPrice(), rules.polluting().maxPrice(), "squares.polluting.priceRange");
        checkRange(rules.green().minPrice(), rules.green().maxPrice(), "squares.greenProject.priceRange");

        Rules.Resilience resilience = rules.resilience();
        check(resilience.levelCosts().size() == resilience.maxLevel(), CONFIG_FILE, "squares.resilience.levelCosts",
                "has " + resilience.levelCosts().size() + " costs, but maxLevel is " + resilience.maxLevel());
        check(resilience.disasterCostCut().size() == resilience.maxLevel(), CONFIG_FILE, "squares.resilience.disasterCostCut",
                "has " + resilience.disasterCostCut().size() + " cuts, but maxLevel is " + resilience.maxLevel());
        for (int i = 0; i < resilience.disasterCostCut().size(); i++) {
            double cut = resilience.disasterCostCut().get(i);
            check(cut > 0 && cut <= 1, CONFIG_FILE, "squares.resilience.disasterCostCut[" + i + "]",
                    "is " + cut + ", expected more than 0 and at most 1");
        }

        Rules.Meter meter = rules.meter();
        for (int i = 0; i < meter.interimTargets().size(); i++) {
            int round = meter.interimTargets().get(i).round();
            check(round >= 1 && round <= game.rounds(), CONFIG_FILE, "climateMeter.interimTargets[" + i + "].round",
                    "is " + round + ", expected 1-" + game.rounds());
        }
        validateBands(meter);

        Rules.Disasters disasters = rules.disasters();
        check(disasters.floodLostIncomeRounds() >= 1, CONFIG_FILE, "disasters.FLOOD.lostIncomeRounds", "should be at least 1");
        check(disasters.stormClosedRounds() >= 1, CONFIG_FILE, "disasters.STORM.closedRounds", "should be at least 1");
        check(disasters.droughtLostGPRounds() >= 1, CONFIG_FILE, "disasters.DROUGHT.lostGPRounds", "should be at least 1");
        for (Map.Entry<String, Rules.Explanation> entry : disasters.explanations().entrySet()) {
            check(!entry.getValue().sources().isEmpty(), CONFIG_FILE, "disasters." + entry.getKey() + ".sources",
                    "is empty; every explanation needs a source (NFR-10)");
        }
    }

    private static void validateBands(Rules.Meter meter) {
        List<Rules.DangerBand> bands = meter.dangerBands();
        check(!bands.isEmpty(), CONFIG_FILE, "climateMeter.dangerBands", "is empty");
        Integer previousMax = null;
        for (int i = 0; i < bands.size(); i++) {
            Rules.DangerBand band = bands.get(i);
            String key = "climateMeter.dangerBands[" + i + "]";
            check(band.band() == i + 1, CONFIG_FILE, key + ".band", "is " + band.band() + ", expected " + (i + 1));
            boolean last = i == bands.size() - 1;
            if (last) {
                check(band.maxMeter() == null, CONFIG_FILE, key + ".maxMeter", "should be null: the top band has no upper limit");
            } else {
                check(band.maxMeter() != null, CONFIG_FILE, key + ".maxMeter", "is null, but only the top band can be open-ended");
                check(previousMax == null || band.maxMeter() > previousMax, CONFIG_FILE, key + ".maxMeter",
                        "is " + band.maxMeter() + ", but must be higher than the band below (" + previousMax + ")");
                previousMax = band.maxMeter();
            }
        }
        check(meter.belowZeroBand() >= 1 && meter.belowZeroBand() <= bands.size(), CONFIG_FILE,
                "climateMeter.belowZeroUsesBand", "is " + meter.belowZeroBand() + ", expected 1-" + bands.size());
    }

    private static void checkRange(int min, int max, String key) {
        check(min <= max, CONFIG_FILE, key, "is [" + min + ", " + max + "], but the lowest price is above the highest");
    }

    // ---- board.json ---------------------------------------------------------

    private static void validateBoard(Rules rules, BoardDef board) {
        List<SquareDef> squares = board.squares();
        check(!squares.isEmpty(), BOARD_FILE, "squares", "is empty");

        for (int i = 0; i < squares.size(); i++) {
            SquareDef square = squares.get(i);
            String key = "squares[" + i + "]";
            check(square.id() == i, BOARD_FILE, key + ".id",
                    "is " + square.id() + ", expected " + i + " (squares must be listed in id order, starting at 0)");
            check(board.districts().containsKey(square.district()), BOARD_FILE, key + ".district",
                    "is '" + square.district() + "', which isn't in districts");
            validatePrice(rules, square, key);
        }

        // Every square is on exactly one route: the loop or the City Centre.
        List<Integer> onRoutes = new ArrayList<>(board.loop());
        onRoutes.addAll(board.cityCentreRoute());
        Set<Integer> seen = new HashSet<>();
        for (int id : onRoutes) {
            check(id >= 0 && id < squares.size(), BOARD_FILE, "loop/cityCentreRoute", "lists square " + id + ", which doesn't exist");
            check(seen.add(id), BOARD_FILE, "loop/cityCentreRoute", "list square " + id + " more than once");
        }
        check(seen.size() == squares.size(), BOARD_FILE, "loop/cityCentreRoute",
                "cover " + seen.size() + " of the " + squares.size() + " squares; every square must be on one of them");

        for (int i = 0; i < board.junctions().size(); i++) {
            int id = board.junctions().get(i);
            check(board.loop().contains(id), BOARD_FILE, "junctions[" + i + "]", "is " + id + ", which isn't on the loop");
        }
        check(board.junctions().contains(board.westJunction()), BOARD_FILE, "cityCentreRoute.connects.westJunction",
                "is " + board.westJunction() + ", which isn't in junctions");
        check(board.junctions().contains(board.eastJunction()), BOARD_FILE, "cityCentreRoute.connects.eastJunction",
                "is " + board.eastJunction() + ", which isn't in junctions");

        for (int i = 0; i < board.gliderStops().size(); i++) {
            int id = board.gliderStops().get(i);
            boolean isStop = id >= 0 && id < squares.size() && squares.get(id).type() == SquareType.GLIDER_STOP;
            check(isStop, BOARD_FILE, "gliderStops[" + i + "]", "is " + id + ", which isn't a GLIDER_STOP square");
        }
        for (SquareDef square : squares) {
            if (square.type() == SquareType.GLIDER_STOP) {
                check(board.gliderStops().contains(square.id()), BOARD_FILE, "gliderStops",
                        "doesn't list square " + square.id() + " (" + square.name() + "), which is a GLIDER_STOP");
            }
        }
        check(board.gliderStops().size() >= 2, BOARD_FILE, "gliderStops", "needs at least 2 stops for a ride to go anywhere");
    }

    private static void validatePrice(Rules rules, SquareDef square, String key) {
        if (!square.ownable()) {
            check(square.price() == null, BOARD_FILE, key + ".price", "is set, but " + square.type() + " squares can't be bought");
            return;
        }
        check(square.price() != null, BOARD_FILE, key + ".price", "is missing; " + square.type() + " squares need a price");
        int min = square.type() == SquareType.POLLUTING ? rules.polluting().minPrice() : rules.green().minPrice();
        int max = square.type() == SquareType.POLLUTING ? rules.polluting().maxPrice() : rules.green().maxPrice();
        check(square.price() >= min && square.price() <= max, BOARD_FILE, key + ".price",
                "is " + square.price() + ", expected " + min + "-" + max + " for " + square.type());
    }

    // ---- climate_cards.json -------------------------------------------------

    private static void validateCards(List<CardDef> cards) {
        check(!cards.isEmpty(), CARDS_FILE, "cards", "is empty");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < cards.size(); i++) {
            CardDef card = cards.get(i);
            String key = "cards[" + i + "]";
            check(ids.add(card.id()), CARDS_FILE, key + ".id", "'" + card.id() + "' is used twice");
            check(!card.sources().isEmpty(), CARDS_FILE, key + ".sources", "is empty; every fact needs a source (NFR-10)");

            switch (card.type()) {
                case QUIZ -> {
                    check(card.answers().size() >= 2, CARDS_FILE, key + ".answers", "needs at least 2 answers");
                    check(card.correctIndex() >= 0 && card.correctIndex() < card.answers().size(), CARDS_FILE,
                            key + ".correctIndex", "is " + card.correctIndex() + ", expected 0-" + (card.answers().size() - 1));
                    check(card.rewardGP() == null || card.rewardGP() >= 0, CARDS_FILE, key + ".reward.gp", "is negative");
                }
                case CHOICE -> {
                    check(card.options().size() == 2, CARDS_FILE, key + ".options",
                            "has " + card.options().size() + ", expected 2 (option A and option B)");
                    // Q-12: options the player can't afford are disabled, so one must never cost money.
                    boolean affordable = false;
                    for (Option option : card.options()) {
                        affordable |= option.effect().money() >= 0;
                    }
                    check(affordable, CARDS_FILE, key + ".options",
                            "all cost money; at least one option must have money of 0 or more, so every player can choose (FR-10 AC-8)");
                }
                case EVENT -> {
                    // The parser has already required an effect.
                }
            }
        }
    }

    private static void check(boolean ok, String file, String key, String problem) {
        if (!ok) {
            throw new ConfigException(file, key, problem);
        }
    }
}
