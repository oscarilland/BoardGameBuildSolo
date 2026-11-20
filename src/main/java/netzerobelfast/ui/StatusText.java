package netzerobelfast.ui;

import netzerobelfast.config.CardDef.Effect;
import netzerobelfast.config.CardDef.Option;
import netzerobelfast.config.CardDef.Source;
import netzerobelfast.config.GameConfig;
import netzerobelfast.config.Rules;
import netzerobelfast.model.CardResult;
import netzerobelfast.model.ChoiceCard;
import netzerobelfast.model.ClimateCardSquare;
import netzerobelfast.model.CoFundingDeal;
import netzerobelfast.model.DisasterResult;
import netzerobelfast.model.EventCard;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.ReturnAttempt;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.GameResult.PlayerScore;
import netzerobelfast.model.GliderStop;
import netzerobelfast.model.GreenProjectSquare;
import netzerobelfast.model.OwnableSquare;
import netzerobelfast.model.Player;
import netzerobelfast.model.PollutingSquare;
import netzerobelfast.model.QuizCard;
import netzerobelfast.model.ResilienceSquare;
import netzerobelfast.model.RoundReport;
import netzerobelfast.model.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Every piece of text the game screen shows, worked out from the model (FR-15, NFR-05 AC-3).
 * No Swing here, so it can be tested headless. The Swing panels only draw these strings.
 *
 * <p>It only reads the model; it never decides a rule. Where a number is shown, it comes from
 * the model or the data.
 */
public final class StatusText {

    /**
     * Words for the danger bands, lowest first. These are words for people, not balance
     * values: the bands' limits come from the data, and a band beyond this list is shown by number only.
     */
    private static final List<String> BAND_WORDS = List.of("low", "moderate", "high", "severe");

    private final Rules rules;
    private final Map<String, String> districts;

    public StatusText(GameConfig config) {
        this.rules = config.rules();
        this.districts = config.board().districts();
    }

    // ---- Top bar (FR-15 AC-1, AC-2) ----------------------------------------------

    /** "Round 4 of 12 · Year 2032", or "Game over · 2050" after the last round. */
    public String roundLine(Game game) {
        if (game.phase() == Game.Phase.GAME_OVER) {
            return "Game over · " + rules.game().endYear();
        }
        return "Round " + game.round() + " of " + rules.game().rounds() + " · Year " + game.year();
    }

    /** The meter's label. It has no unit, so it says what 50 means (Q-03). */
    public String meterTitle() {
        return "Climate Meter (" + rules.game().startYear() + " = " + rules.meter().start() + ")";
    }

    /** "33 · band 3 (high)": the value, then the band as a number and a word (NFR-05 AC-3). */
    public String meterValue(Game game) {
        return game.meter().value() + " · " + bandText(game.meter().dangerBand());
    }

    /** "band 3 (high)". */
    public static String bandText(int band) {
        String word = band >= 1 && band <= BAND_WORDS.size() ? " (" + BAND_WORDS.get(band - 1) + ")" : "";
        return "band " + band + word;
    }

    /**
     * The next target to mark on the meter bar (FR-15 AC-2): the next interim target still to be
     * checked (26 for 2030 until round 3, then 11 for 2040 until round 8), then net zero at the end.
     */
    public Target nextTarget(Game game) {
        for (Rules.InterimTarget target : rules.meter().interimTargets()) {
            if (game.round() <= target.round()) {
                return new Target(target.year() + " target", target.maxMeter());
            }
        }
        return new Target(rules.game().endYear() + " net zero", rules.meter().netZeroAtOrBelow());
    }

    /** A value to mark on the meter bar, and its label. */
    public record Target(String label, int value) {
        public String text() {
            return label + " " + value;
        }
    }

    // ---- Player panel (FR-15 AC-3 to AC-6) ------------------------------------------

    /** "1 · Aoife · Farmer (turn)": seat number (as on the counter), name, character, and "(turn)" for the current player. */
    public String playerTitle(Game game, Player player) {
        String turn = player == game.currentPlayer() && game.phase() != Game.Phase.GAME_OVER ? " (turn)" : "";
        return seat(game, player) + " · " + player.name() + " · " + player.character().name() + turn;
    }

    /** "£21M · 24 GP · 2 green, 1 polluting". */
    public String playerMoney(Game game, Player player) {
        return "£" + player.money() + "M · " + player.greenPoints() + " GP · " + squareCounts(game, player);
    }

    /** "Resilience 1 · Funds: Rooftop Solar", or "Resilience 0 · No co-funding deal" (FR-15 AC-6). */
    public String playerExtras(Game game, Player player) {
        CoFundingDeal deal = player.coFundingDeal();
        String funds = deal == null ? "No co-funding deal" : "Funds: " + game.board().square(deal.square()).name();
        return "Resilience " + player.resilienceLevel() + " · " + funds;
    }

    /** "BANKRUPT (misses turns until a 6)", or empty for a player who isn't (FR-15 AC-5). */
    public Optional<String> playerWarning(Player player) {
        if (!player.bankrupt()) {
            return Optional.empty();
        }
        return Optional.of("BANKRUPT: rolls a " + rules.bankruptcy().escapeRoll() + " to return");
    }

    /** "2 green, 1 polluting". */
    public String squareCounts(Game game, Player player) {
        int green = 0;
        int polluting = 0;
        for (Square square : game.board().squares()) {
            if (square instanceof OwnableSquare owned && owned.owner() == player) {
                if (square instanceof GreenProjectSquare) {
                    green++;
                } else if (square instanceof PollutingSquare) {
                    polluting++;
                }
            }
        }
        return green + " green, " + polluting + " polluting";
    }

    /** The seat number shown on a player's counter and card: 1 to 6, in setup order. */
    public static int seat(Game game, Player player) {
        return game.players().indexOf(player) + 1;
    }

    // ---- The board (NFR-05 AC-2) ------------------------------------------------------

    /** The type label every square shows, so the board can be read without colour. */
    public static String typeLabel(Square square) {
        return switch (square) {
            case PollutingSquare p -> "Polluting";
            case GreenProjectSquare g -> g.nature() ? "Nature" : "Green";
            case ResilienceSquare r -> "Resilience";
            case GliderStop s -> "Glider";
            case ClimateCardSquare c -> "Card";
            default -> "";
        };
    }

    /** The second line on a square: price and owner's seat, or what it does. */
    public String squareDetail(Game game, Square square) {
        if (square instanceof OwnableSquare owned) {
            return owned.owner() == null ? "£" + owned.price() + "M" : "Owner " + seat(game, owned.owner());
        }
        return switch (square) {
            case GliderStop s -> "£" + rules.actions().gliderFare() + "M fare";
            case ResilienceSquare r -> "Protect";
            default -> "Draw a card";
        };
    }

    /** Marks shown on a square for things that change what it earns this round. */
    public List<String> squareMarks(Game game, Square square) {
        List<String> marks = new ArrayList<>();
        if (square instanceof PollutingSquare p && p.lobbiedThisRound()) {
            marks.add("Lobbied");
        }
        if (square instanceof OwnableSquare && game.disasters().shut(square)) {
            marks.add("Closed");
        }
        return marks;
    }

    /** "Aoife is on Harbour Gas Plant (Polluting, £9M, unowned)." under the action bar. */
    public String whereLine(Game game) {
        Player player = game.currentPlayer();
        Square square = game.board().square(player.position());
        StringBuilder text = new StringBuilder(player.name() + " is on " + square.name() + " (" + typeLabel(square));
        if (square instanceof OwnableSquare owned) {
            text.append(", £").append(owned.price()).append("M, ")
                    .append(owned.owner() == null ? "unowned" : "owned by " + owned.owner().name());
        }
        text.append(").");
        game.lastMove().filter(move -> game.phase() == Game.Phase.MOVE_DONE && !move.path().isEmpty()
                        && move.path().get(move.path().size() - 1) == player.position())
                .ifPresent(move -> text.insert(0, "Rolled " + move.diceTotal() + ". "));
        return text.toString();
    }

    /** What the game is waiting for, in words, shown above the buttons. */
    public String prompt(Game game) {
        String name = game.currentPlayer().name();
        return switch (game.phase()) {
            case AWAITING_RETURN_ROLL -> name + " is bankrupt: roll one die. A " + rules.bankruptcy().escapeRoll() + " brings you back.";
            case AWAITING_ROLL -> name + ", roll the dice.";
            case AWAITING_ROUTE -> name + " is at a junction with " + game.stepsLeft() + " steps to go: which way?";
            case AWAITING_DIRECTION -> name + " is on the City Centre route with " + game.stepsLeft() + " steps to go: which way?";
            case AWAITING_QUIZ_ANSWER, AWAITING_CHOICE -> name + " drew a Climate Card.";
            case MOVE_DONE -> game.currentPlayer().bankrupt() ? name + " sits this turn out." : name + ": take an action, or end your turn.";
            case GAME_OVER -> "The game is over.";
        };
    }

    // ---- Climate Cards (FR-10, NFR-10) -------------------------------------------------

    /** "+£6M · Meter +2", or "No change": a card option's or event's effects, shown before choosing. */
    public static String effectText(Effect effect) {
        List<String> parts = new ArrayList<>();
        if (effect.money() != 0) {
            parts.add((effect.money() > 0 ? "+£" : "-£") + Math.abs(effect.money()) + "M");
        }
        if (effect.gp() != 0) {
            parts.add(signed(effect.gp()) + " GP");
        }
        if (effect.meter() != 0) {
            parts.add("Meter " + signed(effect.meter()));
        }
        return parts.isEmpty() ? "No change" : String.join(" · ", parts);
    }

    /** "A: Sell the land (+£6M · Meter +2)". */
    public static String optionText(ChoiceCard card, int index) {
        Option option = card.option(index);
        return (char) ('A' + index) + ": " + option.label() + " (" + effectText(option.effect()) + ")";
    }

    /** What the card did, for the result pop-up. */
    public static String cardOutcome(CardResult result) {
        return switch (result.card()) {
            case QuizCard quiz -> result.correct()
                    ? "Correct! +" + result.gpChange() + " GP."
                    : "Not quite. The answer was " + quiz.answers().get(correctIndex(quiz)) + ".";
            case ChoiceCard choice -> "You chose " + optionText(choice, result.option()) + ".";
            case EventCard event -> effectLine(result) + (result.bankrupt() ? " You can't pay it all, so you are bankrupt." : "");
        };
    }

    private static String effectLine(CardResult result) {
        return effectText(new Effect(result.moneyChange(), result.gpChange(), result.meterChange())) + ".";
    }

    /** The quiz's correct answer. The card only checks answers, so find the one it accepts. */
    static int correctIndex(QuizCard quiz) {
        for (int i = 0; i < quiz.answers().size(); i++) {
            if (quiz.checkAnswer(i)) {
                return i;
            }
        }
        throw new IllegalStateException(quiz.id() + " has no correct answer");
    }

    /** "Source: DAERA (https://...)": title and web address, not clickable (NFR-10 AC-3). */
    public static String sourcesText(List<Source> sources) {
        List<String> parts = new ArrayList<>();
        for (Source source : sources) {
            parts.add(source.title() + " (" + source.url() + ")");
        }
        return (sources.size() == 1 ? "Source: " : "Sources: ") + String.join("; ", parts);
    }

    // ---- End of round (FR-11, FR-12, FR-17) -------------------------------------------

    /** The round report pop-up, one line per step, in the order the steps ran. */
    public List<String> roundReport(Game game, RoundReport report) {
        List<String> lines = new ArrayList<>();
        lines.add("End of round " + report.round() + " (" + report.year() + ")");
        lines.add("1. Squares paid their income and Green Points.");
        lines.add("2. Carbon tax: owners lost " + rules.polluting().carbonTaxGP() + " GP for each Polluting square.");
        lines.add("3. Climate Meter: " + report.meterBefore() + " → " + report.meterAfter()
                + ", " + bandText(report.band()) + ".");
        int step = 4;
        if (report.target() != null) {
            Rules.InterimTarget target = report.target();
            lines.add(step++ + ". " + target.year() + " target (" + target.maxMeter() + " or below): "
                    + (report.targetMet() ? "met. Well done, Belfast!"
                    : "missed. Owners lost " + rules.meter().interimMissPenaltyGP() + " GP for each Polluting square."));
        }
        lines.add(step++ + ". Disaster check: rolled " + report.disasterRoll() + " (a disaster on " + report.band()
                + " or less): " + (report.disaster() == null ? "no disaster." : disasterName(report.disaster()) + "!"));
        if (report.disaster() != null) {
            lines.addAll(disasterLines(game, report.disaster()));
        }
        if (game.phase() != Game.Phase.GAME_OVER) {
            lines.add(step + ". Now round " + game.round() + " · " + game.year() + ".");
        } else {
            lines.add(step + ". That was the last round.");
        }
        return lines;
    }

    private List<String> disasterLines(Game game, DisasterResult disaster) {
        List<String> lines = new ArrayList<>();
        if (disaster.skipped()) {
            lines.add("• Nothing was there to hit, so nothing happened.");
        } else {
            switch (disaster.disaster()) {
                case FLOOD -> lines.add("• " + districts.getOrDefault(disaster.district(), disaster.district())
                        + " flooded: its squares earn nothing for " + rounds(rules.disasters().floodLostIncomeRounds()) + ".");
                case STORM -> lines.add("• " + game.board().square(disaster.square()).name()
                        + " was damaged: closed for " + rounds(rules.disasters().stormClosedRounds()) + ".");
                case HEATWAVE -> lines.add("• Everyone paid for cooling.");
                case DROUGHT -> lines.add("• Nature projects earn no GP for " + rounds(rules.disasters().droughtLostGPRounds()) + ".");
            }
            disaster.payments().forEach((player, paid) -> {
                if (paid > 0) {
                    lines.add("• " + player.name() + " paid £" + paid + "M.");
                }
            });
            for (Player player : disaster.bankrupted()) {
                lines.add("• " + player.name() + " couldn't pay and is bankrupt.");
            }
        }
        lines.add("Why: " + disaster.explanation());
        lines.add(sourcesText(disaster.sources()));
        return lines;
    }

    static String disasterName(DisasterResult disaster) {
        return switch (disaster.disaster()) {
            case FLOOD -> "Flood";
            case STORM -> "Storm";
            case HEATWAVE -> "Heatwave";
            case DROUGHT -> "Drought";
        };
    }

    // ---- Bankruptcy (FR-13) -------------------------------------------------------------

    /** What happened when a bankrupt player's turn began. */
    public String returnText(Player player, ReturnAttempt attempt) {
        Rules.Bankruptcy bankruptcy = rules.bankruptcy();
        String loan = " and is back with a £" + bankruptcy.loanOnReturn() + "M loan (repay £"
                + bankruptcy.repayAtEnd() + "M at the end).";
        if (attempt.returned() && attempt.roll() == 0) {
            return player.name() + " has sat out " + bankruptcy.maxRounds() + " turns" + loan;
        }
        if (attempt.returned()) {
            return player.name() + " rolled a " + attempt.roll() + loan;
        }
        return player.name() + " rolled a " + attempt.roll() + " and needed a " + bankruptcy.escapeRoll()
                + ", so sits this turn out.";
    }

    // ---- The end (FR-14) ------------------------------------------------------------------

    /** The final-scores screen until plan 009's report replaces it. */
    public List<String> finalScores(Game game, GameResult result) {
        List<String> lines = new ArrayList<>();
        lines.add(result.netZero()
                ? "Belfast reached net zero! The meter ended at " + result.finalMeter() + "."
                : "Belfast missed net zero: the meter ended at " + result.finalMeter() + ", so money is worthless.");
        lines.add(result.netZero()
                ? "Score = GP + money ÷ " + rules.scoring().moneyDivisor() + "."
                : "Score = GP, less " + rules.scoring().missedPollutingPenaltyGP() + " for each Polluting square.");
        int place = 1;
        for (PlayerScore score : result.ranking()) {
            lines.add(place++ + ". " + score.player().name() + ": " + score.score().display() + " (" + score.greenPoints()
                    + " GP, £" + score.money() + "M, " + score.greenOwned() + " green, " + score.pollutingOwned() + " polluting)");
        }
        lines.add("Winner: " + result.winner().name() + switch (result.decidedBy()) {
            case SCORE -> ".";
            case GREEN_PROJECTS -> ", on a tie, by owning the most Green Projects.";
            case FEWER_POLLUTING -> ", on a tie, by owning the fewest Polluting squares.";
            case ROLL_OFF -> ", on a tie, by a dice roll-off.";
        });
        return lines;
    }

    private static String rounds(int count) {
        return count == 1 ? "1 round" : count + " rounds";
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }
}
