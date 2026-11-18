package netzerobelfast.model;

import netzerobelfast.config.Rules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-12 and FR-17: disasters by danger band, cut by Resilience, with a climate explanation.
 * Disaster types are scripted as FLOOD 0, STORM 1, HEATWAVE 2, DROUGHT 3.
 */
class DisasterTest {

    private static DisasterResult apply(Game game, Disaster disaster, int band) {
        return game.disasters().apply(disaster, band, game.board(), game.players());
    }

    @Test
    void fr12_ac2_aDisasterHappensWhenTheDieIsAtOrBelowTheBand() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).die(2).values(1).die(3));
        DisasterManager.Check hit = game.disasters().check(2);
        assertEquals(2, hit.roll());
        assertEquals(Disaster.STORM, hit.disaster());

        DisasterManager.Check miss = game.disasters().check(2);
        assertEquals(3, miss.roll());
        assertNull(miss.disaster());
    }

    @Test
    void fr12_ac3_typesAreChosenEquallyOften() {
        DisasterManager manager = new DisasterManager(Games.CONFIG.rules(), new Dice(new Random(13)));
        int[] counts = new int[4];
        for (int i = 0; i < 4_000; i++) {
            counts[manager.check(6).disaster().ordinal()]++; // band 6: every roll is a disaster
        }
        for (Disaster type : Disaster.values()) {
            int count = counts[type.ordinal()];
            assertTrue(count >= 900 && count <= 1_100, type + " came up " + count + " times");
        }
    }

    @Test
    void fr12_ac4_aFloodChargesEachOwnerOncePerSquareThereAndClosesTheDistrict() {
        // Districts with owned squares, in board order: NORTH (value 0), HARBOUR (value 1).
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0));
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        own(game, 1, aoife); // NORTH
        own(game, 4, aoife); // NORTH
        own(game, 6, ben);   // HARBOUR

        DisasterResult flood = apply(game, Disaster.FLOOD, 3);
        assertEquals("NORTH", flood.district());
        assertEquals(Map.of(aoife, 6), flood.payments(), "£1M × band 3 × 2 squares, as one payment");
        assertEquals(34, aoife.money());
        assertEquals(36, ben.money());
        assertTrue(game.disasters().shut(game.board().square(1)));
        assertTrue(game.disasters().shut(game.board().square(0)), "every NORTH square, owned or not");
        assertFalse(game.disasters().shut(game.board().square(6)));
    }

    @Test
    void floodPaymentsAreCutOnceByResilience() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0));
        Player aoife = player(game, "Aoife");
        aoife.raiseResilience(); // level 1: 25% off
        own(game, 1, aoife);
        own(game, 4, aoife);
        DisasterResult flood = apply(game, Disaster.FLOOD, 3);
        assertEquals(5, flood.payments().get(aoife), "£6M less 25% is £4.5M, rounded up to £5M");
    }

    @Test
    void fr12_ac5_aStormChargesTheOwnerAndClosesTheSquare() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0));
        Player aoife = player(game, "Aoife");
        own(game, 1, aoife);
        DisasterResult storm = apply(game, Disaster.STORM, 2);
        assertEquals(1, storm.square());
        assertEquals(Map.of(aoife, 4), storm.payments());
        assertTrue(game.disasters().shut(game.board().square(1)));
    }

    @Test
    void fr12_ac6_aHeatwaveChargesEveryPlayerLessTheirOwnResilience() {
        Game game = Games.quietRound();
        Player chloe = player(game, "Chloe");
        chloe.raiseResilience();
        chloe.raiseResilience(); // level 2: 50% off
        DisasterResult heatwave = apply(game, Disaster.HEATWAVE, 4);
        assertEquals(4, heatwave.payments().get(player(game, "Aoife")));
        assertEquals(2, heatwave.payments().get(chloe));
        assertEquals(36 - 4, player(game, "Ben").money());
        assertEquals(38 - 2, chloe.money());
    }

    @Test
    void fr12_ac7_aDroughtCostsNothingAndStopsNatureGPNextRound() {
        Game game = Games.quietRound();
        DisasterResult drought = apply(game, Disaster.DROUGHT, 4);
        assertTrue(drought.payments().isEmpty());
        assertTrue(game.disasters().drought());
    }

    @Test
    void fr12_ac8_disasterPaymentsCanBankrupt() {
        Game game = Games.quietRound();
        Player aoife = player(game, "Aoife");
        setMoney(aoife, 3);
        DisasterResult heatwave = apply(game, Disaster.HEATWAVE, 4);
        assertEquals(3, heatwave.payments().get(aoife), "only what she had");
        assertTrue(aoife.bankrupt());
        assertEquals(List.of(aoife), heatwave.bankrupted());
    }

    @Test
    void fr12_ac10_q17_aFloodOrStormWithNoOwnedSquaresDoesNothing() {
        Game game = Games.quietRound();
        DisasterResult flood = apply(game, Disaster.FLOOD, 4);
        DisasterResult storm = apply(game, Disaster.STORM, 4);
        assertTrue(flood.skipped());
        assertTrue(storm.skipped());
        assertTrue(flood.payments().isEmpty() && storm.payments().isEmpty());
        assertEquals(40, player(game, "Aoife").money());
    }

    @Test
    void fr17_ac1_ac2_eachDisasterCarriesItsExplanationAndSources() {
        Rules.Disasters data = Games.CONFIG.rules().disasters();
        Game game = Games.quietRound();
        for (Disaster type : Disaster.values()) {
            DisasterResult result = apply(game, type, 1);
            assertEquals(data.explanations().get(type.name()).text(), result.explanation(), type.name());
            assertFalse(result.sources().isEmpty(), type.name());
        }
    }

    @Test
    void fr08_ac6_resilienceCutsTheCostButNotTheClosure() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0));
        Player aoife = player(game, "Aoife");
        aoife.raiseResilience();
        aoife.raiseResilience();
        aoife.raiseResilience(); // level 3: 75% off
        own(game, 1, aoife);
        DisasterResult storm = apply(game, Disaster.STORM, 2);
        assertEquals(1, storm.payments().get(aoife), "£4M less 75%");
        assertTrue(game.disasters().shut(game.board().square(1)), "still closed");
    }

    @Test
    void effectsLastOneRoundThenClear() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0).noDisaster().noDisaster());
        Player aoife = player(game, "Aoife");
        own(game, 1, aoife);
        apply(game, Disaster.STORM, 1);
        apply(game, Disaster.DROUGHT, 1);
        int afterRepair = aoife.money();

        game.endRound();
        assertEquals(afterRepair, aoife.money(), "closed for the next round");
        assertFalse(game.disasters().shut(game.board().square(1)));
        assertFalse(game.disasters().drought());

        game.endRound();
        assertEquals(afterRepair + 7, aoife.money(), "open again the round after");
    }

    @Test
    void fr12_ac9_theRoundReportIncludesTheDisaster() {
        // Meter 50 → 53 after step 3: band 4. A die of 2 is a disaster; type 2 is a Heatwave (£4M each).
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).die(2).values(2));
        game.endRound();
        RoundReport report = game.lastRound().orElseThrow();
        assertEquals(2, report.disasterRoll());
        assertEquals(4, report.band());
        assertEquals(Disaster.HEATWAVE, report.disaster().disaster());
        assertEquals(36, player(game, "Aoife").money());
        assertTrue(report.disaster().explanation().startsWith("Greenhouse gases"));
    }
}
