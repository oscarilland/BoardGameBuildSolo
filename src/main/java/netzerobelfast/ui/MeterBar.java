package netzerobelfast.ui;

import netzerobelfast.config.Rules;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * The Climate Meter as a rounded bar coloured by danger band, with a glowing marker at the
 * value and a dashed line at the next target (FR-15 AC-1, AC-2). The value and band are
 * written beside it as a chip, so colour is never the only signal (NFR-05 AC-3). The bands'
 * limits come from the data. The marker can be drawn between two values while it eases.
 *
 * <p>A {@code JPanel}, not a bare {@code JComponent}, for its accessibility context.
 */
final class MeterBar extends JPanel {

    /** Room above the highest band limit, so the top band has some width on the bar. */
    private static final int TOP_BAND_ROOM = 20;
    private static final int CHIP_ROOM = 190;

    private final Rules.Meter rules;
    private double shown;
    private int value;
    private int band;
    private String valueText = "";
    private StatusText.Target target;

    MeterBar(Rules.Meter rules) {
        this.rules = rules;
        this.shown = rules.start();
        this.value = rules.start();
        setOpaque(false);
        setPreferredSize(new Dimension(560, 54));
    }

    void show(int value, int band, String valueText, StatusText.Target target) {
        this.value = value;
        this.band = band;
        this.valueText = valueText;
        this.target = target;
        getAccessibleContext().setAccessibleName("Climate Meter " + valueText + ", " + target.text());
        repaint();
    }

    /** Where the marker is drawn; an animation moves it towards {@link #value()}. */
    void marker(double at) {
        shown = at;
        repaint();
    }

    double marker() {
        return shown;
    }

    int value() {
        return value;
    }

    private int low() {
        return (int) Math.min(0, Math.min(value, Math.floor(shown)));
    }

    private int high() {
        int top = rules.start();
        for (Rules.DangerBand b : rules.dangerBands()) {
            if (b.maxMeter() != null) {
                top = Math.max(top, b.maxMeter());
            }
        }
        return (int) Math.max(top + TOP_BAND_ROOM, Math.max(value, Math.ceil(shown)));
    }

    private float x(double meter, int width) {
        return (float) ((meter - low()) / (high() - low()) * (width - 1));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Ui.smooth(graphics);
        int width = getWidth() - CHIP_ROOM;
        int barTop = 14;
        int barHeight = 14;

        // The bands as hard-edged colour stops along a rounded track.
        List<Rules.DangerBand> bands = rules.dangerBands();
        float[] stops = new float[bands.size() * 2];
        Color[] colours = new Color[bands.size() * 2];
        double from = low();
        for (int i = 0; i < bands.size(); i++) {
            double to = bands.get(i).maxMeter() == null ? high() : bands.get(i).maxMeter();
            stops[i * 2] = (float) ((from - low()) / (high() - low()));
            stops[i * 2 + 1] = (float) ((to - low()) / (high() - low()));
            colours[i * 2] = Palette.band(bands.get(i).band());
            colours[i * 2 + 1] = Palette.band(bands.get(i).band());
            from = to;
        }
        // Gradient stops must run from 0 to 1 and strictly increase; nudge any repeats.
        stops[0] = 0f;
        for (int i = 1; i < stops.length; i++) {
            stops[i] = Math.max(stops[i], stops[i - 1] + 0.0001f);
        }
        float last = stops[stops.length - 1];
        for (int i = 0; i < stops.length; i++) {
            stops[i] = stops[i] / last;
        }
        g.setColor(Palette.SURFACE);
        g.fill(new RoundRectangle2D.Float(-2, barTop - 2, width + 4, barHeight + 4, barHeight + 4, barHeight + 4));
        g.setPaint(new LinearGradientPaint(0, 0, Math.max(1, width), 0, stops, colours));
        g.fill(new RoundRectangle2D.Float(0, barTop, width, barHeight, barHeight, barHeight));

        g.setFont(Ui.SMALL_BOLD);
        FontMetrics metrics = g.getFontMetrics();

        // The next target: a dashed line and its label under the bar.
        if (target != null) {
            float tx = x(target.value(), width);
            g.setColor(Palette.TEXT);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] {3, 3}, 0));
            g.drawLine((int) tx, barTop - 5, (int) tx, barTop + barHeight + 4);
            String text = "▲ " + target.text();
            int left = (int) Math.max(0, Math.min(width - metrics.stringWidth(text), tx - metrics.stringWidth(text) / 2f));
            g.setColor(Palette.MUTED_TEXT);
            g.drawString(text, left, barTop + barHeight + 6 + metrics.getAscent());
        }

        // The marker: a glowing dot on the bar.
        float vx = x(shown, width);
        Color dot = Palette.band(band);
        for (int i = 3; i > 0; i--) {
            g.setColor(Palette.alpha(dot, 15 * i));
            float r = 9 + i * 3;
            g.fill(new Ellipse2D.Float(vx - r, barTop + barHeight / 2f - r, r * 2, r * 2));
        }
        g.setColor(Palette.TEXT);
        g.fill(new Ellipse2D.Float(vx - 9, barTop + barHeight / 2f - 9, 18, 18));
        g.setColor(dot);
        g.fill(new Ellipse2D.Float(vx - 6, barTop + barHeight / 2f - 6, 12, 12));

        // The value and band, as a chip to the right of the bar; the value counts as the marker moves.
        if (!valueText.isEmpty()) {
            String chip = Math.round(shown) + valueText.substring(valueText.indexOf(' '));
            Ui.chip(g, chip, width + 18, barTop - 6, Palette.band(band), Palette.BACKGROUND, Fonts.BOLD.deriveFont(14f));
        }
        g.dispose();
    }
}
