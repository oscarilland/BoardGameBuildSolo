package netzerobelfast.ui;

import netzerobelfast.config.Rules;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

/**
 * The Climate Meter as a bar coloured by danger band, with a marker at the value and a dashed
 * line at the next target (FR-15 AC-1, AC-2). The value and band are written under the bar, so
 * colour is never the only signal (NFR-05 AC-3). The bands' limits come from the data.
 */
final class MeterBar extends JPanel {

    /** Room above the highest band limit, so the top band has some width on the bar. */
    private static final int TOP_BAND_ROOM = 20;

    private final Rules.Meter rules;
    private int value;
    private String valueText = "";
    private StatusText.Target target;

    MeterBar(Rules.Meter rules) {
        this.rules = rules;
        setOpaque(false);
        setPreferredSize(new Dimension(520, 66));
        setFont(Ui.SMALL.deriveFont(java.awt.Font.BOLD, 13f));
    }

    void show(int value, String valueText, StatusText.Target target) {
        this.value = value;
        this.valueText = valueText;
        this.target = target;
        getAccessibleContext().setAccessibleName("Climate Meter " + valueText + ", " + target.text());
        repaint();
    }

    /** The bar's range: from 0 (or the value, if below 0) to above the top band's lower limit. */
    private int low() {
        return Math.min(0, value);
    }

    private int high() {
        int top = rules.start();
        for (Rules.DangerBand band : rules.dangerBands()) {
            if (band.maxMeter() != null) {
                top = Math.max(top, band.maxMeter());
            }
        }
        return Math.max(top + TOP_BAND_ROOM, value);
    }

    private int x(int meter, int width) {
        return (int) Math.round((meter - low()) / (double) (high() - low()) * (width - 1));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int width = getWidth();
        int barTop = 10;
        int barHeight = 18;

        // Band segments, from the bottom of the range to each band's upper limit.
        int from = low();
        for (Rules.DangerBand band : rules.dangerBands()) {
            int to = band.maxMeter() == null ? high() : band.maxMeter();
            g.setColor(Palette.band(band.band()));
            g.fillRect(x(from, width), barTop, Math.max(1, x(to, width) - x(from, width)), barHeight);
            from = to;
        }
        g.setColor(Palette.HEADER_TEXT);
        g.drawRect(0, barTop, width - 1, barHeight);

        FontMetrics metrics = g.getFontMetrics();
        int textY = barTop + barHeight + metrics.getAscent() + 4;

        // The next target: a dashed line, labelled under the bar.
        if (target != null) {
            int tx = x(target.value(), width);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] {4, 3}, 0));
            g.drawLine(tx, barTop - 4, tx, barTop + barHeight + 2);
            String text = target.text();
            int left = Math.max(0, Math.min(width - metrics.stringWidth(text), tx - metrics.stringWidth(text) / 2));
            g.drawString(text, left, textY + metrics.getHeight());
        }

        // The value: a marker above the bar, and the value and band in words.
        int vx = x(value, width);
        g.setStroke(new BasicStroke(1));
        g.fillPolygon(new Polygon(new int[] {vx - 6, vx + 6, vx}, new int[] {0, 0, barTop + 4}, 3));
        int left = Math.max(0, Math.min(width - metrics.stringWidth(valueText), vx - metrics.stringWidth(valueText) / 2));
        g.drawString(valueText, left, textY);
        g.dispose();
    }
}
