package netzerobelfast.ui;

import netzerobelfast.model.Action;
import netzerobelfast.model.Board.Heading;
import netzerobelfast.model.Board.Route;
import netzerobelfast.model.Game;
import netzerobelfast.model.OwnableSquare;
import netzerobelfast.model.Player;
import netzerobelfast.model.TestGames;
import netzerobelfast.ui.ActionOptions.Button;
import netzerobelfast.ui.ActionOptions.Command;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** NFR-02 AC-2 (only legal, affordable actions enabled) and NFR-05 AC-4 (keys) for the action bar. */
class ActionOptionsTest {

    private static List<String> labels(List<Button> buttons) {
        return buttons.stream().map(Button::label).toList();
    }

    private static Button endTurn(List<Button> buttons) {
        return buttons.get(buttons.size() - 1);
    }

    /** Every shortcut key on the bar is used by one button at most. */
    private static void assertKeysUnique(List<Button> buttons) {
        Set<Character> keys = new HashSet<>();
        for (Button button : buttons) {
            if (button.key() != ActionOptions.NO_KEY) {
                assertTrue(keys.add(button.key()), "key " + button.key() + " used twice in " + buttons);
            }
        }
    }

    @Test
    void nfr02_ac2_beforeMovingOnlyRollIsEnabled() {
        Game game = TestGames.script().start();
        List<Button> buttons = ActionOptions.forGame(game);
        assertEquals(List.of("Roll dice", "End turn"), labels(buttons));
        assertTrue(buttons.get(0).enabled());
        assertEquals(new Command.Roll(), buttons.get(0).command());
        assertFalse(endTurn(buttons).enabled());
        assertEquals("Roll dice (R)", buttons.get(0).text());
        assertKeysUnique(buttons);
    }

    @Test
    void nfr02_ac2_afterMovingTheSquaresActionsAndEndTurn() {
        Game game = TestGames.script().start();
        TestGames.arrive(game, 6);
        List<Button> buttons = ActionOptions.forGame(game);
        Action buy = game.availableActions().get(0);
        assertEquals(List.of("Buy Harbour Gas Plant £" + buy.cost() + "M", "End turn"), labels(buttons));
        assertEquals(new Command.Take(buy), buttons.get(0).command());
        assertEquals('B', buttons.get(0).key());
        assertTrue(endTurn(buttons).enabled());
        assertKeysUnique(buttons);
    }

    @Test
    void nfr02_ac2_exactlyAffordableIsOfferedOneShortIsNot() {
        Game game = TestGames.script().start();
        Player aoife = game.currentPlayer();
        int price = aoife.character().buyPrice((OwnableSquare) game.board().square(6));

        TestGames.setMoney(aoife, price);
        TestGames.arrive(game, 6);
        assertTrue(labels(ActionOptions.forGame(game)).get(0).startsWith("Buy "));

        Game poorer = TestGames.script().start();
        TestGames.setMoney(poorer.currentPlayer(), price - 1);
        TestGames.arrive(poorer, 6);
        assertEquals(List.of("End turn"), labels(ActionOptions.forGame(poorer)));
    }

    @Test
    void nfr05_ac4_secondActionOfAKindHasNoKeyButIsStillAButton() {
        Game game = TestGames.script().start();
        Player aoife = game.currentPlayer();
        TestGames.own(game, 1, aoife);
        TestGames.own(game, 4, aoife);
        TestGames.arrive(game, 3);
        List<Button> buttons = ActionOptions.forGame(game);
        List<Button> converts = buttons.stream().filter(b -> b.label().startsWith("Convert ")).toList();
        assertEquals(2, converts.size());
        assertEquals('V', converts.get(0).key());
        assertEquals(ActionOptions.NO_KEY, converts.get(1).key());
        assertTrue(buttons.stream().anyMatch(b -> b.label().startsWith("Protect: Resilience 1 £") && b.key() == 'P'));
        assertKeysUnique(buttons);
    }

    @Test
    void gliderButtonsNameTheirDestination() {
        Game game = TestGames.script().start();
        TestGames.arrive(game, 12);
        List<String> labels = labels(ActionOptions.forGame(game));
        assertTrue(labels.contains("Glider to Glider West £1M"), labels.toString());
        assertTrue(labels.contains("Glider to Glider City Hall £1M"), labels.toString());
        assertKeysUnique(ActionOptions.forGame(game));
    }

    @Test
    void nfr02_ac2_atAJunctionOnlyTheRouteChoices() {
        // Aoife rolls 10 (square 10); the others roll 3; no disaster; then Aoife rolls 4 and reaches junction 12 with 2 to go.
        Game game = TestGames.script().rolls(10, 3, 3, 3).noDisaster().rolls(4).start();
        for (int i = 0; i < 4; i++) {
            game.roll();
            game.endTurn();
        }
        game.roll();
        assertEquals(Game.Phase.AWAITING_ROUTE, game.phase());
        List<Button> buttons = ActionOptions.forGame(game);
        assertEquals(List.of("Stay on the loop", "Cut through the City Centre", "End turn"), labels(buttons));
        assertEquals(new Command.ChooseRoute(Route.LOOP), buttons.get(0).command());
        assertEquals(new Command.ChooseRoute(Route.CITY_CENTRE), buttons.get(1).command());
        assertFalse(endTurn(buttons).enabled());
        assertKeysUnique(buttons);
    }

    @Test
    void nfr02_ac2_afterAGliderRideToCityHallTheDirectionChoices() {
        Game game = TestGames.script().rolls(3, 3, 3).noDisaster().rolls(5).start();
        TestGames.arrive(game, 12);
        Action toCityHall = game.availableActions().stream()
                .filter(a -> a.kind() == Action.Kind.RIDE_GLIDER && a.destination() == 30).findFirst().orElseThrow();
        game.take(toCityHall);
        game.endTurn();
        for (int i = 0; i < 3; i++) {
            game.roll();
            game.endTurn();
        }
        game.roll();
        assertEquals(Game.Phase.AWAITING_DIRECTION, game.phase());
        List<Button> buttons = ActionOptions.forGame(game);
        assertEquals(new Command.ChooseDirection(Heading.TOWARDS_WEST), buttons.get(0).command());
        assertEquals(new Command.ChooseDirection(Heading.TOWARDS_EAST), buttons.get(1).command());
        assertKeysUnique(buttons);
    }

    @Test
    void aCardWaitingOffersOnlyToAnswerIt() {
        Game game = TestGames.script().rolls(2).start();
        TestGames.nextCard(game, "Q01");
        game.roll();
        List<Button> buttons = ActionOptions.forGame(game);
        assertEquals(List.of("Answer the card", "End turn"), labels(buttons));
        assertEquals(new Command.OpenCard(), buttons.get(0).command());
        assertFalse(endTurn(buttons).enabled());
    }

    @Test
    void aBankruptPlayerRollsToReturn() {
        Game game = TestGames.script().start();
        TestGames.bankrupt(TestGames.player(game, "Ben"));
        TestGames.arrive(game, 3);
        game.endTurn();
        List<Button> buttons = ActionOptions.forGame(game);
        assertEquals(List.of("Roll to return", "End turn"), labels(buttons));
        assertEquals('R', buttons.get(0).key());
    }

    @Test
    void afterTheGameNothingIsEnabled() {
        Game game = UiGames.seeded(2);
        UiGames.playUntilRound(game, 99);
        List<Button> buttons = ActionOptions.forGame(game);
        assertEquals(1, buttons.size());
        assertFalse(buttons.get(0).enabled());
    }

    @Test
    void nfr05_ac4_everyKindOfActionHasItsOwnKey() {
        Set<Character> keys = new HashSet<>();
        for (Action.Kind kind : Action.Kind.values()) {
            char key = ActionOptions.key(kind);
            assertTrue(keys.add(key), kind + " shares a key");
            assertTrue(key != 'R' && key != 'E' && key != 'O', kind + " clashes with roll, end turn or open card");
        }
    }
}
