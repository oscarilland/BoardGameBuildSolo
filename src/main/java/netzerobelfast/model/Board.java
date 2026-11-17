package netzerobelfast.model;

import netzerobelfast.config.BoardDef;
import netzerobelfast.config.BoardDef.SquareDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The board: its squares, and its shape (a clockwise loop, plus a two-way City Centre cut-through
 * between two junctions, Q-05). Everything comes from {@code board.json}, so there are no square
 * numbers in this class.
 *
 * <p>The board moves a counter one square at a time with {@link #next}. {@link Game} calls it
 * once per step and pauses whenever a player has a choice to make, so a move can include any
 * number of choices. (The analysis model's {@code move(from, steps, route)} took one route for
 * the whole move; see the domain model page.)
 */
public final class Board {

    /** Which way to go from a junction. */
    public enum Route { LOOP, CITY_CENTRE }

    /**
     * Direction of travel along the City Centre route. The route's squares are listed in
     * {@code board.json} from the West junction's end to the East junction's end.
     */
    public enum Heading {
        /** Towards the West junction (Glider West, 27): entered from the East junction. */
        TOWARDS_WEST,
        /** Towards the East junction (Glider East, 12): entered from the West junction. */
        TOWARDS_EAST
    }

    /** Where a counter is after one step, and its heading if it's on the City Centre route. */
    public record Step(int position, Heading heading) {}

    private final BoardDef definition;
    private final List<Square> squares = new ArrayList<>();
    private final List<Integer> loop;
    private final List<Integer> cityRoute;
    private final Map<Integer, Integer> loopIndex = new HashMap<>();
    private final Map<Integer, Integer> cityIndex = new HashMap<>();

    public Board(BoardDef definition) {
        this.definition = definition;
        this.loop = definition.loop();
        this.cityRoute = definition.cityCentreRoute();
        for (int i = 0; i < loop.size(); i++) {
            loopIndex.put(loop.get(i), i);
        }
        for (int i = 0; i < cityRoute.size(); i++) {
            cityIndex.put(cityRoute.get(i), i);
        }
        for (SquareDef square : definition.squares()) {
            squares.add(create(square));
        }
    }

    /** Makes the right kind of square for a data entry. */
    private static Square create(SquareDef square) {
        return switch (square.type()) {
            case POLLUTING -> new PollutingSquare(square.id(), square.name(), square.district(), square.price());
            case GREEN_PROJECT -> new GreenProjectSquare(
                    square.id(), square.name(), square.district(), square.price(), square.nature(), null, null);
            case RESILIENCE -> new ResilienceSquare(square.id(), square.name(), square.district());
            case GLIDER_STOP -> new GliderStop(square.id(), square.name(), square.district());
            case CLIMATE_CARD -> new ClimateCardSquare(square.id(), square.name(), square.district());
        };
    }

    // ---- Squares -------------------------------------------------------------

    public Square square(int id) {
        return squares.get(id);
    }

    /** Every square, in id order. */
    public List<Square> squares() {
        return List.copyOf(squares);
    }

    public int size() {
        return squares.size();
    }

    public List<Integer> gliderStops() {
        return definition.gliderStops();
    }

    /** The Polluting squares this player owns, in id order. */
    public List<PollutingSquare> pollutingOwnedBy(Player player) {
        List<PollutingSquare> owned = new ArrayList<>();
        for (Square square : squares) {
            if (square instanceof PollutingSquare polluting && polluting.owner() == player) {
                owned.add(polluting);
            }
        }
        return owned;
    }

    /** Puts a new square object in place of the one with the same id; used when a square is converted. */
    void replace(Square square) {
        squares.set(square.id(), square);
    }

    /** Lobbying lasts one round: clears every lobbied mark. */
    void clearLobbying() {
        for (Square square : squares) {
            if (square instanceof PollutingSquare polluting) {
                polluting.clearLobbying();
            }
        }
    }

    // ---- Shape and movement ---------------------------------------------------

    /** True for the two loop squares where the City Centre route starts and ends. */
    public boolean isJunction(int id) {
        return id == definition.westJunction() || id == definition.eastJunction();
    }

    public boolean onCityRoute(int id) {
        return cityIndex.containsKey(id);
    }

    /**
     * One step on from {@code position}.
     *
     * @param heading the direction of travel if {@code position} is on the City Centre route; ignored on the loop
     * @param route   at a junction, whether to stay on the loop or enter the City Centre; ignored elsewhere
     * @throws IllegalArgumentException if the route is City Centre but the square isn't a junction,
     *                                  or the counter is on the City Centre route without a heading
     */
    public Step next(int position, Heading heading, Route route) {
        if (onCityRoute(position)) {
            if (heading == null) {
                throw new IllegalArgumentException("square " + position + " is on the City Centre route, so a heading is needed");
            }
            int i = cityIndex.get(position);
            if (heading == Heading.TOWARDS_EAST) {
                return i + 1 < cityRoute.size()
                        ? new Step(cityRoute.get(i + 1), Heading.TOWARDS_EAST)
                        : new Step(definition.eastJunction(), null);
            }
            return i > 0
                    ? new Step(cityRoute.get(i - 1), Heading.TOWARDS_WEST)
                    : new Step(definition.westJunction(), null);
        }

        if (route == Route.CITY_CENTRE) {
            if (position == definition.westJunction()) {
                return new Step(cityRoute.get(0), Heading.TOWARDS_EAST);
            }
            if (position == definition.eastJunction()) {
                return new Step(cityRoute.get(cityRoute.size() - 1), Heading.TOWARDS_WEST);
            }
            throw new IllegalArgumentException("square " + position + " isn't a junction, so the City Centre can't be entered here");
        }

        int i = loopIndex.get(position);
        return new Step(loop.get((i + 1) % loop.size()), null);
    }
}
