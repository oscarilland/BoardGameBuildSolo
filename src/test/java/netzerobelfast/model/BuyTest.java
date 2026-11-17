package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;
import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.actions;
import static netzerobelfast.model.Games.arrive;
import static netzerobelfast.model.Games.only;
import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-04: buying an unowned square. Squares used: 5 Heat Pump Retrofit (Green, £9M),
 * 0 Cave Hill Woodland (Green, £10M), 4 Motorway Expansion (Polluting, £6M).
 */
class BuyTest {

    @Test
    void fr04_ac1_exactlyAffordableSquareCanBeBought() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        setMoney(aoife, 9);
        arrive(game, 5);

        Action buy = only(game, Kind.BUY);
        assertEquals(Action.on(Kind.BUY, 9, 5), buy);
        game.take(buy);

        assertEquals(0, aoife.money());
        assertSame(aoife, ((OwnableSquare) game.board().square(5)).owner());
    }

    @Test
    void fr04_ac2_unaffordableSquareIsNotOfferedAndCantBeTaken() {
        Game game = Games.scripted();
        setMoney(game.currentPlayer(), 8);
        arrive(game, 5);

        assertTrue(actions(game, Kind.BUY).isEmpty());
        assertThrows(IllegalActionException.class, () -> game.take(Action.on(Kind.BUY, 9, 5)));
        assertNull(((OwnableSquare) game.board().square(5)).owner());
        assertEquals(8, game.currentPlayer().money());
    }

    @Test
    void fr04_ac3_techEntrepreneurPaysLessForGreenProjectsOnly() {
        Game game = Games.start(new ScriptedRandom().firstPlayer(1), Games.FOUR); // Ben, Tech Entrepreneur
        arrive(game, 0);
        assertEquals(7, only(game, Kind.BUY).cost(), "£10M Green Project, £3M off");

        arrive(game, 4);
        assertEquals(6, only(game, Kind.BUY).cost(), "Polluting squares at full price");
    }

    @Test
    void fr04_ac4_onlyUnownedOwnableSquaresCanBeBought() {
        Game game = Games.scripted();
        Player ben = Games.player(game, "Ben");
        own(game, 5, ben);

        for (int square : new int[] {5, 3, 12, 2}) { // owned, Resilience, Glider stop, Climate Card
            arrive(game, square);
            assertTrue(actions(game, Kind.BUY).isEmpty(), "square " + square);
        }
    }

    @Test
    void fr04_ac5_boughtSquareBelongsToThePlayer() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        arrive(game, 4);
        game.take(only(game, Kind.BUY));

        assertEquals(34, aoife.money());
        assertEquals(1, game.board().pollutingOwnedBy(aoife).size());
        assertEquals(4, game.board().pollutingOwnedBy(aoife).get(0).id());
    }
}
