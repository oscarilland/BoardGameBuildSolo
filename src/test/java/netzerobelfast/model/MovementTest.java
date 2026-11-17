package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * FR-03: rolling and moving, junction choices (Q-05), and the two-way City Centre route.
 * Square numbers are board.json ids; the board drawing numbers them from 1.
 */
class MovementTest {

    private static List<Integer> path(Game game) {
        return game.lastMove().orElseThrow().path();
    }

    /** Ends the other three players' turns with the given rolls, so it's seat 0's turn again. */
    private static void othersTakeTurns(Game game) {
        for (int i = 0; i < 3; i++) {
            game.endTurn();
            game.roll();
        }
        game.endTurn();
    }

    @Test
    void fr03_ac4_firstWorkedExample() {
        // "A player on 11 who rolls 4 and takes the City Centre at 12 moves 12 → 35 → 34 → 33."
        Game game = Games.scripted(4);
        Games.place(game, 11, null);

        game.roll();
        assertEquals(Phase.AWAITING_ROUTE, game.phase());
        assertEquals(12, game.currentPlayer().position());
        assertEquals(3, game.stepsLeft());

        game.chooseRoute(Route.CITY_CENTRE);
        assertEquals(Phase.MOVE_DONE, game.phase());
        assertEquals(List.of(12, 35, 34, 33), path(game));
        assertEquals(4, game.lastMove().orElseThrow().diceTotal());
        assertEquals(33, game.currentPlayer().position());
        assertEquals(Heading.TOWARDS_WEST, game.currentPlayer().heading());
    }

    @Test
    void fr03_ac4_ac7_secondWorkedExample() {
        // "A player on 26 who rolls 9 and takes it at 27 moves 27 → 30 → … → 35 → 12 → 13."
        // There's no question at 12 on the way out (AC-7).
        Game game = Games.scripted(9);
        Games.place(game, 26, null);

        game.roll();
        assertEquals(Phase.AWAITING_ROUTE, game.phase());
        game.chooseRoute(Route.CITY_CENTRE);

        assertEquals(Phase.MOVE_DONE, game.phase());
        assertEquals(List.of(27, 30, 31, 32, 33, 34, 35, 12, 13), path(game));
        assertNull(game.currentPlayer().heading());
    }

    @Test
    void stayingOnTheLoopAtAJunction() {
        Game game = Games.scripted(4);
        Games.place(game, 25, null);

        game.roll();
        game.chooseRoute(Route.LOOP);
        assertEquals(List.of(26, 27, 28, 29), path(game));
    }

    @Test
    void fr03_ac5_noQuestionWithoutAJunction() {
        Game game = Games.scripted(5);
        game.roll();
        assertEquals(Phase.MOVE_DONE, game.phase());
        assertEquals(List.of(1, 2, 3, 4, 5), path(game));
    }

    @Test
    void fr03_ac5_q05_landingOnAJunctionAsksAtTheStartOfTheNextMove() {
        // Aoife lands exactly on 12 with her roll of 4; the others roll 3; then Aoife rolls 3.
        Game game = Games.scripted(4, 3, 3, 3, 3);
        Games.place(game, 8, null);

        game.roll();
        assertEquals(Phase.MOVE_DONE, game.phase(), "no question when the move ends on the junction");
        assertEquals(List.of(9, 10, 11, 12), path(game));

        othersTakeTurns(game);
        assertEquals("Aoife", game.currentPlayer().name());
        game.roll();
        assertEquals(Phase.AWAITING_ROUTE, game.phase(), "asked before the first step");
        assertEquals(3, game.stepsLeft());
        assertEquals(12, game.currentPlayer().position());

        game.chooseRoute(Route.LOOP);
        assertEquals(List.of(13, 14, 15), path(game));
    }

    @Test
    void fr03_ac7_noSecondQuestionAtTheFarJunction() {
        Game game = Games.scripted(3);
        Games.place(game, 31, Heading.TOWARDS_WEST);

        game.roll();
        assertEquals(Phase.MOVE_DONE, game.phase());
        assertEquals(List.of(30, 27, 28), path(game));
    }

    @Test
    void q05_endingOnTheFarJunctionAsksNextTurn() {
        // AC-7 only covers the same move. Stopping exactly on 27 is "landing on a junction".
        Game game = Games.scripted(2, 3, 3, 3, 2);
        Games.place(game, 31, Heading.TOWARDS_WEST);

        game.roll();
        assertEquals(List.of(30, 27), path(game));
        assertEquals(Phase.MOVE_DONE, game.phase());

        othersTakeTurns(game);
        game.roll();
        assertEquals(Phase.AWAITING_ROUTE, game.phase());
        game.chooseRoute(Route.CITY_CENTRE);
        assertEquals(List.of(30, 31), path(game));
    }

    @Test
    void keepsItsDirectionOnTheCutThroughHeadingWest() {
        Game game = Games.scripted(3);
        Games.place(game, 33, Heading.TOWARDS_WEST);
        game.roll();
        assertEquals(List.of(32, 31, 30), path(game));
        assertEquals(Heading.TOWARDS_WEST, game.currentPlayer().heading());
    }

    @Test
    void keepsItsDirectionOnTheCutThroughHeadingEast() {
        Game game = Games.scripted(3);
        Games.place(game, 33, Heading.TOWARDS_EAST);
        game.roll();
        assertEquals(List.of(34, 35, 12), path(game));
        assertNull(game.currentPlayer().heading());
    }

    @Test
    void q18_onTheCityRouteWithNoDirectionAsksWhichWay() {
        Game game = Games.scripted(2);
        Games.place(game, 30, null);

        game.roll();
        assertEquals(Phase.AWAITING_DIRECTION, game.phase());
        assertEquals(2, game.stepsLeft());

        game.chooseDirection(Heading.TOWARDS_EAST);
        assertEquals(List.of(31, 32), path(game));
    }

    @Test
    void q18_choosingWestLeavesAtTheWestJunctionWithoutAskingAgain() {
        Game game = Games.scripted(2);
        Games.place(game, 30, null);

        game.roll();
        game.chooseDirection(Heading.TOWARDS_WEST);
        assertEquals(List.of(27, 28), path(game));
    }

    @Test
    void fr03_ac1_sameSeedAndChoicesGiveTheSameGame() {
        assertEquals(playThreeRounds(new Random(77)), playThreeRounds(new Random(77)));
    }

    /** Plays three rounds, always taking the City Centre and heading east; returns final positions. */
    private static List<Integer> playThreeRounds(Random random) {
        Game game = Games.start(random, Games.FOUR);
        while (game.round() <= 3) {
            switch (game.phase()) {
                case AWAITING_ROLL -> game.roll();
                case AWAITING_ROUTE -> game.chooseRoute(Route.CITY_CENTRE);
                case AWAITING_DIRECTION -> game.chooseDirection(Heading.TOWARDS_EAST);
                case AWAITING_QUIZ_ANSWER -> game.answerQuiz(0);
                case AWAITING_CHOICE -> game.chooseOption(game.affordableOptions().get(0));
                case MOVE_DONE -> game.endTurn();
            }
        }
        return game.players().stream().map(Player::position).toList();
    }
}
