package netzerobelfast.model;

import netzerobelfast.config.CardDef;
import netzerobelfast.config.CardDef.Source;
import netzerobelfast.config.GameConfig;

import java.util.List;

/**
 * A Climate Card (analysis model: {@code ClimateCard} {abstract}). Every card has a real fact
 * and at least one source, shown after the card is resolved (FR-10 AC-6, NFR-10).
 *
 * <p>In the analysis model, {@code resolve(p, g)} asked the player directly. Here the game
 * draws the card, waits in a phase for the player's answer (quiz) or option (choice), then
 * applies it, so the model never talks to the screen (see the game flow page).
 */
public abstract sealed class ClimateCard permits QuizCard, ChoiceCard, EventCard {

    private final String id;
    private final String text;
    private final String fact;
    private final List<Source> sources;

    protected ClimateCard(CardDef definition) {
        this.id = definition.id();
        this.text = definition.text();
        this.fact = definition.fact();
        this.sources = definition.sources();
    }

    /** Makes the right kind of card for a data entry. */
    static ClimateCard from(CardDef definition, GameConfig config) {
        return switch (definition.type()) {
            case QUIZ -> new QuizCard(definition, config.quizReward(definition));
            case CHOICE -> new ChoiceCard(definition);
            case EVENT -> new EventCard(definition);
        };
    }

    public String id() {
        return id;
    }

    /** What the card says, shown when it's drawn. */
    public String text() {
        return text;
    }

    /** The real-world fact, shown after the card is resolved. */
    public String fact() {
        return fact;
    }

    /** Where the fact comes from: each source's title and web address (NFR-10). */
    public List<Source> sources() {
        return sources;
    }

    @Override
    public String toString() {
        return id;
    }
}
