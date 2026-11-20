package netzerobelfast.ui;

import netzerobelfast.config.BoardDef.SquareType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Small vector icons drawn with Java2D (plan 008), so there are no image files to license.
 * They always sit beside a text label, never instead of one (NFR-05 AC-2).
 */
final class Icons {

    enum Icon { FACTORY, LEAF, TREE, SHIELD, TRAM, CARD, COIN, STAR }

    private Icons() {}

    /** The icon for a square type. */
    static Icon forSquare(SquareType type, boolean nature) {
        return switch (type) {
            case POLLUTING -> Icon.FACTORY;
            case GREEN_PROJECT -> nature ? Icon.TREE : Icon.LEAF;
            case RESILIENCE -> Icon.SHIELD;
            case GLIDER_STOP -> Icon.TRAM;
            case CLIMATE_CARD -> Icon.CARD;
        };
    }

    /** Draws an icon in a {@code size} × {@code size} box at (x, y). */
    static void draw(Graphics2D g, Icon icon, double x, double y, double size, Color colour) {
        Graphics2D d = (Graphics2D) g.create();
        d.translate(x, y);
        d.scale(size / 16.0, size / 16.0);
        d.setColor(colour);
        d.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case FACTORY -> {
                Path2D p = new Path2D.Double();
                p.moveTo(1, 15);
                p.lineTo(1, 7);
                p.lineTo(5, 9.5);
                p.lineTo(5, 7);
                p.lineTo(9, 9.5);
                p.lineTo(9, 3);
                p.lineTo(12, 3);
                p.lineTo(12, 15);
                p.closePath();
                d.fill(p);
                d.fill(new Ellipse2D.Double(11, 0, 3, 2.4));
                d.fill(new Ellipse2D.Double(13, -1.5, 2.5, 2));
            }
            case LEAF -> {
                Path2D p = new Path2D.Double();
                p.moveTo(2, 14);
                p.curveTo(1, 6, 7, 1, 15, 1);
                p.curveTo(15, 9, 10, 15, 2, 14);
                p.closePath();
                d.fill(p);
                d.setColor(Palette.alpha(Color.BLACK, 90));
                d.draw(new java.awt.geom.Line2D.Double(3, 13, 11, 5));
            }
            case TREE -> {
                d.fill(new Ellipse2D.Double(2, 1, 12, 10));
                d.fill(new RoundRectangle2D.Double(7, 9, 2.4, 6.5, 1, 1));
            }
            case SHIELD -> {
                Path2D p = new Path2D.Double();
                p.moveTo(8, 0.5);
                p.lineTo(14.5, 3);
                p.curveTo(14.5, 10, 11, 13.5, 8, 15.5);
                p.curveTo(5, 13.5, 1.5, 10, 1.5, 3);
                p.closePath();
                d.fill(p);
            }
            case TRAM -> {
                d.fill(new RoundRectangle2D.Double(2, 2, 12, 10, 4, 4));
                d.setColor(Palette.alpha(Color.BLACK, 120));
                d.fill(new RoundRectangle2D.Double(4, 4, 8, 3.5, 1.5, 1.5));
                d.setColor(colour);
                d.fill(new Ellipse2D.Double(3.5, 12.5, 3, 3));
                d.fill(new Ellipse2D.Double(9.5, 12.5, 3, 3));
            }
            case CARD -> {
                d.rotate(Math.toRadians(-10), 8, 8);
                d.fill(new RoundRectangle2D.Double(3, 1, 10, 14, 3, 3));
                d.setColor(Palette.alpha(Color.BLACK, 140));
                d.setFont(Fonts.BOLD.deriveFont(10f));
                d.drawString("?", 5.6f, 12f);
            }
            case COIN -> {
                d.fill(new Ellipse2D.Double(1, 1, 14, 14));
                d.setColor(Palette.alpha(Color.BLACK, 140));
                d.setFont(Fonts.BOLD.deriveFont(10f));
                d.drawString("£", 4.6f, 12f);
            }
            case STAR -> {
                Path2D p = new Path2D.Double();
                for (int i = 0; i < 10; i++) {
                    double r = i % 2 == 0 ? 7.5 : 3.2;
                    double a = Math.toRadians(-90 + i * 36);
                    double px = 8 + r * Math.cos(a);
                    double py = 8.5 + r * Math.sin(a);
                    if (i == 0) {
                        p.moveTo(px, py);
                    } else {
                        p.lineTo(px, py);
                    }
                }
                p.closePath();
                d.fill(p);
            }
        }
        d.dispose();
    }
}
