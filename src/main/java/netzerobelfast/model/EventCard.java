package netzerobelfast.model;

import netzerobelfast.config.CardDef;
import netzerobelfast.config.CardDef.Effect;

/**
 * An event card: something that happens to the player, applied at once (FR-10 AC-5). A cost is
 * compulsory, so it can make the player bankrupt, and Resilience doesn't reduce it.
 */
public final class EventCard extends ClimateCard {

    private final Effect effect;

    EventCard(CardDef definition) {
        super(definition);
        this.effect = definition.effect();
    }

    public Effect effect() {
        return effect;
    }
}
