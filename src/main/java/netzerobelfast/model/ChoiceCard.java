package netzerobelfast.model;

import netzerobelfast.config.CardDef;
import netzerobelfast.config.CardDef.Option;

import java.util.List;

/**
 * A choice card: a trade-off between option A (index 0) and option B (index 1). Each option's
 * money, GP and meter changes are shown before choosing, and apply at once (FR-10 AC-4).
 */
public final class ChoiceCard extends ClimateCard {

    private final List<Option> options;

    ChoiceCard(CardDef definition) {
        super(definition);
        this.options = definition.options();
    }

    public List<Option> options() {
        return options;
    }

    public Option option(int index) {
        return options.get(index);
    }
}
