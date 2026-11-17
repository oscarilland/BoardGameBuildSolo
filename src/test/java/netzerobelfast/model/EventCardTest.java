package netzerobelfast.model;

import netzerobelfast.config.CardDef.Effect;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-10 AC-5: event cards apply at once, with no Resilience reduction, and a cost the player
 * can't pay makes them bankrupt (FR-13 AC-1, AC-2). E01 costs £3M; E03 pays £3M.
 */
class EventCardTest {

    /** Sets the current player's money, then lands them on a Climate Card square with {@code id} on top. */
    private static Game drawWithMoney(String id, int money) {
        Game game = Games.scripted();
        Games.setMoney(game.currentPlayer(), money);
        Games.nextCard(game, id);
        Games.arrive(game, 2);
        return game;
    }

    @Test
    void fr10_ac5_aCostAppliesAtOnce() {
        Game game = drawWithMoney("E01", 40);
        assertEquals(Phase.MOVE_DONE, game.phase(), "events don't wait for the player");
        assertEquals(37, game.currentPlayer().money());

        CardResult result = game.lastCard().orElseThrow();
        assertEquals(-3, result.moneyChange());
        assertFalse(result.bankrupt());
        assertEquals(new Effect(-3, 0, 0), ((EventCard) result.card()).effect());
    }

    @Test
    void aGainAppliesAtOnce() {
        Game game = drawWithMoney("E03", 40);
        assertEquals(43, game.currentPlayer().money());
        assertEquals(3, game.lastCard().orElseThrow().moneyChange());
    }

    @Test
    void fr08_ac6_resilienceDoesntReduceEventCosts() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        aoife.raiseResilience();
        aoife.raiseResilience();
        aoife.raiseResilience();
        Games.nextCard(game, "E01");
        Games.arrive(game, 2);
        assertEquals(37, aoife.money(), "level 3 Resilience, but the full £3M is paid");
    }

    @Test
    void fr13_ac1_aCostLargerThanTheirMoneyMakesThemBankrupt() {
        Game game = drawWithMoney("E01", 2);
        Player aoife = game.currentPlayer();
        assertEquals(0, aoife.money(), "the other £1M is written off");
        assertTrue(aoife.bankrupt());

        CardResult result = game.lastCard().orElseThrow();
        assertTrue(result.bankrupt());
        assertEquals(-2, result.moneyChange(), "only the £2M they had was paid");
    }

    @Test
    void fr13_ac2_payingExactlyWhatTheyHaveIsNotBankruptcy() {
        Game game = drawWithMoney("E01", 3);
        assertEquals(0, game.currentPlayer().money());
        assertFalse(game.currentPlayer().bankrupt());
    }

    @Test
    void anAlreadyBankruptPlayerIsntMadeBankruptAgain() {
        Game game = drawWithMoney("E01", 0);
        assertTrue(game.currentPlayer().bankrupt());
        assertTrue(game.lastCard().orElseThrow().bankrupt());

        Games.nextCard(game, "E02");
        Games.arrive(game, 2);
        assertFalse(game.lastCard().orElseThrow().bankrupt(), "they were bankrupt before this card");
        assertEquals(0, game.currentPlayer().money());
    }
}
