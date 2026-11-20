package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

/**
 * A rounded card with a soft drop shadow (plan 008), used for player cards, pop-ups and form
 * rows. It paints its own background, so its children should be transparent.
 */
class CardPanel extends JPanel {

    static final int ARC = 18;
    /** Room left round the card for its shadow. */
    static final int SHADOW = 6;

    private Color fill;
    private Color border;
    private int borderWidth;
    private Color glow;

    CardPanel(LayoutManager layout, Color fill, Color border, int borderWidth) {
        super(layout);
        this.fill = fill;
        this.border = border;
        this.borderWidth = borderWidth;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10 + borderWidth, 14 + borderWidth, 10 + borderWidth + SHADOW,
                14 + borderWidth + SHADOW / 2));
    }

    /** Sets a coloured glow round the card, or null for none (the current player's card). */
    void glow(Color colour) {
        this.glow = colour;
        repaint();
    }

    void colours(Color fill, Color border, int borderWidth) {
        this.fill = fill;
        this.border = border;
        this.borderWidth = borderWidth;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Ui.smooth(graphics);
        int w = getWidth() - SHADOW / 2 - 1;
        int h = getHeight() - SHADOW - 1;
        // The shadow: a few offset, fading rounded rectangles.
        for (int i = SHADOW; i > 0; i -= 2) {
            g.setColor(Palette.alpha(Color.BLACK, 26));
            g.fillRoundRect(SHADOW / 4, i, w, h, ARC + i, ARC + i);
        }
        if (glow != null) {
            for (int i = 3; i > 0; i--) {
                g.setColor(Palette.alpha(glow, 30 * i));
                g.setStroke(new BasicStroke(i * 2f));
                g.drawRoundRect(0, 0, w, h, ARC, ARC);
            }
        }
        g.setColor(fill);
        g.fillRoundRect(0, 0, w, h, ARC, ARC);
        if (border != null && borderWidth > 0) {
            g.setColor(border);
            g.setStroke(new BasicStroke(borderWidth));
            g.drawRoundRect(borderWidth / 2, borderWidth / 2, w - borderWidth, h - borderWidth, ARC, ARC);
        }
        g.dispose();
    }
}
