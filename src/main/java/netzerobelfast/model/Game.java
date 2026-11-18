package netzerobelfast.model;

import netzerobelfast.config.CardDef;
import netzerobelfast.config.CardDef.Effect;
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
import java.util.Random;
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
 * <p>Built so far: starting a game (FR-01, FR-02), moving (FR-03, plan 002), the actions after
 * moving (FR-04 to FR-09, plan 003): {@link #availableActions()} lists what the player can do and
 * {@link #take(Action)} does it; Climate Cards (FR-10, plan 004), drawn on arrival; and the end
 * of each round, disasters and bankruptcy (FR-11 to FR-13, FR-17, plan 005). After the last
 * round the game is in {@code GAME_OVER}; scoring is plan 006.
 */
public final class Game {

    /** What the game is waiting for. */
    public enum Phase {
        /** A bankrupt player rolls one die to see if they're back in the game (FR-13 AC-4). */
        AWAITING_RETURN_ROLL,
        /** The current player rolls the dice. */
        AWAITING_ROLL,
        /** The move has reached a junction with steps left: stay on the loop or take the City Centre? */
        AWAITING_ROUTE,
        /** The player is on the City Centre route without a direction (after a Glider ride, Q-18): which way? */
        AWAITING_DIRECTION,
        /** A quiz card was drawn: which answer? See {@link #pendingCard()}. */
        AWAITING_QUIZ_ANSWER,
        /** A choice card was drawn: option A or B? See {@link #affordableOptions()}. */
        AWAITING_CHOICE,
        /** The move is finished: the player may take the actions in {@link #availableActions()}, then ends their turn. */
        MOVE_DONE,
        /** The last round has ended. Nothing more can happen; scoring is plan 006. */
        GAME_OVER
    }

    /**
     * A bankrupt player's turn: the die rolled to return (0 if they returned automatically), and
     * whether they're back.
     */
    public record ReturnAttempt(int roll, boolean returned) {}

    /** One player as entered on the setup screen. */
    public record PlayerSetup(String name, String characterId) {}

    /** The last finished move: the dice total and every square passed through, ending where the counter stopped. */
    public record Move(int diceTotal, List<Integer> path) {}

    private final Rules rules;
    private final Board board;
    private final Dice dice;
    private final CardDeck deck;
    private final ClimateMeter meter;
    private final DisasterManager disasters;
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

    // What the current player has done since moving (the rules allow one landing action and one conversion per turn).
    private boolean landingActionUsed;
    private boolean convertedThisTurn;

    // Climate Cards (plan 004).
    private ClimateCard pendingCard;
    private CardResult lastCard;

    // End of round and bankruptcy (plan 005).
    private RoundReport lastRound;
    private ReturnAttempt lastReturn;

    private Game(Rules rules, Board board, Dice dice, List<ClimateCard> cards, List<Player> players, int firstPlayer) {
        this.rules = rules;
        this.board = board;
        this.dice = dice;
        // The deck gets its own Random, seeded from the game's, so one seed still replays the whole game.
        this.deck = new CardDeck(cards, new Random(dice.random().nextLong()));
        this.meter = new ClimateMeter(rules);
        this.disasters = new DisasterManager(rules, dice);
        this.players = List.copyOf(players);
        this.firstPlayer = firstPlayer;
        this.current = firstPlayer;
        this.year = rules.game().startYear();
        this.yearsPerRound = rules.game().yearsPerRound();
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
        Map<String, Character> characters = Character.fromAll(config.rules()).stream()
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
        List<ClimateCard> cards = new ArrayList<>();
        for (CardDef card : config.cards()) {
            cards.add(ClimateCard.from(card, config));
        }
        return new Game(config.rules(), new Board(config.board()), dice, cards, players, first);
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

    /** The balance values from {@code config.json}. */
    public Rules rules() {
        return rules;
    }

    /** The shared Climate Meter. */
    public ClimateMeter meter() {
        return meter;
    }

    /** The card waiting for a quiz answer or a choice, if any. */
    public Optional<ClimateCard> pendingCard() {
        return Optional.ofNullable(pendingCard);
    }

    /** The last card resolved this turn, with what happened and its fact and sources, to show the player. */
    public Optional<CardResult> lastCard() {
        return Optional.ofNullable(lastCard);
    }

    CardDeck deck() {
        return deck;
    }

    /** The lasting disaster effects (flooded districts, closed squares, Drought) in force. */
    public DisasterManager disasters() {
        return disasters;
    }

    /** What happened at the end of the last round, if a round has ended. */
    public Optional<RoundReport> lastRound() {
        return Optional.ofNullable(lastRound);
    }

    /** The current player's attempt to return from bankruptcy this turn, if they were bankrupt at its start. */
    public Optional<ReturnAttempt> lastReturn() {
        return Optional.ofNullable(lastReturn);
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
        landingActionUsed = false;
        convertedThisTurn = false;
        lastCard = null;
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

    /**
     * Ends the current player's turn. After the last player in the round, the end of the round
     * runs (FR-11 AC-1); after the last round, the game is over.
     */
    public void endTurn() {
        require(Phase.MOVE_DONE, "end the turn");
        current = (current + 1) % players.size();
        if (current == firstPlayer) {
            endRound();
        }
        if (phase != Phase.GAME_OVER) {
            startTurn();
        }
    }

    // ---- Bankruptcy (FR-13, plan 005) -------------------------------------------

    /**
     * Sets up the new current player's turn. A bankrupt player first rolls to return, unless
     * they've already sat out long enough to return automatically (Q-14).
     */
    private void startTurn() {
        Player player = currentPlayer();
        lastReturn = null;
        if (!player.bankrupt()) {
            phase = Phase.AWAITING_ROLL;
        } else if (player.turnsBankrupt() >= rules.bankruptcy().maxRounds()) {
            returnFromBankruptcy(player);
            lastReturn = new ReturnAttempt(0, true);
            phase = Phase.AWAITING_ROLL;
        } else {
            phase = Phase.AWAITING_RETURN_ROLL;
        }
    }

    /**
     * Answers {@code AWAITING_RETURN_ROLL} (FR-13 AC-4): roll one die. On the escape roll (6) the
     * player is back with a loan and takes a normal roll this turn; otherwise they sit this turn out,
     * with no move and no actions.
     */
    public void rollToReturn() {
        require(Phase.AWAITING_RETURN_ROLL, "roll to return");
        Player player = currentPlayer();
        int roll = dice.rollOne();
        if (roll == rules.bankruptcy().escapeRoll()) {
            returnFromBankruptcy(player);
            lastReturn = new ReturnAttempt(roll, true);
            phase = Phase.AWAITING_ROLL;
        } else {
            player.missTurn();
            lastReturn = new ReturnAttempt(roll, false);
            lastMove = null;
            lastCard = null;
            phase = Phase.MOVE_DONE;
        }
    }

    private void returnFromBankruptcy(Player player) {
        player.returnFromBankruptcy(rules.bankruptcy().loanOnReturn(), rules.bankruptcy().repayAtEnd());
    }

    // ---- End of round (FR-11, plan 005) ----------------------------------------

    /**
     * The end of the round: the six steps in the order FR-11 requires (End Round sequence diagram).
     * Package-private so tests can run it directly.
     */
    void endRound() {
        List<RoundReport.Step> steps = new ArrayList<>();

        // Step 1: income and GP from squares and co-funding deals.
        payIncome();
        disasters.endOfIncome();
        steps.add(RoundReport.Step.INCOME);

        // Step 2: carbon tax, on every owned Polluting square, whatever its state (Q-15).
        int tax = rules.polluting().carbonTaxGP();
        for (Square square : board.squares()) {
            if (square instanceof PollutingSquare polluting && polluting.owner() != null) {
                polluting.owner().addGP(-tax);
            }
        }
        steps.add(RoundReport.Step.CARBON_TAX);

        // Step 3: the Climate Meter. Closed and flooded squares still count (Q-15);
        // a square the Activist lobbied this round doesn't (FR-06 AC-5).
        int meterBefore = meter.value();
        int polluting = 0;
        int green = 0;
        for (Square square : board.squares()) {
            if (square instanceof PollutingSquare p && p.owner() != null && !p.meterRiseCancelled()) {
                polluting++;
            } else if (square instanceof GreenProjectSquare g && g.owner() != null) {
                green++;
            }
        }
        meter.update(polluting, green);
        steps.add(RoundReport.Step.METER);

        // Step 4: the interim target, only in its round (2030 and 2040).
        Rules.InterimTarget target = meter.targetFor(round).orElse(null);
        boolean targetMet = false;
        if (target != null) {
            targetMet = meter.meetsTarget(target);
            if (!targetMet) {
                int penalty = rules.meter().interimMissPenaltyGP();
                for (Square square : board.squares()) {
                    if (square instanceof PollutingSquare p && p.owner() != null) {
                        p.owner().addGP(-penalty);
                    }
                }
            }
            steps.add(RoundReport.Step.INTERIM_TARGET);
        }

        // Step 5: the disaster check, using the band of the updated meter.
        int band = meter.dangerBand();
        DisasterManager.Check check = disasters.check(band);
        DisasterResult disaster = check.disaster() == null ? null
                : disasters.apply(check.disaster(), band, board, players);
        steps.add(RoundReport.Step.DISASTER_CHECK);

        // Step 6: lobbying ends, and the year and round advance.
        board.clearLobbying();
        steps.add(RoundReport.Step.ADVANCE);
        lastRound = new RoundReport(round, year, meterBefore, meter.value(), band, target, targetMet,
                check.roll(), disaster, List.copyOf(steps));
        round++;
        year += yearsPerRound;
        if (round > rules.game().rounds()) {
            phase = Phase.GAME_OVER;
        }
    }

    /**
     * Step 1 (FR-11 AC-3). Squares that are storm-closed or in a flooded district earn nothing,
     * and nor do co-funding deals on them (Q-15). Lobbied squares earn no money. Bankrupt owners
     * get the GP but no money (FR-13 AC-7). Nature projects get no GP in a Drought.
     */
    private void payIncome() {
        for (Square square : board.squares()) {
            if (!(square instanceof OwnableSquare ownable) || ownable.owner() == null || disasters.shut(square)) {
                continue;
            }
            Player owner = ownable.owner();
            if (square instanceof PollutingSquare polluting) {
                if (!polluting.lobbiedThisRound() && !owner.bankrupt()) {
                    owner.receive(rules.polluting().incomePerRound());
                }
            } else if (square instanceof GreenProjectSquare greenProject) {
                if (!owner.bankrupt()) {
                    owner.receive(rules.green().incomePerRound() + owner.character().incomeBonus(greenProject, owner));
                }
                if (!(greenProject.nature() && disasters.drought())) {
                    int gp = rules.green().gpPerRound() + (greenProject.nature() ? owner.character().natureBonusGP() : 0);
                    owner.addGP(gp);
                }
            }
        }

        int dealGP = rules.actions().coFundGPEach();
        for (Player funder : players) {
            CoFundingDeal deal = funder.coFundingDeal();
            if (deal == null) {
                continue;
            }
            Square square = board.square(deal.square());
            if (square instanceof GreenProjectSquare funded && funded.owner() != null && !disasters.shut(square)) {
                funder.addGP(dealGP);
                funded.owner().addGP(dealGP);
            }
        }
    }

    // ---- Actions after moving (plan 003) ------------------------------------

    /**
     * What the current player can do now (FR-04 to FR-09): the landing actions their square
     * offers, if they haven't taken one this turn, and converting any Polluting square they own,
     * if they haven't converted this turn. Only actions they can afford are listed. Empty unless
     * the move is done.
     */
    public List<Action> availableActions() {
        Player player = currentPlayer();
        // A bankrupt player takes no actions, including converting (FR-07 AC-6, FR-13 AC-3).
        if (phase != Phase.MOVE_DONE || player.bankrupt()) {
            return List.of();
        }
        List<Action> actions = new ArrayList<>();
        if (!landingActionUsed) {
            actions.addAll(board.square(player.position()).landOn(player, this));
        }
        if (!convertedThisTurn) {
            int cost = rules.actions().convertCost();
            for (PollutingSquare square : board.pollutingOwnedBy(player)) {
                actions.add(Action.on(Action.Kind.CONVERT, cost, square.id()));
            }
        }
        actions.removeIf(action -> action.cost() > player.money());
        return List.copyOf(actions);
    }

    /**
     * Does one of the actions from {@link #availableActions()}.
     *
     * @throws IllegalActionException if the action isn't in that list right now; nothing changes
     */
    public void take(Action action) {
        if (!availableActions().contains(action)) {
            throw new IllegalActionException("That action isn't available now.");
        }
        Player player = currentPlayer();
        player.pay(action.cost());
        switch (action.kind()) {
            case BUY -> ((OwnableSquare) board.square(action.square())).setOwner(player);
            case CO_FUND -> player.startDeal(new CoFundingDeal(player, action.square()));
            case LOBBY -> ((PollutingSquare) board.square(action.square())).lobby(player);
            case BUY_RESILIENCE -> player.raiseResilience();
            case RIDE_GLIDER -> {
                player.addGP(player.character().gliderRideGP());
                // No heading: arriving at City Hall by Glider, the next move asks which way (Q-18).
                player.moveTo(action.destination(), null);
            }
            case CONVERT -> {
                PollutingSquare polluting = (PollutingSquare) board.square(action.square());
                board.replace(polluting.convert(player));
                convertedThisTurn = true;
            }
        }
        if (action.isLandingAction()) {
            landingActionUsed = true;
        }
    }

    /**
     * Test set-up only: puts the current player on {@code square} as if they had just moved
     * there, ready to act. Saves tests from scripting dice to reach a square.
     */
    void arriveForTest(int square) {
        currentPlayer().moveTo(square, null);
        landingActionUsed = false;
        convertedThisTurn = false;
        lastCard = null;
        phase = Phase.MOVE_DONE;
        landed();
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
        landed();
    }

    /** What happens on arrival, before the player chooses actions: on a Climate Card square, a card is drawn. */
    private void landed() {
        if (board.square(currentPlayer().position()) instanceof ClimateCardSquare) {
            drawCard();
        }
    }

    // ---- Climate Cards (plan 004) ---------------------------------------------

    /**
     * Draws a card (FR-10 AC-1). The draw is compulsory, so it uses up the landing action.
     * Quiz and choice cards wait for the player; an event card applies at once.
     */
    private void drawCard() {
        ClimateCard card = deck.draw();
        landingActionUsed = true;
        switch (card) {
            case QuizCard quiz -> {
                pendingCard = quiz;
                phase = Phase.AWAITING_QUIZ_ANSWER;
            }
            case ChoiceCard choice -> {
                pendingCard = choice;
                phase = Phase.AWAITING_CHOICE;
            }
            case EventCard event -> {
                Effect effect = event.effect();
                Player player = currentPlayer();
                boolean wasBankrupt = player.bankrupt();
                int moneyChange;
                if (effect.money() >= 0) {
                    player.receive(effect.money());
                    moneyChange = effect.money();
                } else {
                    // Compulsory, and not reduced by Resilience (FR-08 AC-6).
                    moneyChange = -player.payCompulsory(-effect.money());
                }
                player.addGP(effect.gp());
                meter.adjust(effect.meter());
                boolean madeBankrupt = !wasBankrupt && player.bankrupt();
                lastCard = new CardResult(event, -1, false, -1, moneyChange, effect.gp(), effect.meter(), madeBankrupt);
            }
        }
    }

    /** Answers {@code AWAITING_QUIZ_ANSWER}: a correct answer earns the card's reward in GP (FR-10 AC-3). */
    public void answerQuiz(int answer) {
        require(Phase.AWAITING_QUIZ_ANSWER, "answer a quiz");
        QuizCard quiz = (QuizCard) pendingCard;
        if (answer < 0 || answer >= quiz.answers().size()) {
            throw new IllegalActionException("Choose one of the " + quiz.answers().size() + " answers.");
        }
        boolean correct = quiz.checkAnswer(answer);
        int gp = correct ? quiz.reward() : 0;
        currentPlayer().addGP(gp);
        lastCard = new CardResult(quiz, answer, correct, -1, 0, gp, 0, false);
        pendingCard = null;
        phase = Phase.MOVE_DONE;
    }

    /**
     * The choice card's options the player can take: those that don't cost more than they have
     * (Q-12). Empty unless a choice card is waiting. The data guarantees at least one.
     */
    public List<Integer> affordableOptions() {
        if (phase != Phase.AWAITING_CHOICE) {
            return List.of();
        }
        ChoiceCard choice = (ChoiceCard) pendingCard;
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i < choice.options().size(); i++) {
            if (-choice.option(i).effect().money() <= currentPlayer().money()) {
                options.add(i);
            }
        }
        return options;
    }

    /**
     * Answers {@code AWAITING_CHOICE} with option 0 (A) or 1 (B). Its money, GP and meter changes
     * apply at once (FR-10 AC-4).
     *
     * @throws IllegalActionException if the option isn't in {@link #affordableOptions()}; nothing changes
     */
    public void chooseOption(int option) {
        require(Phase.AWAITING_CHOICE, "choose a card option");
        if (!affordableOptions().contains(option)) {
            throw new IllegalActionException("That option isn't available.");
        }
        ChoiceCard choice = (ChoiceCard) pendingCard;
        Effect effect = choice.option(option).effect();
        Player player = currentPlayer();
        if (effect.money() >= 0) {
            player.receive(effect.money());
        } else {
            player.pay(-effect.money());
        }
        player.addGP(effect.gp());
        meter.adjust(effect.meter());
        lastCard = new CardResult(choice, -1, false, option, effect.money(), effect.gp(), effect.meter(), false);
        pendingCard = null;
        phase = Phase.MOVE_DONE;
    }

    private void require(Phase expected, String action) {
        if (phase != expected) {
            throw new IllegalActionException("Can't " + action + " now: the game is waiting for " + describe(phase) + ".");
        }
    }

    private static String describe(Phase phase) {
        return switch (phase) {
            case AWAITING_RETURN_ROLL -> "a roll to return from bankruptcy";
            case GAME_OVER -> "nothing: the game is over";
            case AWAITING_ROLL -> "a dice roll";
            case AWAITING_ROUTE -> "a route choice";
            case AWAITING_DIRECTION -> "a direction choice";
            case AWAITING_QUIZ_ANSWER -> "a quiz answer";
            case AWAITING_CHOICE -> "a card option";
            case MOVE_DONE -> "the turn to end";
        };
    }
}
