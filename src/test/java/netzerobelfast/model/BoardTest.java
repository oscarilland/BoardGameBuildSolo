package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Board.Step;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One-step movement on the real board, from board.json. */
class BoardTest {

    private final Board board = new Board(Games.CONFIG.board());

    /** Walks {@code steps} squares, taking {@code route} at the first step only. */
    private List<Integer> walk(int from, Heading heading, Route firstRoute, int steps) {
        List<Integer> path = new ArrayList<>();
        int position = from;
        Route route = firstRoute;
        for (int i = 0; i < steps; i++) {
            Step step = board.next(position, heading, route);
            position = step.position();
            heading = step.heading();
            route = Route.LOOP;
            path.add(position);
        }
        return path;
    }

    @Test
    void knowsTheJunctionsAndTheCityRoute() {
        assertTrue(board.isJunction(12));
        assertTrue(board.isJunction(27));
        assertFalse(board.isJunction(13));
        assertTrue(board.onCityRoute(30));
        assertTrue(board.onCityRoute(35));
        assertFalse(board.onCityRoute(12));
        assertEquals(36, board.size());
        assertEquals("Glider East", board.square(12).name());
    }

    @Test
    void fr03_ac2_loopWrapsFrom29To0() {
        assertEquals(List.of(29, 0, 1, 2), walk(28, null, Route.LOOP, 4));
    }

    @Test
    void stayingOnTheLoopAtAJunctionCarriesOnClockwise() {
        assertEquals(List.of(13, 14), walk(12, null, Route.LOOP, 2));
        assertEquals(List.of(28, 29), walk(27, null, Route.LOOP, 2));
    }

    @Test
    void fr03_ac4_cityCentreFromTheEastJunction() {
        assertEquals(List.of(35, 34, 33, 32, 31, 30, 27, 28), walk(12, null, Route.CITY_CENTRE, 8));
    }

    @Test
    void fr03_ac4_cityCentreFromTheWestJunction() {
        assertEquals(List.of(30, 31, 32, 33, 34, 35, 12, 13), walk(27, null, Route.CITY_CENTRE, 8));
    }

    @Test
    void headingIsSetOnTheRouteAndClearedOffIt() {
        assertEquals(new Step(35, Heading.TOWARDS_WEST), board.next(12, null, Route.CITY_CENTRE));
        assertEquals(new Step(30, Heading.TOWARDS_EAST), board.next(27, null, Route.CITY_CENTRE));
        Step out = board.next(30, Heading.TOWARDS_WEST, Route.LOOP);
        assertEquals(27, out.position());
        assertNull(out.heading());
    }

    @Test
    void cityCentreOnlyFromAJunction() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> board.next(5, null, Route.CITY_CENTRE));
        assertEquals("square 5 isn't a junction, so the City Centre can't be entered here", e.getMessage());
    }

    @Test
    void onTheCityRouteAHeadingIsNeeded() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> board.next(30, null, Route.LOOP));
        assertEquals("square 30 is on the City Centre route, so a heading is needed", e.getMessage());
    }
}
