package netzerobelfast.model;

import java.util.Objects;
import java.util.Random;

/**
 * The game's dice, and its only source of randomness.
 *
 * <p>The {@link Random} is passed in rather than created here, so a test can give it a fixed
 * seed and get the same rolls every time. Every random choice in the game (first player,
 * disasters, card shuffles) will use this one source, so a seed replays a whole game.
 */
public final class Dice {

    private static final int SIDES = 6;

    private final Random random;

    public Dice(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    /** One six-sided die: 1 to 6. Used for disasters and returning from bankruptcy. */
    public int rollOne() {
        return random.nextInt(SIDES) + 1;
    }

    /** Two dice added together: 2 to 12. Used for movement (FR-03). */
    public int rollTwo() {
        return rollOne() + rollOne();
    }

    /** The shared random source, for choices that aren't dice rolls, such as shuffling cards. */
    public Random random() {
        return random;
    }
}
