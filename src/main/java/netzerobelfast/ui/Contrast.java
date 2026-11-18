package netzerobelfast.ui;

import java.awt.Color;

/**
 * The WCAG 2.1 contrast ratio between two colours (NFR-05 AC-1).
 *
 * <p>Each colour's relative luminance is worked out from its red, green and blue channels, and
 * the ratio is (lighter + 0.05) / (darker + 0.05): 1 for two identical colours, 21 for black
 * on white. Normal text needs at least 4.5, large text at least 3.
 */
public final class Contrast {

    /** WCAG 2.1 AA minimum for normal text. */
    public static final double NORMAL_TEXT = 4.5;

    /** WCAG 2.1 AA minimum for large text (18pt, or 14pt bold). */
    public static final double LARGE_TEXT = 3.0;

    private Contrast() {}

    public static double ratio(Color a, Color b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    /** Relative luminance, as defined by WCAG 2.1, from 0 (black) to 1 (white). */
    static double luminance(Color color) {
        return 0.2126 * channel(color.getRed()) + 0.7152 * channel(color.getGreen()) + 0.0722 * channel(color.getBlue());
    }

    /** Turns a 0-255 sRGB channel into a linear light value, using the WCAG formula. */
    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
