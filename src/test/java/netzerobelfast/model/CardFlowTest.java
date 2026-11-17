package netzerobelfast.model;

import netzerobelfast.config.CardDef.Source;
import netzerobelfast.model.Action.Kind;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** How a card fits into a turn: drawn on arrival, answered, then the fact and sources shown (FR-10 AC-1, AC-6). */
class CardFlowTest {

    @Test
    void fr10_ac1_rollingOntoAClimateCardSquareDrawsACard() {
        Game game = Games.scripted(2);
        Games.nextCard(game, "Q01");
        game.roll();
        assertEquals(2, game.currentPlayer().position());
        assertEquals(Phase.AWAITING_QUIZ_ANSWER, game.phase());
        assertEquals("Q01", game.pendingCard().orElseThrow().id());
        assertEquals(29, game.deck().remaining());
    }

    @Test
    void nfr08_ac4_nothingElseIsAllowedWhileACardWaits() {
        Game game = Games.scripted(2);
        Games.nextCard(game, "C01");
        game.roll();

        assertTrue(game.availableActions().isEmpty());
        assertTrue(game.affordableOptions().size() == 2);
        assertEquals("Can't end the turn now: the game is waiting for a card option.",
                assertThrows(IllegalActionException.class, game::endTurn).getMessage());
        assertThrows(IllegalActionException.class, game::roll);
        assertThrows(IllegalActionException.class, () -> game.answerQuiz(0));
        assertEquals(Phase.AWAITING_CHOICE, game.phase());
    }

    @Test
    void answeringOutsideACardIsRefused() {
        Game game = Games.scripted();
        assertEquals("Can't answer a quiz now: the game is waiting for a dice roll.",
                assertThrows(IllegalActionException.class, () -> game.answerQuiz(0)).getMessage());
        assertThrows(IllegalActionException.class, () -> game.chooseOption(0));
        assertTrue(game.affordableOptions().isEmpty());
    }

    @Test
    void afterACardTheLandingActionIsUsedButConvertingIsAllowed() {
        Game game = Games.scripted();
        Games.own(game, 1, game.currentPlayer());
        Games.nextCard(game, "Q01");
        Games.arrive(game, 2);
        game.answerQuiz(2);

        assertEquals(Phase.MOVE_DONE, game.phase());
        List<Action> actions = game.availableActions();
        assertEquals(List.of(Action.on(Kind.CONVERT, 5, 1)), actions);
        game.endTurn();
    }

    @Test
    void fr10_ac6_nfr10_ac3_theFactAndEverySourceAreAvailableAfterwards() {
        Game game = Games.scripted();
        Games.nextCard(game, "C05");
        Games.arrive(game, 2);
        game.chooseOption(1);

        CardResult result = game.lastCard().orElseThrow();
        assertTrue(result.fact().startsWith("Agriculture is Northern Ireland's biggest source of emissions"));
        List<Source> sources = result.sources();
        assertEquals(2, sources.size());
        assertEquals("NISRA", sources.get(0).title());
        assertTrue(sources.get(0).url().startsWith("https://datavis.nisra.gov.uk/"));
        assertEquals("The Water Report", sources.get(1).title());
    }

    @Test
    void theLastCardIsClearedWhenTheNextMoveStarts() {
        Game game = Games.scripted(3, 3, 3, 3);
        Games.nextCard(game, "E03");
        Games.arrive(game, 2);
        assertTrue(game.lastCard().isPresent());

        game.endTurn();
        game.roll();
        assertFalse(game.lastCard().isPresent());
    }
}
