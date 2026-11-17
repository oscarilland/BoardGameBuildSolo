package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

/** The Farmer. Ability: nature projects earn extra GP every round (paid at the end of the round, plan 005). */
public final class Farmer extends Character {

    private final int natureBonusGP;

    Farmer(CharacterDef definition, Rules rules) {
        super(definition, rules);
        this.natureBonusGP = number("natureBonusGP");
    }

    /** Extra GP per round for each nature project this player owns. */
    public int natureBonusGP() {
        return natureBonusGP;
    }
}