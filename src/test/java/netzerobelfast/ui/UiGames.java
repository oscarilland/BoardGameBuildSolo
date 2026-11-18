package netzerobelfast.ui;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.Dice;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.PlayerSetup;
import netzerobelfast.model.TestGames;

import java.util.List;
import java.util.Random;

/** Test helper for the UI tests: seeded games, and a quick way to play rounds. */
final class UiGames {

    static final GameConfig CONFIG = TestGames.CONFIG;

    static final List<PlayerSetup> FOUR = List.of(
            new PlayerSetup("Aoife", "INDUSTRIALIST"),
            new PlayerSetup("Ben", "TECH_ENTREPRENEUR"),
            new PlayerSetup("Chloe", "COUNCIL_PLANNER"),
            new PlayerSetup("Dev", "FARMER"));

    private UiGames() {}

    static Game seeded(long seed) {
        return Game.start(CONFIG, new Dice(new Random(seed)), FOUR);
    }

    /**
     * Plays without buying anything until the game reaches {@code round} (or ends): rolls, stays
     * on the loop, answers cards with the first option it can, and ends each turn.
     */
    static void playUntilRound(Game game, int round) {
        int guard = 0;
        while (game.round() < round && game.phase() != Game.Phase.GAME_OVER) {
            step(game);
            if (++guard > 10_000) {
                throw new AssertionError("the game didn't reach round " + round);
            }
        }
    }

    /** Takes the next step a player who never buys anything would take. */
    static void step(Game game) {
        switch (game.phase()) {
            case AWAITING_RETURN_ROLL -> game.rollToReturn();
            case AWAITING_ROLL -> game.roll();
            case AWAITING_ROUTE -> game.chooseRoute(netzerobelfast.model.Board.Route.LOOP);
            case AWAITING_DIRECTION -> game.chooseDirection(netzerobelfast.model.Board.Heading.TOWARDS_EAST);
            case AWAITING_QUIZ_ANSWER -> game.answerQuiz(0);
            case AWAITING_CHOICE -> game.chooseOption(game.affordableOptions().get(0));
            case MOVE_DONE -> game.endTurn();
            case GAME_OVER -> {
                // Nothing to do.
            }
        }
    }
}
