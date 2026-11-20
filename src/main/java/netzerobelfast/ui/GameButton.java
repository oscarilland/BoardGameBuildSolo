package netzerobelfast.ui;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A pill-shaped button painted by the game itself (plan 008): hover, pressed and disabled
 * states and a thick amber focus ring, all in {@link Palette} colours, so every state's text is
 * contrast-tested. Enter presses it as well as Space (NFR-05 AC-4). Hover needs no timer: it
 * repaints on the mouse events.
 *
 * <p>Long text is wrapped by Swing's HTML when the label starts with {@code <html>}; the
 * painted background and ring still apply.
 */
final class GameButton extends JButton {

    /** The main action on a screen uses the accent colour; the rest are quieter. */
    enum Kind { PRIMARY, SECONDARY }

    private static final int ARC = 999;
    private final Kind kind;
    private boolean hover;
    private boolean focused;

    GameButton(String text, Kind kind, Runnable onPress) {
        super(text);
        this.kind = kind;
        setFont(Ui.BOLD);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setMargin(new Insets(10, 22, 10, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateColours();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                updateColours();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                updateColours();
                repaint();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("released ENTER"), "released");
        addActionListener(e -> onPress.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateColours();
    }

    /** The text colour for the current state; the background is painted in {@link #paintComponent}. */
    private void updateColours() {
        setForeground(!isEnabled() ? Palette.BUTTON_DISABLED_TEXT
                : kind == Kind.PRIMARY ? Palette.ACCENT_TEXT : Palette.BUTTON_TEXT);
    }

    private Color fill() {
        if (!isEnabled()) {
            return Palette.BUTTON_DISABLED;
        }
        boolean lit = hover || getModel().isPressed();
        return kind == Kind.PRIMARY ? (lit ? Palette.ACCENT_HOVER : Palette.ACCENT)
                : (lit ? Palette.BUTTON_HOVER : Palette.BUTTON);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width + 8, Math.max(size.height + 4, 40));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Ui.smooth(graphics);
        int w = getWidth();
        int h = getHeight();
        int arc = Math.min(ARC, h);
        if (isEnabled()) {
            g.setColor(Palette.SHADOW);
            g.fillRoundRect(2, 4, w - 4, h - 5, arc, arc);
        }
        g.setColor(fill());
        g.fillRoundRect(2, 2, w - 4, h - 5, arc, arc);
        if (kind == Kind.PRIMARY && isEnabled()) {
            // A soft glow along the top edge, so the main action stands out.
            g.setColor(Palette.alpha(Color.WHITE, 40));
            g.fillRoundRect(4, 3, w - 8, (h - 5) / 2, arc, arc);
        }
        if (focused) {
            g.setColor(Palette.FOCUS);
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(2, 2, w - 5, h - 6, arc, arc);
        }
        g.dispose();
        super.paintComponent(graphics);
    }

}
