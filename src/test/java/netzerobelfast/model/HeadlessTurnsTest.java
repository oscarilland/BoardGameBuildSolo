package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plays many rounds with random choices and no person involved: groundwork for the 20-game
 * headless run in NFR-06 (plan 006), and a check that no sequence of choices breaks movement.
 */
class HeadlessTurnsTest {

    @Test
    void tenRoundsWithRandomChoicesNeverBreak() {
        for (int seed = 0; seed < 30; seed++) {
            Random random = new Random(seed);
            int playerCount = 4 + seed % 3;
            Game game = Games.start(random, Games.SIX.subList(0, playerCount));
            Random choices = new Random(seed * 31L);
            int moves = 0;

            while (game.round() <= 10) {
                switch (game.phase()) {
                    case AWAITING_ROLL -> game.roll();
                    case AWAITING_ROUTE -> game.chooseRoute(choices.nextBoolean() ? Route.LOOP : Route.CITY_CENTRE);
                    case AWAITING_DIRECTION -> game.chooseDirection(
                            choices.nextBoolean() ? Heading.TOWARDS_WEST : Heading.TOWARDS_EAST);
                    case MOVE_DONE -> {
                        Game.Move move = game.lastMove().orElseThrow();
                        assertEquals(move.diceTotal(), move.path().size(), "one square per pip");
                        for (int square : move.path()) {
                            assertTrue(square >= 0 && square < game.board().size(), "square " + square);
                        }
                        moves++;
                        game.endTurn();
                    }
                }
            }

            assertEquals(10 * playerCount, moves, "seed " + seed);
            assertEquals(2026 + 10 * 2, game.year(), "seed " + seed);
        }
    }
}
