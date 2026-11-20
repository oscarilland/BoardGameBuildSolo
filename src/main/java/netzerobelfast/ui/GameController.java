package netzerobelfast.ui;

import netzerobelfast.model.CardResult;
import netzerobelfast.model.ChoiceCard;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.ReturnAttempt;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.IllegalActionException;
import netzerobelfast.model.Player;
import netzerobelfast.model.QuizCard;
import netzerobelfast.model.RoundReport;
import netzerobelfast.ui.ActionOptions.Command;

import java.util.List;

/**
 * The only way the screens change the game. Every button, key and pop-up answer comes here;
 * the controller makes the one matching {@link Game} call, then tells the view to redraw once
 * and shows whatever the call started (a card, a round report, a return roll, the end).
 *
 * <p>No Swing here: the view is an interface, so tests drive the controller with a fake view.
 * Nothing waits in a loop: Swing calls {@link #perform} when a player presses something (NFR-11).
 */
public final class GameController {

    /** What the controller needs from the screen. The Swing game screen implements it. */
    public interface View {
        /** Redraw everything from the model, starting any animations of what changed. */
        void refresh();

        /**
         * Wait until any animation has finished, without blocking the window, so a pop-up opens
         * after the counter has arrived (plan 008). Returns at once if nothing is moving.
         */
        void settle();

        /** The model refused a call; nothing changed. */
        void showError(String message);

        /** Ask the quiz question and return the answer chosen (0-based). The pop-up can't be closed without one. */
        int askQuiz(QuizCard card);

        /** Show both options and their effects, and return the one chosen from {@code affordable}. */
        int askChoice(ChoiceCard card, List<Integer> affordable);

        void showCardResult(CardResult result);

        void showRoundReport(RoundReport report);

        void showReturn(Player player, ReturnAttempt attempt);

        void showGameOver(GameResult result);
    }

    private final Game game;
    private final View view;

    public GameController(Game game, View view) {
        this.game = game;
        this.view = view;
    }

    public Game game() {
        return game;
    }

    /** Does what a button asks. */
    public void perform(Command command) {
        if (command instanceof Command.OpenCard) {
            askPendingCard(game.currentPlayer());
            return;
        }
        apply(() -> {
            switch (command) {
                case Command.Roll r -> game.roll();
                case Command.RollToReturn r -> game.rollToReturn();
                case Command.ChooseRoute c -> game.chooseRoute(c.route());
                case Command.ChooseDirection c -> game.chooseDirection(c.heading());
                case Command.Take t -> game.take(t.action());
                case Command.EndTurn e -> game.endTurn();
                case Command.OpenCard o -> throw new IllegalStateException("handled above");
            }
        });
    }

    /**
     * Makes one call, then redraws once and shows what it started. If the model refuses, the
     * player sees why, and nothing else happens.
     */
    private void apply(Runnable call) {
        // Compare by identity: each card, round and return makes a new result object.
        CardResult cardBefore = game.lastCard().orElse(null);
        RoundReport roundBefore = game.lastRound().orElse(null);
        ReturnAttempt returnBefore = game.lastReturn().orElse(null);
        Player mover = game.currentPlayer();
        try {
            call.run();
        } catch (IllegalActionException e) {
            view.showError(e.getMessage());
            view.refresh();
            return;
        }
        view.refresh();

        CardResult card = game.lastCard().orElse(null);
        RoundReport round = game.lastRound().orElse(null);
        ReturnAttempt attempt = game.lastReturn().orElse(null);
        boolean newCard = card != null && card != cardBefore;
        boolean newRound = round != null && round != roundBefore;
        boolean newReturn = attempt != null && attempt != returnBefore;
        boolean over = game.phase() == Game.Phase.GAME_OVER;
        boolean asking = !over && game.currentPlayer() == mover
                && (game.phase() == Game.Phase.AWAITING_QUIZ_ANSWER || game.phase() == Game.Phase.AWAITING_CHOICE);
        if (newCard || newRound || newReturn || over || asking) {
            view.settle();
        }

        if (newCard) {
            view.showCardResult(card);
        }
        if (newRound) {
            view.showRoundReport(round);
        }
        if (newReturn) {
            // After End turn the new current player's return is shown; after Roll to return, the same player's.
            view.showReturn(game.currentPlayer(), attempt);
        }
        if (over) {
            game.result().ifPresent(view::showGameOver);
            return;
        }
        askPendingCard(mover);
    }

    /** A quiz or choice card waits for an answer: ask, then apply it like any other call. */
    private void askPendingCard(Player mover) {
        if (game.currentPlayer() != mover) {
            return;
        }
        switch (game.phase()) {
            case AWAITING_QUIZ_ANSWER -> {
                QuizCard quiz = (QuizCard) game.pendingCard().orElseThrow();
                int answer = view.askQuiz(quiz);
                apply(() -> game.answerQuiz(answer));
            }
            case AWAITING_CHOICE -> {
                ChoiceCard choice = (ChoiceCard) game.pendingCard().orElseThrow();
                int option = view.askChoice(choice, game.affordableOptions());
                apply(() -> game.chooseOption(option));
            }
            default -> {
                // Nothing waiting.
            }
        }
    }
}
