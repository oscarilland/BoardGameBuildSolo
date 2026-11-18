package netzerobelfast;

import netzerobelfast.config.ConfigException;
import netzerobelfast.config.GameConfig;
import netzerobelfast.model.Character;
import netzerobelfast.sim.BalanceReport;
import netzerobelfast.ui.MainWindow;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

/**
 * Starts Net Zero Belfast.
 *
 * <ul>
 *   <li>{@code java -jar net-zero-belfast.jar} loads the data and opens the window.</li>
 *   <li>{@code java -jar net-zero-belfast.jar --check} loads and validates the data, prints
 *       what it found and exits: 0 if the data is fine, 1 if not. No window, so it works in CI.</li>
 *   <li>{@code java -jar net-zero-belfast.jar --simulate [games]} plays that many games (1,000 by
 *       default) with computer players and prints a balance report. No window.</li>
 *   <li>{@code java -jar net-zero-belfast.jar --seed N} opens the window with the game's random
 *       source seeded, so a game played by hand can be replayed exactly while testing.</li>
 * </ul>
 */
public final class App {

    /** Games played by {@code --simulate} when no number is given. */
    private static final int DEFAULT_SIMULATED_GAMES = 1000;

    /** A fixed seed, so the same {@code --simulate} command always prints the same report. */
    private static final long SIMULATION_SEED = 2026;

    private App() {}

    public static void main(String[] args) {
        boolean checkOnly = List.of(args).contains("--check");

        GameConfig config;
        try {
            config = GameConfig.load();
            // Every character in the data must have a class in the game; fail now, not at game start.
            Character.fromAll(config.rules());
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
        int simulate = List.of(args).indexOf("--simulate");
        if (simulate >= 0) {
            int games = DEFAULT_SIMULATED_GAMES;
            if (simulate + 1 < args.length) {
                try {
                    games = Integer.parseInt(args[simulate + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("--simulate takes a number of games, for example --simulate 1000");
                    System.exit(1);
                    return;
                }
            }
            long started = System.nanoTime();
            System.out.print(BalanceReport.run(config, games, SIMULATION_SEED).format());
            System.out.printf("(%.1f s)%n", (System.nanoTime() - started) / 1e9);
            return;
        }
        // The game's one random source (src/AGENTS.md rule 3), seeded only when testing by hand.
        Random random;
        int seed = List.of(args).indexOf("--seed");
        if (seed >= 0) {
            try {
                random = new Random(Long.parseLong(args[seed + 1]));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                System.err.println("--seed takes a number, for example --seed 2026");
                System.exit(1);
                return;
            }
        } else {
            random = new Random();
        }
        SwingUtilities.invokeLater(() -> MainWindow.show(config, random));
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
