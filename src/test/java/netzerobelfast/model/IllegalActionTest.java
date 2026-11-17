package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** NFR-08 AC-4: the model rejects actions the rules don't allow now, and leaves its state unchanged. */
class IllegalActionTest {

    /** Checks the action is rejected and nothing about the game changed. */
    private static IllegalActionException assertRejected(Game game, Executable action) {
        Phase phase = game.phase();
        int position = game.currentPlayer().position();
        int steps = game.stepsLeft();
        String player = game.currentPlayer().name();

        IllegalActionException e = assertThrows(IllegalActionException.class, action);

        assertEquals(phase, game.phase());
        assertEquals(position, game.currentPlayer().position());
        assertEquals(steps, game.stepsLeft());
        assertEquals(player, game.currentPlayer().name());
        return e;
    }

    @Test
    void whileAwaitingARoll() {
        Game game = Games.scripted(3);
        assertEquals("Can't choose a route now: the game is waiting for a dice roll.",
                assertRejected(game, () -> game.chooseRoute(Route.LOOP)).getMessage());
        assertRejected(game, () -> game.chooseDirection(Heading.TOWARDS_EAST));
        assertEquals("Can't end the turn now: the game is waiting for a dice roll.",
                assertRejected(game, game::endTurn).getMessage());
    }

    @Test
    void rollingTwice() {
        Game game = Games.scripted(3, 3);
        game.roll();
        assertEquals("Can't roll the dice now: the game is waiting for the turn to end.",
                assertRejected(game, game::roll).getMessage());
    }

    @Test
    void whileAwaitingARoute() {
        Game game = Games.scripted(4);
        Games.place(game, 11, null);
        game.roll();

        assertEquals("Can't end the turn now: the game is waiting for a route choice.",
                assertRejected(game, game::endTurn).getMessage());
        assertRejected(game, game::roll);
        assertRejected(game, () -> game.chooseDirection(Heading.TOWARDS_WEST));
        assertEquals("Choose the loop or the City Centre.",
                assertRejected(game, () -> game.chooseRoute(null)).getMessage());
    }

    @Test
    void whileAwaitingADirection() {
        Game game = Games.scripted(2);
        Games.place(game, 30, null);
        game.roll();

        assertEquals("Can't choose a route now: the game is waiting for a direction choice.",
                assertRejected(game, () -> game.chooseRoute(Route.CITY_CENTRE)).getMessage());
        assertEquals("Choose a direction along the City Centre route.",
                assertRejected(game, () -> game.chooseDirection(null)).getMessage());
    }
}
