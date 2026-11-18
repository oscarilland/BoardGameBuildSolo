package netzerobelfast.model;

import netzerobelfast.config.Rules.InterimTarget;

import java.util.List;

/**
 * What happened at the end of a round, for the end-of-round pop-up (plan 007).
 *
 * @param round        the round that just ended
 * @param year         its year
 * @param meterBefore  the meter before step 3 (after any Choice cards this round)
 * @param meterAfter   the meter after step 3
 * @param band         the danger band used in step 5
 * @param target       the interim target checked in step 4, or null if this round has none
 * @param targetMet    whether it was met (false if there was no target)
 * @param disasterRoll the step 5 die
 * @param disaster     what the disaster did, or null if there wasn't one
 * @param steps        the steps in the order they ran (FR-11 AC-2)
 */
public record RoundReport(
        int round,
        int year,
        int meterBefore,
        int meterAfter,
        int band,
        InterimTarget target,
        boolean targetMet,
        int disasterRoll,
        DisasterResult disaster,
        List<Step> steps) {

    /** The end-of-round steps, in the order FR-11 requires. */
    public enum Step { INCOME, CARBON_TAX, METER, INTERIM_TARGET, DISASTER_CHECK, ADVANCE }
}
