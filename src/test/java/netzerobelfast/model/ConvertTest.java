package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;
import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.actions;
import static netzerobelfast.model.Games.arrive;
import static netzerobelfast.model.Games.only;
import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-07: converting a Polluting square to a Green Project. Squares used: 1 Waste Incinerator
 * (£5M), 4 Motorway Expansion (£6M), 8 Freight Terminal (£4M). Landing on your own Polluting
 * square offers no landing action, so only conversions are on offer there.
 */
class ConvertTest {

    @Test
    void fr07_ac1_convertingMakesAGreenProjectWithTheSameOwnerAndPrice() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        own(game, 1, aoife);
        arrive(game, 1);

        Action convert = only(game, Kind.CONVERT);
        assertEquals(Action.on(Kind.CONVERT, 5, 1), convert);
        game.take(convert);

        assertEquals(35, aoife.money());
        GreenProjectSquare green = assertInstanceOf(GreenProjectSquare.class, game.board().square(1));
        assertSame(aoife, green.owner());
        assertEquals(5, green.price());
        assertEquals("Waste Incinerator", green.name());
        assertEquals("NORTH", green.district());
        assertTrue(game.board().pollutingOwnedBy(aoife).isEmpty());
    }

    @Test
    void fr07_ac8_aConvertedSquareIsNotANatureProject() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        arrive(game, 1);
        game.take(only(game, Kind.CONVERT));
        assertFalse(((GreenProjectSquare) game.board().square(1)).nature());
    }

    @Test
    void fr07_ac2_onlyOneConversionPerTurn() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        own(game, 4, game.currentPlayer());
        arrive(game, 1);
        assertEquals(2, actions(game, Kind.CONVERT).size(), "either square can be converted");

        game.take(Action.on(Kind.CONVERT, 5, 1));
        assertTrue(actions(game, Kind.CONVERT).isEmpty());
        assertThrows(IllegalActionException.class, () -> game.take(Action.on(Kind.CONVERT, 5, 4)));
        assertInstanceOf(PollutingSquare.class, game.board().square(4));
    }

    @Test
    void fr07_ac2_convertingDoesntUseUpTheLandingAction() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        arrive(game, 0); // Cave Hill Woodland, unowned
        game.take(only(game, Kind.CONVERT));
        assertEquals(1, actions(game, Kind.BUY).size(), "can still buy the square landed on");
    }

    @Test
    void fr07_ac3_notOfferedWithoutFiveMillion() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        setMoney(game.currentPlayer(), 4);
        arrive(game, 1);
        assertTrue(actions(game, Kind.CONVERT).isEmpty());
    }

    @Test
    void fr07_ac7_buyAndConvertInTheSameTurn() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        arrive(game, 8);
        game.take(only(game, Kind.BUY));
        game.take(only(game, Kind.CONVERT));

        assertEquals(40 - 4 - 5, aoife.money());
        assertInstanceOf(GreenProjectSquare.class, game.board().square(8));
    }

    @Test
    void fr07_ac5_theSquareRemembersWhoConvertedIt() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer(); // Industrialist
        own(game, 1, aoife);
        arrive(game, 1);
        game.take(only(game, Kind.CONVERT));
        assertSame(aoife, ((GreenProjectSquare) game.board().square(1)).convertedBy());

        // A Green Project bought as green was never converted, so earns no Industrialist bonus.
        arrive(game, 5);
        game.take(only(game, Kind.BUY));
        assertNull(((GreenProjectSquare) game.board().square(5)).convertedBy());
    }

    @Test
    void aConvertedSquareCanBeCoFundedByOthers() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        arrive(game, 1);
        game.take(only(game, Kind.CONVERT));
        game.endTurn();

        arrive(game, 1); // Ben lands on Aoife's converted square
        assertEquals(1, actions(game, Kind.CO_FUND).size());
        assertTrue(actions(game, Kind.LOBBY).isEmpty());
    }
}
