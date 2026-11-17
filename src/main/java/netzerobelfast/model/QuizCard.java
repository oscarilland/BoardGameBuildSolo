package netzerobelfast.model;

import netzerobelfast.config.CardDef;

import java.util.List;

/** A quiz card: four answers, one correct. A correct answer earns GP (FR-10 AC-3). */
public final class QuizCard extends ClimateCard {

    private final List<String> answers;
    private final int correctIndex;
    private final int reward;

    QuizCard(CardDef definition, int reward) {
        super(definition);
        this.answers = definition.answers();
        this.correctIndex = definition.correctIndex();
        this.reward = reward;
    }

    public List<String> answers() {
        return answers;
    }

    /** GP for a correct answer: the card's own reward, or the config default (Q-13). */
    public int reward() {
        return reward;
    }

    public boolean checkAnswer(int index) {
        return index == correctIndex;
    }
}
