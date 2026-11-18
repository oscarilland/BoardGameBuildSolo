package netzerobelfast.sim;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.Action;
import netzerobelfast.model.Action.Kind;
import netzerobelfast.model.Dice;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.Phase;
import netzerobelfast.model.Game.PlayerSetup;
import netzerobelfast.model.GreenProjectSquare;
import netzerobelfast.model.PollutingSquare;
import netzerobelfast.model.QuizCard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The computer players choose only from what the game offers, and each plays to its strategy.
 * Games here are moved on by rolling until the current player lands somewhere useful.
 */
class StrategyTest {

    private static final GameConfig CONFIG = GameConfig.load();

    private static Game newGame(long seed) {
        return Game.start(CONFIG, new Dice(new Random(seed)), List.of(
                new PlayerSetup("Ann", "INDUSTRIALIST"), new PlayerSetup("Bo", "TECH_ENTREPRENEUR"),
                new PlayerSetup("Cal", "COUNCIL_PLANNER"), new PlayerSetup("Di", "FARMER")));
    }

    /** Plays one game with every seat on {@code strategy}, checking each choice is one the game offered. */
    private static Game playChecked(Strategy strategy, long seed) {
        Game game = newGame(seed);
        Random random = new Random(seed);
        while (game.phase() != Phase.GAME_OVER) {
            switch (game.phase()) {
                case AWAITING_RETURN_ROLL -> game.rollToReturn();
                case AWAITING_ROLL -> game.roll();
                case AWAITING_ROUTE -> game.chooseRoute(strategy.route(game, random));
                case AWAITING_DIRECTION -> game.chooseDirection(strategy.direction(game, random));
                case AWAITING_QUIZ_ANSWER -> {
                    int answer = strategy.quizAnswer((QuizCard) game.pendingCard().orElseThrow(), random);
                    assertTrue(answer >= 0 && answer < 4);
                    game.answerQuiz(answer);
                }
                case AWAITING_CHOICE -> {
                    List<Integer> affordable = game.affordableOptions();
                    int option = strategy.option(game, affordable, random);
                    assertTrue(affordable.contains(option), strategy.name() + " chose an unaffordable option");
                    game.chooseOption(option);
                }
                case MOVE_DONE -> {
                    for (int i = 0; i < 2; i++) {
                        List<Action> available = game.availableActions();
                        Optional<Action> chosen = strategy.action(game, available, random);
                        if (chosen.isEmpty()) {
                            break;
                        }
                        assertTrue(available.contains(chosen.get()), strategy.name() + " chose an action not on offer");
                        game.take(chosen.get());
                    }
                    if (strategy.name().equals("green")) {
                        assertTrue(game.board().pollutingOwnedBy(game.currentPlayer()).isEmpty(),
                                "green never keeps a Polluting square past its turn");
                    }
                    game.endTurn();
                }
                case GAME_OVER -> { }
            }
        }
        return game;
    }

    @Test
    void everyStrategyOnlyChoosesWhatIsOffered() {
        for (long seed = 0; seed < 5; seed++) {
            playChecked(Strategies.random(0.6), seed);
            playChecked(Strategies.green(), seed);
            playChecked(Strategies.polluter(), seed);
        }
    }

    @Test
    void thePolluterBuysPollutionAndNeverConverts() {
        Game game = playChecked(Strategies.polluter(), 3);
        long converted = game.board().squares().stream()
                .filter(s -> s instanceof GreenProjectSquare g && g.convertedBy() != null).count();
        assertEquals(0, converted);
        assertTrue(game.board().squares().stream().anyMatch(s -> s instanceof PollutingSquare p && p.owner() != null),
                "a polluter game ends with Polluting squares owned");
        assertTrue(game.board().squares().stream().noneMatch(s -> s instanceof GreenProjectSquare g && g.owner() != null),
                "and no Green Projects");
    }

    @Test
    void greenBuysAPollutingSquareOnlyToConvertIt() {
        Game game = playChecked(Strategies.green(), 4);
        assertTrue(game.board().squares().stream().noneMatch(s -> s instanceof PollutingSquare p && p.owner() != null));
    }

    @Test
    void namesForTheReport() {
        assertEquals("random", Strategies.random(0.5).name());
        assertEquals("green", Strategies.green().name());
        assertEquals("polluter", Strategies.polluter().name());
    }

    @Test
    void theRandomStrategyMayChooseNothing() {
        // With no lean towards anything, an empty list of actions gives no choice.
        Game game = newGame(1);
        assertTrue(Strategies.random(0.5).action(game, List.of(), new Random(1)).isEmpty());
        assertInstanceOf(Optional.class, Strategies.green().action(game, List.of(), new Random(1)));
        assertTrue(Strategies.polluter().action(game, List.of(new Action(Kind.BUY_RESILIENCE, 3, 3, Action.NONE)), new Random(1)).isEmpty(),
                "the polluter ignores Resilience");
    }
}
