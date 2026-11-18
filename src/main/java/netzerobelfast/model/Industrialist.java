package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

/**
 * The Industrialist. Ability, "just transition": a polluting square they convert earns extra
 * income every round. Squares record who converted them ({@link GreenProjectSquare#convertedBy});
 * the extra income is paid at the end of the round (plan 005).
 */
public final class Industrialist extends Character {

    private final int convertedBonusIncome;

    Industrialist(CharacterDef definition, Rules rules) {
        super(definition, rules);
        this.convertedBonusIncome = number("convertedBonusIncome");
    }

    /** Extra £M per round from each square this player converted. */
    public int convertedBonusIncome() {
        return convertedBonusIncome;
    }

    /** "Just transition": a Green Project they converted from Polluting earns extra (FR-07 AC-5). */
    @Override
    public int incomeBonus(GreenProjectSquare square, Player owner) {
        return square.convertedBy() == owner ? convertedBonusIncome : 0;
    }
}