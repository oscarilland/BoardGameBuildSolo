package netzerobelfast.ui;

import netzerobelfast.config.BoardDef;
import netzerobelfast.config.BoardDef.SquareDef;
import netzerobelfast.model.Game;
import netzerobelfast.model.OwnableSquare;
import netzerobelfast.model.Player;
import netzerobelfast.model.Square;
import netzerobelfast.ui.BoardLayout.Cell;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The board, painted (plan 008 style): rounded tiles with their type's colour band, icon and
 * label, the owner as a numbered badge in their seat colour, and shaded counters with seat
 * numbers (NFR-05 AC-2: readable without colour). A tooltip gives each square's full details,
 * and the tile under the mouse is highlighted.
 *
 * <p>The tiles change only when the game does, so they're painted once into an image and
 * reused; an animation frame only redraws the counters and dice on top.
 *
 * <p>A {@code JPanel} rather than a bare {@code JComponent}, because only the panel has an
 * accessibility context for screen readers (a bare component's is null).
 */
final class BoardPanel extends JPanel {

    private static final int GAP = 6;
    private static final int ARC = 14;
    private static final Font NAME = Fonts.SEMIBOLD.deriveFont(12f);
    private static final Font DETAIL = Fonts.REGULAR.deriveFont(11f);
    private static final Font DETAIL_BOLD = Fonts.SEMIBOLD.deriveFont(11f);
    private static final Font COUNTER = Fonts.BOLD.deriveFont(11f);

    private final BoardDef board;
    private final BoardLayout layout;
    private final StatusText text;
    private Game game;
    private BufferedImage tiles;
    private int hovered = -1;

    // Animation state, drawn over the tiles.
    private Player sliding;
    private List<Integer> slidePath;
    private double slideProgress;
    private int diceTotal;
    private double diceProgress = -1;

    BoardPanel(BoardDef board, StatusText text) {
        this.board = board;
        this.layout = new BoardLayout(board);
        this.text = text;
        setOpaque(true);
        setBackground(Palette.BACKGROUND);
        setPreferredSize(new Dimension(920, 520));
        setToolTipText("");
        getAccessibleContext().setAccessibleName("The board");
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int square = squareAt(e.getPoint());
                if (square != hovered) {
                    hovered = square;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = -1;
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Shows the game as it is now; the tiles are repainted once. */
    void show(Game game) {
        this.game = game;
        tiles = null;
        repaint();
    }

    /** Draws {@code player}'s counter part-way along {@code path} (square ids, start first); progress 0 to 1. */
    void slide(Player player, List<Integer> path, double progress) {
        sliding = player;
        slidePath = path;
        slideProgress = progress;
        repaint();
    }

    void endSlide() {
        sliding = null;
        slidePath = null;
        repaint();
    }

    /** Draws the dice tumbling (progress 0 to 1), then showing the total. Progress below 0 hides them. */
    void dice(int total, double progress) {
        diceTotal = total;
        diceProgress = progress;
        repaint();
    }

    private Rectangle bounds(int square) {
        Cell cell = layout.cell(square);
        double w = getWidth() / (double) layout.columns();
        double h = getHeight() / (double) layout.rows();
        return new Rectangle((int) (cell.column() * w) + GAP / 2, (int) (cell.row() * h) + GAP / 2,
                (int) w - GAP, (int) h - GAP);
    }

    private int squareAt(Point point) {
        for (SquareDef def : board.squares()) {
            if (bounds(def.id()).contains(point)) {
                return def.id();
            }
        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (game == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        if (tiles == null || tiles.getWidth() != getWidth() || tiles.getHeight() != getHeight()) {
            tiles = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D t = Ui.smooth(tiles.getGraphics());
            paintTiles(t);
            t.dispose();
        }
        Graphics2D g = Ui.smooth(graphics);
        g.drawImage(tiles, 0, 0, null);
        if (hovered >= 0) {
            Rectangle r = bounds(hovered);
            g.setColor(Palette.alpha(Palette.TEXT, 140));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(r.x, r.y, r.width, r.height, ARC, ARC);
        }
        paintCounters(g);
        paintDice(g);
        g.dispose();
    }

    // ---- Tiles -----------------------------------------------------------------

    private void paintTiles(Graphics2D g) {
        paintCentre(g);
        paintCityCentreLinks(g);
        int here = game.phase() == Game.Phase.GAME_OVER ? -1 : game.currentPlayer().position();
        for (SquareDef def : board.squares()) {
            paintSquare(g, def, game.board().square(def.id()), def.id() == here);
        }
    }

    /** The open middle of the board: a soft glow and the game's name. */
    private void paintCentre(Graphics2D g) {
        Rectangle topLeft = bounds(board.loop().get(0));
        int x = topLeft.x + topLeft.width + GAP;
        int y = topLeft.y + topLeft.height + GAP;
        int w = getWidth() - 2 * x;
        int h = getHeight() - 2 * y;
        g.setPaint(new GradientPaint(x, y, Palette.SURFACE, x, y + h, Palette.BACKGROUND));
        g.fillRoundRect(x, y, w, h, 28, 28);
        g.setColor(Palette.alpha(Palette.ACCENT, 30));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 28, 28);
        g.setFont(Fonts.BOLD.deriveFont(Math.max(14f, h / 9f)));
        FontMetrics metrics = g.getFontMetrics();
        String name = "NET ZERO BELFAST";
        g.setColor(Palette.alpha(Palette.TEXT, 28));
        g.drawString(name, x + (w - metrics.stringWidth(name)) / 2, y + h - h / 8);
    }

    /** Dashed lines from each junction to the end of the City Centre route next to it, and the route's label. */
    private void paintCityCentreLinks(Graphics2D g) {
        List<Integer> route = board.cityCentreRoute();
        g.setColor(Palette.alpha(Palette.ACCENT, 150));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[] {2, 8}, 0));
        link(g, board.westJunction(), route.get(0));
        link(g, board.eastJunction(), route.get(route.size() - 1));
        g.setStroke(new BasicStroke(1));
        Rectangle first = bounds(route.get(0));
        Rectangle last = bounds(route.get(route.size() - 1));
        g.setFont(Ui.SMALL_BOLD);
        String label = "⇄  CITY CENTRE CUT-THROUGH  ·  EITHER WAY";
        FontMetrics metrics = g.getFontMetrics();
        int centre = (first.x + last.x + last.width) / 2;
        g.setColor(Palette.MUTED_TEXT);
        g.drawString(label, centre - metrics.stringWidth(label) / 2, first.y - 8);
    }

    private void link(Graphics2D g, int from, int to) {
        Rectangle a = bounds(from);
        Rectangle b = bounds(to);
        int y = a.y + a.height / 2;
        if (a.x < b.x) {
            g.drawLine(a.x + a.width + 4, y, b.x - 4, y);
        } else {
            g.drawLine(b.x + b.width + 4, y, a.x - 4, y);
        }
    }

    private void paintSquare(Graphics2D g, SquareDef def, Square square, boolean here) {
        Rectangle r = bounds(def.id());
        Color band = Palette.squareBand(def.type(), def.nature());

        // A glow under the current player's square.
        if (here) {
            for (int i = 4; i > 0; i--) {
                g.setColor(Palette.alpha(Palette.ACCENT, 26 * i));
                g.setStroke(new BasicStroke(i * 2.5f));
                g.drawRoundRect(r.x, r.y, r.width, r.height, ARC, ARC);
            }
        }
        // Shadow, body, top band.
        g.setColor(Palette.SHADOW);
        g.fillRoundRect(r.x + 1, r.y + 3, r.width, r.height, ARC, ARC);
        g.setColor(Palette.squareFill(def.type(), def.nature()));
        g.fillRoundRect(r.x, r.y, r.width, r.height, ARC, ARC);
        Graphics2D clip = (Graphics2D) g.create();
        clip.clip(new java.awt.geom.RoundRectangle2D.Double(r.x, r.y, r.width, r.height, ARC, ARC));
        clip.setColor(band);
        clip.fillRect(r.x, r.y, r.width, 4);
        clip.setPaint(new GradientPaint(r.x, r.y, Palette.alpha(band, 40), r.x, r.y + r.height / 2f, Palette.alpha(band, 0)));
        clip.fillRect(r.x, r.y + 4, r.width, r.height / 2);
        clip.dispose();
        boolean junction = board.junctions().contains(def.id());
        g.setColor(junction ? band : Palette.alpha(band, 90));
        g.setStroke(new BasicStroke(junction ? 2.2f : 1f));
        g.drawRoundRect(r.x, r.y, r.width, r.height, ARC, ARC);

        int pad = 7;
        int width = r.width - 2 * pad;
        int iconSize = Math.max(12, Math.min(16, r.height / 5));
        Icons.draw(g, Icons.forSquare(def.type(), def.nature()), r.x + pad, r.y + pad + 2, iconSize, band);

        // On a small tile the type is shown by its icon alone (NFR-05 AC-2 allows text or a
        // distinct icon; the tooltip still says it in words), and a mark ("Closed", "Lobbied")
        // takes the name's second line, so nothing overlaps and the price always shows.
        g.setFont(NAME);
        FontMetrics nameMetrics = g.getFontMetrics();
        g.setFont(DETAIL);
        FontMetrics detail = g.getFontMetrics();
        boolean compact = r.height < 2 * pad + 2 * nameMetrics.getHeight() + 2 * detail.getHeight() + 4;
        List<String> marks = text.squareMarks(game, square);
        boolean markOnNameLine = compact && !marks.isEmpty();

        // The name, beside the icon on its first line.
        int y = r.y + pad + 2;
        g.setColor(Palette.TEXT);
        g.setFont(NAME);
        List<String> lines = fit(square.name(), nameMetrics, width - iconSize - 5, width, markOnNameLine ? 1 : 2);
        for (int i = 0; i < lines.size(); i++) {
            int x = r.x + pad + (i == 0 ? iconSize + 5 : 0);
            y += nameMetrics.getAscent();
            g.drawString(lines.get(i), x, y);
            y += nameMetrics.getDescent();
        }
        if (!marks.isEmpty()) {
            g.setColor(Palette.WARNING_TEXT);
            g.setFont(DETAIL_BOLD);
            g.drawString(clip(String.join(", ", marks), detail, width), r.x + pad, y + detail.getAscent() + 1);
        }

        // The type label, and the price or owner, at the bottom.
        g.setFont(DETAIL);
        int bottom = r.y + r.height - pad;
        String type = StatusText.typeLabel(square) + (junction ? "  ★ junction" : "");
        g.setColor(Palette.MUTED_TEXT);
        if (compact) {
            if (junction) {
                String star = "★";
                g.drawString(star, r.x + r.width - pad - detail.stringWidth(star), bottom - detail.getDescent());
                width -= detail.stringWidth(star) + 4;
            }
        } else {
            g.drawString(clip(type, detail, width), r.x + pad, bottom - detail.getHeight() - 2);
        }
        if (square instanceof OwnableSquare owned && owned.owner() != null) {
            int seat = StatusText.seat(game, owned.owner());
            int d = detail.getHeight();
            g.setColor(Palette.counter(seat));
            g.fillOval(r.x + pad, bottom - d + 1, d, d);
            g.setColor(Palette.COUNTER_TEXT);
            g.setFont(DETAIL_BOLD);
            String number = String.valueOf(seat);
            FontMetrics bold = g.getFontMetrics();
            g.drawString(number, r.x + pad + (d - bold.stringWidth(number)) / 2, bottom - d + 1 + (d + bold.getAscent() - bold.getDescent()) / 2);
            g.setColor(Palette.TEXT);
            g.setFont(DETAIL);
            g.drawString(clip("Owner " + seat, detail, width - d - 4), r.x + pad + d + 4, bottom - detail.getDescent());
        } else {
            g.setColor(Palette.TEXT);
            g.setFont(DETAIL_BOLD);
            g.drawString(clip(text.squareDetail(game, square), detail, width), r.x + pad, bottom - detail.getDescent());
        }
    }

    // ---- Counters and dice ---------------------------------------------------------

    /** Where a counter sits in a square: a slot near the right edge, up to three a row. */
    private Point2D.Double slot(int square, int index, int size) {
        Rectangle r = bounds(square);
        double x = r.x + r.width - 6 - (index % 3 + 1) * (size + 3);
        double y = r.y + r.height / 2.0 - size / 2.0 + (index / 3) * (size + 3) - 2;
        return new Point2D.Double(x, y);
    }

    private int counterSize() {
        Rectangle r = bounds(board.loop().get(0));
        return Math.max(16, Math.min(22, r.width / 5));
    }

    private void paintCounters(Graphics2D g) {
        Map<Integer, List<Player>> bySquare = new HashMap<>();
        for (Player player : game.players()) {
            if (player != sliding) {
                bySquare.computeIfAbsent(player.position(), k -> new ArrayList<>()).add(player);
            }
        }
        int size = counterSize();
        bySquare.forEach((square, players) -> {
            for (int i = 0; i < players.size(); i++) {
                Point2D.Double at = slot(square, i, size);
                counter(g, players.get(i), at.x, at.y, size);
            }
        });
        if (sliding != null && slidePath != null && !slidePath.isEmpty()) {
            // Move along the path's squares one after another, at an even pace.
            double along = slideProgress * (slidePath.size() - 1);
            int step = Math.min(slidePath.size() - 1, (int) Math.floor(along));
            int next = Math.min(slidePath.size() - 1, step + 1);
            double f = along - step;
            Point2D.Double a = slot(slidePath.get(step), 0, size);
            Point2D.Double b = slot(slidePath.get(next), 0, size);
            // A small hop on each step, so it reads as moving square by square.
            double hop = Math.sin(f * Math.PI) * size * 0.45;
            counter(g, sliding, a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f - hop, size);
        }
    }

    private void counter(Graphics2D g, Player player, double x, double y, int size) {
        int seat = StatusText.seat(game, player);
        boolean current = game.phase() != Game.Phase.GAME_OVER && player == game.currentPlayer();
        g.setColor(Palette.SHADOW);
        g.fill(new java.awt.geom.Ellipse2D.Double(x + 1, y + 3, size, size));
        Color colour = Palette.counter(seat);
        g.setPaint(new GradientPaint((float) x, (float) y, colour.brighter(), (float) x, (float) (y + size), colour.darker()));
        g.fill(new java.awt.geom.Ellipse2D.Double(x, y, size, size));
        g.setColor(current ? Palette.FOCUS : Palette.alpha(Palette.TEXT, 200));
        g.setStroke(new BasicStroke(current ? 2.5f : 1.5f));
        g.draw(new java.awt.geom.Ellipse2D.Double(x, y, size, size));
        g.setFont(COUNTER);
        FontMetrics metrics = g.getFontMetrics();
        String number = String.valueOf(seat);
        g.setColor(Palette.COUNTER_TEXT);
        g.drawString(number, (float) (x + (size - metrics.stringWidth(number)) / 2.0),
                (float) (y + (size + metrics.getAscent() - metrics.getDescent()) / 2.0));
    }

    /** Two dice tumbling in the middle of the board, then the total. */
    private void paintDice(Graphics2D g) {
        if (diceProgress < 0) {
            return;
        }
        int size = Math.max(34, Math.min(64, getHeight() / 10));
        int cx = getWidth() / 2;
        // In the open space between the top row and the City Centre route.
        int cy = (int) (getHeight() * (2.0 / 7.0)) - size / 2 - 6;
        if (diceProgress < 1) {
            // Faces change as the dice roll. These faces are only for show: the real roll is
            // already in the model, and the total is shown when the tumble ends.
            int frame = (int) (diceProgress * 14);
            double spin = (1 - diceProgress) * Math.PI * 3;
            die(g, cx - size - 8, cy, size, frame % 6 + 1, spin);
            die(g, cx + 8, cy, size, (frame * 5 + 3) % 6 + 1, -spin * 0.8);
        } else {
            String label = "Rolled " + diceTotal;
            g.setFont(Fonts.BOLD.deriveFont(size * 0.55f));
            FontMetrics metrics = g.getFontMetrics();
            int w = metrics.stringWidth(label) + 40;
            int h = metrics.getHeight() + 16;
            g.setColor(Palette.SHADOW);
            g.fillRoundRect(cx - w / 2 + 2, cy + 4, w, h, h, h);
            g.setColor(Palette.ACCENT);
            g.fillRoundRect(cx - w / 2, cy, w, h, h, h);
            g.setColor(Palette.ACCENT_TEXT);
            g.drawString(label, cx - metrics.stringWidth(label) / 2, cy + 8 + metrics.getAscent());
        }
    }

    private static void die(Graphics2D g, int x, int y, int size, int face, double angle) {
        Graphics2D d = (Graphics2D) g.create();
        d.rotate(angle, x + size / 2.0, y + size / 2.0);
        d.setColor(Palette.SHADOW);
        d.fillRoundRect(x + 2, y + 5, size, size, size / 4, size / 4);
        d.setColor(Palette.TEXT);
        d.fillRoundRect(x, y, size, size, size / 4, size / 4);
        d.setColor(Palette.BACKGROUND);
        int p = size / 7;
        int[][] spots = switch (face) {
            case 1 -> new int[][] {{2, 2}};
            case 2 -> new int[][] {{1, 1}, {3, 3}};
            case 3 -> new int[][] {{1, 1}, {2, 2}, {3, 3}};
            case 4 -> new int[][] {{1, 1}, {3, 1}, {1, 3}, {3, 3}};
            case 5 -> new int[][] {{1, 1}, {3, 1}, {2, 2}, {1, 3}, {3, 3}};
            default -> new int[][] {{1, 1}, {3, 1}, {1, 2}, {3, 2}, {1, 3}, {3, 3}};
        };
        for (int[] s : spots) {
            d.fillOval(x + s[0] * size / 4 - p / 2, y + s[1] * size / 4 - p / 2, p, p);
        }
        d.dispose();
    }

    // ---- Text fitting and tooltips ---------------------------------------------------

    /** Splits a name into at most {@code lines} lines that fit (the first narrower, beside the icon), shortening the last with "…". */
    private static List<String> fit(String name, FontMetrics metrics, int firstWidth, int width, int lines) {
        List<String> result = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : name.split(" ")) {
            int room = result.isEmpty() ? firstWidth : width;
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (metrics.stringWidth(candidate) <= room || line.isEmpty()) {
                line = new StringBuilder(candidate);
            } else {
                result.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        result.add(line.toString());
        if (result.size() > lines) {
            List<String> kept = new ArrayList<>(result.subList(0, lines));
            kept.set(lines - 1, kept.get(lines - 1) + "…");
            result = kept;
        }
        for (int i = 0; i < result.size(); i++) {
            result.set(i, clip(result.get(i), metrics, i == 0 ? firstWidth : width));
        }
        return result;
    }

    private static String clip(String text, FontMetrics metrics, int width) {
        if (metrics.stringWidth(text) <= width) {
            return text;
        }
        String shorter = text;
        while (shorter.length() > 1 && metrics.stringWidth(shorter + "…") > width) {
            shorter = shorter.substring(0, shorter.length() - 1);
        }
        return shorter + "…";
    }

    /** The full details of the square under the mouse. */
    @Override
    public String getToolTipText(MouseEvent event) {
        if (game == null) {
            return null;
        }
        int id = squareAt(event.getPoint());
        if (id < 0) {
            return null;
        }
        Square square = game.board().square(id);
        String marks = String.join(", ", text.squareMarks(game, square));
        return square.name() + " · " + StatusText.typeLabel(square) + " · " + text.squareDetail(game, square)
                + (marks.isEmpty() ? "" : " · " + marks);
    }
}
