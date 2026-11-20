package netzerobelfast.ui;

import netzerobelfast.model.Game;
import netzerobelfast.model.TestGames;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plan 008: the animator, driven by a fake clock, and the counter's slide (NFR-04 AC-2, NFR-11 AC-2). */
class AnimatorTest {

    /** A clock the test moves by hand. */
    static final class FakeClock {
        long now;
    }

    /** A ticker that records whether it's running; the test calls {@code tick()} itself. */
    static final class FakeTicker implements Animator.Ticker {
        boolean running;
        int starts;

        @Override
        public void start(Runnable onTick) {
            running = true;
            starts++;
        }

        @Override
        public void stop() {
            running = false;
        }

        @Override
        public boolean running() {
            return running;
        }
    }

    private final FakeClock clock = new FakeClock();
    private final FakeTicker ticker = new FakeTicker();
    private final Animator animator = new Animator(() -> clock.now, ticker);

    @Test
    void anAnimationRunsToExactlyItsEnd() {
        List<Double> frames = new ArrayList<>();
        AtomicInteger done = new AtomicInteger();
        animator.play(400, frames::add, done::incrementAndGet);
        assertEquals(0.0, frames.get(0));
        assertTrue(ticker.running);

        clock.now = 200;
        animator.tick();
        assertEquals(0.5, frames.get(frames.size() - 1), 1e-9);
        assertEquals(0, done.get());

        clock.now = 450;
        animator.tick();
        assertEquals(1.0, frames.get(frames.size() - 1));
        assertEquals(1, done.get());
    }

    @Test
    void nfr11_ac2_theTimerStopsWhenTheLastAnimationEnds() {
        animator.play(100, t -> {}, null);
        animator.play(300, t -> {}, null);
        assertEquals(1, ticker.starts, "one timer for both");
        clock.now = 150;
        animator.tick();
        assertTrue(ticker.running, "one still running");
        assertEquals(1, animator.count());
        clock.now = 300;
        animator.tick();
        assertFalse(ticker.running);
        assertFalse(animator.busy());
    }

    @Test
    void nfr04_ac2_skipJumpsEverythingToTheEnd() {
        List<Double> a = new ArrayList<>();
        List<Double> b = new ArrayList<>();
        animator.play(1000, a::add, null);
        animator.play(500, b::add, null);
        animator.skip();
        assertEquals(1.0, a.get(a.size() - 1));
        assertEquals(1.0, b.get(b.size() - 1));
        assertFalse(ticker.running);
        assertFalse(animator.busy());
    }

    @Test
    void nfr04_ac2_skipAlsoFinishesAnimationsStartedByOthers() {
        // The dice start the slide when they end; one skip must finish both.
        List<Double> slide = new ArrayList<>();
        animator.play(450, t -> {}, () -> animator.play(800, slide::add, null));
        animator.skip();
        assertEquals(1.0, slide.get(slide.size() - 1));
        assertFalse(animator.busy());
        assertFalse(ticker.running);
    }

    @Test
    void whenIdleWaitsForTheLastAnimation() {
        AtomicInteger ran = new AtomicInteger();
        animator.whenIdle(ran::incrementAndGet);
        assertEquals(1, ran.get(), "runs at once when idle");
        animator.play(100, t -> {}, null);
        animator.whenIdle(ran::incrementAndGet);
        assertEquals(1, ran.get());
        clock.now = 100;
        animator.tick();
        assertEquals(2, ran.get());
    }

    @Test
    void anAnimationStartedWhenAnotherEndsKeepsTheTimerGoing() {
        // The dice start the slide when they finish.
        List<Double> slide = new ArrayList<>();
        animator.play(100, t -> {}, () -> animator.play(200, slide::add, null));
        clock.now = 100;
        animator.tick();
        assertTrue(animator.busy());
        assertTrue(ticker.running);
        clock.now = 300;
        animator.tick();
        assertEquals(1.0, slide.get(slide.size() - 1));
        assertFalse(ticker.running);
    }

    @Test
    void easingStartsAndEndsGently() {
        assertEquals(0.0, Animator.ease(0));
        assertEquals(0.5, Animator.ease(0.5), 1e-9);
        assertEquals(1.0, Animator.ease(1));
        assertTrue(Animator.ease(0.1) < 0.1);
        assertTrue(Animator.ease(0.9) > 0.9);
        assertEquals(1.0, Animator.ease(2));
    }

    // ---- The counter's slide ---------------------------------------------------------

    @Test
    void aMoveSlidesAlongItsPathSquareBySquare() {
        Game game = TestGames.script().rolls(5).start();
        game.roll();
        Game.Move move = game.lastMove().orElseThrow();
        assertEquals(List.of(0, 1, 2, 3, 4, 5), GameScreen.slidePath(0, 5, move));
    }

    @Test
    void aSlideThroughTheCityCentreFollowsTheRoute() {
        // Aoife reaches junction 12 in round 2 and cuts through the City Centre (35, 34, ...).
        Game game = TestGames.script().rolls(10, 3, 3, 3).noDisaster().rolls(4).start();
        for (int i = 0; i < 4; i++) {
            game.roll();
            game.endTurn();
        }
        game.roll();
        game.chooseRoute(netzerobelfast.model.Board.Route.CITY_CENTRE);
        Game.Move move = game.lastMove().orElseThrow();
        List<Integer> path = GameScreen.slidePath(10, game.currentPlayer().position(), move);
        assertEquals(List.of(10, 11, 12, 35, 34), path);
    }

    @Test
    void aGliderRideGoesStraightThere() {
        assertEquals(List.of(12, 30), GameScreen.slidePath(12, 30, null));
    }

    @Test
    void nfr04_ac2_aSlideNeverTakesMoreThanASecond() {
        assertEquals(280, GameScreen.slideMillis(1), "short moves still take a moment");
        assertEquals(840, GameScreen.slideMillis(12));
        assertEquals(1000, GameScreen.slideMillis(40));
    }
}
