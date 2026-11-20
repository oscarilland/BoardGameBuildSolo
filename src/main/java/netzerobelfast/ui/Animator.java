package netzerobelfast.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.LongSupplier;

/**
 * Runs the game's animations (plan 008): dice, counters sliding, the meter easing.
 *
 * <p>This is the only class in {@code ui} that may use a timer (NFR-11 AC-2, checked by a
 * test). The timer runs only while an animation is running, and stops the moment the last one
 * ends, so an idle game uses no CPU. The model has already changed before an animation starts:
 * animation is only how the change is drawn, so skipping one can never change the game.
 *
 * <p>The clock and the timer are passed in, so tests can drive it with a fake clock.
 */
final class Animator {

    /** About 60 frames a second. */
    static final int FRAME_MILLIS = 16;

    /** Starts and stops repeated ticks. The Swing version wraps a {@code javax.swing.Timer}. */
    interface Ticker {
        void start(Runnable onTick);

        void stop();

        boolean running();
    }

    /** One animation: its length, and what to draw at each point from 0 (start) to 1 (end). */
    private record Animation(long start, long length, DoubleConsumer frame, Runnable done) {}

    private final LongSupplier clock;
    private final Ticker ticker;
    private final List<Animation> running = new ArrayList<>();
    private final List<Runnable> whenIdle = new ArrayList<>();

    Animator(LongSupplier clockMillis, Ticker ticker) {
        this.clock = clockMillis;
        this.ticker = ticker;
    }

    /** The real animator: the system clock and a Swing timer on the event dispatch thread. */
    static Animator swing() {
        return new Animator(System::currentTimeMillis, new SwingTicker());
    }

    /**
     * Starts an animation lasting {@code millis}. {@code frame} gets the progress, eased, from
     * 0 to 1; it always gets exactly 1 at the end, so the final picture is the true state.
     */
    void play(long millis, DoubleConsumer frame, Runnable done) {
        running.add(new Animation(clock.getAsLong(), Math.max(1, millis), frame, done));
        frame.accept(0);
        if (!ticker.running()) {
            ticker.start(this::tick);
        }
    }

    /** One frame: advance every animation, and finish those that have run their length. */
    void tick() {
        long now = clock.getAsLong();
        List<Animation> finished = new ArrayList<>();
        for (Animation animation : List.copyOf(running)) {
            double t = (now - animation.start()) / (double) animation.length();
            if (t >= 1) {
                finished.add(animation);
            } else {
                animation.frame().accept(ease(t));
            }
        }
        finish(finished);
    }

    /**
     * Jumps every running animation to its end at once (any key or click). An animation that
     * starts another when it ends (the dice start the counter's slide) is followed through too,
     * so nothing is left running.
     */
    void skip() {
        while (!running.isEmpty()) {
            finish(List.copyOf(running));
        }
    }

    private void finish(List<Animation> finished) {
        for (Animation animation : finished) {
            running.remove(animation);
            animation.frame().accept(1);
            if (animation.done() != null) {
                animation.done().run();
            }
        }
        if (running.isEmpty()) {
            ticker.stop();
            List<Runnable> waiting = List.copyOf(whenIdle);
            whenIdle.clear();
            waiting.forEach(Runnable::run);
        }
    }

    boolean busy() {
        return !running.isEmpty();
    }

    /** Runs {@code action} once nothing is animating: at once if nothing is. */
    void whenIdle(Runnable action) {
        if (running.isEmpty()) {
            action.run();
        } else {
            whenIdle.add(action);
        }
    }

    /** Ease in and out, so movement starts and stops gently. */
    static double ease(double t) {
        double clamped = Math.max(0, Math.min(1, t));
        return clamped < 0.5 ? 2 * clamped * clamped : 1 - Math.pow(-2 * clamped + 2, 2) / 2;
    }

    /** The real ticker: a Swing timer, so ticks arrive on the event dispatch thread. */
    private static final class SwingTicker implements Ticker {
        private javax.swing.Timer timer;

        @Override
        public void start(Runnable onTick) {
            stop();
            timer = new javax.swing.Timer(FRAME_MILLIS, e -> onTick.run());
            timer.start();
        }

        @Override
        public void stop() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }

        @Override
        public boolean running() {
            return timer != null && timer.isRunning();
        }
    }

    /** How many animations are running now. */
    int count() {
        return running.size();
    }
}
