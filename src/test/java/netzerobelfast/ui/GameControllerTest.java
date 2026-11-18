package netzerobelfast.ui;

import netzerobelfast.model.Action;
import netzerobelfast.model.CardResult;
import netzerobelfast.model.ChoiceCard;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.ReturnAttempt;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.Player;
import netzerobelfast.model.QuizCard;
import netzerobelfast.model.RoundReport;
import netzerobelfast.model.TestGames;
import netzerobelfast.ui.ActionOptions.Button;
import netzerobelfast.ui.ActionOptions.Command;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The controller: one model call per press, one redraw, and the right pop-up for what happened. */
class GameControllerTest {

    /** Records what the controller asked the screen to do, and answers cards from a script. */
    private static final class FakeView implements GameController.View {
        final List<String> calls = new ArrayList<>();
        final Deque<Integer> answers = new ArrayDeque<>();
        final List<CardResult> cards = new ArrayList<>();
        final List<RoundReport> rounds = new ArrayList<>();
        final List<ReturnAttempt> returns = new ArrayList<>();
        final List<GameResult> ends = new ArrayList<>();
        List<Integer> affordableSeen;

        long count(String call) {
            return calls.stream().filter(call::equals).count();
        }

        @Override
        public void refresh() {
            calls.add("refresh");
        }

        @Override
        public void showError(String message) {
            calls.add("error");
        }

        @Override
        public int askQuiz(QuizCard card) {
            calls.add("quiz");
            return answers.isEmpty() ? 0 : answers.removeFirst();
        }

        @Override
        public int askChoice(ChoiceCard card, List<Integer> affordable) {
            calls.add("choice");
            affordableSeen = affordable;
            return answers.isEmpty() ? affordable.get(0) : answers.removeFirst();
        }

        @Override
        public void showCardResult(CardResult result) {
            calls.add("card");
            cards.add(result);
        }

        @Override
        public void showRoundReport(RoundReport report) {
            calls.add("round");
            rounds.add(report);
        }

        @Override
        public void showReturn(Player player, ReturnAttempt attempt) {
            calls.add("return:" + player.name());
            returns.add(attempt);
        }

        @Override
        public void showGameOver(GameResult result) {
            calls.add("over");
            ends.add(result);
        }
    }

    @Test
    void aPressMakesOneCallAndOneRedraw() {
        Game game = TestGames.script().rolls(3).start();
        FakeView view = new FakeView();
        new GameController(game, view).perform(new Command.Roll());
        assertEquals(List.of("refresh"), view.calls);
        assertEquals(Game.Phase.MOVE_DONE, game.phase());
        assertEquals(3, game.currentPlayer().position());
    }

    @Test
    void anIllegalPressShowsWhyAndChangesNothing() {
        Game game = TestGames.script().start();
        FakeView view = new FakeView();
        GameController controller = new GameController(game, view);
        int money = game.currentPlayer().money();
        // End turn before rolling, and an action that isn't on offer.
        controller.perform(new Command.EndTurn());
        controller.perform(new Command.Take(new Action(Action.Kind.BUY, 1, 6, Action.NONE)));
        assertEquals(List.of("error", "refresh", "error", "refresh"), view.calls);
        assertEquals(Game.Phase.AWAITING_ROLL, game.phase());
        assertEquals(money, game.currentPlayer().money());
    }

    @Test
    void takingAnActionGoesThroughTheModel() {
        Game game = TestGames.script().start();
        TestGames.arrive(game, 6);
        FakeView view = new FakeView();
        Button buy = ActionOptions.forGame(game).get(0);
        new GameController(game, view).perform(buy.command());
        assertEquals(List.of("refresh"), view.calls);
        assertSame(game.currentPlayer(), ((netzerobelfast.model.OwnableSquare) game.board().square(6)).owner());
    }

    @Test
    void aQuizCardIsAskedThenItsResultShown() {
        Game game = TestGames.script().rolls(2).start();
        TestGames.nextCard(game, "Q01");
        FakeView view = new FakeView();
        view.answers.add(2); // 2050: the right answer
        new GameController(game, view).perform(new Command.Roll());
        assertEquals(List.of("refresh", "quiz", "refresh", "card"), view.calls);
        assertTrue(view.cards.get(0).correct());
        assertEquals(Game.Phase.MOVE_DONE, game.phase());
    }

    @Test
    void aChoiceCardOffersOnlyAffordableOptions() {
        Game game = TestGames.script().rolls(2).start();
        TestGames.nextCard(game, "C01");
        FakeView view = new FakeView();
        new GameController(game, view).perform(new Command.Roll());
        assertEquals(List.of("refresh", "choice", "refresh", "card"), view.calls);
        assertEquals(List.of(0, 1), view.affordableSeen);
    }

    @Test
    void anEventCardIsShownWithoutAQuestion() {
        Game game = TestGames.script().rolls(2).start();
        TestGames.nextCard(game, "E05");
        FakeView view = new FakeView();
        new GameController(game, view).perform(new Command.Roll());
        assertEquals(List.of("refresh", "card"), view.calls);
    }

    @Test
    void aWrongAnswerIndexIsRefusedAndTheCardCanBeReopened() {
        Game game = TestGames.script().rolls(2).start();
        TestGames.nextCard(game, "Q01");
        FakeView view = new FakeView();
        view.answers.add(9);
        GameController controller = new GameController(game, view);
        controller.perform(new Command.Roll());
        assertEquals(List.of("refresh", "quiz", "error", "refresh"), view.calls);
        assertEquals(Game.Phase.AWAITING_QUIZ_ANSWER, game.phase());

        view.calls.clear();
        controller.perform(new Command.OpenCard());
        assertEquals(List.of("quiz", "refresh", "card"), view.calls);
        assertEquals(Game.Phase.MOVE_DONE, game.phase());
    }

    @Test
    void theRoundReportIsShownWhenTheRoundEnds() {
        Game game = TestGames.script().rolls(3, 3, 3, 3).noDisaster().start();
        FakeView view = new FakeView();
        GameController controller = new GameController(game, view);
        for (int i = 0; i < 4; i++) {
            controller.perform(new Command.Roll());
            controller.perform(new Command.EndTurn());
        }
        assertEquals(1, view.count("round"));
        assertEquals(1, view.rounds.get(0).round());
        assertEquals("round", view.calls.get(view.calls.size() - 1));
        assertEquals(8, view.count("refresh"));
    }

    @Test
    void aBankruptPlayersReturnRollIsShown() {
        Game game = TestGames.script().die(6).start();
        TestGames.bankrupt(TestGames.player(game, "Ben"));
        TestGames.arrive(game, 3);
        FakeView view = new FakeView();
        GameController controller = new GameController(game, view);
        controller.perform(new Command.EndTurn());
        assertEquals(Game.Phase.AWAITING_RETURN_ROLL, game.phase());
        controller.perform(new Command.RollToReturn());
        assertEquals(List.of("refresh", "refresh", "return:Ben"), view.calls);
        assertTrue(view.returns.get(0).returned());
    }

    @Test
    void aWholeGameEndsOnTheFinalScores() {
        Game game = UiGames.seeded(7);
        FakeView view = new FakeView();
        GameController controller = new GameController(game, view);
        int guard = 0;
        while (game.phase() != Game.Phase.GAME_OVER && guard++ < 5_000) {
            // Press the first enabled button, as a player who never buys would: roll, choose, end turn.
            List<Button> buttons = ActionOptions.forGame(game);
            Button press = buttons.stream()
                    .filter(b -> b.enabled() && !(b.command() instanceof Command.Take))
                    .findFirst().orElseThrow();
            controller.perform(press.command());
        }
        assertEquals(Game.Phase.GAME_OVER, game.phase());
        assertEquals(1, view.count("over"));
        assertEquals(UiGames.CONFIG.rules().game().rounds(), view.count("round"));
        assertEquals("over", view.calls.get(view.calls.size() - 1));
        assertSame(game.result().orElseThrow(), view.ends.get(0));
    }
}
