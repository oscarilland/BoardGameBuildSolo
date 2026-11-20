package netzerobelfast.ui;

import netzerobelfast.config.Rules;
import netzerobelfast.ui.HowToPlay.Page;
import netzerobelfast.ui.HowToPlay.Picture;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * "How to play": the five tutorial pages from {@link HowToPlay}, each with a simple picture
 * (NFR-02 AC-1). Left and right arrow keys turn the pages.
 */
final class HowToPlayScreen extends JPanel {

    private static final int TEXT_WIDTH = 620;

    private final List<Page> pages;
    private final Rules rules;
    private final CardPanel body = new CardPanel(null, Palette.SURFACE, Palette.BORDER, 1);
    private final JLabel pageNumber = Ui.label("", Ui.BODY, Palette.MUTED_TEXT);
    private final JButton back;
    private final JButton next;
    private int page;

    HowToPlayScreen(Rules rules, Runnable onDone) {
        super(new BorderLayout());
        this.rules = rules;
        this.pages = HowToPlay.pages(rules);
        setBackground(Palette.BACKGROUND);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        centre.add(body);
        add(centre, BorderLayout.CENTER);

        back = Ui.button("◀ Back", () -> turn(-1));
        next = Ui.primary("Next ▶", () -> turn(1));
        JButton done = Ui.button("Back to the title", onDone);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        row.setOpaque(false);
        row.add(back);
        row.add(pageNumber);
        row.add(next);
        row.add(done);
        add(row, BorderLayout.SOUTH);

        Ui.bindKey(this, "LEFT", "previous", () -> turn(-1));
        Ui.bindKey(this, "RIGHT", "next", () -> turn(1));
        Ui.bindKey(this, "ESCAPE", "done", onDone);
        show(0);
    }

    void show(int index) {
        page = Math.max(0, Math.min(pages.size() - 1, index));
        Page shown = pages.get(page);
        body.removeAll();
        JLabel step = Ui.label("HOW TO PLAY  ·  " + (page + 1) + " / " + pages.size(), Ui.SMALL_BOLD, Palette.ACCENT);
        step.setAlignmentX(LEFT_ALIGNMENT);
        body.add(step);
        body.add(Box.createVerticalStrut(6));
        JLabel heading = Ui.label(shown.title(), Ui.TITLE.deriveFont(34f), Palette.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        body.add(heading);
        body.add(Box.createVerticalStrut(16));
        PictureView picture = new PictureView(shown.picture(), rules);
        picture.setAlignmentX(LEFT_ALIGNMENT);
        body.add(picture);
        body.add(Box.createVerticalStrut(16));
        for (String paragraph : shown.paragraphs()) {
            JLabel label = Ui.wrapped(paragraph, Ui.BODY.deriveFont(18f), Palette.TEXT, TEXT_WIDTH);
            label.setAlignmentX(LEFT_ALIGNMENT);
            body.add(label);
            body.add(Box.createVerticalStrut(10));
        }
        body.setBorder(BorderFactory.createEmptyBorder(28, 34, 28 + CardPanel.SHADOW, 34 + CardPanel.SHADOW / 2));
        pageNumber.setText("Page " + (page + 1) + " of " + pages.size());
        back.setEnabled(page > 0);
        next.setEnabled(page < pages.size() - 1);
        body.revalidate();
        body.repaint();
        (next.isEnabled() ? next : back).requestFocusInWindow();
    }

    private void turn(int by) {
        show(page + by);
    }

    /** The page's picture, drawn from the palette. Labels sit beside the colours, never on them. */
    private static final class PictureView extends JPanel {
        private final Picture picture;
        private final Rules rules;

        PictureView(Picture picture, Rules rules) {
            this.picture = picture;
            this.rules = rules;
            Dimension size = new Dimension(TEXT_WIDTH, 90);
            setPreferredSize(size);
            setMaximumSize(size);
            setOpaque(false);
            setFont(Ui.BODY);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Ui.smooth(graphics);
            g.setFont(Ui.BODY);
            FontMetrics metrics = g.getFontMetrics();
            switch (picture) {
                case METER -> {
                    List<Rules.DangerBand> bands = rules.meter().dangerBands();
                    int width = TEXT_WIDTH / bands.size();
                    g.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 10, width * bands.size(), 22, 22, 22));
                    for (int i = 0; i < bands.size(); i++) {
                        g.setColor(Palette.band(bands.get(i).band()));
                        g.fillRect(i * width, 10, width, 22);
                    }
                    g.setClip(null);
                    g.setColor(Palette.TEXT);
                    for (int i = 0; i < bands.size(); i++) {
                        g.drawString(StatusText.bandText(bands.get(i).band()), i * width + 4, 54);
                    }
                    g.drawString("Start: " + rules.meter().start() + " · Net zero: " + rules.meter().netZeroAtOrBelow()
                            + " or below", 0, 84);
                }
                case DICE -> {
                    die(g, 0, 3);
                    die(g, 80, 4);
                    g.setColor(Palette.TEXT);
                    g.setFont(Fonts.BOLD.deriveFont(18f));
                    g.drawString("3 + 4 = move 7 squares", 170, 48);
                }
                case SQUARES -> {
                    String[] labels = {"Polluting", "Green", "Nature", "Resilience", "Glider", "Card"};
                    java.awt.Color[] fills = {Palette.POLLUTING, Palette.GREEN, Palette.NATURE, Palette.RESILIENCE,
                            Palette.GLIDER, Palette.CLIMATE_CARD};
                    netzerobelfast.config.BoardDef.SquareType[] types = {
                            netzerobelfast.config.BoardDef.SquareType.POLLUTING, netzerobelfast.config.BoardDef.SquareType.GREEN_PROJECT,
                            netzerobelfast.config.BoardDef.SquareType.GREEN_PROJECT, netzerobelfast.config.BoardDef.SquareType.RESILIENCE,
                            netzerobelfast.config.BoardDef.SquareType.GLIDER_STOP, netzerobelfast.config.BoardDef.SquareType.CLIMATE_CARD};
                    for (int i = 0; i < labels.length; i++) {
                        int x = i * 102;
                        boolean nature = i == 2;
                        java.awt.Color band = Palette.squareBand(types[i], nature);
                        g.setColor(fills[i]);
                        g.fillRoundRect(x, 6, 94, 64, 14, 14);
                        g.setColor(band);
                        g.fillRoundRect(x, 6, 94, 6, 6, 6);
                        g.drawRoundRect(x, 6, 94, 64, 14, 14);
                        Icons.draw(g, Icons.forSquare(types[i], nature), x + 8, 20, 18, band);
                        g.setColor(Palette.TEXT);
                        g.setFont(Ui.SMALL_BOLD);
                        g.drawString(labels[i], x + 8, 58);
                    }
                }
                case BANDS -> {
                    List<Rules.DangerBand> bands = rules.meter().dangerBands();
                    int width = TEXT_WIDTH / bands.size();
                    for (int i = 0; i < bands.size(); i++) {
                        int band = bands.get(i).band();
                        g.setColor(Palette.band(band));
                        g.fillOval(i * width, 10, 28, 28);
                        g.setColor(Palette.TEXT);
                        g.drawOval(i * width, 10, 28, 28);
                        g.drawString("band " + band, i * width + 34, 22);
                        g.drawString("roll " + band + " or less", i * width + 34, 40);
                    }
                    g.drawString("Each round one die is rolled. Roll the band or less, and a disaster strikes.", 0, 70);
                }
                case SCORE -> {
                    box(g, metrics, 0, "Net zero: GP + money ÷ " + rules.scoring().moneyDivisor(), Palette.GREEN);
                    box(g, metrics, TEXT_WIDTH / 2 + 10, "Missed: GP − " + rules.scoring().missedPollutingPenaltyGP()
                            + " per Polluting", Palette.POLLUTING);
                }
            }
            g.dispose();
        }

        private static void die(Graphics2D g, int x, int pips) {
            g.setColor(Palette.TEXT);
            g.fillRoundRect(x, 10, 64, 64, 16, 16);
            g.setColor(Palette.BACKGROUND);
            int[][] spots = pips == 3 ? new int[][] {{16, 16}, {32, 32}, {48, 48}}
                    : new int[][] {{18, 18}, {46, 18}, {18, 46}, {46, 46}};
            for (int[] spot : spots) {
                g.fillOval(x + spot[0] - 5, 10 + spot[1] - 5, 10, 10);
            }
        }

        private static void box(Graphics2D g, FontMetrics metrics, int x, String text, java.awt.Color fill) {
            int width = TEXT_WIDTH / 2 - 10;
            g.setColor(fill);
            g.fillRoundRect(x, 10, width, 60, 16, 16);
            g.setColor(Palette.BORDER);
            g.drawRoundRect(x, 10, width, 60, 16, 16);
            g.setColor(Palette.TEXT);
            g.drawString(text, x + (width - metrics.stringWidth(text)) / 2, 45);
        }
    }
}
