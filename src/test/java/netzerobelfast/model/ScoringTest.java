package netzerobelfast.model;

import netzerobelfast.model.GameResult.TieBreak;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-14: Legacy Scores and the single winner. Each test sets up the end state and scores it
 * directly. Starting GP: Aoife 8, Ben 10, Chloe 12, Dev 13. The meter starts at 50 (missed);
 * {@link #netZero} brings it to 0.
 */
class ScoringTest {

    private static void netZero(Game game) {
        game.meter().adjust(-game.meter().value());
    }

    private static void setGP(Player player, int gp) {
        player.addGP(gp - player.greenPoints());
    }

    @Test
    void fr14_ac1_netZeroCountsGPPlusMoneyLessRepaymentsOverFive() {
        Game game = Games.scripted();
        Player aoife = player(game, "Aoife");
        aoife.returnFromBankruptcy(10, 15); // one return: £15M to repay
        setMoney(aoife, 45);
        setGP(aoife, 30);
        netZero(game);

        GameResult result = Scoring.score(game);
        assertTrue(result.netZero());
        assertEquals("36.0", result.scoreOf(aoife).score().display(), "30 + (45 − 15) ÷ 5");
        assertEquals(15, result.scoreOf(aoife).loanRepayments());
    }

    @Test
    void fr14_ac2_missingNetZeroMakesMoneyWorthlessAndCostsFivePerPollutingSquare() {
        Game game = Games.scripted();
        Player aoife = player(game, "Aoife");
        setMoney(aoife, 80);
        setGP(aoife, 30);
        own(game, 1, aoife);
        own(game, 4, aoife);
        game.meter().adjust(1 - game.meter().value());

        GameResult result = Scoring.score(game);
        assertFalse(result.netZero(), "AC-3: the meter at 1 misses net zero");
        assertEquals("20.0", result.scoreOf(aoife).score().display(), "30 − 5 × 2; the £80M counts for nothing");
        assertEquals(2, result.scoreOf(aoife).pollutingOwned());
    }

    @Test
    void fr14_ac4_aTieGoesToMoreGreenProjects() {
        Game game = Games.scripted();
        setGP(player(game, "Aoife"), 20);
        setGP(player(game, "Ben"), 20);
        own(game, 5, player(game, "Ben"));

        GameResult result = Scoring.score(game);
        assertSame(player(game, "Ben"), result.winner());
        assertEquals(TieBreak.GREEN_PROJECTS, result.decidedBy());
    }

    @Test
    void fr14_ac5_thenToFewerPollutingSquares() {
        Game game = Games.scripted();
        setGP(player(game, "Aoife"), 25);
        own(game, 1, player(game, "Aoife"));    // 25 − 5 = 20
        setGP(player(game, "Ben"), 30);
        own(game, 4, player(game, "Ben"));
        own(game, 6, player(game, "Ben"));      // 30 − 10 = 20

        GameResult result = Scoring.score(game);
        assertSame(player(game, "Aoife"), result.winner());
        assertEquals(TieBreak.FEWER_POLLUTING, result.decidedBy());
    }

    @Test
    void fr14_ac5_q01_thenADieRollOffThatRerollsTies() {
        // Aoife and Ben tie on everything; they roll 4 and 4, then 2 and 5.
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).die(4).die(4).die(2).die(5));
        setGP(player(game, "Aoife"), 20);
        setGP(player(game, "Ben"), 20);

        GameResult result = Scoring.score(game);
        assertSame(player(game, "Ben"), result.winner());
        assertEquals(TieBreak.ROLL_OFF, result.decidedBy());
        assertEquals(Map.of(player(game, "Aoife"), List.of(4, 2), player(game, "Ben"), List.of(4, 5)), result.rollOff());
        assertSame(player(game, "Ben"), result.ranking().get(0).player());
    }

    @Test
    void fr14_ac6_scoresAreExactAndShownToOneDecimalPlace() {
        Game game = Games.scripted();
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        setGP(aoife, 30);
        setMoney(aoife, 22); // 34.4
        setGP(ben, 30);
        setMoney(ben, 21);   // 34.2
        netZero(game);

        GameResult result = Scoring.score(game);
        assertSame(aoife, result.winner());
        assertEquals(TieBreak.SCORE, result.decidedBy());
        assertEquals("34.4", result.scoreOf(aoife).score().display());
        assertEquals("34.2", result.scoreOf(ben).score().display());
        assertEquals(new BigDecimal("34.4"), result.scoreOf(aoife).score().value());
    }

    @Test
    void scoresCanBeNegative() {
        Game game = Games.scripted();
        Player aoife = player(game, "Aoife");
        setGP(aoife, 2);
        own(game, 1, aoife);
        assertEquals("-3.0", Scoring.score(game).scoreOf(aoife).score().display());
    }

    @Test
    void theRankingIsBestFirstAndHasEveryPlayer() {
        Game game = Games.scripted();
        GameResult result = Scoring.score(game); // missed net zero: scores are GP 13, 12, 10, 8
        assertEquals(List.of("Dev", "Chloe", "Ben", "Aoife"), result.ranking().stream().map(s -> s.player().name()).toList());
        assertSame(result.ranking().get(0).player(), result.winner());
        assertTrue(result.rollOff().isEmpty());
    }

    @Test
    void legacyScoresCompareExactly() {
        LegacyScore a = new LegacyScore(172, 5);
        LegacyScore b = new LegacyScore(171, 5);
        assertTrue(a.compareTo(b) > 0);
        assertEquals(0, a.compareTo(new LegacyScore(172, 5)));
        assertEquals("34.4", a.toString());
    }
}
