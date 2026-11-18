package netzerobelfast.model;

import netzerobelfast.config.Rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The shared Climate Meter (analysis model: {@code ClimateMeter}). It rises with pollution and
 * falls with green projects; at or below the net zero value after the last round, Belfast
 * reaches net zero. Every number comes from {@code climateMeter} and the square rules in
 * {@code config.json}.
 *
 * <p>Choice cards change it at once ({@link #adjust}); the end of each round updates it from the
 * squares owned ({@link #update}) and records the value for the end-of-game graph (FR-16). It
 * can go below 0 (Q-02).
 */
public final class ClimateMeter {

    private final Rules rules;
    private final List<Integer> history = new ArrayList<>();
    private int value;

    ClimateMeter(Rules rules) {
        this.rules = rules;
        this.value = rules.meter().start();
        history.add(value);
    }

    public int value() {
        return value;
    }

    /** The value at the start and after every round so far: one more entry than rounds played. */
    public List<Integer> history() {
        return List.copyOf(history);
    }

    void adjust(int delta) {
        value += delta;
    }

    /**
     * FR-11 step 3: +base for existing emissions, plus each Polluting square's rise, plus each
     * Green Project's fall (a negative number in the data). Records the new value.
     */
    void update(int pollutingSquares, int greenProjects) {
        value += rules.meter().basePerRound()
                + pollutingSquares * rules.polluting().meterPerRound()
                + greenProjects * rules.green().meterPerRound();
        history.add(value);
    }

    /**
     * The danger band for the current value (FR-12 AC-1): the first band whose upper limit isn't
     * exceeded, or the top band. Below 0 uses the band the data names.
     */
    public int dangerBand() {
        if (value < 0) {
            return rules.meter().belowZeroBand();
        }
        for (Rules.DangerBand band : rules.meter().dangerBands()) {
            if (band.maxMeter() == null || value <= band.maxMeter()) {
                return band.band();
            }
        }
        throw new IllegalStateException("the data's danger bands don't cover " + value);
    }

    /** The interim target checked at the end of this round, if there is one (rounds 3 and 8 in the data). */
    public Optional<Rules.InterimTarget> targetFor(int round) {
        return rules.meter().interimTargets().stream().filter(t -> t.round() == round).findFirst();
    }

    /** True if the meter is at or below the target's value. */
    public boolean meetsTarget(Rules.InterimTarget target) {
        return value <= target.maxMeter();
    }

    /** True if the meter is at or below the net zero value (0 in the data). */
    public boolean netZero() {
        return value <= rules.meter().netZeroAtOrBelow();
    }
}
