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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-05: co-funding another player's Green Project. The GP it pays (AC-3, AC-5, AC-6) is paid
 * at the end of the round, in plan 005.
 */
class CoFundTest {

    @Test
    void fr05_ac1_coFundingCostsThreeAndStartsADeal() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        own(game, 5, Games.player(game, "Ben"));
        arrive(game, 5);

        Action coFund = only(game, Kind.CO_FUND);
        assertEquals(3, coFund.cost());
        game.take(coFund);

        assertEquals(37, aoife.money());
        assertEquals(new CoFundingDeal(aoife, 5), aoife.coFundingDeal());
    }

    @Test
    void fr05_ac2_notOfferedOnYourOwnSquare() {
        Game game = Games.scripted();
        own(game, 5, game.currentPlayer());
        arrive(game, 5);
        assertTrue(actions(game, Kind.CO_FUND).isEmpty());
    }

    @Test
    void fr05_ac2_notOfferedOnPollutingSquares() {
        Game game = Games.scripted();
        own(game, 1, Games.player(game, "Ben"));
        arrive(game, 1);
        assertTrue(actions(game, Kind.CO_FUND).isEmpty());
        assertEquals(1, actions(game, Kind.LOBBY).size(), "a rival's Polluting square offers lobbying instead");
    }

    @Test
    void fr05_ac2_notOfferedWithoutThreeMillion() {
        Game game = Games.scripted();
        own(game, 5, Games.player(game, "Ben"));
        setMoney(game.currentPlayer(), 2);
        arrive(game, 5);
        assertTrue(actions(game, Kind.CO_FUND).isEmpty());
    }

    @Test
    void fr05_unownedGreenProjectOffersBuyingNotCoFunding() {
        Game game = Games.scripted();
        arrive(game, 5);
        assertTrue(actions(game, Kind.CO_FUND).isEmpty());
        assertEquals(1, actions(game, Kind.BUY).size());
    }

    @Test
    void fr05_ac4_aNewDealReplacesTheOld() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        own(game, 5, Games.player(game, "Ben"));
        own(game, 7, Games.player(game, "Chloe"));

        arrive(game, 5);
        game.take(only(game, Kind.CO_FUND));
        arrive(game, 7);
        game.take(only(game, Kind.CO_FUND));

        assertEquals(new CoFundingDeal(aoife, 7), aoife.coFundingDeal());
        assertEquals(34, aoife.money());
    }

    @Test
    void fr05_ac7_notOfferedOnTheSquareYouAlreadyFund() {
        Game game = Games.scripted();
        own(game, 5, Games.player(game, "Ben"));
        arrive(game, 5);
        game.take(only(game, Kind.CO_FUND));

        arrive(game, 5); // landing there again on a later turn
        assertTrue(actions(game, Kind.CO_FUND).isEmpty());
    }

    @Test
    void noDealAtTheStart() {
        assertNull(Games.scripted().currentPlayer().coFundingDeal());
    }
}
