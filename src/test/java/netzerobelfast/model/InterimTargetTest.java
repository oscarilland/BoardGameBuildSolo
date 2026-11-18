package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-11 step 4: in round 3 (2030) the meter must be at most 26, and in round 8 (2040) at most 11,
 * or every Polluting square costs its owner 2 GP. Aoife owns two Polluting squares (1 and 4), so
 * the meter rises by 3 + 4 = 7 in her rounds, and she pays 2 GP of carbon tax every round.
 */
class InterimTargetTest {

    /** Plays empty rounds until {@code round}, sets the meter so step 3 lands on {@code meterAfter}, then ends that round. */
    private static Game endRoundWithMeter(int round, int meterAfter) {
        ScriptedRandom script = new ScriptedRandom().firstPlayer(0);
        for (int i = 0; i < round; i++) {
            script.noDisaster();
        }
        Game game = Games.withScript(script);
        for (int i = 1; i < round; i++) {
            game.endRound();
        }
        own(game, 1, player(game, "Aoife"));
        own(game, 4, player(game, "Aoife"));
        game.meter().adjust(meterAfter - 7 - game.meter().value());
        game.endRound();
        assertEquals(meterAfter, game.meter().value());
        return game;
    }

    @Test
    void fr11_ac6_missingThe2030TargetCostsTwoGPPerPollutingSquare() {
        Game game = endRoundWithMeter(3, 27);
        assertEquals(8 - 2 - 4, player(game, "Aoife").greenPoints(), "carbon tax 2, target missed 4");
        RoundReport report = game.lastRound().orElseThrow();
        assertEquals(26, report.target().maxMeter());
        assertFalse(report.targetMet());
        assertEquals(2030, report.year());
    }

    @Test
    void fr11_ac6_meetingThe2030TargetCostsNothing() {
        Game game = endRoundWithMeter(3, 26);
        assertEquals(8 - 2, player(game, "Aoife").greenPoints());
        assertTrue(game.lastRound().orElseThrow().targetMet());
    }

    @Test
    void fr11_ac6_the2040TargetIsEleven() {
        assertEquals(8 - 2 - 4, player(endRoundWithMeter(8, 12), "Aoife").greenPoints());
        assertEquals(8 - 2, player(endRoundWithMeter(8, 11), "Aoife").greenPoints());
    }

    @Test
    void fr11_ac6_noCheckInOtherRounds() {
        Game game = endRoundWithMeter(4, 60);
        assertEquals(8 - 2, player(game, "Aoife").greenPoints());
        assertNull(game.lastRound().orElseThrow().target());
    }
}
