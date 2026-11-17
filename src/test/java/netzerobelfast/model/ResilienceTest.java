package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;
import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.actions;
import static netzerobelfast.model.Games.arrive;
import static netzerobelfast.model.Games.only;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-08: buying Resilience levels on a Resilience square (3 Milewater Flood Defence), and the
 * cut it gives to disaster payments. Disasters themselves arrive in plan 005.
 */
class ResilienceTest {

    private static final int RESILIENCE_SQUARE = 3;

    /** Buys the next level from a fresh landing and returns what it cost. */
    private static int buyNextLevel(Game game) {
        arrive(game, RESILIENCE_SQUARE);
        Action buy = only(game, Kind.BUY_RESILIENCE);
        game.take(buy);
        return buy.cost();
    }

    @Test
    void fr08_ac1_ac2_levelsCostThreeFiveSevenThenNoMore() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        assertEquals(3, buyNextLevel(game));
        assertEquals(5, buyNextLevel(game));
        assertEquals(7, buyNextLevel(game));
        assertEquals(3, aoife.resilienceLevel());
        assertEquals(40 - 15, aoife.money());

        arrive(game, RESILIENCE_SQUARE);
        assertTrue(actions(game, Kind.BUY_RESILIENCE).isEmpty());
    }

    @Test
    void fr08_ac3_oneLevelPerLanding() {
        Game game = Games.scripted();
        arrive(game, RESILIENCE_SQUARE);
        game.take(only(game, Kind.BUY_RESILIENCE));
        assertTrue(actions(game, Kind.BUY_RESILIENCE).isEmpty());
        assertEquals(1, game.currentPlayer().resilienceLevel());
    }

    @Test
    void fr08_ac4_councilPlannerPaysTwoMillionLessPerLevel() {
        Game game = Games.start(new ScriptedRandom().firstPlayer(2), Games.FOUR); // Chloe, Council Planner
        assertEquals(1, buyNextLevel(game));
        assertEquals(3, buyNextLevel(game));
        assertEquals(5, buyNextLevel(game));
    }

    @Test
    void notOfferedIfTheNextLevelIsUnaffordable() {
        Game game = Games.scripted();
        setMoney(game.currentPlayer(), 2);
        arrive(game, RESILIENCE_SQUARE);
        assertTrue(actions(game, Kind.BUY_RESILIENCE).isEmpty());
    }

    @Test
    void fr08_ac5_ac7_disasterPaymentsAreCutAndRoundedHalfUp() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        assertEquals(5, aoife.disasterCost(5), "level 0: no cut");

        buyNextLevel(game); // level 1, 25% off
        assertEquals(2, aoife.disasterCost(2), "£1.5M rounds up to £2M");
        assertEquals(3, aoife.disasterCost(4));

        buyNextLevel(game); // level 2, 50% off
        assertEquals(2, aoife.disasterCost(4), "AC-5: Heatwave in band 4");
        assertEquals(2, aoife.disasterCost(3), "£1.5M rounds up to £2M");

        buyNextLevel(game); // level 3, 75% off
        assertEquals(1, aoife.disasterCost(2), "£0.5M rounds up to £1M");
        assertEquals(2, aoife.disasterCost(8));
    }
}
