package netzerobelfast.config;

import java.util.List;

/**
 * One Climate Card from {@code climate_cards.json}.
 *
 * <p>Fields that only some card types use are empty for the others: a Choice card has no
 * {@code answers} (empty list) and a {@code correctIndex} of -1; only an Event card has an
 * {@code effect}.
 *
 * @param rewardGP the card's own quiz reward, or null to use the config default (Q-13);
 *                 see {@link GameConfig#quizReward(CardDef)}
 * @param sources  where the fact comes from; at least one (NFR-10)
 */
public record CardDef(
        String id,
        Type type,
        String text,
        List<String> answers,
        int correctIndex,
        Integer rewardGP,
        List<Option> options,
        Effect effect,
        String fact,
        List<Source> sources) {

    public enum Type { QUIZ, CHOICE, EVENT }

    /** Changes to a player's money (£M) and Green Points, and to the shared Climate Meter. */
    public record Effect(int money, int gp, int meter) {}

    /** One option on a Choice card. */
    public record Option(String label, Effect effect) {}

    /** A cited source for a card's fact. */
    public record Source(String title, String url) {}
}
