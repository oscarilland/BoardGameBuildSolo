package netzerobelfast.ui;

import netzerobelfast.config.Rules;
import netzerobelfast.model.Game.PlayerSetup;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * When the setup screen's Start button is enabled (FR-01). No Swing here, so it can be tested.
 *
 * <p>This only checks that the form is complete, to guide players as they fill it in. The model
 * is still the final check: {@code Game.start} enforces every FR-01 rule, and the screen shows
 * its message if it refuses.
 */
public final class SetupRules {

    private SetupRules() {}

    /** What still needs doing before the game can start, or empty if it's ready. */
    public static Optional<String> missing(List<PlayerSetup> rows, Rules.Game rules) {
        if (rows.size() < rules.minPlayers() || rows.size() > rules.maxPlayers()) {
            return Optional.of("Choose " + rules.minPlayers() + " to " + rules.maxPlayers() + " players.");
        }
        Set<String> characters = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            PlayerSetup row = rows.get(i);
            String name = row.name() == null ? "" : row.name().strip();
            if (name.isEmpty()) {
                return Optional.of("Player " + (i + 1) + " needs a name.");
            }
            if (row.characterId() == null) {
                return Optional.of("Player " + (i + 1) + " needs a character.");
            }
            if (!characters.add(row.characterId())) {
                return Optional.of("Player " + (i + 1) + " has a character someone else has chosen.");
            }
        }
        return Optional.empty();
    }
}
