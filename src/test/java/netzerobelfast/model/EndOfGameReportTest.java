package netzerobelfast.model;

import netzerobelfast.model.Game.Phase;
import netzerobelfast.model.GameResult.MeterLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FR-16 (as data) and the result arriving at game over (FR-14, FR-11 AC-7). */
class EndOfGameReportTest {

    /** A game whose 12 ends of round have no disaster. */
    private static Game quietGame() {
        ScriptedRandom script = new ScriptedRandom().firstPlayer(0);
        for (int i = 0; i < 12; i++) {
            script.noDisaster();
        }
        return Games.withScript(script);
    }

    private static void endRounds(Game game, int rounds) {
        for (int i = 0; i < rounds; i++) {
            game.endRound();
        }
    }

    @Test
    void theResultArrivesOnceTheGameIsOver() {
        Game game = quietGame();
        endRounds(game, 11);
        assertTrue(game.result().isEmpty());
        game.endRound();
        assertEquals(Phase.GAME_OVER, game.phase());
        GameResult result = game.result().orElseThrow();
        assertEquals(4, result.ranking().size());
        assertSame(result.ranking().get(0).player(), result.winner());
        assertEquals(game.meter().value(), result.finalMeter());
    }

    @Test
    void fr16_ac1_theMeterHistoryAndTheLinesToDraw() {
        Game game = quietGame();
        endRounds(game, 12);
        GameResult result = game.result().orElseThrow();
        assertEquals(13, result.meterHistory().size());
        assertEquals(50, result.meterHistory().get(0));
        assertEquals(List.of(new MeterLine("2030 target", 2030, 26), new MeterLine("2040 target", 2040, 11),
                new MeterLine("Net zero", null, 0)), result.meterLines());
    }

    @Test
    void fr16_ac2_sharesCountEveryRoundOfPollution() {
        Game game = quietGame();
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        own(game, 1, aoife);
        own(game, 4, aoife);
        own(game, 6, ben);

        endRounds(game, 6);
        Games.convert(game, 1, aoife);
        Games.convert(game, 4, aoife);
        endRounds(game, 6);

        assertEquals(12, aoife.pollutingSquareRounds(), "2 squares × 6 rounds; converting later doesn't erase them");
        assertEquals(12, ben.pollutingSquareRounds(), "1 square × 12 rounds");
        GameResult result = game.result().orElseThrow();
        assertEquals(new BigDecimal("50.0"), result.pollutingShares().get(aoife));
        assertEquals(new BigDecimal("50.0"), result.pollutingShares().get(ben));
        assertEquals(new BigDecimal("0.0"), result.pollutingShares().get(player(game, "Chloe")));
    }

    @Test
    void fr16_ac2_sharesAddUpToAHundred() {
        Game game = quietGame();
        own(game, 1, player(game, "Aoife"));
        own(game, 4, player(game, "Ben"));
        own(game, 6, player(game, "Chloe"));
        endRounds(game, 12);
        BigDecimal total = game.result().orElseThrow().pollutingShares().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(total.subtract(new BigDecimal("100")).abs().compareTo(new BigDecimal("0.2")) <= 0,
                "thirds round to 33.3 each: " + total);
    }

    @Test
    void fr16_ac2_nobodyPollutedSoEverySharesIsZero() {
        Game game = quietGame();
        endRounds(game, 12);
        game.result().orElseThrow().pollutingShares().values()
                .forEach(share -> assertEquals(new BigDecimal("0.0"), share));
    }

    @Test
    void fr16_ac3_theReportsScoresAreTheScoring() {
        Game game = quietGame();
        endRounds(game, 12);
        GameResult result = game.result().orElseThrow();
        assertEquals("Dev", result.winner().name(), "missed net zero; Dev has the most GP");
        assertEquals("13.0", result.scoreOf(result.winner()).score().display());
    }
}
