package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

/**
 * The Community Activist. Ability: lobbying is free, and a square they lobby adds nothing to
 * the Climate Meter that round (the meter is updated at the end of the round, plan 005).
 */
public final class CommunityActivist extends Character {

    private final int lobbyCost;
    private final boolean lobbyCancelsMeter;

    CommunityActivist(CharacterDef definition, Rules rules) {
        super(definition, rules);
        this.lobbyCost = number("lobbyCost");
        this.lobbyCancelsMeter = flag("lobbyCancelsMeter");
    }

    @Override
    public int lobbyCost() {
        return lobbyCost;
    }

    @Override
    public boolean lobbyCancelsMeter() {
        return lobbyCancelsMeter;
    }
}