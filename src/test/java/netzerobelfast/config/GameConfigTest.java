package netzerobelfast.config;

import netzerobelfast.config.BoardDef.SquareDef;
import netzerobelfast.config.BoardDef.SquareType;
import netzerobelfast.config.CardDef.Type;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Loading the real data files (NFR-07 AC-1, AC-2). Broken files are in {@link GameConfigValidationTest}. */
class GameConfigTest {

    private static GameConfig config;

    @BeforeAll
    static void loadRealData() {
        config = GameConfig.load();
    }

    @Test
    void nfr07_ac1_loadsTheBoardAsDesigned() {
        List<SquareDef> squares = config.board().squares();
        assertEquals(36, squares.size());

        Map<SquareType, Long> counts = squares.stream()
                .collect(Collectors.groupingBy(SquareDef::type, Collectors.counting()));
        assertEquals(10, counts.get(SquareType.POLLUTING));
        assertEquals(12, counts.get(SquareType.GREEN_PROJECT));
        assertEquals(4, counts.get(SquareType.RESILIENCE));
        assertEquals(3, counts.get(SquareType.GLIDER_STOP));
        assertEquals(7, counts.get(SquareType.CLIMATE_CARD));
        assertEquals(4, squares.stream().filter(SquareDef::nature).count());

        assertEquals(30, config.board().loop().size());
        assertEquals(List.of(30, 31, 32, 33, 34, 35), config.board().cityCentreRoute());
        assertEquals(27, config.board().westJunction());
        assertEquals(12, config.board().eastJunction());
        assertEquals(List.of(12, 27, 30), config.board().gliderStops());
        assertEquals(6, config.board().districts().size());
    }

    @Test
    void nfr07_ac1_readsSquareDetails() {
        SquareDef caveHill = config.board().square(0);
        assertEquals("Cave Hill Woodland", caveHill.name());
        assertEquals(SquareType.GREEN_PROJECT, caveHill.type());
        assertEquals("NORTH", caveHill.district());
        assertEquals(10, caveHill.price());
        assertTrue(caveHill.nature());
        assertTrue(caveHill.ownable());

        SquareDef gliderEast = config.board().square(12);
        assertEquals(SquareType.GLIDER_STOP, gliderEast.type());
        assertNull(gliderEast.price());
        assertFalse(gliderEast.ownable());
    }

    @Test
    void q05_workingBoardRecordsTwoWayCutThrough() {
        assertTrue(config.board().cityCentreDirection().startsWith("two-way"));
    }

    @Test
    void nfr07_ac1_loadsTheCharacters() {
        List<Rules.CharacterDef> characters = config.rules().characters();
        assertEquals(6, characters.size());

        Map<String, Rules.CharacterDef> byId = characters.stream()
                .collect(Collectors.toMap(Rules.CharacterDef::id, Function.identity()));
        Rules.CharacterDef industrialist = byId.get("INDUSTRIALIST");
        assertEquals(40, industrialist.startMoney());
        assertEquals(8, industrialist.startGP());
        assertEquals(2, industrialist.numbers().get("convertedBonusIncome"));

        Rules.CharacterDef activist = byId.get("COMMUNITY_ACTIVIST");
        assertEquals(32, activist.startMoney());
        assertEquals(18, activist.startGP());
        assertEquals(0, activist.numbers().get("lobbyCost"));
        assertTrue(activist.flags().get("lobbyCancelsMeter"));

        Rules.CharacterDef student = byId.get("STUDENT_COMMUTER");
        assertEquals(0, student.numbers().get("gliderFare"));
        assertEquals(1, student.numbers().get("gliderRideGP"));
    }

    @Test
    void nfr07_ac1_loadsTheBalanceValues() {
        Rules rules = config.rules();
        assertEquals(new Rules.Game(4, 6, 12, 2026, 2, 2050), rules.game());
        assertEquals(new Rules.Polluting(4, 7, 7, 1, 2), rules.polluting());
        assertEquals(new Rules.Green(8, 12, 2, 2, -1), rules.green());
        assertEquals(new Rules.Actions(5, 1, 3, 1, 1, 2, 1), rules.actions());
        assertEquals(List.of(3, 5, 7), rules.resilience().levelCosts());
        assertEquals(List.of(0.25, 0.5, 0.75), rules.resilience().disasterCostCut());
        assertEquals(3, rules.resilience().maxLevel());
        assertEquals(2, rules.quizCorrectGP());
        assertEquals(new Rules.Disasters(1, 1, 2, 1, 1), rules.disasters());
        assertEquals(new Rules.Bankruptcy(6, 2, 10, 15), rules.bankruptcy());
        assertEquals(new Rules.Scoring(5, 5), rules.scoring());
    }

    @Test
    void nfr07_ac1_loadsTheClimateMeter() {
        Rules.Meter meter = config.rules().meter();
        assertEquals(50, meter.start());
        assertEquals(3, meter.basePerRound());
        assertEquals(0, meter.netZeroAtOrBelow());
        assertEquals(List.of(new Rules.InterimTarget(2030, 3, 26), new Rules.InterimTarget(2040, 8, 11)),
                meter.interimTargets());
        assertEquals(2, meter.interimMissPenaltyGP());
        assertEquals(List.of(
                new Rules.DangerBand(1, 10), new Rules.DangerBand(2, 25),
                new Rules.DangerBand(3, 40), new Rules.DangerBand(4, null)), meter.dangerBands());
        assertEquals(1, meter.belowZeroBand());
    }

    @Test
    void nfr07_ac1_loadsTheDeck() {
        List<CardDef> cards = config.cards();
        assertEquals(30, cards.size());
        Map<Type, Long> counts = cards.stream().collect(Collectors.groupingBy(CardDef::type, Collectors.counting()));
        assertEquals(12, counts.get(Type.QUIZ));
        assertEquals(9, counts.get(Type.CHOICE));
        assertEquals(9, counts.get(Type.EVENT));

        for (CardDef card : cards) {
            assertFalse(card.fact().isBlank(), card.id());
            assertFalse(card.sources().isEmpty(), card.id());
            switch (card.type()) {
                case QUIZ -> assertEquals(4, card.answers().size(), card.id());
                case CHOICE -> assertEquals(2, card.options().size(), card.id());
                case EVENT -> assertTrue(card.effect() != null, card.id());
            }
        }

        CardDef q01 = cards.get(0);
        assertEquals("Q01", q01.id());
        assertEquals(2, q01.correctIndex());
        assertEquals("2050", q01.answers().get(2));

        CardDef c01 = cards.stream().filter(c -> c.id().equals("C01")).findFirst().orElseThrow();
        assertEquals(new CardDef.Effect(6, 0, 2), c01.options().get(0).effect());
        assertEquals(new CardDef.Effect(-2, 1, 0), c01.options().get(1).effect());

        CardDef c05 = cards.stream().filter(c -> c.id().equals("C05")).findFirst().orElseThrow();
        assertEquals(2, c05.sources().size());
    }

    @Test
    void readsNonAsciiText() {
        CardDef e01 = config.cards().stream().filter(c -> c.id().equals("E01")).findFirst().orElseThrow();
        assertTrue(e01.fact().contains("Éowyn"), e01.fact());
    }

    @Test
    void summaryDescribesWhatWasLoaded() {
        assertEquals("Loaded 36 squares, 30 cards, 6 characters", config.summary());
    }

    @Test
    void nfr07_ac2_valuesComeFromTheDataFiles(@TempDir Path folder) {
        TestData.copyInto(folder);
        TestData.edit(folder, GameConfig.CONFIG_FILE,
                c -> TestData.obj(c, "squares", "polluting").addProperty("incomePerRound", 6));

        assertEquals(6, GameConfig.load(folder).rules().polluting().incomePerRound());
    }

    @Test
    void loadingFromAFolderMatchesThePackagedData(@TempDir Path folder) {
        GameConfig copy = GameConfig.load(TestData.copyInto(folder));
        assertEquals(config.rules(), copy.rules());
        assertEquals(config.board(), copy.board());
        assertEquals(config.cards(), copy.cards());
    }

    @Test
    void q13_quizRewardUsesTheCardsOwnValue(@TempDir Path folder) {
        TestData.copyInto(folder);
        TestData.edit(folder, GameConfig.CARDS_FILE,
                c -> TestData.obj(TestData.card(c, 0), "reward").addProperty("gp", 3));

        GameConfig changed = GameConfig.load(folder);
        assertEquals(3, changed.quizReward(changed.cards().get(0)));
    }

    @Test
    void q13_quizRewardFallsBackToTheConfigDefault(@TempDir Path folder) {
        TestData.copyInto(folder);
        TestData.edit(folder, GameConfig.CARDS_FILE, c -> TestData.card(c, 0).remove("reward"));
        TestData.edit(folder, GameConfig.CONFIG_FILE,
                c -> TestData.obj(c, "squares", "climateCard").addProperty("quizCorrectGP", 5));

        GameConfig changed = GameConfig.load(folder);
        CardDef q01 = changed.cards().get(0);
        assertNull(q01.rewardGP());
        assertEquals(5, changed.quizReward(q01));
    }

    @Test
    void commentKeysAreIgnored(@TempDir Path folder) {
        TestData.copyInto(folder);
        TestData.edit(folder, GameConfig.CONFIG_FILE, c -> {
            TestData.obj(TestData.character(c, 0), "params").addProperty("_note", "a comment");
            c.addProperty("_another", 1);
        });
        TestData.edit(folder, GameConfig.BOARD_FILE,
                b -> TestData.obj(b, "districts").addProperty("_note", "not a district"));

        GameConfig changed = GameConfig.load(folder);
        assertEquals(6, changed.board().districts().size());
        assertFalse(changed.rules().characters().get(0).numbers().containsKey("_note"));
    }

    @Test
    void nfr07_ac3_aMissingFolderIsReportedByFileName(@TempDir Path folder) {
        ConfigException e = assertThrows(ConfigException.class, () -> GameConfig.load(folder.resolve("nope")));
        assertEquals("config.json: not found", e.getMessage());
    }
}
