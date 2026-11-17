package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

/** The Council Planner. Ability: each Resilience level costs less. */
public final class CouncilPlanner extends Character {

    private final int resilienceDiscount;

    CouncilPlanner(CharacterDef definition, Rules rules) {
        super(definition, rules);
        this.resilienceDiscount = number("resilienceDiscount");
    }

    @Override
    public int resilienceCost(int level) {
        return Math.max(0, super.resilienceCost(level) - resilienceDiscount);
    }
}