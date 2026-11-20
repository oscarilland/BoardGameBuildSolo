package netzerobelfast.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * A drop-down list in the dark theme (plan 008): Swing's default draws a light arrow button
 * that doesn't match. The list's colours come from {@link Palette} through {@link Ui#installDefaults}.
 */
final class DarkComboBox {

    private DarkComboBox() {}

    static <T> JComboBox<T> style(JComboBox<T> combo) {
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = Ui.smooth(graphics);
                        g.setColor(Palette.RAISED);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Palette.ACCENT);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        g.fill(new Polygon(new int[] {cx - 5, cx + 5, cx}, new int[] {cy - 2, cy - 2, cy + 4}, 3));
                        g.dispose();
                    }
                };
                arrow.setBorder(BorderFactory.createEmptyBorder());
                arrow.setFocusable(false);
                return arrow;
            }
        });
        combo.setBackground(Palette.RAISED);
        combo.setForeground(Palette.TEXT);
        combo.setFont(Ui.BODY);
        combo.setBorder(BorderFactory.createLineBorder(Palette.BORDER, 1));
        return combo;
    }
}
