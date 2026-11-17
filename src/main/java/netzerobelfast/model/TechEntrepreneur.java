package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

/** The Tech Entrepreneur. Ability: Green Projects cost less to buy. */
public final class TechEntrepreneur extends Character {

    private final int greenProjectDiscount;

    TechEntrepreneur(CharacterDef definition, Rules rules) {
        super(definition, rules);
        this.greenProjectDiscount = number("greenProjectDiscount");
    }

    @Override
    public int buyPrice(OwnableSquare square) {
        if (square instanceof GreenProjectSquare) {
            return Math.max(0, square.price() - greenProjectDiscount);
        }
        return square.price();
    }
}