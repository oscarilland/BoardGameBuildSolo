package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** FR-11 step 2: the carbon tax, 1 GP per owned Polluting square, whatever its state (Q-15). */
class CarbonTaxTest {

    @Test
    void fr11_ac4_threePollutingSquaresCostThreeGP() {
        Game game = Games.quietRound();
        Player aoife = player(game, "Aoife");
        own(game, 1, aoife);
        own(game, 4, aoife);
        own(game, 6, aoife);
        game.endRound();
        assertEquals(8 - 3, aoife.greenPoints());
    }

    @Test
    void fr11_ac9_fr06_ac3_lobbiedAndClosedSquaresStillPay() {
        // The storm picks the first owned square (value 0): square 1.
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0).noDisaster());
        Player aoife = player(game, "Aoife");
        own(game, 1, aoife);
        own(game, 4, aoife);
        game.disasters().apply(Disaster.STORM, 1, game.board(), game.players());
        Games.lobby(game, 4, player(game, "Ben"));

        game.endRound();
        assertEquals(8 - 2, aoife.greenPoints());
    }

    @Test
    void greenProjectsPayNoTax() {
        Game game = Games.quietRound();
        Player ben = player(game, "Ben");
        own(game, 5, ben);
        game.endRound();
        assertEquals(12, ben.greenPoints());
    }
}
