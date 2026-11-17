package netzerobelfast.model;

/**
 * Something the current player can do after moving (analysis model: the {@code Action[]} that
 * {@code Square.landOn()} returns).
 *
 * <p>{@link Game#availableActions()} lists the actions that are legal, affordable and would
 * change something; {@link Game#take(Action)} only accepts an action from that list. The UI's
 * buttons and the bots both choose from the list, so neither can break the rules.
 *
 * @param kind        what the action is
 * @param cost        £M the player pays, after their character's discounts
 * @param square      the square it acts on: the one landed on, or for {@code CONVERT} the square to convert
 * @param destination for {@code RIDE_GLIDER}, the stop to ride to; otherwise {@link #NONE}
 */
public record Action(Kind kind, int cost, int square, int destination) {

    public static final int NONE = -1;

    public enum Kind {
        /** Buy the unowned square landed on (FR-04). */
        BUY,
        /** Co-fund another player's Green Project (FR-05). */
        CO_FUND,
        /** Lobby another player's Polluting square for this round (FR-06). */
        LOBBY,
        /** Convert one of your own Polluting squares, anywhere on the board (FR-07). */
        CONVERT,
        /** Buy your next Resilience level (FR-08). */
        BUY_RESILIENCE,
        /** Ride the Glider to another stop (FR-09). */
        RIDE_GLIDER
    }

    static Action on(Kind kind, int cost, int square) {
        return new Action(kind, cost, square, NONE);
    }

    /** True for the landing actions; converting is a separate step and doesn't use one up. */
    public boolean isLandingAction() {
        return kind != Kind.CONVERT;
    }
}
