package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;

/**
 * The title screen: Start, How to play, Quit, over a painted night view of Belfast (plan 008):
 * a skyline with two harbour gantry cranes, and a band of light in the meter's colours.
 */
final class TitleScreen extends JPanel {

    /** The skyline's colour, darker than the page. */
    private static final Color SKYLINE = new Color(0x070C17);
    private static final Color SKY_TOP = new Color(0x13254A);

    private final GameButton start;

    TitleScreen(String tagline, Runnable onStart, Runnable onHowToPlay, Runnable onQuit) {
        super(new GridBagLayout());
        setOpaque(true);
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel kicker = Ui.label("A CLIMATE ACTION BOARD GAME", Ui.SMALL_BOLD, Palette.ACCENT);
        JLabel title = new JLabel("Net Zero Belfast") {
            @Override
            protected void paintComponent(Graphics graphics) {
                // A soft glow behind the title.
                Graphics2D g = Ui.smooth(graphics);
                g.setFont(getFont());
                FontMetrics metrics = g.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = metrics.getAscent();
                for (int i = 6; i > 0; i -= 2) {
                    g.setColor(Palette.alpha(Palette.ACCENT, 14));
                    g.drawString(getText(), x - i / 2, y);
                    g.drawString(getText(), x + i / 2, y);
                }
                g.setColor(Palette.TEXT);
                g.drawString(getText(), x, y);
                g.dispose();
            }
        };
        title.setFont(Fonts.BOLD.deriveFont(64f));
        title.setForeground(Palette.TEXT);
        JLabel subtitle = Ui.label(tagline, Fonts.REGULAR.deriveFont(17f), Palette.MUTED_TEXT);
        for (JLabel label : new JLabel[] {kicker, title, subtitle}) {
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        column.add(kicker);
        column.add(Box.createVerticalStrut(6));
        column.add(title);
        column.add(Box.createVerticalStrut(8));
        column.add(subtitle);
        column.add(Box.createVerticalStrut(34));

        start = Ui.primary("Start a game", onStart);
        start.setMnemonic(KeyEvent.VK_S);
        GameButton howTo = Ui.button("How to play", onHowToPlay);
        howTo.setMnemonic(KeyEvent.VK_H);
        GameButton quit = Ui.button("Quit", onQuit);
        quit.setMnemonic(KeyEvent.VK_Q);
        for (GameButton button : new GameButton[] {start, howTo, quit}) {
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            Dimension size = new Dimension(260, 48);
            button.setPreferredSize(size);
            button.setMaximumSize(size);
            column.add(button);
            column.add(Box.createVerticalStrut(12));
        }
        column.setBorder(BorderFactory.createEmptyBorder(24, 24, 120, 24));
        add(column);
    }

    /** Puts the keyboard focus on Start, so Enter starts a game. */
    void focusStart() {
        start.requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Ui.smooth(graphics);
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, Palette.BACKGROUND, 0, h, SKY_TOP));
        g.fillRect(0, 0, w, h);

        // Stars at fixed places from a simple formula, so the sky looks the same every time.
        for (int i = 0; i < 70; i++) {
            int x = (int) ((i * 7919L) % Math.max(1, w));
            int y = (int) ((i * 104729L) % Math.max(1, (int) (h * 0.55)));
            g.setColor(Palette.alpha(Palette.TEXT, 40 + (i * 37) % 90));
            int s = i % 7 == 0 ? 3 : 2;
            g.fillOval(x, y, s, s);
        }

        // A band of light along the horizon, in the meter's colours.
        int horizon = (int) (h * 0.82);
        Color[] bands = Palette.BANDS.toArray(new Color[0]);
        float[] stops = new float[bands.length];
        for (int i = 0; i < bands.length; i++) {
            stops[i] = i / (float) (bands.length - 1);
            bands[i] = Palette.alpha(bands[i], 70);
        }
        g.setPaint(new LinearGradientPaint(0, 0, Math.max(1, w), 0, stops, bands));
        g.fillRect(0, horizon - 30, w, 34);

        // The skyline: blocks of varied width and height.
        g.setColor(SKYLINE);
        Path2D city = new Path2D.Double();
        city.moveTo(0, h);
        double x = 0;
        int i = 0;
        while (x < w) {
            double bw = 30 + (i * 53) % 70;
            double bh = 30 + (i * 89) % 110;
            city.lineTo(x, horizon - bh);
            city.lineTo(x + bw, horizon - bh);
            x += bw;
            i++;
        }
        city.lineTo(w, h);
        city.closePath();
        g.fill(city);
        crane(g, (int) (w * 0.70), horizon, (int) (h * 0.26));
        crane(g, (int) (w * 0.82), horizon, (int) (h * 0.23));
        // Lit windows.
        for (int k = 0; k < 120; k++) {
            int wx = (int) ((k * 3571L) % Math.max(1, w));
            int wy = horizon - 12 - (int) ((k * 2129L) % 90);
            g.setColor(Palette.alpha(Palette.CLIMATE_CARD_BAND, 60 + (k * 13) % 80));
            g.fillRect(wx, wy, 3, 4);
        }
        g.setColor(SKYLINE);
        g.fillRect(0, horizon, w, h - horizon);
        g.dispose();
    }

    /** A gantry crane: two legs and a beam, in the cranes' yellow. */
    private static void crane(Graphics2D g, int x, int ground, int height) {
        g.setColor(Palette.alpha(Palette.CLIMATE_CARD_BAND, 190));
        int span = height / 2;
        g.fillRect(x, ground - height, 10, height);
        g.fillRect(x + span, ground - height, 10, height);
        g.fillRect(x - 10, ground - height - 12, span + 30, 16);
    }
}
