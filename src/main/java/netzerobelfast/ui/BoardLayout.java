package netzerobelfast.ui;

import netzerobelfast.config.BoardDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Where each square sits on the drawn board, as in the team's board layout: the loop round the
 * edge of a grid, clockwise from the top-left corner, and the City Centre route along the middle
 * row between the two junctions.
 *
 * <p>This is drawing geometry, not balance, so it lives in code. It works from the loop and
 * route order in {@code board.json}, so renaming or re-pricing squares doesn't affect it.
 */
public final class BoardLayout {

    /**
     * Rows in the grid. With the 30-square loop this gives the 10 × 7 ring of the board layout,
     * with both junctions on the middle row. A loop of another length gets a ring with the same
     * number of rows and as many columns as it needs.
     */
    static final int ROWS = 7;

    /** A grid position: column from the left and row from the top, both from 0. */
    public record Cell(int column, int row) {}

    private final int columns;
    private final Map<Integer, Cell> cells = new HashMap<>();

    public BoardLayout(BoardDef board) {
        List<Integer> loop = board.loop();
        // The ring has 2 × columns + 2 × (ROWS - 2) squares.
        int sides = loop.size() - 2 * (ROWS - 2);
        if (sides < 4 || sides % 2 != 0) {
            throw new IllegalArgumentException("a loop of " + loop.size() + " squares doesn't fit a ring of " + ROWS + " rows");
        }
        columns = sides / 2;

        for (int i = 0; i < loop.size(); i++) {
            cells.put(loop.get(i), ringCell(i));
        }

        // The City Centre route runs between the junctions, listed from the West junction's end.
        Cell west = cells.get(board.westJunction());
        Cell east = cells.get(board.eastJunction());
        List<Integer> route = board.cityCentreRoute();
        int gap = Math.abs(east.column() - west.column()) - 1;
        if (west.row() != east.row() || gap < route.size()) {
            throw new IllegalArgumentException("the City Centre route doesn't fit between the junctions");
        }
        int step = east.column() > west.column() ? 1 : -1;
        int first = west.column() + step * (1 + (gap - route.size() + 1) / 2);
        for (int i = 0; i < route.size(); i++) {
            cells.put(route.get(i), new Cell(first + step * i, west.row()));
        }
    }

    /** The cell for the i-th loop square: along the top, down the right, back along the bottom, up the left. */
    private Cell ringCell(int i) {
        int down = ROWS - 1;
        if (i < columns) {
            return new Cell(i, 0);
        }
        i -= columns;
        if (i < down - 1) {
            return new Cell(columns - 1, i + 1);
        }
        i -= down - 1;
        if (i < columns) {
            return new Cell(columns - 1 - i, down);
        }
        i -= columns;
        return new Cell(0, down - 1 - i);
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return ROWS;
    }

    public Cell cell(int square) {
        Cell cell = cells.get(square);
        if (cell == null) {
            throw new IllegalArgumentException("square " + square + " has no place on the board");
        }
        return cell;
    }

    /** Every square's cell. */
    public Map<Integer, Cell> cells() {
        return Map.copyOf(cells);
    }
}
