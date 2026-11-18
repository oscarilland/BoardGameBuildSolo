package netzerobelfast.ui;

import netzerobelfast.config.CardDef.Effect;
import netzerobelfast.config.CardDef.Source;
import netzerobelfast.model.Game;
import netzerobelfast.model.Game.ReturnAttempt;
import netzerobelfast.model.Player;
import netzerobelfast.model.Square;
import netzerobelfast.model.TestGames;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FR-15 and NFR-05 AC-3: the text the game screen shows. */
class StatusTextTest {

    private final StatusText text = new StatusText(UiGames.CONFIG);

    private static Game fresh() {
        return TestGames.script().start();
    }

    @Test
    void fr15_ac1_topBarShowsRoundYearMeterAndBand() {
        Game game = fresh();
        int rounds = UiGames.CONFIG.rules().game().rounds();
        int year = UiGames.CONFIG.rules().game().startYear();
        assertEquals("Round 1 of " + rounds + " · Year " + year, text.roundLine(game));
        assertEquals("Climate Meter (" + year + " = " + game.meter().value() + ")", text.meterTitle());
        assertEquals(game.meter().value() + " · band " + game.meter().dangerBand(), text.meterValue(game).split(" \\(")[0]);
    }

    @Test
    void fr15_ac1_roundAndYearAdvance() {
        Game game = UiGames.seeded(3);
        UiGames.playUntilRound(game, 2);
        assertTrue(text.roundLine(game).startsWith("Round 2 of 12 · Year 2028"), text.roundLine(game));
    }

    @Test
    void nfr05_ac3_bandHasNumberAndWord() {
        assertEquals("band 1 (low)", StatusText.bandText(1));
        assertEquals("band 2 (moderate)", StatusText.bandText(2));
        assertEquals("band 3 (high)", StatusText.bandText(3));
        assertEquals("band 4 (severe)", StatusText.bandText(4));
        // A band the words don't cover is still shown by number.
        assertEquals("band 5", StatusText.bandText(5));
        assertEquals("band 0", StatusText.bandText(0));
    }

    @Test
    void nfr05_ac3_meterAtZeroAndBelowZero() {
        Game game = fresh();
        TestGames.adjustMeter(game, -game.meter().value());
        assertEquals("0 · band 1 (low)", text.meterValue(game));
        TestGames.adjustMeter(game, -3);
        // Below 0 uses the band the data names (Q-02), and the value is shown as it is.
        assertEquals("-3 · band 1 (low)", text.meterValue(game));
    }

    @Test
    void fr15_ac2_targetMarkerIs26ThenElevenThenNetZero() {
        Game game = UiGames.seeded(5);
        assertEquals(new StatusText.Target("2030 target", 26), text.nextTarget(game));
        UiGames.playUntilRound(game, 3);
        assertEquals("2030 target 26", text.nextTarget(game).text());
        UiGames.playUntilRound(game, 4);
        assertEquals("2040 target 11", text.nextTarget(game).text());
        UiGames.playUntilRound(game, 8);
        assertEquals("2040 target 11", text.nextTarget(game).text());
        UiGames.playUntilRound(game, 9);
        assertEquals("2050 net zero 0", text.nextTarget(game).text());
    }

    @Test
    void fr15_ac4_currentPlayerMarkedInText() {
        Game game = fresh();
        Player aoife = TestGames.player(game, "Aoife");
        Player ben = TestGames.player(game, "Ben");
        assertEquals("1 · Aoife · Industrialist (turn)", text.playerTitle(game, aoife));
        assertEquals("2 · Ben · Tech Entrepreneur", text.playerTitle(game, ben));
    }

    @Test
    void fr15_ac3_playerLineCountsGreenAndPolluting() {
        Game game = fresh();
        Player aoife = TestGames.player(game, "Aoife");
        assertEquals("£" + aoife.money() + "M · " + aoife.greenPoints() + " GP · 0 green, 0 polluting",
                text.playerMoney(game, aoife));
        TestGames.own(game, 0, aoife);
        TestGames.own(game, 1, aoife);
        TestGames.own(game, 4, aoife);
        TestGames.setMoney(aoife, 7);
        assertEquals("£7M · " + aoife.greenPoints() + " GP · 1 green, 2 polluting", text.playerMoney(game, aoife));
    }

    @Test
    void fr15_ac6_resilienceAndDealShown() {
        Game game = fresh();
        Player chloe = TestGames.player(game, "Chloe");
        assertEquals("Resilience 0 · No co-funding deal", text.playerExtras(game, chloe));
        TestGames.raiseResilience(chloe);
        TestGames.deal(chloe, 17);
        assertEquals("Resilience 1 · Funds: Rooftop Solar", text.playerExtras(game, chloe));
    }

    @Test
    void fr15_ac5_bankruptMarked() {
        Game game = fresh();
        Player dev = TestGames.player(game, "Dev");
        assertTrue(text.playerWarning(dev).isEmpty());
        TestGames.bankrupt(dev);
        assertEquals("BANKRUPT: rolls a 6 to return", text.playerWarning(dev).orElseThrow());
    }

    @Test
    void nfr05_ac2_squaresShowTypeAndDetail() {
        Game game = fresh();
        assertEquals("Nature", StatusText.typeLabel(game.board().square(0)));
        assertEquals("Polluting", StatusText.typeLabel(game.board().square(1)));
        assertEquals("Card", StatusText.typeLabel(game.board().square(2)));
        assertEquals("Resilience", StatusText.typeLabel(game.board().square(3)));
        assertEquals("Green", StatusText.typeLabel(game.board().square(5)));
        assertEquals("Glider", StatusText.typeLabel(game.board().square(12)));

        Square gasPlant = game.board().square(6);
        assertTrue(text.squareDetail(game, gasPlant).matches("£\\d+M"));
        TestGames.own(game, 6, TestGames.player(game, "Ben"));
        assertEquals("Owner 2", text.squareDetail(game, gasPlant));
        assertEquals("Draw a card", text.squareDetail(game, game.board().square(2)));
        assertEquals("Protect", text.squareDetail(game, game.board().square(3)));
        assertEquals("£" + UiGames.CONFIG.rules().actions().gliderFare() + "M fare", text.squareDetail(game, game.board().square(12)));

        assertEquals(List.of(), text.squareMarks(game, gasPlant));
        TestGames.lobby(game, 6, TestGames.player(game, "Aoife"));
        assertEquals(List.of("Lobbied"), text.squareMarks(game, gasPlant));
    }

    @Test
    void whereAndPromptFollowThePhase() {
        Game game = TestGames.script().rolls(3).start();
        assertEquals("Aoife, roll the dice.", text.prompt(game));
        game.roll();
        assertEquals("Rolled 3. Aoife is on Milewater Flood Defence (Resilience).", text.whereLine(game));
        assertEquals("Aoife: take an action, or end your turn.", text.prompt(game));
        TestGames.arrive(game, 6);
        assertTrue(text.whereLine(game).startsWith("Aoife is on Harbour Gas Plant (Polluting, £"), text.whereLine(game));
        assertTrue(text.whereLine(game).endsWith("M, unowned)."), text.whereLine(game));
    }

    @Test
    void effectsAreShownWithSigns() {
        assertEquals("+£6M · Meter +2", StatusText.effectText(new Effect(6, 0, 2)));
        assertEquals("-£2M · +1 GP", StatusText.effectText(new Effect(-2, 1, 0)));
        assertEquals("-3 GP · Meter -1", StatusText.effectText(new Effect(0, -3, -1)));
        assertEquals("No change", StatusText.effectText(new Effect(0, 0, 0)));
    }

    @Test
    void sourcesShowTitleAndAddress() {
        assertEquals("Source: DAERA (https://example.org/a)",
                StatusText.sourcesText(List.of(new Source("DAERA", "https://example.org/a"))));
        assertEquals("Sources: A (u1); B (u2)",
                StatusText.sourcesText(List.of(new Source("A", "u1"), new Source("B", "u2"))));
    }

    @Test
    void returnTextCoversEveryOutcome() {
        Game game = fresh();
        Player ben = TestGames.player(game, "Ben");
        assertTrue(text.returnText(ben, new ReturnAttempt(6, true)).startsWith("Ben rolled a 6 and is back with a £"));
        assertEquals("Ben rolled a 2 and needed a 6, so sits this turn out.", text.returnText(ben, new ReturnAttempt(2, false)));
        assertTrue(text.returnText(ben, new ReturnAttempt(0, true)).startsWith("Ben has sat out "));
    }

    @Test
    void roundReportListsTheStepsInOrder() {
        Game game = TestGames.script().rolls(3, 3, 3, 3).noDisaster().start();
        for (int i = 0; i < 4; i++) {
            game.roll();
            game.endTurn();
        }
        List<String> lines = text.roundReport(game, game.lastRound().orElseThrow());
        assertEquals("End of round 1 (2026)", lines.get(0));
        assertTrue(lines.get(1).startsWith("1. "));
        assertTrue(lines.get(2).startsWith("2. Carbon tax"));
        assertTrue(lines.get(3).startsWith("3. Climate Meter: 50 → "), lines.get(3));
        assertEquals("4. Disaster check: rolled 6 (a disaster on " + game.lastRound().orElseThrow().band()
                + " or less): no disaster.", lines.get(4));
        assertEquals("5. Now round 2 · 2028.", lines.get(5));
    }

    @Test
    void roundReportExplainsADisaster() {
        // A 1 on the disaster die is a disaster at any band; value 2 picks the third disaster type (Heatwave).
        Game game = TestGames.script().rolls(3, 3, 3, 3).die(1).values(2).start();
        for (int i = 0; i < 4; i++) {
            game.roll();
            game.endTurn();
        }
        List<String> lines = text.roundReport(game, game.lastRound().orElseThrow());
        String all = String.join("\n", lines);
        assertTrue(all.contains("Heatwave!"), all);
        assertTrue(all.contains("Everyone paid for cooling."), all);
        assertTrue(all.contains("Why: "), all);
        assertTrue(all.contains("Source"), all);
    }

    @Test
    void finalScoresNameTheWinner() {
        Game game = UiGames.seeded(11);
        UiGames.playUntilRound(game, 99);
        assertEquals(Game.Phase.GAME_OVER, game.phase());
        List<String> lines = text.finalScores(game, game.result().orElseThrow());
        assertTrue(lines.get(0).startsWith("Belfast"));
        assertTrue(lines.get(lines.size() - 1).startsWith("Winner: " + game.result().orElseThrow().winner().name()));
        assertEquals(2 + 4 + 1, lines.size());
        assertEquals("Game over · 2050", text.roundLine(game));
        assertFalse(text.playerTitle(game, game.players().get(0)).contains("(turn)"));
    }
}
