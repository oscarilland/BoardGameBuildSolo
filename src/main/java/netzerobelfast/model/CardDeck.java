package netzerobelfast.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * The Climate Card deck (analysis model: {@code CardDeck}). Shuffled at the start; cards are
 * drawn from the top, and when the draw pile runs out the used cards are shuffled back in
 * (FR-10 AC-2). No card comes up twice until every card has been drawn.
 *
 * <p>The deck shuffles with its own {@link Random}, which {@link Game} seeds from the game's one
 * random source. A whole game still replays from one seed, and shuffling doesn't change the
 * dice sequence that tests script.
 */
public final class CardDeck {

    private final Deque<ClimateCard> drawPile = new ArrayDeque<>();
    private final List<ClimateCard> used = new ArrayList<>();
    private final Random random;

    CardDeck(List<ClimateCard> cards, Random random) {
        this.random = random;
        List<ClimateCard> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled, random);
        drawPile.addAll(shuffled);
    }

    public ClimateCard draw() {
        if (drawPile.isEmpty()) {
            reshuffle();
        }
        ClimateCard card = drawPile.removeFirst();
        used.add(card);
        return card;
    }

    /** Shuffles the used cards back into the draw pile. */
    void reshuffle() {
        List<ClimateCard> cards = new ArrayList<>(used);
        used.clear();
        Collections.shuffle(cards, random);
        drawPile.addAll(cards);
    }

    /** Cards left before the next reshuffle. */
    public int remaining() {
        return drawPile.size();
    }

    /** Test set-up only: makes the card with this id the next one drawn. */
    void putOnTop(String id) {
        ClimateCard card = find(id);
        drawPile.remove(card);
        used.remove(card);
        drawPile.addFirst(card);
    }

    private ClimateCard find(String id) {
        for (ClimateCard card : drawPile) {
            if (card.id().equals(id)) {
                return card;
            }
        }
        for (ClimateCard card : used) {
            if (card.id().equals(id)) {
                return card;
            }
        }
        throw new IllegalArgumentException("no card " + id);
    }
}
