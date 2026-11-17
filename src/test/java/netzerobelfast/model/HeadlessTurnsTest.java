package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plays many rounds with random choices and no person involved: groundwork for the 20-game
 * headless run in NFR-06 (plan 006), and a check that no sequence of choices or actions puts
 * the game into an impossible state.
 */
class HeadlessTurnsTest {

    @Test
    void tenRoundsWithRandomChoicesAndActionsNeverBreak() {
        for (int seed = 0; seed < 30; seed++) {
            Random random = new Random(seed);
            int playerCount = 4 + seed % 3;
            Game game = Games.start(random, Games.SIX.subList(0, playerCount));
            Random choices = new Random(seed * 31L);
            int moves = 0;
            int actionsTaken = 0;

            while (game.round() <= 10) {
                switch (game.phase()) {
                    case AWAITING_ROLL -> game.roll();
                    case AWAITING_ROUTE -> game.chooseRoute(choices.nextBoolean() ? Route.LOOP : Route.CITY_CENTRE);
                    case AWAITING_DIRECTION -> game.chooseDirection(
                            choices.nextBoolean() ? Heading.TOWARDS_WEST : Heading.TOWARDS_EAST);
                    case MOVE_DONE -> {
                        Game.Move move = game.lastMove().orElseThrow();
                        assertEquals(move.diceTotal(), move.path().size(), "one square per pip");
                        moves++;
                        // Take up to two actions (a landing action and a conversion), each 60% likely.
                        for (int i = 0; i < 2; i++) {
                            List<Action> available = game.availableActions();
                            if (!available.isEmpty() && choices.nextDouble() < 0.6) {
                                game.take(available.get(choices.nextInt(available.size())));
                                actionsTaken++;
                            }
                        }
                        checkInvariants(game);
                        game.endTurn();
                    }
                }
            }

            assertEquals(10 * playerCount, moves, "seed " + seed);
            assertEquals(2026 + 10 * 2, game.year(), "seed " + seed);
            assertTrue(actionsTaken > 0, "seed " + seed);
        }
    }

    private static void checkInvariants(Game game) {
        for (Player player : game.players()) {
            assertTrue(player.money() >= 0, player + " has £" + player.money() + "M");
            assertTrue(player.position() >= 0 && player.position() < game.board().size(), player + " is off the board");
            assertTrue(player.resilienceLevel() <= game.rules().resilience().maxLevel());
            CoFundingDeal deal = player.coFundingDeal();
            if (deal != null) {
                assertTrue(game.board().square(deal.square()) instanceof GreenProjectSquare, "deals are on Green Projects");
                assertTrue(((OwnableSquare) game.board().square(deal.square())).owner() != player, "not your own square");
            }
        }
        for (Square square : game.board().squares()) {
            if (square instanceof OwnableSquare ownable && ownable.owner() != null) {
                assertTrue(game.players().contains(ownable.owner()), square + " has an owner who isn't playing");
            }
        }
    }
}
