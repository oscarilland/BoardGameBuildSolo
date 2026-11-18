package netzerobelfast.sim;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.Action;
import netzerobelfast.model.Dice;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.PlayerSetup;
import netzerobelfast.model.GameResult;
import netzerobelfast.model.Player;
import netzerobelfast.model.QuizCard;
import netzerobelfast.model.RoundReport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Plays one complete game with computer players and no person involved (NFR-06 AC-2). Every
 * random choice comes from the seed: the game's dice from it, and the players' choices from a
 * second {@link Random} seeded from it, so the same seed replays the same game.
 */
public final class Simulator {

    /** One seat: the player's name, character and strategy. */
    public record Seat(String name, String characterId, Strategy strategy) {}

    /**
     * A finished simulated game.
     *
     * @param strategies   which strategy each player used
     * @param bankruptcies how many times a player went bankrupt
     * @param disasters    how many disasters happened (including skipped Floods and Storms)
     * @param turns        how many turns were played
     */
    public record Outcome(GameResult result, Map<Player, Strategy> strategies, int bankruptcies, int disasters, int turns) {}

    /** The most actions a computer player takes in one turn: a landing action and a conversion. */
    private static final int MAX_ACTIONS_PER_TURN = 2;

    private Simulator() {}

    public static Outcome play(GameConfig config, long seed, List<Seat> seats) {
        List<PlayerSetup> setups = new ArrayList<>();
        for (Seat seat : seats) {
            setups.add(new PlayerSetup(seat.name(), seat.characterId()));
        }
        Game game = Game.start(config, new Dice(new Random(seed)), setups);
        Random choices = new Random(seed * 31 + 7);

        Map<Player, Strategy> strategies = new LinkedHashMap<>();
        for (int i = 0; i < seats.size(); i++) {
            strategies.put(game.players().get(i), seats.get(i).strategy());
        }

        int bankruptcies = 0;
        int disasters = 0;
        int turns = 0;
        int lastRoundSeen = 0;
        while (game.phase() != Game.Phase.GAME_OVER) {
            Strategy strategy = strategies.get(game.currentPlayer());
            switch (game.phase()) {
                case AWAITING_RETURN_ROLL -> game.rollToReturn();
                case AWAITING_ROLL -> game.roll();
                case AWAITING_ROUTE -> game.chooseRoute(strategy.route(game, choices));
                case AWAITING_DIRECTION -> game.chooseDirection(strategy.direction(game, choices));
                case AWAITING_QUIZ_ANSWER -> game.answerQuiz(
                        strategy.quizAnswer((QuizCard) game.pendingCard().orElseThrow(), choices));
                case AWAITING_CHOICE -> game.chooseOption(strategy.option(game, game.affordableOptions(), choices));
                case MOVE_DONE -> {
                    if (game.lastCard().map(c -> c.bankrupt()).orElse(false)) {
                        bankruptcies++;
                    }
                    for (int i = 0; i < MAX_ACTIONS_PER_TURN; i++) {
                        List<Action> available = game.availableActions();
                        if (available.isEmpty()) {
                            break;
                        }
                        Optional<Action> chosen = strategy.action(game, available, choices);
                        if (chosen.isEmpty()) {
                            break;
                        }
                        game.take(chosen.get());
                    }
                    turns++;
                    game.endTurn();
                    Optional<RoundReport> report = game.lastRound();
                    if (report.isPresent() && report.get().round() != lastRoundSeen) {
                        lastRoundSeen = report.get().round();
                        if (report.get().disaster() != null) {
                            disasters++;
                            bankruptcies += report.get().disaster().bankrupted().size();
                        }
                    }
                }
                case GAME_OVER -> throw new IllegalStateException("unreachable");
            }
        }
        return new Outcome(game.result().orElseThrow(), strategies, bankruptcies, disasters, turns);
    }
}
