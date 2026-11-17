package netzerobelfast.config;

/**
 * A data file is missing, isn't valid JSON, or breaks a validation rule.
 *
 * <p>The message always names the file and, where there is one, the key, for example
 * {@code board.json: squares[7].price is 3, expected 8-12 for GREEN_PROJECT}, so a bad
 * edit to a data file can be found without reading code (NFR-07 AC-3).
 *
 * <p>It's unchecked because it's thrown from deep inside parsing and validation, and
 * handled in exactly one place: at start-up, in {@code App}.
 */
public class ConfigException extends RuntimeException {

    public ConfigException(String file, String key, String problem) {
        super(file + ": " + key + " " + problem);
    }

    public ConfigException(String file, String problem) {
        super(file + ": " + problem);
    }
}
