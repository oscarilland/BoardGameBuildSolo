package netzerobelfast.model;

/**
 * The shared Climate Meter (analysis model: {@code ClimateMeter}). It rises with pollution and
 * falls with green projects; at 0 or below after the last round, Belfast reaches net zero.
 *
 * <p>Plan 004 builds only the start value and {@link #adjust}, which Choice cards use straight
 * away (FR-10 AC-4). The end-of-round update, the history, danger bands and interim targets
 * arrive in plan 005. The meter can go below 0 (Q-02).
 */
public final class ClimateMeter {

    private int value;

    ClimateMeter(int start) {
        this.value = start;
    }

    public int value() {
        return value;
    }

    void adjust(int delta) {
        value += delta;
    }
}
