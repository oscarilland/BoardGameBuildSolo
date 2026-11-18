package netzerobelfast.ui;

import netzerobelfast.model.Action;
import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Game;
import netzerobelfast.model.Player;
import netzerobelfast.model.Square;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The buttons on the action bar for the game's current phase (NFR-02 AC-2, NFR-05 AC-4).
 * No Swing here, so it can be tested headless.
 *
 * <p>The buttons come straight from the model: one per action in {@code availableActions()},
 * so the UI never decides what is legal or affordable. The rest are the phase's own calls
 * (roll, choose a route, end the turn). End turn is always shown, and enabled only when the
 * game allows it, so players always know where it is.
 */
public final class ActionOptions {

    /** No shortcut key. */
    public static final char NO_KEY = 0;

    private ActionOptions() {}

    /** What pressing a button does: one call on the game, through the controller. */
    public sealed interface Command {
        record Roll() implements Command {}
        record RollToReturn() implements Command {}
        record ChooseRoute(Route route) implements Command {}
        record ChooseDirection(Heading heading) implements Command {}
        record Take(Action action) implements Command {}
        record EndTurn() implements Command {}
        /** Opens the waiting quiz or choice card again; it normally opens by itself. */
        record OpenCard() implements Command {}
    }

    /**
     * One button.
     *
     * @param label   what it says, including any cost, for example "Buy Harbour Gas Plant £9M"
     * @param key     its shortcut key, or {@link #NO_KEY}
     * @param enabled whether it can be pressed now
     */
    public record Button(String label, char key, boolean enabled, Command command) {
        /** The label with its shortcut, for example "Roll dice (R)". */
        public String text() {
            return key == NO_KEY ? label : label + " (" + key + ")";
        }
    }

    public static List<Button> forGame(Game game) {
        List<Button> buttons = new ArrayList<>();
        switch (game.phase()) {
            case AWAITING_RETURN_ROLL -> buttons.add(new Button("Roll to return", 'R', true, new Command.RollToReturn()));
            case AWAITING_ROLL -> buttons.add(new Button("Roll dice", 'R', true, new Command.Roll()));
            case AWAITING_ROUTE -> {
                buttons.add(new Button("Stay on the loop", '1', true, new Command.ChooseRoute(Route.LOOP)));
                buttons.add(new Button("Cut through the City Centre", '2', true, new Command.ChooseRoute(Route.CITY_CENTRE)));
            }
            case AWAITING_DIRECTION -> {
                buttons.add(new Button("Towards Glider West", '1', true, new Command.ChooseDirection(Heading.TOWARDS_WEST)));
                buttons.add(new Button("Towards Glider East", '2', true, new Command.ChooseDirection(Heading.TOWARDS_EAST)));
            }
            case MOVE_DONE -> {
                // The first action of each kind gets the kind's key; others (a second Glider stop,
                // a second square to convert) are reached with Tab.
                Set<Action.Kind> keyed = EnumSet.noneOf(Action.Kind.class);
                for (Action action : game.availableActions()) {
                    char key = keyed.add(action.kind()) ? key(action.kind()) : NO_KEY;
                    buttons.add(new Button(label(game, action), key, true, new Command.Take(action)));
                }
            }
            // The card pop-up opens by itself; this reopens it if it was ever closed without an answer.
            case AWAITING_QUIZ_ANSWER, AWAITING_CHOICE -> buttons.add(new Button("Answer the card", 'O', true, new Command.OpenCard()));
            case GAME_OVER -> {
                // Nothing left to do.
            }
        }
        buttons.add(new Button("End turn", 'E', game.phase() == Game.Phase.MOVE_DONE, new Command.EndTurn()));
        return buttons;
    }

    /** The shortcut key for each kind of action. */
    static char key(Action.Kind kind) {
        return switch (kind) {
            case BUY -> 'B';
            case CO_FUND -> 'C';
            case LOBBY -> 'L';
            case CONVERT -> 'V';
            case BUY_RESILIENCE -> 'P';
            case RIDE_GLIDER -> 'G';
        };
    }

    /** "Buy Harbour Gas Plant £9M", "Glider to City Hall £1M", "Protect: Resilience 2 £4M". */
    static String label(Game game, Action action) {
        Player player = game.currentPlayer();
        String cost = " £" + action.cost() + "M";
        return switch (action.kind()) {
            case BUY -> "Buy " + name(game, action.square()) + cost;
            case CO_FUND -> "Co-fund " + name(game, action.square()) + cost;
            case LOBBY -> "Lobby " + name(game, action.square()) + cost;
            case CONVERT -> "Convert " + name(game, action.square()) + cost;
            case BUY_RESILIENCE -> "Protect: Resilience " + (player.resilienceLevel() + 1) + cost;
            case RIDE_GLIDER -> "Glider to " + name(game, action.destination()) + cost;
        };
    }

    private static String name(Game game, int id) {
        Square square = game.board().square(id);
        return square.name();
    }
}
