package netzerobelfast.ui;

import netzerobelfast.config.BoardDef;
import netzerobelfast.config.BoardDef.SquareDef;
import netzerobelfast.model.Game;
import netzerobelfast.model.Player;
import netzerobelfast.model.Square;
import netzerobelfast.ui.BoardLayout.Cell;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The board, painted: every square with its name, type label and price or owner, and the
 * players' numbered counters (NFR-05 AC-2: readable without colour). A tooltip gives each
 * square's full details. It only reads the game.
 *
 * <p>A {@code JPanel} rather than a bare {@code JComponent}, because only the panel has an
 * accessibility context for screen readers (a bare component's is null).
 */
final class BoardPanel extends JPanel {

    private static final int GAP = 4;
    private static final Font NAME = Ui.SMALL.deriveFont(Font.BOLD);
    private static final Font DETAIL = Ui.SMALL.deriveFont(11f);

    private final BoardDef board;
    private final BoardLayout layout;
    private final StatusText text;
    private Game game;

    BoardPanel(BoardDef board, StatusText text) {
        this.board = board;
        this.layout = new BoardLayout(board);
        this.text = text;
        setPreferredSize(new Dimension(920, 520));
        setToolTipText("");
        getAccessibleContext().setAccessibleName("The board");
    }

    void show(Game game) {
        this.game = game;
        repaint();
    }

    private Rectangle bounds(int square) {
        Cell cell = layout.cell(square);
        double w = getWidth() / (double) layout.columns();
        double h = getHeight() / (double) layout.rows();
        return new Rectangle((int) (cell.column() * w) + GAP / 2, (int) (cell.row() * h) + GAP / 2,
                (int) w - GAP, (int) h - GAP);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (game == null) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Palette.BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());

        paintCityCentreLinks(g);
        for (SquareDef def : board.squares()) {
            paintSquare(g, def, game.board().square(def.id()));
        }
        paintCounters(g);
        g.dispose();
    }

    /** Dashed lines from each junction to the end of the City Centre route next to it, and the route's label. */
    private void paintCityCentreLinks(Graphics2D g) {
        List<Integer> route = board.cityCentreRoute();
        g.setColor(Palette.MUTED_TEXT);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] {8, 6}, 0));
        link(g, board.westJunction(), route.get(0));
        link(g, board.eastJunction(), route.get(route.size() - 1));
        g.setStroke(new BasicStroke(1));
        Rectangle first = bounds(route.get(0));
        Rectangle last = bounds(route.get(route.size() - 1));
        g.setFont(Ui.SMALL);
        String label = "⇄ City Centre cut-through (either way)";
        FontMetrics metrics = g.getFontMetrics();
        int centre = (first.x + last.x + last.width) / 2;
        g.drawString(label, centre - metrics.stringWidth(label) / 2, first.y - 6);
    }

    private void link(Graphics2D g, int from, int to) {
        Rectangle a = bounds(from);
        Rectangle b = bounds(to);
        int y = a.y + a.height / 2;
        if (a.x < b.x) {
            g.drawLine(a.x + a.width, y, b.x, y);
        } else {
            g.drawLine(b.x + b.width, y, a.x, y);
        }
    }

    private void paintSquare(Graphics2D g, SquareDef def, Square square) {
        Rectangle r = bounds(def.id());
        g.setColor(Palette.squareFill(def.type(), def.nature()));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        boolean junction = board.junctions().contains(def.id());
        g.setColor(junction ? Palette.TEXT : Palette.BORDER);
        g.setStroke(new BasicStroke(junction ? 2.5f : 1));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setStroke(new BasicStroke(1));

        int pad = 5;
        int width = r.width - 2 * pad;
        int y = r.y + pad;
        g.setColor(Palette.TEXT);
        g.setFont(NAME);
        FontMetrics nameMetrics = g.getFontMetrics();
        for (String line : fit(square.name(), nameMetrics, width, 2)) {
            y += nameMetrics.getAscent();
            g.drawString(line, r.x + pad, y);
            y += nameMetrics.getDescent();
        }

        // The type label and the price or owner sit at the bottom of the square.
        g.setFont(DETAIL);
        FontMetrics detail = g.getFontMetrics();
        int bottom = r.y + r.height - pad - detail.getDescent();
        String type = StatusText.typeLabel(square) + (junction ? " ★" : "");
        g.drawString(type, r.x + pad, bottom - detail.getHeight());
        g.drawString(clip(text.squareDetail(game, square), detail, width), r.x + pad, bottom);

        List<String> marks = text.squareMarks(game, square);
        if (!marks.isEmpty() && y + detail.getHeight() < bottom - detail.getHeight()) {
            g.setColor(Palette.WARNING_TEXT);
            g.setFont(DETAIL.deriveFont(Font.BOLD));
            g.drawString(clip(String.join(", ", marks), detail, width), r.x + pad, y + detail.getAscent() + 1);
        }
    }

    /** Each player's counter: a disc with their seat number, in the square's bottom-right corner. */
    private void paintCounters(Graphics2D g) {
        Map<Integer, List<Player>> bySquare = new java.util.HashMap<>();
        for (Player player : game.players()) {
            bySquare.computeIfAbsent(player.position(), k -> new ArrayList<>()).add(player);
        }
        g.setFont(Ui.SMALL.deriveFont(Font.BOLD, 11f));
        FontMetrics metrics = g.getFontMetrics();
        bySquare.forEach((square, players) -> {
            Rectangle r = bounds(square);
            int size = Math.max(14, Math.min(20, r.width / 5));
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                int seat = StatusText.seat(game, player);
                // Up to three a row, filling from the right.
                int x = r.x + r.width - 3 - (i % 3 + 1) * (size + 2);
                int y = r.y + r.height / 2 - size / 2 + (i / 3) * (size + 2) - 4;
                g.setColor(Palette.COUNTERS.get((seat - 1) % Palette.COUNTERS.size()));
                g.fillOval(x, y, size, size);
                if (player == game.currentPlayer()) {
                    g.setColor(Palette.TEXT);
                    g.setStroke(new BasicStroke(2.5f));
                    g.drawOval(x - 2, y - 2, size + 4, size + 4);
                    g.setStroke(new BasicStroke(1));
                }
                g.setColor(Palette.COUNTER_TEXT);
                String number = String.valueOf(seat);
                g.drawString(number, x + (size - metrics.stringWidth(number)) / 2,
                        y + (size + metrics.getAscent() - metrics.getDescent()) / 2);
            }
        });
    }

    /** Splits a name into at most {@code lines} lines that fit, shortening the last with "…". */
    private static List<String> fit(String name, FontMetrics metrics, int width, int lines) {
        List<String> result = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : name.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (metrics.stringWidth(candidate) <= width || line.isEmpty()) {
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
        result.replaceAll(text -> clip(text, metrics, width));
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
        for (SquareDef def : board.squares()) {
            if (bounds(def.id()).contains(event.getPoint())) {
                Square square = game.board().square(def.id());
                String marks = String.join(", ", text.squareMarks(game, square));
                return square.name() + " · " + StatusText.typeLabel(square) + " · " + text.squareDetail(game, square)
                        + (marks.isEmpty() ? "" : " · " + marks);
            }
        }
        return null;
    }
}
