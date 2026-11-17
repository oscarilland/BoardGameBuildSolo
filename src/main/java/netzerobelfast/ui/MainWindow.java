package netzerobelfast.ui;

import netzerobelfast.config.GameConfig;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * The game window. For now (plan 001) it only shows what was loaded, to prove that Swing and
 * the packaged jar work. The real screens come in plan 007.
 *
 * <p>Swing isn't thread-safe: create and change windows only on the event dispatch thread,
 * which is why {@link netzerobelfast.App} calls {@link #show} through
 * {@code SwingUtilities.invokeLater}.
 */
public final class MainWindow {

    private MainWindow() {}

    public static void show(GameConfig config) {
        JFrame frame = new JFrame("Net Zero Belfast");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel status = new JLabel(config.summary(), SwingConstants.CENTER);
        status.setFont(status.getFont().deriveFont(Font.PLAIN, 18f));
        frame.add(status, BorderLayout.CENTER);

        frame.setMinimumSize(new Dimension(1024, 700));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
