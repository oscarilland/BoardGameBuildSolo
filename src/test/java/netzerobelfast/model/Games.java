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

    /** Puts the current player on a square (test set-up only). */
    static void place(Game game, int square, Board.Heading heading) {
        game.currentPlayer().moveTo(square, heading);
    }
}
