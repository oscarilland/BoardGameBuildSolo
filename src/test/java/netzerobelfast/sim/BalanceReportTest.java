package netzerobelfast.sim;

import netzerobelfast.config.GameConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The balance report behind {@code --simulate}. */
class BalanceReportTest {

    private static final GameConfig CONFIG = GameConfig.load();

    @Test
    void everyGameHasOneWinnerSoWinsAddUpToTheGames() {
        BalanceReport report = BalanceReport.run(CONFIG, 60, 1);
        assertEquals(60, report.games());
        assertEquals(60, report.byCharacter().values().stream().mapToInt(BalanceReport.Rate::wins).sum());
        assertEquals(60, report.byStrategy().values().stream().mapToInt(BalanceReport.Rate::wins).sum());
        assertEquals(6, report.byCharacter().size());
        assertEquals(3, report.byStrategy().size());
    }

    @Test
    void ratesAreBetweenZeroAndOne() {
        BalanceReport report = BalanceReport.run(CONFIG, 60, 2);
        assertTrue(report.netZeroRate() >= 0 && report.netZeroRate() <= 1);
        report.netZeroByMix().values().forEach(rate -> assertTrue(rate >= 0 && rate <= 1));
        report.byCharacter().values().forEach(rate -> {
            assertTrue(rate.winRate() >= 0 && rate.winRate() <= 1);
            assertTrue(rate.fairShare() > 0.15 && rate.fairShare() < 0.26, "fair share for 4-6 players: " + rate.fairShare());
        });
        assertTrue(report.bankruptcyGames() >= 0 && report.bankruptcyGames() <= 1);
        assertTrue(report.disastersPerGame() > 0);
    }

    @Test
    void theSameSeedGivesTheSameReport() {
        assertEquals(BalanceReport.run(CONFIG, 30, 7), BalanceReport.run(CONFIG, 30, 7));
    }

    @Test
    void theTextReportNamesEverything() {
        String text = BalanceReport.run(CONFIG, 12, 3).format();
        assertTrue(text.startsWith("Net Zero Belfast balance report: 12 simulated games"));
        for (String part : new String[] {"Belfast reached net zero", "mostly green", "Farmer", "polluter", "Bankruptcy", "Disasters",
                "A guide, not a playtest"}) {
            assertTrue(text.contains(part), "missing: " + part);
        }
    }

    @Test
    void noGamesGivesAnEmptyReport() {
        BalanceReport report = BalanceReport.run(CONFIG, 0, 1);
        assertEquals(0, report.netZeroRate());
        assertTrue(report.byCharacter().isEmpty());
        assertEquals(0, new BalanceReport.Rate(0, 0, 0.2).winRate());
    }
}
