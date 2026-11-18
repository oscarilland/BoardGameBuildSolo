package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;

/** The title screen: Start, How to play, Quit. */
final class TitleScreen extends JPanel {

    private final JButton start;

    TitleScreen(String tagline, Runnable onStart, Runnable onHowToPlay, Runnable onQuit) {
        super(new GridBagLayout());
        setBackground(Palette.BACKGROUND);
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = Ui.label("Net Zero Belfast", Ui.TITLE, Palette.TEXT);
        JLabel subtitle = Ui.label(tagline, Ui.BODY, Palette.MUTED_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(title);
        column.add(Box.createVerticalStrut(8));
        column.add(subtitle);
        column.add(Box.createVerticalStrut(32));

        start = Ui.button("Start a game", onStart);
        start.setMnemonic(KeyEvent.VK_S);
        JButton howTo = Ui.button("How to play", onHowToPlay);
        howTo.setMnemonic(KeyEvent.VK_H);
        JButton quit = Ui.button("Quit", onQuit);
        quit.setMnemonic(KeyEvent.VK_Q);
        for (JButton button : new JButton[] {start, howTo, quit}) {
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new java.awt.Dimension(240, button.getPreferredSize().height));
            column.add(button);
            column.add(Box.createVerticalStrut(10));
        }
        column.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        add(column);
    }

    /** Puts the keyboard focus on Start, so Enter starts a game. */
    void focusStart() {
        start.requestFocusInWindow();
    }
}
