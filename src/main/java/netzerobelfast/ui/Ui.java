package netzerobelfast.ui;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

/**
 * Shared Swing helpers: fonts, painted buttons and cards, chips, and wrapped labels. Every
 * colour comes from {@link Palette}, every font from {@link Fonts}.
 */
final class Ui {

    static final Font BODY = Fonts.REGULAR.deriveFont(15f);
    static final Font BOLD = Fonts.SEMIBOLD.deriveFont(15f);
    static final Font SMALL = Fonts.REGULAR.deriveFont(12f);
    static final Font SMALL_BOLD = Fonts.SEMIBOLD.deriveFont(12f);
    static final Font HEADING = Fonts.BOLD.deriveFont(22f);
    static final Font TITLE = Fonts.BOLD.deriveFont(48f);

    private Ui() {}

    /** Sets the defaults Swing's own controls use (text fields, lists, tooltips, scroll bars) to the dark theme. */
    static void installDefaults() {
        ColorUIResource background = new ColorUIResource(Palette.BACKGROUND);
        ColorUIResource raised = new ColorUIResource(Palette.RAISED);
        ColorUIResource text = new ColorUIResource(Palette.TEXT);
        FontUIResource body = new FontUIResource(BODY);
        UIManager.put("Panel.background", background);
        UIManager.put("OptionPane.background", background);
        UIManager.put("Viewport.background", background);
        UIManager.put("ScrollPane.background", background);
        UIManager.put("ScrollBar.background", background);
        UIManager.put("ScrollBar.track", background);
        UIManager.put("ScrollBar.thumb", raised);
        UIManager.put("ScrollBar.thumbHighlight", raised);
        UIManager.put("ScrollBar.thumbShadow", raised);
        UIManager.put("ScrollBar.thumbDarkShadow", raised);
        UIManager.put("ToolTip.background", raised);
        UIManager.put("ToolTip.foreground", text);
        UIManager.put("ToolTip.font", body);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Palette.BORDER));
        for (String control : new String[] {"TextField", "ComboBox", "List"}) {
            UIManager.put(control + ".background", raised);
            UIManager.put(control + ".foreground", text);
            UIManager.put(control + ".font", body);
            UIManager.put(control + ".selectionBackground", new ColorUIResource(Palette.BUTTON_HOVER));
            UIManager.put(control + ".selectionForeground", text);
        }
        UIManager.put("TextField.caretForeground", new ColorUIResource(Palette.ACCENT));
        UIManager.put("TextField.inactiveForeground", new ColorUIResource(Palette.MUTED_TEXT));
        UIManager.put("ComboBox.buttonBackground", raised);
        UIManager.put("ComboBox.disabledForeground", new ColorUIResource(Palette.MUTED_TEXT));
        UIManager.put("Label.foreground", text);
    }

    /** Graphics with smooth shapes and text; callers dispose it. */
    static Graphics2D smooth(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g;
    }

    /** A quieter button. */
    static GameButton button(String text, Runnable onPress) {
        return new GameButton(text, GameButton.Kind.SECONDARY, onPress);
    }

    /** The screen's main action, in the accent colour. */
    static GameButton primary(String text, Runnable onPress) {
        return new GameButton(text, GameButton.Kind.PRIMARY, onPress);
    }

    /** A rounded card with a soft shadow; {@code fill} and {@code border} come from the palette. */
    static CardPanel card(LayoutManager layout, Color fill, Color border, int borderWidth) {
        return new CardPanel(layout, fill, border, borderWidth);
    }

    /**
     * A label that wraps its text at {@code width} pixels. The text is escaped, so names can't
     * inject HTML. Use {@link Fonts#REGULAR} or {@link Fonts#SEMIBOLD} here: Swing's HTML finds
     * fonts by family, and Inter Bold shares its family name with Inter Regular, so it would
     * show as Regular.
     */
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

    /**
     * Draws a small rounded chip with text, for example "band 3 · high" or "YOUR TURN", and
     * returns its width.
     */
    static int chip(Graphics2D g, String text, int x, int y, Color fill, Color textColour, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int w = metrics.stringWidth(text) + 16;
        int h = metrics.getHeight() + 4;
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, h, h);
        g.setColor(textColour);
        g.drawString(text, x + 8, y + 2 + metrics.getAscent());
        return w;
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
