package netzerobelfast.model;

import netzerobelfast.model.Game.Phase;
import netzerobelfast.model.Game.ReturnAttempt;
import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-13: a bankrupt player sits out, rolls to return (a 6), returns automatically the turn after
 * (Q-14), and comes back with a £10M loan that costs £15M at the end. Seats: Aoife (first), Ben,
 * Chloe, Dev. Ben goes bankrupt during Aoife's turn, so his next turn is his first bankrupt turn.
 */
class BankruptcyTest {

    /** Aoife stands on square 3 (no action), Ben is made bankrupt, and the turn passes to Ben. */
    private static Game benBankruptAtHisTurn(ScriptedRandom script) {
        Game game = Games.withScript(script);
        Games.arrive(game, 3);
        Games.bankrupt(player(game, "Ben"));
        game.endTurn();
        assertEquals("Ben", game.currentPlayer().name());
        return game;
    }

    @Test
    void fr13_ac4_ac6_aSixBringsThemBackWithALoanAndANormalTurn() {
        Game game = benBankruptAtHisTurn(new ScriptedRandom().firstPlayer(0).die(6).rolls(5));
        Player ben = game.currentPlayer();
        assertEquals(Phase.AWAITING_RETURN_ROLL, game.phase());

        game.rollToReturn();
        assertEquals(new ReturnAttempt(6, true), game.lastReturn().orElseThrow());
        assertFalse(ben.bankrupt());
        assertEquals(10, ben.money());
        assertEquals(15, ben.loanRepayments());
        assertEquals(Phase.AWAITING_ROLL, game.phase());

        game.roll();
        assertEquals(5, ben.position(), "AC-6: the rest of the turn is normal");
    }

    @Test
    void fr13_ac3_fr03_ac6_fr07_ac6_anythingElseAndTheyDontMoveOrAct() {
        Game game = benBankruptAtHisTurn(new ScriptedRandom().firstPlayer(0).die(3));
        Player ben = game.currentPlayer();
        own(game, 1, ben);

        game.rollToReturn();
        assertEquals(new ReturnAttempt(3, false), game.lastReturn().orElseThrow());
        assertTrue(ben.bankrupt());
        assertEquals(Phase.MOVE_DONE, game.phase());
        assertEquals(0, ben.position(), "no move");
        assertTrue(game.availableActions().isEmpty(), "no actions, not even converting");
        assertTrue(game.lastMove().isEmpty());
        game.endTurn();
        assertEquals("Chloe", game.currentPlayer().name());
    }

    @Test
    void fr13_ac5_q14_theyReturnAutomaticallyTheTurnAfter() {
        // Ben fails (3); Chloe and Dev roll 3; the round ends quietly; Aoife rolls 3; then Ben is back without rolling.
        Game game = benBankruptAtHisTurn(new ScriptedRandom().firstPlayer(0).die(3).rolls(3, 3).noDisaster().rolls(3));
        game.rollToReturn();
        game.endTurn();
        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
        }

        Player ben = game.currentPlayer();
        assertEquals("Ben", ben.name());
        assertEquals(Phase.AWAITING_ROLL, game.phase(), "no roll to return this time");
        assertEquals(new ReturnAttempt(0, true), game.lastReturn().orElseThrow());
        assertFalse(ben.bankrupt());
        assertEquals(10, ben.money());
        assertEquals(15, ben.loanRepayments());
    }

    @Test
    void aBankruptPlayersSquaresEarnGPButNoMoney() {
        Game game = Games.quietRound();
        Player ben = player(game, "Ben");
        own(game, 5, ben);
        Games.bankrupt(ben);
        game.endRound();
        assertEquals(0, ben.money());
        assertEquals(12, ben.greenPoints());
    }

    @Test
    void goingBankruptTwiceMeansTwoLoansToRepay() {
        Game game = Games.quietRound();
        Player ben = player(game, "Ben");
        Games.bankrupt(ben);
        ben.returnFromBankruptcy(10, 15);
        Games.bankrupt(ben);
        assertEquals(1, ben.turnsBankrupt(), "the count starts again");
        ben.returnFromBankruptcy(10, 15);
        assertEquals(30, ben.loanRepayments());
    }

    @Test
    void anEventCardBankruptcyMeansARollToReturnNextTurn() {
        // Aoife draws E01 (−£3M) with £2M, then the others roll 3, the round ends quietly, and it's Aoife's turn.
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).rolls(3, 3, 3).noDisaster());
        Games.setMoney(game.currentPlayer(), 2);
        Games.nextCard(game, "E01");
        Games.arrive(game, 2);
        Player aoife = game.currentPlayer();
        assertTrue(aoife.bankrupt());
        assertTrue(game.availableActions().isEmpty());

        game.endTurn();
        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
        }
        assertEquals("Aoife", game.currentPlayer().name());
        assertEquals(Phase.AWAITING_RETURN_ROLL, game.phase());
    }

    @Test
    void rollingToReturnIsOnlyForABankruptPlayersTurn() {
        Game game = Games.scripted();
        assertEquals("Can't roll to return now: the game is waiting for a dice roll.",
                assertThrows(IllegalActionException.class, game::rollToReturn).getMessage());

        Game bankruptTurn = benBankruptAtHisTurn(new ScriptedRandom().firstPlayer(0));
        assertEquals("Can't roll the dice now: the game is waiting for a roll to return from bankruptcy.",
                assertThrows(IllegalActionException.class, bankruptTurn::roll).getMessage());
    }
}
