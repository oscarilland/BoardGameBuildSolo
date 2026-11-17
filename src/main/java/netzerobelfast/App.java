package netzerobelfast;

import netzerobelfast.config.ConfigException;
import netzerobelfast.config.GameConfig;
import netzerobelfast.model.Character;
import netzerobelfast.ui.MainWindow;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Starts Net Zero Belfast.
 *
 * <ul>
 *   <li>{@code java -jar net-zero-belfast.jar} loads the data and opens the window.</li>
 *   <li>{@code java -jar net-zero-belfast.jar --check} loads and validates the data, prints
 *       what it found and exits: 0 if the data is fine, 1 if not. No window, so it works in CI.</li>
 * </ul>
 */
public final class App {

    private App() {}

    public static void main(String[] args) {
        boolean checkOnly = List.of(args).contains("--check");

        GameConfig config;
        try {
            config = GameConfig.load();
            // Every character in the data must have a class in the game; fail now, not at game start.
            Character.fromAll(config.rules().characters());
        } catch (ConfigException | IllegalArgumentException e) {
            System.err.println("Net Zero Belfast can't start: " + e.getMessage());
            if (!checkOnly && !GraphicsEnvironment.isHeadless()) {
                showError(e.getMessage());
            }
            System.exit(1);
            return;
        }

        if (checkOnly) {
            System.out.println(config.summary());
            return;
        }
        SwingUtilities.invokeLater(() -> MainWindow.show(config));
    }

    /** Shows a start-up error in a dialog, on the event dispatch thread, and waits until it's closed. */
    private static void showError(String message) {
        try {
            SwingUtilities.invokeAndWait(() -> JOptionPane.showMessageDialog(
                    null, message, "Net Zero Belfast can't start", JOptionPane.ERROR_MESSAGE));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            // The dialog itself failed; the message has already gone to the console.
        }
    }
}
