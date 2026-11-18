package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * The final scores and the winner (FR-14). Plan 008 replaces this with the full end-of-game
 * report: the meter graph and each player's share of the pollution (FR-16).
 */
final class GameOverScreen extends JPanel {

    private final JButton again;

    GameOverScreen(List<String> lines, Runnable onPlayAgain, Runnable onQuit) {
        super(new GridBagLayout());
        setBackground(Palette.BACKGROUND);
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        column.add(Ui.label("Game over", Ui.TITLE.deriveFont(36f), Palette.TEXT));
        column.add(Box.createVerticalStrut(16));
        for (int i = 0; i < lines.size(); i++) {
            boolean last = i == lines.size() - 1;
            column.add(Ui.wrapped(lines.get(i), last || i == 0 ? Ui.BOLD.deriveFont(18f) : Ui.BODY.deriveFont(17f),
                    Palette.TEXT, 700));
            column.add(Box.createVerticalStrut(8));
        }
        again = Ui.button("Play again", onPlayAgain);
        JButton quit = Ui.button("Quit", onQuit);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        row.setOpaque(false);
        row.add(again);
        row.add(Box.createHorizontalStrut(12));
        row.add(quit);
        row.setAlignmentX(LEFT_ALIGNMENT);
        column.add(row);
        for (var component : column.getComponents()) {
            ((javax.swing.JComponent) component).setAlignmentX(LEFT_ALIGNMENT);
        }
        add(column);
    }

    void focusPlayAgain() {
        again.requestFocusInWindow();
    }
}
