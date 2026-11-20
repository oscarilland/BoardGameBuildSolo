package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.List;

/**
 * The game's pop-ups (plan 008 style): rounded cards with a header coloured by kind, over a
 * dimmed game window, placed over the board so the top bar stays readable (FR-15 AC-1). Each has
 * a focused default button and its own keys.
 *
 * <p>A modal dialog doesn't busy-wait: Swing keeps handling events while it's open, and the
 * call returns when the player closes it (NFR-11).
 */
final class Dialogs {

    /** What kind of pop-up it is; sets the header colour. The title always says it in words too. */
    enum Tone {
        QUIZ(Palette.HEADER_QUIZ), CHOICE(Palette.HEADER_CHOICE), EVENT(Palette.HEADER_EVENT),
        ROUND(Palette.HEADER_ROUND), DISASTER(Palette.HEADER_DISASTER), INFO(Palette.BUTTON);

        final Color header;

        Tone(Color header) {
            this.header = header;
        }
    }

    /** Text width inside a dialog, in pixels. */
    static final int WIDTH = 500;

    private Dialogs() {}

    /** A choice between options; returns the index of the one pressed. The dialog can only be closed by choosing. */
    static int choose(Component over, Tone tone, String title, List<String> body, List<String> options,
            List<Boolean> enabled, List<Character> keys) {
        JDialog dialog = dialog(over, title);
        int[] chosen = {-1};
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setOpaque(false);
        GameButton first = null;
        for (int i = 0; i < options.size(); i++) {
            int index = i;
            Runnable press = () -> {
                chosen[0] = index;
                dialog.dispose();
            };
            if (!enabled.get(i)) {
                // Plain text, not a disabled button, so it stays readable (NFR-05 AC-1).
                JLabel unavailable = Ui.wrapped(options.get(i), Ui.BOLD, Palette.MUTED_TEXT, WIDTH - 40);
                unavailable.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
                unavailable.setAlignmentX(Component.LEFT_ALIGNMENT);
                buttons.add(unavailable);
                buttons.add(Box.createVerticalStrut(8));
                continue;
            }
            // Choice options already start with their letter ("A: ..."); quiz answers get their number.
            String label = options.get(i).startsWith(keys.get(i) + ":") ? options.get(i) : keys.get(i) + "   " + options.get(i);
            GameButton button = Ui.button(Ui.html(label, WIDTH - 60), press);
            button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new java.awt.Dimension(WIDTH + 20, Integer.MAX_VALUE));
            buttons.add(button);
            buttons.add(Box.createVerticalStrut(8));
            if (first == null) {
                first = button;
            }
            Ui.bindKey(dialog.getRootPane(), String.valueOf(Character.toUpperCase(keys.get(i))), "option" + i, press);
        }
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        show(dialog, over, tone, body, buttons, first);
        return chosen[0];
    }

    /** A message with an OK button; Enter or Escape closes it. */
    static void tell(Component over, Tone tone, String title, List<String> body) {
        JDialog dialog = dialog(over, title);
        GameButton ok = Ui.primary("OK", dialog::dispose);
        Ui.bindKey(dialog.getRootPane(), "ESCAPE", "close", dialog::dispose);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setOpaque(false);
        row.add(ok);
        dialog.getRootPane().setDefaultButton(ok);
        show(dialog, over, tone, body, row, ok);
    }

    private static JDialog dialog(Component over, String title) {
        Window owner = over == null ? null : SwingUtilities.getWindowAncestor(over);
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        if (translucent()) {
            dialog.setBackground(new Color(0, 0, 0, 0));
        }
        return dialog;
    }

    /** True if windows can have see-through corners on this computer; if not, pop-ups are square. */
    private static boolean translucent() {
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return device.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    }

    private static void show(JDialog dialog, Component over, Tone tone, List<String> body, JComponent controls, GameButton focus) {
        dialog.setContentPane(content(tone, dialog.getTitle(), body, controls));
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
        // Dim the game behind the pop-up while it's open.
        Window owner = dialog.getOwner();
        Component glass = owner instanceof RootPaneContainer root ? root.getGlassPane() : null;
        Component dimmer = null;
        if (owner instanceof JFrame frame) {
            dimmer = new Dimmer();
            frame.setGlassPane(dimmer);
            dimmer.setVisible(true);
        }
        try {
            dialog.setVisible(true);
        } finally {
            if (owner instanceof JFrame frame && glass != null) {
                if (dimmer != null) {
                    dimmer.setVisible(false);
                }
                frame.setGlassPane(glass);
            }
        }
    }

    /** What a pop-up shows: a coloured header with its title, its lines (sources quieter), and its buttons. */
    static JPanel content(Tone tone, String title, List<String> body, JComponent controls) {
        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Ui.smooth(graphics);
                int arc = 22;
                g.setColor(Palette.CARD);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g.setColor(tone.header);
                g.fillRoundRect(0, 0, getWidth() - 1, 56, arc, arc);
                g.fillRect(0, 30, getWidth() - 1, 26);
                g.setColor(Palette.BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g.dispose();
            }
        };
        content.setOpaque(false);
        JLabel heading = Ui.label(title, Fonts.BOLD.deriveFont(20f), Palette.HEADER_LABEL);
        heading.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        content.add(heading, BorderLayout.NORTH);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(14, 22, 6, 22));
        for (String line : body) {
            boolean quiet = line.startsWith("Source");
            JLabel label = Ui.wrapped(line, quiet ? Ui.SMALL : Ui.BODY, quiet ? Palette.MUTED_TEXT : Palette.TEXT, WIDTH);
            text.add(label);
            text.add(Box.createVerticalStrut(7));
        }
        content.add(text, BorderLayout.CENTER);
        controls.setBorder(BorderFactory.createEmptyBorder(6, 22, 18, 22));
        content.add(controls, BorderLayout.SOUTH);
        return content;
    }

    /** A see-through dark layer over the game window, behind a pop-up. A panel, for its accessibility context. */
    private static final class Dimmer extends JPanel {
        Dimmer() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Palette.DIM);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
