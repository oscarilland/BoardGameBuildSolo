package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;
import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.actions;
import static netzerobelfast.model.Games.arrive;
import static netzerobelfast.model.Games.only;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The turn's action rules: one landing action, offered actions only, and NFR-08 AC-4. */
class ActionRulesTest {

    @Test
    void noActionsBeforeTheMoveIsDone() {
        Game game = Games.scripted();
        assertTrue(game.availableActions().isEmpty());
        IllegalActionException e = assertThrows(IllegalActionException.class,
                () -> game.take(Action.on(Kind.BUY, 10, 0)));
        assertEquals("That action isn't available now.", e.getMessage());
    }

    @Test
    void nfr08_ac4_anActionNotOnTheListIsRefusedAndChangesNothing() {
        Game game = Games.scripted();
        arrive(game, 5); // £9M Green Project
        Action cheap = Action.on(Kind.BUY, 1, 5); // the right square at the wrong price

        assertThrows(IllegalActionException.class, () -> game.take(cheap));
        assertEquals(40, game.currentPlayer().money());
        assertNull(((OwnableSquare) game.board().square(5)).owner());
    }

    @Test
    void oneLandingActionPerTurn() {
        Game game = Games.scripted();
        arrive(game, 3);
        game.take(only(game, Kind.BUY_RESILIENCE));
        assertTrue(game.availableActions().isEmpty());
    }

    @Test
    void theLimitsResetOnTheNextTurn() {
        // Aoife rolls 3 to Milewater Flood Defence (3) and buys a level; the others roll 2;
        // Aoife then rolls 3 to Harbour Gas Plant (6), where she can buy.
        Game game = Games.scripted(3, 2, 2, 2, 3);
        game.roll();
        assertEquals(3, game.currentPlayer().position());
        game.take(only(game, Kind.BUY_RESILIENCE));
        game.endTurn();
        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
        }

        game.roll();
        assertEquals(6, game.currentPlayer().position());
        assertEquals(1, actions(game, Kind.BUY).size());
    }

    @Test
    void climateCardSquaresOfferNothingYet() {
        Game game = Games.scripted();
        arrive(game, 2);
        assertTrue(game.availableActions().isEmpty());
    }

    @Test
    void onlyConvertingIsNotALandingAction() {
        assertFalse(Action.on(Kind.CONVERT, 5, 1).isLandingAction());
        for (Kind kind : new Kind[] {Kind.BUY, Kind.CO_FUND, Kind.LOBBY, Kind.BUY_RESILIENCE, Kind.RIDE_GLIDER}) {
            assertTrue(Action.on(kind, 0, 0).isLandingAction(), kind.name());
        }
    }

    @Test
    void squaresDescribeThemselves() {
        assertEquals("Glider East (12)", Games.scripted().board().square(12).toString());
    }
}
