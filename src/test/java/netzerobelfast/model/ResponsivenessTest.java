package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** NFR-04 AC-3: a headless proxy for "every action responds within 1 second". */
class ResponsivenessTest {

    /** The budget for one end of round, the heaviest single step in the game. */
    private static final long BUDGET_NANOS = 50_000_000L;

    @Test
    void nfr04_ac3_endOfRoundUnder50ms() {
        // A busy six-player board: every Polluting square and Green Project owned.
        Game game = Games.start(new Random(2026), Games.SIX);
        int seat = 0;
        for (Square square : game.board().squares()) {
            if (square instanceof OwnableSquare) {
                Games.own(game, square.id(), game.players().get(seat++ % game.players().size()));
            }
        }
        // Best of several runs, so one slow moment on a shared CI machine doesn't fail the build.
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 6; i++) {
            long started = System.nanoTime();
            game.endRound();
            best = Math.min(best, System.nanoTime() - started);
        }
        assertTrue(best < BUDGET_NANOS, "fastest end of round took " + best / 1_000_000.0 + " ms");
    }
}
