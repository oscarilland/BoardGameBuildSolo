package netzerobelfast.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A Legacy Score, kept exact (Q-16). Money counts as money ÷ 5, which is usually a fraction, so
 * the score is stored multiplied by that divisor: 5 × GP + money in a net zero game. Comparing
 * these whole numbers is exact, so 36.4 always beats 36.2, and nothing is rounded before the
 * winner is chosen. {@link #display()} shows one decimal place.
 *
 * <p>This replaces the analysis model's {@code legacyScore(netZero): int}.
 *
 * @param scaled  the score multiplied by {@code divisor}
 * @param divisor the money divisor from {@code scoring.moneyDivisor} (5)
 */
public record LegacyScore(int scaled, int divisor) implements Comparable<LegacyScore> {

    /** The score as a decimal: exact for a divisor of 5 or 2, otherwise to four places. */
    public BigDecimal value() {
        return BigDecimal.valueOf(scaled).divide(BigDecimal.valueOf(divisor), 4, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /** The score to one decimal place, for the end screen, for example "36.4" or "-3.0". */
    public String display() {
        return BigDecimal.valueOf(scaled).divide(BigDecimal.valueOf(divisor), 1, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public int compareTo(LegacyScore other) {
        // Both scores use the same divisor in a game; cross-multiplying keeps it exact anyway.
        return Long.compare((long) scaled * other.divisor, (long) other.scaled * divisor);
    }

    @Override
    public String toString() {
        return display();
    }
}
