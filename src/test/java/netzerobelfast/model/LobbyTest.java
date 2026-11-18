package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;
import org.junit.jupiter.api.Test;

import static netzerobelfast.model.Games.actions;
import static netzerobelfast.model.Games.arrive;
import static netzerobelfast.model.Games.only;
import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.setMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FR-06: lobbying another player's Polluting square (1 Waste Incinerator). The lost income and
 * meter effects (AC-1's income, AC-3, AC-4) happen at the end of the round, in plan 005.
 */
class LobbyTest {

    private static PollutingSquare incinerator(Game game) {
        return (PollutingSquare) game.board().square(1);
    }

    @Test
    void fr06_ac1_lobbyingCostsTwoAndMarksTheSquareForThisRound() {
        Game game = Games.scripted();
        own(game, 1, Games.player(game, "Ben"));
        arrive(game, 1);

        Action lobby = only(game, Kind.LOBBY);
        assertEquals(2, lobby.cost());
        game.take(lobby);

        assertEquals(38, game.currentPlayer().money());
        assertTrue(incinerator(game).lobbiedThisRound());
        assertFalse(incinerator(game).meterRiseCancelled(), "only the Activist's lobbying cancels the meter rise");
    }

    @Test
    void fr06_ac5_activistLobbiesFreeAndCancelsTheMeterRise() {
        Game game = Games.scriptedSix(5); // Finn, Community Activist
        own(game, 1, Games.player(game, "Aoife"));
        setMoney(game.currentPlayer(), 0);
        arrive(game, 1);

        Action lobby = only(game, Kind.LOBBY);
        assertEquals(0, lobby.cost());
        game.take(lobby);

        assertEquals(0, game.currentPlayer().money());
        assertTrue(incinerator(game).meterRiseCancelled());
    }

    @Test
    void fr06_ac6_notOfferedOnYourOwnSquareOrAGreenProject() {
        Game game = Games.scripted();
        own(game, 1, game.currentPlayer());
        arrive(game, 1);
        assertTrue(actions(game, Kind.LOBBY).isEmpty());

        own(game, 5, Games.player(game, "Ben"));
        arrive(game, 5);
        assertTrue(actions(game, Kind.LOBBY).isEmpty());
    }

    @Test
    void fr06_ac6_notOfferedWithoutTwoMillion() {
        Game game = Games.scripted();
        own(game, 1, Games.player(game, "Ben"));
        setMoney(game.currentPlayer(), 1);
        arrive(game, 1);
        assertTrue(actions(game, Kind.LOBBY).isEmpty());
    }

    @Test
    void fr06_unownedPollutingSquareOffersBuyingNotLobbying() {
        Game game = Games.scripted();
        arrive(game, 1);
        assertTrue(actions(game, Kind.LOBBY).isEmpty());
    }

    @Test
    void fr06_ac7_notOfferedOnASquareAlreadyLobbiedThisRound() {
        Game game = Games.scripted();
        own(game, 1, Games.player(game, "Ben"));
        arrive(game, 1);
        game.take(only(game, Kind.LOBBY));

        arrive(game, 1);
        assertTrue(actions(game, Kind.LOBBY).isEmpty());
    }

    @Test
    void fr06_ac2_theMarkLastsUntilTheEndOfTheRound() {
        // Aoife lobbies, then Ben, Chloe and Dev each roll 3 and end their turns.
        Game game = Games.start(new ScriptedRandom().firstPlayer(0).rolls(3, 3, 3).noDisaster(), Games.FOUR);
        own(game, 1, Games.player(game, "Ben"));
        arrive(game, 1);
        game.take(only(game, Kind.LOBBY));
        game.endTurn();
        assertTrue(incinerator(game).lobbiedThisRound(), "still round 1");

        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
        }
        assertEquals(2, game.round());
        assertFalse(incinerator(game).lobbiedThisRound());
    }
}
