package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plays complete games with random choices and no person involved: groundwork for the 20-game
 * headless run in NFR-06 (plan 006), and a check that no sequence of choices, cards, disasters or
 * bankruptcies puts the game into an impossible state.
 */
class HeadlessTurnsTest {

    private int disasters;

    /**
     * 30 complete games. Bankruptcy is rare with these random players (none went bankrupt in 60
     * games while this was written, even when they spent everything they could), so its rules are
     * tested directly in {@link BankruptcyTest}; this run still handles a return roll if one comes up.
     */
    @Test
    void completeGamesWithRandomChoicesNeverBreak() {
        playGames(0, 0.6);
        assertTrue(disasters > 0, "30 games without a single disaster");
    }

    private void playGames(int firstSeed, double actionChance) {
        for (int seed = firstSeed; seed < firstSeed + 30; seed++) {
            Random random = new Random(seed);
            int playerCount = 4 + seed % 3;
            Game game = Games.start(random, Games.SIX.subList(0, playerCount));
            Random choices = new Random(seed * 31L);
            int actionsTaken = 0;
            int cards = 0;
            int lastRoundSeen = 0;

            while (game.phase() != Phase.GAME_OVER) {
                switch (game.phase()) {
                    case AWAITING_RETURN_ROLL -> game.rollToReturn();
                    case AWAITING_ROLL -> {
                        assertTrue(!game.currentPlayer().bankrupt(), "a bankrupt player never rolls to move");
                        game.lastReturn().ifPresent(r -> assertTrue(r.returned()));
                        game.roll();
                    }
                    case AWAITING_ROUTE -> game.chooseRoute(choices.nextBoolean() ? Route.LOOP : Route.CITY_CENTRE);
                    case AWAITING_DIRECTION -> game.chooseDirection(
                            choices.nextBoolean() ? Heading.TOWARDS_WEST : Heading.TOWARDS_EAST);
                    case AWAITING_QUIZ_ANSWER -> {
                        game.answerQuiz(choices.nextInt(4));
                        cards++;
                    }
                    case AWAITING_CHOICE -> {
                        List<Integer> options = game.affordableOptions();
                        assertTrue(!options.isEmpty(), "a choice card always has an affordable option");
                        game.chooseOption(options.get(choices.nextInt(options.size())));
                        cards++;
                    }
                    case MOVE_DONE -> {
                        // Take up to two actions (a landing action and a conversion), each with the given chance.
                        for (int i = 0; i < 2; i++) {
                            List<Action> available = game.availableActions();
                            if (!available.isEmpty() && choices.nextDouble() < actionChance) {
                                game.take(available.get(choices.nextInt(available.size())));
                                actionsTaken++;
                            }
                        }
                        checkInvariants(game);
                        game.endTurn();
                        if (game.lastRound().isPresent() && game.lastRound().get().round() != lastRoundSeen) {
                            RoundReport report = game.lastRound().get();
                            lastRoundSeen = report.round();
                            if (report.disaster() != null) {
                                disasters++;
                            }
                            checkInvariants(game);
                        }
                    }
                    case GAME_OVER -> throw new AssertionError("unreachable");
                }
            }

            String context = "seed " + seed;
            assertEquals(13, game.round(), context);
            assertEquals(2050, game.year(), context);
            assertEquals(13, game.meter().history().size(), context + ": the start and one value per round");
            assertTrue(actionsTaken > 0, context);
            assertTrue(cards > 0, context + ": no quiz or choice card came up");
        }
    }

    private static void checkInvariants(Game game) {
        for (Player player : game.players()) {
            assertTrue(player.money() >= 0, player + " has £" + player.money() + "M");
            if (player.bankrupt()) {
                assertEquals(0, player.money(), player + " is bankrupt but has money");
            }
            assertTrue(player.loanRepayments() % game.rules().bankruptcy().repayAtEnd() == 0);
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
