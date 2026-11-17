package netzerobelfast.config;

import java.util.List;
import java.util.Map;

/**
 * Everything in {@code board.json}: districts, the loop, the City Centre route, junctions,
 * Glider stops and the squares, in id order.
 *
 * @param districts           district code to display name, such as {@code NORTH} to "North Belfast"
 * @param loop                square ids on the clockwise loop, in order
 * @param cityCentreRoute     square ids on the cut-through, in order from the West junction
 * @param westJunction        the loop square next to the start of the cut-through (27)
 * @param eastJunction        the loop square next to the end of the cut-through (12)
 * @param cityCentreDirection how the cut-through may be travelled; two-way since Q-05
 */
public record BoardDef(
        Map<String, String> districts,
        List<Integer> loop,
        List<Integer> cityCentreRoute,
        int westJunction,
        int eastJunction,
        List<Integer> junctions,
        List<Integer> gliderStops,
        String cityCentreDirection,
        List<SquareDef> squares) {

    public enum SquareType { POLLUTING, GREEN_PROJECT, RESILIENCE, GLIDER_STOP, CLIMATE_CARD }

    /**
     * One square.
     *
     * @param price  the purchase price, or null for squares that can't be owned
     * @param nature true for nature projects (woodland, peatland), which matter for the Farmer and Droughts
     */
    public record SquareDef(int id, String name, SquareType type, String district, Integer price, boolean nature) {

        /** Polluting squares and Green Projects can be bought and owned; the rest can't. */
        public boolean ownable() {
            return type == SquareType.POLLUTING || type == SquareType.GREEN_PROJECT;
        }
    }

    /** The square with this id. Squares are validated to be listed in id order, so the id is the index. */
    public SquareDef square(int id) {
        return squares.get(id);
    }
}
