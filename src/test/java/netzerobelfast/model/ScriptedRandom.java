package netzerobelfast.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Test helper: a {@link Random} that returns a scripted sequence from {@code nextInt(bound)},
 * so a test can say exactly who goes first and what each roll is.
 *
 * <p>{@code Game.start} uses one value to pick the first player, then each roll of two dice
 * uses two values (one per die, 0-5 for faces 1-6).
 */
final class ScriptedRandom extends Random {

    private final Deque<Integer> values = new ArrayDeque<>();

    /** Scripts the first player's seat (0-based). */
    ScriptedRandom firstPlayer(int seat) {
        values.add(seat);
        return this;
    }

    /** Scripts two-dice totals, for example {@code rolls(4, 9)}. Each total is split into two valid dice. */
    ScriptedRandom rolls(int... totals) {
        for (int total : totals) {
            if (total < 2 || total > 12) {
                throw new IllegalArgumentException("two dice can't total " + total);
            }
            int first = Math.max(1, total - 6);
            values.add(first - 1);
            values.add(total - first - 1);
        }
        return this;
    }

    @Override
    public int nextInt(int bound) {
        if (values.isEmpty()) {
            throw new IllegalStateException("the test didn't script enough random values");
        }
        int value = values.removeFirst();
        if (value < 0 || value >= bound) {
            throw new IllegalStateException("scripted value " + value + " is outside 0-" + (bound - 1));
        }
        return value;
    }
}
