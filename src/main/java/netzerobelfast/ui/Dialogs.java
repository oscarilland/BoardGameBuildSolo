package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

/**
 * The game's pop-ups: modal dialogs drawn in the palette's colours, placed over the board so
 * the top bar stays visible (FR-15 AC-1). Each has a focused default button, and its own keys.
 *
 * <p>A modal dialog doesn't busy-wait: Swing keeps handling events while it's open, and the
 * call returns when the player closes it (NFR-11).
 */
final class Dialogs {

    /** Text width inside a dialog, in pixels. */
    static final int WIDTH = 480;

    private Dialogs() {}

    /** A choice between buttons; returns the index of the one pressed. The dialog can only be closed by choosing. */
    static int choose(Component over, String title, List<String> body, List<String> options, List<Boolean> enabled, List<Character> keys) {
        JDialog dialog = dialog(over, title);
        int[] chosen = {-1};
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setOpaque(false);
        JButton first = null;
        for (int i = 0; i < options.size(); i++) {
            int index = i;
            Runnable press = () -> {
                chosen[0] = index;
                dialog.dispose();
            };
            if (!enabled.get(i)) {
                // Not a disabled button: Swing greys out a disabled button's wrapped text too faintly
                // to read (NFR-05 AC-1), so an option that can't be taken is plain text instead.
                JLabel unavailable = Ui.wrapped(options.get(i), Ui.BOLD, Palette.MUTED_TEXT, WIDTH - 40);
                unavailable.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
                unavailable.setAlignmentX(Component.LEFT_ALIGNMENT);
                buttons.add(unavailable);
                buttons.add(Box.createVerticalStrut(6));
                continue;
            }
            JButton button = Ui.button(Ui.html(options.get(i) + " (" + keys.get(i) + ")", WIDTH - 40), press);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttons.add(button);
            buttons.add(Box.createVerticalStrut(6));
            if (first == null) {
                first = button;
            }
            Ui.bindKey(dialog.getRootPane(), String.valueOf(java.lang.Character.toUpperCase(keys.get(i))), "option" + i, press);
        }
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        show(dialog, over, body, buttons, first);
        return chosen[0];
    }

    /** A message with an OK button; Enter or Escape closes it. */
    static void tell(Component over, String title, List<String> body) {
        JDialog dialog = dialog(over, title);
        JButton ok = Ui.button("OK", dialog::dispose);
        Ui.bindKey(dialog.getRootPane(), "ESCAPE", "close", dialog::dispose);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        row.setOpaque(false);
        row.add(ok);
        dialog.getRootPane().setDefaultButton(ok);
        show(dialog, over, body, row, ok);
    }

    private static JDialog dialog(Component over, String title) {
        Window owner = over == null ? null : javax.swing.SwingUtilities.getWindowAncestor(over);
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setBackground(Palette.CARD);
        return dialog;
    }

    private static void show(JDialog dialog, Component over, List<String> body, JComponent controls, JButton focus) {
        dialog.setContentPane(content(dialog.getTitle(), body, controls));
        dialog.pack();
        dialog.setLocationRelativeTo(over);
        if (focus != null) {
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowOpened(java.awt.event.WindowEvent e) {
                    focus.requestFocusInWindow();
                }
            });
        }
        dialog.setVisible(true);
    }

    /** What a pop-up shows: its title, its lines (sources in smaller, quieter text), and its buttons. */
    static JPanel content(String title, List<String> body, JComponent controls) {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Palette.CARD);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.BORDER, 2), BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel heading = Ui.label(title, Ui.HEADING, Palette.TEXT);
        text.add(heading);
        text.add(Box.createVerticalStrut(8));
        for (String line : body) {
            boolean quiet = line.startsWith("Source");
            text.add(Ui.wrapped(line, quiet ? Ui.SMALL : Ui.BODY, quiet ? Palette.MUTED_TEXT : Palette.TEXT, WIDTH));
            text.add(Box.createVerticalStrut(6));
        }
        content.add(text, BorderLayout.CENTER);
        content.add(controls, BorderLayout.SOUTH);
        return content;
    }
}
