package netzerobelfast.model;

import netzerobelfast.config.GameConfig;
import netzerobelfast.model.Game.PlayerSetup;

import java.util.List;
import java.util.Random;

/** Test helper: the real data, and quick ways to start a game. */
final class Games {

    static final GameConfig CONFIG = GameConfig.load();

    /** Four players, one per character in seat order: Industrialist, Tech Entrepreneur, Council Planner, Farmer. */
    static final List<PlayerSetup> FOUR = List.of(
            new PlayerSetup("Aoife", "INDUSTRIALIST"),
            new PlayerSetup("Ben", "TECH_ENTREPRENEUR"),
            new PlayerSetup("Chloe", "COUNCIL_PLANNER"),
            new PlayerSetup("Dev", "FARMER"));

    static final List<PlayerSetup> SIX = List.of(
            new PlayerSetup("Aoife", "INDUSTRIALIST"),
            new PlayerSetup("Ben", "TECH_ENTREPRENEUR"),
            new PlayerSetup("Chloe", "COUNCIL_PLANNER"),
            new PlayerSetup("Dev", "FARMER"),
            new PlayerSetup("Eve", "STUDENT_COMMUTER"),
            new PlayerSetup("Finn", "COMMUNITY_ACTIVIST"));

    private Games() {}

    static Game start(Random random, List<PlayerSetup> setups) {
        return Game.start(CONFIG, new Dice(random), setups);
    }

    /** A four-player game where seat 0 goes first and the rolls are scripted. */
    static Game scripted(int... rolls) {
        return start(new ScriptedRandom().firstPlayer(0).rolls(rolls), FOUR);
    }

    /** A six-player game where {@code seat} goes first and the rolls are scripted. Seats: Aoife, Ben, Chloe, Dev, Eve (Student), Finn (Activist). */
    static Game scriptedSix(int seat, int... rolls) {
        return start(new ScriptedRandom().firstPlayer(seat).rolls(rolls), SIX);
    }

    /** Puts the current player on a square (test set-up only). */
    static void place(Game game, int square, Board.Heading heading) {
        game.currentPlayer().moveTo(square, heading);
    }

    /** Puts the current player on a square as if they'd just moved there, ready to act. */
    static void arrive(Game game, int square) {
        game.arriveForTest(square);
    }

    /** A four-player game, Aoife first, with the random values after that scripted (dice, disasters). */
    static Game withScript(ScriptedRandom afterFirstPlayer) {
        return start(afterFirstPlayer, FOUR);
    }

    /** A four-player game, Aoife first, whose first end of round has no disaster. */
    static Game quietRound() {
        return start(new ScriptedRandom().firstPlayer(0).noDisaster(), FOUR);
    }

    /** Marks a Polluting square as lobbied this round by {@code by}. */
    static void lobby(Game game, int square, Player by) {
        ((PollutingSquare) game.board().square(square)).lobby(by);
    }

    /** Converts a Polluting square as if {@code by} had converted it (no payment). */
    static void convert(Game game, int square, Player by) {
        game.board().replace(((PollutingSquare) game.board().square(square)).convert(by));
    }

    /** Starts a co-funding deal for {@code funder} on a Green Project (no payment). */
    static void deal(Player funder, int square) {
        funder.startDeal(new CoFundingDeal(funder, square));
    }

    /** Makes a player bankrupt by a compulsory payment they can't cover. */
    static void bankrupt(Player player) {
        player.payCompulsory(player.money() + 1);
    }

    /** Makes the card with this id (such as "Q01") the next one drawn. */
    static void nextCard(Game game, String id) {
        game.deck().putOnTop(id);
    }

    /** Gives a square to a player (test set-up only). */
    static void own(Game game, int square, Player owner) {
        ((OwnableSquare) game.board().square(square)).setOwner(owner);
    }

    static void setMoney(Player player, int money) {
        player.receive(money - player.money());
    }

    static Player player(Game game, String name) {
        return game.players().stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
    }

    /** The available actions of one kind. */
    static List<Action> actions(Game game, Action.Kind kind) {
        return game.availableActions().stream().filter(a -> a.kind() == kind).toList();
    }

    /** The single available action of a kind; fails if there isn't exactly one. */
    static Action only(Game game, Action.Kind kind) {
        List<Action> found = actions(game, kind);
        if (found.size() != 1) {
            throw new AssertionError("expected one " + kind + " action, found " + found + " in " + game.availableActions());
        }
        return found.get(0);
    }
}
