package netzerobelfast.model;

import netzerobelfast.model.Game.Phase;
import netzerobelfast.model.RoundReport.Step;
import org.junit.jupiter.api.Test;

import java.util.List;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FR-11 AC-1, AC-2 and AC-7: when the end of the round runs, in what order, and the end of the game. */
class RoundOrderTest {

    @Test
    void fr11_ac1_theEndOfTheRoundRunsAfterTheLastPlayerOnly() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).rolls(3, 3, 3, 3).noDisaster());
        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
            assertTrue(game.lastRound().isEmpty(), "not after player " + (i + 1));
        }
        game.roll();
        game.endTurn();
        assertEquals(1, game.lastRound().orElseThrow().round());
        assertEquals(2, game.round());
        assertEquals(2028, game.year());
        assertEquals(Phase.AWAITING_ROLL, game.phase());
    }

    @Test
    void fr11_ac2_theStepsRunInOrder() {
        Game game = Games.quietRound();
        game.endRound();
        assertEquals(List.of(Step.INCOME, Step.CARBON_TAX, Step.METER, Step.DISASTER_CHECK, Step.ADVANCE),
                game.lastRound().orElseThrow().steps());
    }

    @Test
    void fr11_ac2_theInterimTargetStepRunsInItsRound() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).noDisaster().noDisaster().noDisaster());
        game.endRound();
        game.endRound();
        game.endRound();
        assertEquals(List.of(Step.INCOME, Step.CARBON_TAX, Step.METER, Step.INTERIM_TARGET, Step.DISASTER_CHECK, Step.ADVANCE),
                game.lastRound().orElseThrow().steps());
    }

    @Test
    void fr11_ac2_theDangerBandComesFromTheUpdatedMeter() {
        // Meter 40 (band 3) before; Aoife's Polluting square makes it 45 (band 4) in step 3.
        // A die of 4 is then a disaster; with the old band it wouldn't be.
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).die(4).values(3));
        own(game, 1, player(game, "Aoife"));
        game.meter().adjust(40 - 50);
        game.endRound();
        RoundReport report = game.lastRound().orElseThrow();
        assertEquals(45, report.meterAfter());
        assertEquals(4, report.band());
        assertEquals(Disaster.DROUGHT, report.disaster().disaster());
    }

    @Test
    void noDisasterIsReportedAsNull() {
        Game game = Games.quietRound();
        game.endRound();
        assertNull(game.lastRound().orElseThrow().disaster());
        assertEquals(6, game.lastRound().orElseThrow().disasterRoll());
    }

    @Test
    void fr11_ac7_afterRoundTwelveTheGameIsOver() {
        ScriptedRandom script = new ScriptedRandom().firstPlayer(0);
        for (int i = 0; i < 12; i++) {
            script.noDisaster();
        }
        Game game = Games.withScript(script);
        for (int i = 0; i < 11; i++) {
            game.endRound();
            assertTrue(game.phase() != Phase.GAME_OVER, "not over after round " + (i + 1));
        }
        game.endRound();
        assertEquals(Phase.GAME_OVER, game.phase());
        assertEquals(12, game.lastRound().orElseThrow().round());
        assertEquals(2048, game.lastRound().orElseThrow().year());
        assertEquals(2050, game.year());
        assertEquals(13, game.meter().history().size());

        assertEquals("Can't roll the dice now: the game is waiting for nothing: the game is over.",
                assertThrows(IllegalActionException.class, game::roll).getMessage());
        assertThrows(IllegalActionException.class, game::endTurn);
        assertTrue(game.availableActions().isEmpty());
    }

    @Test
    void theLastTurnOfTheLastRoundEndsTheGame() {
        ScriptedRandom script = new ScriptedRandom().firstPlayer(0);
        for (int i = 0; i < 11; i++) {
            script.noDisaster();
        }
        script.rolls(3, 3, 3, 3).noDisaster();
        Game game = Games.withScript(script);
        for (int i = 0; i < 11; i++) {
            game.endRound();
        }
        for (int i = 0; i < 4; i++) {
            game.roll();
            game.endTurn();
        }
        assertEquals(Phase.GAME_OVER, game.phase());
    }
}
