package netzerobelfast.model;

import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FR-10 AC-3: quiz cards. Q01 asks for NI's net zero year; "2050" is answer 2. Square 2 is a Climate Card square. */
class QuizCardTest {

    private static Game drawQ01() {
        Game game = Games.scripted();
        Games.nextCard(game, "Q01");
        Games.arrive(game, 2);
        return game;
    }

    @Test
    void fr10_ac3_aCorrectAnswerEarnsTwoGP() {
        Game game = drawQ01();
        assertEquals(Phase.AWAITING_QUIZ_ANSWER, game.phase());
        QuizCard q01 = (QuizCard) game.pendingCard().orElseThrow();
        assertEquals(List.of("2030", "2040", "2050", "2060"), q01.answers());
        assertEquals(2, q01.reward());

        game.answerQuiz(2);
        assertEquals(8 + 2, game.currentPlayer().greenPoints());
        CardResult result = game.lastCard().orElseThrow();
        assertTrue(result.correct());
        assertEquals(2, result.answer());
        assertEquals(2, result.gpChange());
        assertEquals(Phase.MOVE_DONE, game.phase());
        assertTrue(game.pendingCard().isEmpty());
    }

    @Test
    void fr10_ac3_aWrongAnswerEarnsNothing() {
        Game game = drawQ01();
        game.answerQuiz(0);
        assertEquals(8, game.currentPlayer().greenPoints());
        assertFalse(game.lastCard().orElseThrow().correct());
        assertEquals(0, game.lastCard().orElseThrow().gpChange());
    }

    @Test
    void anAnswerThatIsntOnTheCardIsRefused() {
        Game game = drawQ01();
        IllegalActionException e = assertThrows(IllegalActionException.class, () -> game.answerQuiz(4));
        assertEquals("Choose one of the 4 answers.", e.getMessage());
        assertThrows(IllegalActionException.class, () -> game.answerQuiz(-1));
        assertEquals(Phase.AWAITING_QUIZ_ANSWER, game.phase());
        assertEquals(8, game.currentPlayer().greenPoints());
    }

    @Test
    void checkAnswerOnTheCard() {
        QuizCard q01 = (QuizCard) drawQ01().pendingCard().orElseThrow();
        assertTrue(q01.checkAnswer(2));
        assertFalse(q01.checkAnswer(1));
        assertEquals("What year does Northern Ireland's Climate Change Act set as the deadline for net zero?", q01.text());
    }
}
