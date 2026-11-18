package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;
import netzerobelfast.model.Game.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static netzerobelfast.model.Games.actions;
import static netzerobelfast.model.Games.arrive;
import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FR-09: riding the Glider between stops 12 (East), 27 (West) and 30 (City Hall). */
class GliderTest {

    @Test
    void fr09_ac1_ridesToEitherOtherStopForOneMillion() {
        Game game = Games.scripted();
        Player aoife = game.currentPlayer();
        arrive(game, 12);

        assertEquals(List.of(new Action(Kind.RIDE_GLIDER, 1, 12, 27), new Action(Kind.RIDE_GLIDER, 1, 12, 30)),
                actions(game, Kind.RIDE_GLIDER));

        game.take(new Action(Kind.RIDE_GLIDER, 1, 12, 27));
        assertEquals(27, aoife.position());
        assertEquals(39, aoife.money());
        assertEquals(8, aoife.greenPoints(), "only the Student Commuter earns GP for riding");
    }

    @Test
    void fr09_ac2_noRideWithoutTheFare() {
        Game game = Games.scripted();
        setMoney(game.currentPlayer(), 0);
        arrive(game, 12);
        assertTrue(actions(game, Kind.RIDE_GLIDER).isEmpty());
    }

    @Test
    void fr09_ac3_studentCommuterRidesFreeAndEarnsGP() {
        Game game = Games.scriptedSix(4); // Eve, Student Commuter
        Player eve = game.currentPlayer();
        setMoney(eve, 0);
        arrive(game, 27);

        List<Action> rides = actions(game, Kind.RIDE_GLIDER);
        assertEquals(2, rides.size());
        assertTrue(rides.stream().allMatch(a -> a.cost() == 0));

        game.take(new Action(Kind.RIDE_GLIDER, 0, 27, 12));
        assertEquals(12, eve.position());
        assertEquals(16 + 1, eve.greenPoints());
    }

    @Test
    void fr09_ac4_ridingIsOptional() {
        Game game = Games.scripted();
        arrive(game, 12);
        game.endTurn();
        assertEquals(Phase.AWAITING_ROLL, game.phase());
    }

    @Test
    void fr09_ac5_noLandingActionAfterARideButConvertingIsStillAllowed() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        arrive(game, 12);
        game.take(new Action(Kind.RIDE_GLIDER, 1, 12, 27));

        assertTrue(actions(game, Kind.RIDE_GLIDER).isEmpty(), "no ride on from 27");
        assertEquals(1, actions(game, Kind.CONVERT).size());
    }

    @Test
    void fr09_ac6_q18_afterRidingToCityHallTheNextMoveAsksWhichWay() {
        // Aoife rides 12 → 30; Ben, Chloe and Dev roll 3; then Aoife rolls 3.
        Game game = Games.start(new ScriptedRandom().firstPlayer(0).rolls(3, 3, 3).noDisaster().rolls(3), Games.FOUR);
        Player aoife = game.currentPlayer();
        arrive(game, 12);
        game.take(new Action(Kind.RIDE_GLIDER, 1, 12, 30));
        assertEquals(30, aoife.position());
        assertNull(aoife.heading());

        game.endTurn();
        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
        }
        game.roll();
        assertEquals(Phase.AWAITING_DIRECTION, game.phase());
        game.chooseDirection(Board.Heading.TOWARDS_EAST);
        assertEquals(List.of(31, 32, 33), game.lastMove().orElseThrow().path());
    }

    @Test
    void fromCityHallTheRidesGoToTheOtherTwoStops() {
        Game game = Games.scripted();
        arrive(game, 30);
        assertEquals(List.of(12, 27), actions(game, Kind.RIDE_GLIDER).stream().map(Action::destination).toList());
    }
}
