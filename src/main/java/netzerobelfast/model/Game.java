package netzerobelfast.model;

import netzerobelfast.config.GameConfig;
import netzerobelfast.config.Rules;
import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Board.Step;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The game controller (analysis model: {@code Game}).
 *
 * <p>A game is always in one {@link Phase}, which says what it's waiting for. Each public
 * action checks the phase first and throws {@link IllegalActionException} if it doesn't fit,
 * leaving the state unchanged. The game never asks a person anything itself: when a player has
 * a choice, it stops in a phase such as {@code AWAITING_ROUTE}, and the UI (or a bot, or a
 * test) answers by calling the matching method. See the game flow page.
 *
 * <p>Plan 002 covers starting a game (FR-01, FR-02) and moving (FR-03). Landing actions and the
 * end-of-round steps come in later plans; for now a round simply advances the year.
 */
public final class Game {

    /** What the game is waiting for. */
    public enum Phase {
        /** The current player rolls the dice. */
        AWAITING_ROLL,
        /** The move has reached a junction with steps left: stay on the loop or take the City Centre? */
        AWAITING_ROUTE,
        /** The player is on the City Centre route without a direction (after a Glider ride, Q-18): which way? */
        AWAITING_DIRECTION,
        /** The move is finished; the player ends their turn. */
        MOVE_DONE
    }

    /** One player as entered on the setup screen. */
    public record PlayerSetup(String name, String characterId) {}

    /** The last finished move: the dice total and every square passed through, ending where the counter stopped. */
    public record Move(int diceTotal, List<Integer> path) {}

    private final Board board;
    private final Dice dice;
    private final List<Player> players;
    private final int firstPlayer;
    private final int yearsPerRound;

    private int current;
    private int round = 1;
    private int year;
    private Phase phase = Phase.AWAITING_ROLL;

    // The move in progress.
    private int diceTotal;
    private int stepsLeft;
    private final List<Integer> path = new ArrayList<>();
    private Route chosenRoute;
    private boolean leftCityRouteThisStep;
    private Move lastMove;

    private Game(Board board, Dice dice, List<Player> players, int firstPlayer, Rules.Game rules) {
        this.board = board;
        this.dice = dice;
        this.players = List.copyOf(players);
        this.firstPlayer = firstPlayer;
        this.current = firstPlayer;
        this.year = rules.startYear();
        this.yearsPerRound = rules.yearsPerRound();
    }

    /**
     * Starts a game (use case Start Game, FR-01 and FR-02).
     *
     * <p>Players keep the order they were entered in; the first player is chosen at random
     * from them, and turns then go round in that order. Everyone starts on the first square
     * of the loop, with no landing action (Q-06).
     *
     * @throws IllegalArgumentException with a message for the setup screen if the players aren't valid
     */
    public static Game start(GameConfig config, Dice dice, List<PlayerSetup> setups) {
        Rules.Game rules = config.rules().game();
        Map<String, Character> characters = Character.fromAll(config.rules().characters()).stream()
                .collect(Collectors.toMap(Character::id, Function.identity()));

        if (setups.size() < rules.minPlayers() || setups.size() > rules.maxPlayers()) {
            throw new IllegalArgumentException("A game needs " + rules.minPlayers() + " to " + rules.maxPlayers()
                    + " players, not " + setups.size() + ".");
        }

        Set<String> namesUsed = new HashSet<>();
        Set<String> charactersUsed = new HashSet<>();
        int start = config.board().loop().get(0);
        List<Player> players = new ArrayList<>();
        for (PlayerSetup setup : setups) {
            String name = setup.name() == null ? "" : setup.name().strip();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Every player needs a name.");
            }
            if (name.length() > rules.maxNameLength()) {
                throw new IllegalArgumentException("\"" + name + "\" is too long: names can be up to "
                        + rules.maxNameLength() + " characters.");
            }
            if (!namesUsed.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Two players can't both be called \"" + name + "\".");
            }
            Character character = characters.get(setup.characterId());
            if (character == null) {
                throw new IllegalArgumentException("There's no character called \"" + setup.characterId() + "\".");
            }
            if (!charactersUsed.add(character.id())) {
                throw new IllegalArgumentException("Two players can't both be the " + character.name() + ".");
            }
            players.add(new Player(name, character, start));
        }

        int first = dice.random().nextInt(players.size());
        return new Game(new Board(config.board()), dice, players, first, rules);
    }

    // ---- State ---------------------------------------------------------------

    public Phase phase() {
        return phase;
    }

    /** Every player, in turn order starting from seat 1 (not from the first player). */
    public List<Player> players() {
        return players;
    }

    public Player currentPlayer() {
        return players.get(current);
    }

    /** The player who went first in round 1. */
    public Player firstPlayer() {
        return players.get(firstPlayer);
    }

    public int round() {
        return round;
    }

    public int year() {
        return year;
    }

    public Board board() {
        return board;
    }

    /** The last finished move, if any player has moved yet. */
    public Optional<Move> lastMove() {
        return Optional.ofNullable(lastMove);
    }

    /** Steps still to go in a move that's paused for a choice; 0 otherwise. */
    public int stepsLeft() {
        return stepsLeft;
    }

    // ---- Actions -------------------------------------------------------------

    /** Rolls two dice and moves the current player, pausing if they reach a choice (FR-03). */
    public void roll() {
        require(Phase.AWAITING_ROLL, "roll the dice");
        diceTotal = dice.rollTwo();
        stepsLeft = diceTotal;
        path.clear();
        chosenRoute = null;
        leftCityRouteThisStep = false;
        continueMove();
    }

    /** Answers {@code AWAITING_ROUTE}: stay on the loop or take the City Centre route. */
    public void chooseRoute(Route route) {
        require(Phase.AWAITING_ROUTE, "choose a route");
        if (route == null) {
            throw new IllegalActionException("Choose the loop or the City Centre.");
        }
        chosenRoute = route;
        continueMove();
    }

    /** Answers {@code AWAITING_DIRECTION}: which way to travel along the City Centre route (Q-18). */
    public void chooseDirection(Heading heading) {
        require(Phase.AWAITING_DIRECTION, "choose a direction");
        if (heading == null) {
            throw new IllegalActionException("Choose a direction along the City Centre route.");
        }
        currentPlayer().moveTo(currentPlayer().position(), heading);
        continueMove();
    }

    /** Ends the current player's turn. After the last player, the next round starts. */
    public void endTurn() {
        require(Phase.MOVE_DONE, "end the turn");
        current = (current + 1) % players.size();
        if (current == firstPlayer) {
            // FR-11 step 6. Steps 1-5 of the end of the round arrive in plan 005.
            round++;
            year += yearsPerRound;
        }
        phase = Phase.AWAITING_ROLL;
    }

    // ---- Movement ------------------------------------------------------------

    /**
     * Moves one square at a time until the steps run out, or until the player has a choice.
     *
     * <p>The rules, in the order they're checked before each step:
     * <ol>
     *   <li>On the City Centre route with no direction (only after a Glider ride): ask which way.</li>
     *   <li>On a junction, unless the player has just come out of the City Centre onto it in this
     *       move (FR-03 AC-7): ask for the route. A player who landed exactly on a junction last turn
     *       is asked at the start of this move (Q-05).</li>
     *   <li>Otherwise take one step, using the route just chosen, if any.</li>
     * </ol>
     */
    private void continueMove() {
        Player player = currentPlayer();
        while (stepsLeft > 0) {
            int position = player.position();
            if (board.onCityRoute(position) && player.heading() == null) {
                phase = Phase.AWAITING_DIRECTION;
                return;
            }
            if (board.isJunction(position) && !leftCityRouteThisStep && chosenRoute == null) {
                phase = Phase.AWAITING_ROUTE;
                return;
            }

            Route route = chosenRoute == null ? Route.LOOP : chosenRoute;
            chosenRoute = null;
            Step step = board.next(position, player.heading(), route);
            leftCityRouteThisStep = board.onCityRoute(position) && !board.onCityRoute(step.position());
            player.moveTo(step.position(), step.heading());
            path.add(step.position());
            stepsLeft--;
        }
        lastMove = new Move(diceTotal, List.copyOf(path));
        phase = Phase.MOVE_DONE;
    }

    private void require(Phase expected, String action) {
        if (phase != expected) {
            throw new IllegalActionException("Can't " + action + " now: the game is waiting for " + describe(phase) + ".");
        }
    }

    private static String describe(Phase phase) {
        return switch (phase) {
            case AWAITING_ROLL -> "a dice roll";
            case AWAITING_ROUTE -> "a route choice";
            case AWAITING_DIRECTION -> "a direction choice";
            case MOVE_DONE -> "the turn to end";
        };
    }
}
