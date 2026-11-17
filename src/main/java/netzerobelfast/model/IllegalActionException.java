package netzerobelfast.model;

/**
 * Something asked the game to do what the rules don't allow right now, such as rolling twice
 * or choosing a route when there's no junction (NFR-08 AC-4).
 *
 * <p>The UI should only offer legal actions, so in normal play this never happens. If it does,
 * it's a bug in the caller, and the game's state is left unchanged.
 */
public class IllegalActionException extends RuntimeException {

    public IllegalActionException(String message) {
        super(message);
    }
}
