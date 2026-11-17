package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** FR-10 AC-1 and AC-2: the deck of 30 cards, shuffled from the seed, with no repeats until a reshuffle. */
class CardDeckTest {

    private static List<String> draw(CardDeck deck, int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(deck.draw().id());
        }
        return ids;
    }

    @Test
    void fr10_ac1_theDeckHasTheThirtyCardsFromTheData() {
        Game game = Games.start(new Random(1), Games.FOUR);
        assertEquals(30, game.deck().remaining());
        Set<String> ids = new HashSet<>(draw(game.deck(), 30));
        assertEquals(30, ids.size());
        assertEquals(true, ids.contains("Q01") && ids.contains("C09") && ids.contains("E09"));
    }

    @Test
    void fr10_ac2_theSameSeedGivesTheSameOrder() {
        List<String> first = draw(Games.start(new Random(42), Games.FOUR).deck(), 30);
        List<String> second = draw(Games.start(new Random(42), Games.FOUR).deck(), 30);
        assertEquals(first, second);
        assertNotEquals(first, draw(Games.start(new Random(43), Games.FOUR).deck(), 30), "a different seed shuffles differently");
    }

    @Test
    void fr10_ac2_noRepeatsUntilEveryCardIsDrawnThenAReshuffle() {
        CardDeck deck = Games.start(new Random(7), Games.FOUR).deck();
        Set<String> firstPile = new HashSet<>(draw(deck, 30));
        assertEquals(30, firstPile.size());
        assertEquals(0, deck.remaining());

        String thirtyFirst = deck.draw().id();
        assertEquals(true, firstPile.contains(thirtyFirst), "the 31st card comes from the reshuffled pile");
        assertEquals(29, deck.remaining());
        Set<String> secondPile = new HashSet<>(draw(deck, 29));
        secondPile.add(thirtyFirst);
        assertEquals(30, secondPile.size(), "the second pile has no repeats either");
    }

    @Test
    void putOnTopChoosesTheNextCard() {
        Game game = Games.start(new Random(3), Games.FOUR);
        Games.nextCard(game, "C05");
        ClimateCard card = game.deck().draw();
        assertEquals("C05", card.id());
        assertInstanceOf(ChoiceCard.class, card);
        assertEquals("C05", card.toString());

        Games.nextCard(game, "C05"); // from the used pile back to the top
        assertEquals("C05", game.deck().draw().id());
        assertThrows(IllegalArgumentException.class, () -> Games.nextCard(game, "Z99"));
    }

    @Test
    void shufflingDoesntChangeTheScriptedDice() {
        // The deck has its own Random, so plans 002-003's scripted rolls still come out as scripted.
        Game game = Games.scripted(7);
        game.roll();
        assertEquals(7, game.lastMove().orElseThrow().diceTotal());
    }
}
