package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * The final scores and the winner (FR-14). Plan 009 replaces this with the full end-of-game
 * report: the meter graph and each player's share of the pollution (FR-16).
 *
 * <p>The lines come from {@code StatusText.finalScores}: the outcome, the scoring rule, one line
 * per player in order, then the winner.
 */
final class GameOverScreen extends JPanel {

    private final GameButton again;

    GameOverScreen(int endYear, List<String> lines, Runnable onPlayAgain, Runnable onQuit) {
        super(new GridBagLayout());
        setBackground(Palette.BACKGROUND);
        CardPanel card = new CardPanel(null, Palette.SURFACE, Palette.BORDER, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 34, 28 + CardPanel.SHADOW, 34 + CardPanel.SHADOW / 2));
        card.add(Ui.label("GAME OVER  ·  " + endYear, Ui.SMALL_BOLD, Palette.ACCENT));
        card.add(Box.createVerticalStrut(6));
        if (!lines.isEmpty()) {
            String winner = lines.get(lines.size() - 1);
            card.add(Ui.wrapped(winner, Fonts.SEMIBOLD.deriveFont(30f), Palette.TEXT, 700));
            card.add(Box.createVerticalStrut(14));
        }
        for (int i = 0; i < lines.size() - 1; i++) {
            boolean heading = i < 2;
            JLabel label = Ui.wrapped(lines.get(i), heading ? Ui.BOLD.deriveFont(17f) : Ui.BODY.deriveFont(16f),
                    i == 1 ? Palette.MUTED_TEXT : Palette.TEXT, 700);
            card.add(label);
            card.add(Box.createVerticalStrut(i == 1 ? 14 : 6));
        }
        again = Ui.primary("Play again", onPlayAgain);
        GameButton quit = Ui.button("Quit", onQuit);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        row.setOpaque(false);
        row.add(again);
        row.add(Box.createHorizontalStrut(12));
        row.add(quit);
        card.add(row);
        for (var component : card.getComponents()) {
            ((JComponent) component).setAlignmentX(LEFT_ALIGNMENT);
        }
        add(card);
    }

    void focusPlayAgain() {
        again.requestFocusInWindow();
    }
}
