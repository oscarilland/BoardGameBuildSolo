package netzerobelfast.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static netzerobelfast.model.Games.own;
import static netzerobelfast.model.Games.player;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * FR-11 step 3: the meter rises 3 for existing emissions, 2 per owned Polluting square, and
 * falls 1 per owned Green Project. Polluting: 1, 4, 6, 8. Green: 0, 5, 7, 13, 14.
 */
class MeterUpdateTest {

    /** A six-player game (so the Activist, Finn, is playing) whose first end of round has no disaster. */
    private static Game sixPlayers() {
        return Games.start(new ScriptedRandom().firstPlayer(0).noDisaster(), Games.SIX);
    }

    private static void ownFourPollutingAndFiveGreen(Game game) {
        Player aoife = player(game, "Aoife");
        Player ben = player(game, "Ben");
        for (int square : new int[] {1, 4, 6, 8}) {
            own(game, square, aoife);
        }
        for (int square : new int[] {0, 5, 7, 13, 14}) {
            own(game, square, ben);
        }
    }

    @Test
    void fr11_ac5_fr06_ac5_theActivistsLobbyingCancelsOneRise() {
        Game game = sixPlayers();
        ownFourPollutingAndFiveGreen(game);
        Games.lobby(game, 1, player(game, "Finn"));

        game.endRound();
        assertEquals(50 + 3 + 2 * 3 - 5, game.meter().value());
        assertEquals(54, game.lastRound().orElseThrow().meterAfter());
        assertEquals(50, game.lastRound().orElseThrow().meterBefore());
    }

    @Test
    void fr06_ac4_otherPlayersLobbyingLeavesTheRise() {
        Game game = sixPlayers();
        ownFourPollutingAndFiveGreen(game);
        Games.lobby(game, 1, player(game, "Chloe"));

        game.endRound();
        assertEquals(50 + 3 + 2 * 4 - 5, game.meter().value());
    }

    @Test
    void fr11_ac9_closedSquaresStillCount() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).values(0).noDisaster());
        own(game, 1, player(game, "Aoife"));
        game.disasters().apply(Disaster.STORM, 1, game.board(), game.players());

        game.endRound();
        assertEquals(50 + 3 + 2, game.meter().value());
    }

    @Test
    void fr11_ac10_theMeterCanEndARoundBelowZero() {
        Game game = Games.quietRound();
        game.meter().adjust(1 - 50);
        Player ben = player(game, "Ben");
        for (int square : new int[] {0, 5, 7, 13, 14, 17}) {
            own(game, square, ben);
        }
        game.endRound();
        assertEquals(1 + 3 - 6, game.meter().value());
    }

    @Test
    void fr11_ac8_oneHistoryValuePerRound() {
        Game game = Games.withScript(new ScriptedRandom().firstPlayer(0).noDisaster().noDisaster());
        own(game, 1, player(game, "Aoife"));
        game.endRound();
        game.endRound();
        assertEquals(List.of(50, 55, 60), game.meter().history());
    }
}
