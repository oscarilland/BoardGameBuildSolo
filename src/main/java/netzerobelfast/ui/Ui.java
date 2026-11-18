package netzerobelfast.ui;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Shared Swing helpers: fonts, buttons with a visible focus ring, and wrapped labels. Every
 * colour comes from {@link Palette}.
 */
final class Ui {

    static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    static final Font BOLD = BODY.deriveFont(Font.BOLD);
    static final Font SMALL = BODY.deriveFont(12f);
    static final Font HEADING = BODY.deriveFont(Font.BOLD, 22f);
    static final Font TITLE = BODY.deriveFont(Font.BOLD, 44f);

    private static final Border FOCUSED = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Palette.FOCUS, 3), BorderFactory.createEmptyBorder(6, 12, 6, 12));
    private static final Border UNFOCUSED = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Palette.BORDER, 1), BorderFactory.createEmptyBorder(8, 14, 8, 14));

    private Ui() {}

    /** Sets Swing's defaults that buttons, tooltips and dialogs use, so they follow the palette. */
    static void installDefaults() {
        UIManager.put("Button.disabledText", Palette.BUTTON_DISABLED_TEXT);
        UIManager.put("Button.select", Palette.HEADER);
        UIManager.put("Panel.background", Palette.BACKGROUND);
        UIManager.put("OptionPane.background", Palette.BACKGROUND);
        UIManager.put("ToolTip.background", Palette.SURFACE);
        UIManager.put("ToolTip.foreground", Palette.TEXT);
        UIManager.put("ToolTip.font", BODY);
    }

    /**
     * A button in the palette's colours, with a thick focus ring, that Enter presses as well as
     * Space (NFR-05 AC-4).
     */
    static JButton button(String text, Runnable onPress) {
        JButton button = new JButton(text) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setBackground(enabled ? Palette.BUTTON : Palette.BUTTON_DISABLED);
            }
        };
        button.setFont(BOLD);
        button.setForeground(Palette.BUTTON_TEXT);
        button.setBackground(Palette.BUTTON);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(UNFOCUSED);
        button.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                button.setBorder(FOCUSED);
            }

            @Override
            public void focusLost(FocusEvent e) {
                button.setBorder(UNFOCUSED);
            }
        });
        button.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        button.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("released ENTER"), "released");
        button.addActionListener(e -> onPress.run());
        return button;
    }

    /** A label that wraps its text at {@code width} pixels. The text is escaped, so names can't inject HTML. */
    static JLabel wrapped(String text, Font font, Color color, int width) {
        JLabel label = new JLabel(html(text, width));
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /** Wraps plain text as Swing HTML at a width in pixels, escaping it and keeping line breaks. */
    static String html(String text, int width) {
        String escaped = breakLongWords(text).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width: " + width + "px'>" + escaped + "</body></html>";
    }

    /** Longer than this, a word with no spaces (a web address) is split over lines. */
    private static final int LONG_WORD = 48;

    /**
     * Swing's HTML can't wrap a long web address, so it would run off the edge. This breaks any
     * long word onto new lines after a "/" or "-", without adding or changing any characters.
     */
    static String breakLongWords(String text) {
        StringBuilder result = new StringBuilder();
        for (String word : text.split(" ", -1)) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            int sinceBreak = 0;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                result.append(c);
                sinceBreak++;
                if ((c == '/' || c == '-') && sinceBreak >= LONG_WORD && i < word.length() - 1 && word.length() > LONG_WORD) {
                    result.append('\n');
                    sinceBreak = 0;
                }
            }
        }
        return result.toString();
    }

    /** Binds a key, anywhere in the component's window, to an action. */
    static void bindKey(JComponent component, String key, String name, Runnable action) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        component.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
