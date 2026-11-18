package netzerobelfast.sim;

import netzerobelfast.config.CardDef.Effect;
import netzerobelfast.model.Action;
import netzerobelfast.model.Action.Kind;
import netzerobelfast.model.ChoiceCard;
import netzerobelfast.model.Game;
import netzerobelfast.model.GreenProjectSquare;
import netzerobelfast.model.PollutingSquare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The three computer-player strategies used for testing and balance (plan 006). They follow the
 * Python balance simulation's bots, so the two sets of results can be compared; they're a guide
 * to balance, not a substitute for playtests.
 */
public final class Strategies {

    private Strategies() {}

    /** Leans green or polluting at random (a lean near 1 is mostly green). */
    public static Strategy random(double greenLean) {
        return new Leaning(greenLean);
    }

    /** Buys Green Projects, converts, co-funds and buys Resilience; picks the greener option. */
    public static Strategy green() {
        return new Green();
    }

    /** Buys Polluting squares, never converts, picks the money option and lobbies rivals. */
    public static Strategy polluter() {
        return new Polluter();
    }

    private static boolean onGreen(Game game, Action action) {
        return game.board().square(action.square()) instanceof GreenProjectSquare;
    }

    private static boolean onPolluting(Game game, Action action) {
        return game.board().square(action.square()) instanceof PollutingSquare;
    }

    private static ChoiceCard choiceCard(Game game) {
        return (ChoiceCard) game.pendingCard().orElseThrow();
    }

    // ---- Random, with a lean --------------------------------------------------

    private record Leaning(double lean) implements Strategy {

        @Override
        public String name() {
            return "random";
        }

        @Override
        public Optional<Action> action(Game game, List<Action> available, Random random) {
            List<Action> shuffled = new ArrayList<>(available);
            Collections.shuffle(shuffled, random);
            for (Action action : shuffled) {
                double chance = switch (action.kind()) {
                    case BUY -> onGreen(game, action) ? lean + 0.2 : (1 - lean) + 0.15;
                    case CONVERT, CO_FUND -> lean;
                    case LOBBY -> lean * 0.4;
                    case BUY_RESILIENCE -> 0.35;
                    case RIDE_GLIDER -> 0.3;
                };
                if (random.nextDouble() < chance) {
                    return Optional.of(action);
                }
            }
            return Optional.empty();
        }

        @Override
        public int option(Game game, List<Integer> affordable, Random random) {
            // The greener option is the one that moves the meter least.
            int greener = Collections.min(affordable, Comparator.comparingInt(i -> choiceCard(game).option(i).effect().meter()));
            return random.nextDouble() < lean ? greener : affordable.get(random.nextInt(affordable.size()));
        }
    }

    // ---- Green -------------------------------------------------------------------

    private static final class Green implements Strategy {

        @Override
        public String name() {
            return "green";
        }

        @Override
        public Optional<Action> action(Game game, List<Action> available, Random random) {
            for (Kind kind : new Kind[] {Kind.CONVERT, Kind.CO_FUND, Kind.BUY_RESILIENCE}) {
                for (Action action : available) {
                    if (action.kind() == kind) {
                        return Optional.of(action);
                    }
                }
            }
            for (Action action : available) {
                if (action.kind() == Kind.BUY && onGreen(game, action)) {
                    return Optional.of(action);
                }
            }
            // A Polluting square only if it can be converted straight away (FR-07 AC-7): this turn's
            // conversion is unused and the player can afford the price plus the conversion.
            int convert = game.rules().actions().convertCost();
            if (!game.hasConvertedThisTurn()) {
                for (Action action : available) {
                    if (action.kind() == Kind.BUY && onPolluting(game, action)
                            && action.cost() + convert <= game.currentPlayer().money()) {
                        return Optional.of(action);
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public int option(Game game, List<Integer> affordable, Random random) {
            Comparator<Integer> greener = Comparator.comparingInt((Integer i) -> choiceCard(game).option(i).effect().meter())
                    .thenComparing(Comparator.comparingInt((Integer i) -> choiceCard(game).option(i).effect().gp()).reversed());
            return Collections.min(affordable, greener);
        }
    }

    // ---- Polluter -----------------------------------------------------------------

    private static final class Polluter implements Strategy {

        @Override
        public String name() {
            return "polluter";
        }

        @Override
        public Optional<Action> action(Game game, List<Action> available, Random random) {
            for (Action action : available) {
                if (action.kind() == Kind.BUY && onPolluting(game, action)) {
                    return Optional.of(action);
                }
            }
            for (Action action : available) {
                if (action.kind() == Kind.LOBBY) {
                    return Optional.of(action);
                }
            }
            return Optional.empty();
        }

        @Override
        public int option(Game game, List<Integer> affordable, Random random) {
            return Collections.max(affordable, Comparator.comparingInt(i -> moneyOf(choiceCard(game).option(i).effect())));
        }

        private static int moneyOf(Effect effect) {
            return effect.money();
        }
    }
}
