package netzerobelfast.model;

import netzerobelfast.config.GameConfig;

/**
 * Test helper for tests outside {@code model} (such as the UI's): public access to the
 * package-private set-up in {@link Games} and {@link ScriptedRandom}. Test code only.
 */
public final class TestGames {

    public static final GameConfig CONFIG = Games.CONFIG;

    private TestGames() {}

    /** Scripted random values for a four-player game where seat 0 (Aoife) goes first. */
    public static final class Script {
        private final ScriptedRandom random = new ScriptedRandom().firstPlayer(0);

        /** Two-dice totals. */
        public Script rolls(int... totals) {
            random.rolls(totals);
            return this;
        }

        /** One die, such as the disaster die or a roll to return. */
        public Script die(int face) {
            random.die(face);
            return this;
        }

        /** The disaster die as a 6: no disaster. */
        public Script noDisaster() {
            random.noDisaster();
            return this;
        }

        /** Raw values, such as a disaster type or a district. */
        public Script values(int... raw) {
            random.values(raw);
            return this;
        }

        /** Aoife, Ben, Chloe and Dev (Industrialist, Tech Entrepreneur, Council Planner, Farmer). */
        public Game start() {
            return Games.withScript(random);
        }
    }

    public static Script script() {
        return new Script();
    }

    public static void arrive(Game game, int square) {
        Games.arrive(game, square);
    }

    public static void nextCard(Game game, String id) {
        Games.nextCard(game, id);
    }

    public static void own(Game game, int square, Player owner) {
        Games.own(game, square, owner);
    }

    public static void setMoney(Player player, int money) {
        Games.setMoney(player, money);
    }

    public static void bankrupt(Player player) {
        Games.bankrupt(player);
    }

    public static void deal(Player funder, int square) {
        Games.deal(funder, square);
    }

    public static void lobby(Game game, int square, Player by) {
        Games.lobby(game, square, by);
    }

    public static void raiseResilience(Player player) {
        player.raiseResilience();
    }

    /** Moves the Climate Meter directly, as a card would. */
    public static void adjustMeter(Game game, int delta) {
        game.meter().adjust(delta);
    }

    public static Player player(Game game, String name) {
        return Games.player(game, name);
    }
}
