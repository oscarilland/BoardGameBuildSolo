package netzerobelfast.sim;

import netzerobelfast.model.Action;
import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Game;
import netzerobelfast.model.QuizCard;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * How a computer player makes every choice the game asks for. A strategy only ever picks from
 * what the game offers ({@link Game#availableActions()}, {@link Game#affordableOptions()}), so it
 * can never break a rule. Its {@code random} is seeded from the game's seed, so a simulated game
 * replays exactly.
 */
public interface Strategy {

    /** A short name for the balance report, such as "green". */
    String name();

    /** One of the actions on offer, or empty to stop acting and end the turn. Called until it returns empty. */
    Optional<Action> action(Game game, List<Action> available, Random random);

    /** Option A (0) or B (1), from the affordable ones. */
    int option(Game game, List<Integer> affordable, Random random);

    default Route route(Game game, Random random) {
        return random.nextBoolean() ? Route.LOOP : Route.CITY_CENTRE;
    }

    default Heading direction(Game game, Random random) {
        return random.nextBoolean() ? Heading.TOWARDS_WEST : Heading.TOWARDS_EAST;
    }

    /**
     * A quiz answer. Players know about 70% of the answers, as in the Python balance simulation:
     * the right answer that often, otherwise a random one.
     */
    default int quizAnswer(QuizCard quiz, Random random) {
        if (random.nextDouble() < 0.7) {
            for (int i = 0; i < quiz.answers().size(); i++) {
                if (quiz.checkAnswer(i)) {
                    return i;
                }
            }
        }
        return random.nextInt(quiz.answers().size());
    }
}
