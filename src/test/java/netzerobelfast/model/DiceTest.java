package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The dice: repeatable with a seed, always in range (groundwork for FR-03 AC-1). */
class DiceTest {

    @Test
    void fr03_ac1_sameSeedGivesSameRolls() {
        Dice first = new Dice(new Random(42));
        Dice second = new Dice(new Random(42));

        int[] a = new int[1_000];
        int[] b = new int[1_000];
        for (int i = 0; i < a.length; i++) {
            a[i] = first.rollTwo();
            b[i] = second.rollTwo();
        }
        assertArrayEquals(a, b);
    }

    @Test
    void fr03_ac1_rollsStayInRangeAndCoverEveryValue() {
        Dice dice = new Dice(new Random(7));
        boolean[] seenOne = new boolean[7];
        boolean[] seenTwo = new boolean[13];
        for (int i = 0; i < 10_000; i++) {
            int one = dice.rollOne();
            int two = dice.rollTwo();
            assertTrue(one >= 1 && one <= 6, "rollOne gave " + one);
            assertTrue(two >= 2 && two <= 12, "rollTwo gave " + two);
            seenOne[one] = true;
            seenTwo[two] = true;
        }
        for (int face = 1; face <= 6; face++) {
            assertTrue(seenOne[face], "rollOne never gave " + face);
        }
        for (int total = 2; total <= 12; total++) {
            assertTrue(seenTwo[total], "rollTwo never gave " + total);
        }
    }

    @Test
    void eachFaceComesUpAboutEquallyOften() {
        // Guards against an off-by-one such as nextInt(5) + 1. Expected 10,000 each.
        Dice dice = new Dice(new Random(2026));
        int[] counts = new int[7];
        for (int i = 0; i < 60_000; i++) {
            counts[dice.rollOne()]++;
        }
        for (int face = 1; face <= 6; face++) {
            assertTrue(counts[face] >= 9_000 && counts[face] <= 11_000, "face " + face + " came up " + counts[face] + " times");
        }
    }

    @Test
    void sharesItsRandomSource() {
        Random random = new Random(1);
        assertSame(random, new Dice(random).random());
    }

    @Test
    void needsARandomSource() {
        assertThrows(NullPointerException.class, () -> new Dice(null));
    }
}
