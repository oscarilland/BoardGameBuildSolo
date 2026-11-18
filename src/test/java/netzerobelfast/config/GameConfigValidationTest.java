package netzerobelfast.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static netzerobelfast.config.GameConfig.BOARD_FILE;
import static netzerobelfast.config.GameConfig.CARDS_FILE;
import static netzerobelfast.config.GameConfig.CONFIG_FILE;
import static netzerobelfast.config.TestData.card;
import static netzerobelfast.config.TestData.character;
import static netzerobelfast.config.TestData.obj;
import static netzerobelfast.config.TestData.square;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NFR-07 AC-3 and NFR-08 AC-5: a broken data file stops loading with a message that names the
 * file and the key. Each test copies the real data and breaks exactly one thing.
 */
class GameConfigValidationTest {

    @TempDir
    Path folder;

    @BeforeEach
    void copyRealData() {
        TestData.copyInto(folder);
    }

    /** Applies {@code change} to {@code file}, loads, and checks the error message. */
    private void assertRejected(String file, Consumer<JsonObject> change, String expectedMessage) {
        TestData.edit(folder, file, change);
        assertMessage(expectedMessage);
    }

    private void assertMessage(String expectedMessage) {
        ConfigException e = assertThrows(ConfigException.class, () -> GameConfig.load(folder));
        assertEquals(expectedMessage, e.getMessage());
    }

    private void assertMessageStartsWith(String expectedStart) {
        ConfigException e = assertThrows(ConfigException.class, () -> GameConfig.load(folder));
        assertTrue(e.getMessage().startsWith(expectedStart), e.getMessage());
    }

    // ---- Files and JSON syntax ------------------------------------------------

    @Test
    void missingFile() throws IOException {
        Files.delete(folder.resolve(BOARD_FILE));
        assertMessage("board.json: not found");
    }

    @Test
    void malformedJson() {
        TestData.write(folder, CONFIG_FILE, "{ \"game\": ");
        assertMessageStartsWith("config.json: is not valid JSON");
    }

    @Test
    void unquotedKeysAreRejected() {
        // Gson accepts this in its default lenient mode; the loader reads strictly.
        TestData.write(folder, CONFIG_FILE, "{ game: {} }");
        assertMessageStartsWith("config.json: is not valid JSON");
    }

    @Test
    void textAfterTheEndIsRejected() {
        TestData.write(folder, CARDS_FILE, "{ \"cards\": [] } extra");
        assertMessageStartsWith("climate_cards.json: is not valid JSON");
    }

    @Test
    void topLevelMustBeAnObject() {
        TestData.write(folder, CONFIG_FILE, "[]");
        assertMessage("config.json: (top level) should be an object");
    }

    // ---- Missing keys and wrong types ----------------------------------------

    @Test
    void missingKey() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").remove("rounds"), "config.json: game.rounds is missing");
    }

    @Test
    void nullValue() {
        assertRejected(CONFIG_FILE, c -> obj(c, "bankruptcy").add("loanOnReturn", JsonNull.INSTANCE),
                "config.json: bankruptcy.loanOnReturn is missing");
    }

    @Test
    void textWhereANumberIsExpected() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("rounds", "twelve"),
                "config.json: game.rounds should be a whole number");
    }

    @Test
    void fractionWhereAWholeNumberIsExpected() {
        assertRejected(CONFIG_FILE, c -> obj(c, "squares", "polluting").addProperty("incomePerRound", 7.5),
                "config.json: squares.polluting.incomePerRound should be a whole number, but is 7.5");
    }

    @Test
    void textWhereAFractionIsExpected() {
        assertRejected(CONFIG_FILE, c -> {
            JsonArray cuts = obj(c, "squares", "resilience").getAsJsonArray("disasterCostCut");
            cuts.set(0, new com.google.gson.JsonPrimitive("quarter"));
        }, "config.json: squares.resilience.disasterCostCut[0] should be a number");
    }

    @Test
    void numberWhereAListIsExpected() {
        assertRejected(CONFIG_FILE, c -> obj(c, "squares", "resilience").addProperty("levelCosts", 3),
                "config.json: squares.resilience.levelCosts should be a list");
    }

    @Test
    void numberWhereTextIsExpected() {
        assertRejected(BOARD_FILE, b -> square(b, 3).addProperty("name", 3),
                "board.json: squares[3].name should be text");
    }

    @Test
    void wrongTypeForAFlag() {
        assertRejected(BOARD_FILE, b -> square(b, 0).addProperty("isNature", "yes"),
                "board.json: squares[0].isNature should be true or false");
    }

    @Test
    void blankText() {
        assertRejected(CARDS_FILE, c -> card(c, 4).addProperty("fact", "   "),
                "climate_cards.json: cards[4].fact should not be empty");
    }

    @Test
    void priceRangeNeedsTwoValues() {
        assertRejected(CONFIG_FILE, c -> {
            JsonArray range = new JsonArray();
            range.add(4);
            obj(c, "squares", "polluting").add("priceRange", range);
        }, "config.json: squares.polluting.priceRange should be [lowest, highest]");
    }

    // ---- config.json rules ---------------------------------------------------

    @Test
    void minPlayersAboveMaxPlayers() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("minPlayers", 7),
                "config.json: game.minPlayers is 7, but game.maxPlayers is 6");
    }

    @Test
    void zeroMinPlayers() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("minPlayers", 0),
                "config.json: game.minPlayers is 0, expected at least 1");
    }

    @Test
    void missingMaxNameLength() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").remove("maxNameLength"),
                "config.json: game.maxNameLength is missing");
    }

    @Test
    void zeroMaxNameLength() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("maxNameLength", 0),
                "config.json: game.maxNameLength is 0, expected at least 1");
    }

    @Test
    void zeroRounds() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("rounds", 0),
                "config.json: game.rounds is 0, expected at least 1");
    }

    @Test
    void zeroYearsPerRound() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("yearsPerRound", 0),
                "config.json: game.yearsPerRound is 0, expected at least 1");
    }

    @Test
    void yearsDontAddUp() {
        assertRejected(CONFIG_FILE, c -> obj(c, "game").addProperty("endYear", 2052),
                "config.json: game.endYear is 2052, but startYear + rounds × yearsPerRound is 2050");
    }

    @Test
    void fewerCharactersThanPlayers() {
        assertRejected(CONFIG_FILE, c -> c.getAsJsonArray("characters").remove(0),
                "config.json: characters has 5, but each of up to 6 players needs a different one");
    }

    @Test
    void duplicateCharacterId() {
        assertRejected(CONFIG_FILE, c -> character(c, 1).addProperty("id", "INDUSTRIALIST"),
                "config.json: characters[1].id 'INDUSTRIALIST' is used twice");
    }

    @Test
    void negativeStartingMoney() {
        assertRejected(CONFIG_FILE, c -> character(c, 2).addProperty("startMoney", -1),
                "config.json: characters[2].startMoney is negative");
    }

    @Test
    void priceRangeBackwards() {
        assertRejected(CONFIG_FILE, c -> {
            JsonArray range = new JsonArray();
            range.add(12);
            range.add(8);
            obj(c, "squares", "greenProject").add("priceRange", range);
        }, "config.json: squares.greenProject.priceRange is [12, 8], but the lowest price is above the highest");
    }

    @Test
    void resilienceCostsDontMatchMaxLevel() {
        assertRejected(CONFIG_FILE, c -> obj(c, "squares", "resilience").addProperty("maxLevel", 4),
                "config.json: squares.resilience.levelCosts has 3 costs, but maxLevel is 4");
    }

    @Test
    void resilienceCutsDontMatchMaxLevel() {
        assertRejected(CONFIG_FILE, c -> obj(c, "squares", "resilience").getAsJsonArray("disasterCostCut").remove(2),
                "config.json: squares.resilience.disasterCostCut has 2 cuts, but maxLevel is 3");
    }

    @Test
    void resilienceCutAboveOne() {
        assertRejected(CONFIG_FILE, c -> obj(c, "squares", "resilience").getAsJsonArray("disasterCostCut")
                        .set(2, new com.google.gson.JsonPrimitive(1.5)),
                "config.json: squares.resilience.disasterCostCut[2] is 1.5, expected more than 0 and at most 1");
    }

    @Test
    void interimTargetAfterTheLastRound() {
        assertRejected(CONFIG_FILE, c -> obj(c, "climateMeter").getAsJsonArray("interimTargets")
                        .get(1).getAsJsonObject().addProperty("round", 13),
                "config.json: climateMeter.interimTargets[1].round is 13, expected 1-12");
    }

    @Test
    void dangerBandsOutOfOrder() {
        assertRejected(CONFIG_FILE, c -> band(c, 2).addProperty("maxMeter", 20),
                "config.json: climateMeter.dangerBands[2].maxMeter is 20, but must be higher than the band below (25)");
    }

    @Test
    void dangerBandsMisnumbered() {
        assertRejected(CONFIG_FILE, c -> band(c, 1).addProperty("band", 3),
                "config.json: climateMeter.dangerBands[1].band is 3, expected 2");
    }

    @Test
    void openEndedBandThatIsntTheTop() {
        assertRejected(CONFIG_FILE, c -> band(c, 1).add("maxMeter", JsonNull.INSTANCE),
                "config.json: climateMeter.dangerBands[1].maxMeter is null, but only the top band can be open-ended");
    }

    @Test
    void topBandWithALimit() {
        assertRejected(CONFIG_FILE, c -> band(c, 3).addProperty("maxMeter", 60),
                "config.json: climateMeter.dangerBands[3].maxMeter should be null: the top band has no upper limit");
    }

    @Test
    void noDangerBands() {
        assertRejected(CONFIG_FILE, c -> obj(c, "climateMeter").add("dangerBands", new JsonArray()),
                "config.json: climateMeter.dangerBands is empty");
    }

    @Test
    void belowZeroBandDoesntExist() {
        assertRejected(CONFIG_FILE, c -> obj(c, "climateMeter").addProperty("belowZeroUsesBand", 5),
                "config.json: climateMeter.belowZeroUsesBand is 5, expected 1-4");
    }

    private static JsonObject band(JsonObject config, int index) {
        return obj(config, "climateMeter").getAsJsonArray("dangerBands").get(index).getAsJsonObject();
    }

    // ---- board.json rules ----------------------------------------------------

    @Test
    void squaresOutOfIdOrder() {
        assertRejected(BOARD_FILE, b -> square(b, 7).addProperty("id", 8),
                "board.json: squares[7].id is 8, expected 7 (squares must be listed in id order, starting at 0)");
    }

    @Test
    void unknownDistrict() {
        assertRejected(BOARD_FILE, b -> square(b, 5).addProperty("district", "BANGOR"),
                "board.json: squares[5].district is 'BANGOR', which isn't in districts");
    }

    @Test
    void unknownSquareType() {
        assertRejected(BOARD_FILE, b -> square(b, 1).addProperty("type", "FACTORY"),
                "board.json: squares[1].type is 'FACTORY', expected one of "
                        + "[POLLUTING, GREEN_PROJECT, RESILIENCE, GLIDER_STOP, CLIMATE_CARD]");
    }

    @Test
    void greenPriceOutOfRange() {
        assertRejected(BOARD_FILE, b -> square(b, 7).addProperty("price", 3),
                "board.json: squares[7].price is 3, expected 8-12 for GREEN_PROJECT");
    }

    @Test
    void pollutingPriceOutOfRange() {
        assertRejected(BOARD_FILE, b -> square(b, 1).addProperty("price", 99),
                "board.json: squares[1].price is 99, expected 4-7 for POLLUTING");
    }

    @Test
    void missingPrice() {
        assertRejected(BOARD_FILE, b -> square(b, 1).remove("price"),
                "board.json: squares[1].price is missing; POLLUTING squares need a price");
    }

    @Test
    void priceOnASquareThatCantBeBought() {
        assertRejected(BOARD_FILE, b -> square(b, 3).addProperty("price", 3),
                "board.json: squares[3].price is set, but RESILIENCE squares can't be bought");
    }

    @Test
    void natureFlagOnAPollutingSquare() {
        assertRejected(BOARD_FILE, b -> square(b, 1).addProperty("isNature", false),
                "board.json: squares[1].isNature is only allowed on GREEN_PROJECT squares");
    }

    @Test
    void squareMissingFromTheRoutes() {
        assertRejected(BOARD_FILE, b -> obj(b, "loop").getAsJsonArray("squareIds").remove(5),
                "board.json: loop/cityCentreRoute cover 35 of the 36 squares; every square must be on one of them");
    }

    @Test
    void squareOnTwoRoutes() {
        assertRejected(BOARD_FILE, b -> obj(b, "cityCentreRoute").getAsJsonArray("squareIds")
                        .set(0, new com.google.gson.JsonPrimitive(5)),
                "board.json: loop/cityCentreRoute list square 5 more than once");
    }

    @Test
    void routeListsASquareThatDoesntExist() {
        assertRejected(BOARD_FILE, b -> obj(b, "loop").getAsJsonArray("squareIds").add(40),
                "board.json: loop/cityCentreRoute lists square 40, which doesn't exist");
    }

    @Test
    void junctionOffTheLoop() {
        assertRejected(BOARD_FILE, b -> b.getAsJsonArray("junctions").set(1, new com.google.gson.JsonPrimitive(31)),
                "board.json: junctions[1] is 31, which isn't on the loop");
    }

    @Test
    void westJunctionNotAJunction() {
        assertRejected(BOARD_FILE, b -> obj(b, "cityCentreRoute", "connects").addProperty("westJunction", 26),
                "board.json: cityCentreRoute.connects.westJunction is 26, which isn't in junctions");
    }

    @Test
    void eastJunctionNotAJunction() {
        assertRejected(BOARD_FILE, b -> obj(b, "cityCentreRoute", "connects").addProperty("eastJunction", 11),
                "board.json: cityCentreRoute.connects.eastJunction is 11, which isn't in junctions");
    }

    @Test
    void gliderStopThatIsntAGliderSquare() {
        assertRejected(BOARD_FILE, b -> b.getAsJsonArray("gliderStops").set(2, new com.google.gson.JsonPrimitive(0)),
                "board.json: gliderStops[2] is 0, which isn't a GLIDER_STOP square");
    }

    @Test
    void gliderSquareNotListedAsAStop() {
        assertRejected(BOARD_FILE, b -> b.getAsJsonArray("gliderStops").remove(2),
                "board.json: gliderStops doesn't list square 30 (Glider City Hall), which is a GLIDER_STOP");
    }

    @Test
    void tooFewGliderStops() {
        assertRejected(BOARD_FILE, b -> {
            // Make 27 and 30 ordinary Climate Card squares, leaving one stop.
            square(b, 27).addProperty("type", "CLIMATE_CARD");
            square(b, 30).addProperty("type", "CLIMATE_CARD");
            JsonArray stops = new JsonArray();
            stops.add(12);
            b.add("gliderStops", stops);
        }, "board.json: gliderStops needs at least 2 stops for a ride to go anywhere");
    }

    @Test
    void noSquares() {
        assertRejected(BOARD_FILE, b -> b.add("squares", new JsonArray()), "board.json: squares is empty");
    }

    // ---- climate_cards.json rules --------------------------------------------

    @Test
    void noCards() {
        assertRejected(CARDS_FILE, c -> c.add("cards", new JsonArray()), "climate_cards.json: cards is empty");
    }

    @Test
    void duplicateCardId() {
        assertRejected(CARDS_FILE, c -> card(c, 1).addProperty("id", "Q01"),
                "climate_cards.json: cards[1].id 'Q01' is used twice");
    }

    @Test
    void unknownCardType() {
        assertRejected(CARDS_FILE, c -> card(c, 0).addProperty("type", "PUZZLE"),
                "climate_cards.json: cards[0].type is 'PUZZLE', expected one of [QUIZ, CHOICE, EVENT]");
    }

    @Test
    void quizCorrectIndexOutOfRange() {
        assertRejected(CARDS_FILE, c -> card(c, 0).addProperty("correctIndex", 4),
                "climate_cards.json: cards[0].correctIndex is 4, expected 0-3");
    }

    @Test
    void quizWithOneAnswer() {
        assertRejected(CARDS_FILE, c -> {
            JsonArray answers = new JsonArray();
            answers.add("2050");
            card(c, 0).add("answers", answers);
            card(c, 0).addProperty("correctIndex", 0);
        }, "climate_cards.json: cards[0].answers needs at least 2 answers");
    }

    @Test
    void negativeQuizReward() {
        assertRejected(CARDS_FILE, c -> obj(card(c, 0), "reward").addProperty("gp", -1),
                "climate_cards.json: cards[0].reward.gp is negative");
    }

    @Test
    void fr10_ac8_choiceCardWhereEveryOptionCostsMoney() {
        // Card 12 is C01: "Take the deal" (+6) becomes a cost, so no option is affordable to a broke player.
        assertRejected(CARDS_FILE, c -> card(c, 12).getAsJsonArray("options").get(0).getAsJsonObject()
                        .addProperty("money", -1),
                "climate_cards.json: cards[12].options all cost money; at least one option must have money of 0 "
                        + "or more, so every player can choose (FR-10 AC-8)");
    }

    @Test
    void choiceCardWithThreeOptions() {
        assertRejected(CARDS_FILE, c -> {
            JsonArray options = card(c, 12).getAsJsonArray("options");
            options.add(options.get(0).deepCopy());
        }, "climate_cards.json: cards[12].options has 3, expected 2 (option A and option B)");
    }

    @Test
    void eventCardWithoutAnEffect() {
        assertRejected(CARDS_FILE, c -> card(c, 21).remove("effect"),
                "climate_cards.json: cards[21].effect is missing");
    }

    @Test
    void nfr10_ac1_cardWithNoSources() {
        assertRejected(CARDS_FILE, c -> card(c, 5).add("sources", new JsonArray()),
                "climate_cards.json: cards[5].sources is empty; every fact needs a source (NFR-10)");
    }

    @Test
    void nfr10_ac2_disasterWithoutAnExplanation() {
        assertRejected(CONFIG_FILE, c -> obj(c, "disasters", "STORM").remove("explanation"),
                "config.json: disasters.STORM.explanation is missing");
    }

    @Test
    void nfr10_ac2_disasterExplanationWithoutASource() {
        assertRejected(CONFIG_FILE, c -> obj(c, "disasters", "DROUGHT").add("sources", new JsonArray()),
                "config.json: disasters.DROUGHT.sources is empty; every explanation needs a source (NFR-10)");
    }

    @Test
    void disasterEffectsLastAtLeastOneRound() {
        assertRejected(CONFIG_FILE, c -> obj(c, "disasters", "FLOOD").addProperty("lostIncomeRounds", 0),
                "config.json: disasters.FLOOD.lostIncomeRounds should be at least 1");
    }

    @Test
    void stormClosuresLastAtLeastOneRound() {
        assertRejected(CONFIG_FILE, c -> obj(c, "disasters", "STORM").addProperty("closedRounds", 0),
                "config.json: disasters.STORM.closedRounds should be at least 1");
    }

    @Test
    void droughtsLastAtLeastOneRound() {
        assertRejected(CONFIG_FILE, c -> obj(c, "disasters", "DROUGHT").addProperty("lostGPRounds", 0),
                "config.json: disasters.DROUGHT.lostGPRounds should be at least 1");
    }

    @Test
    void nfr10_ac1_sourceWithoutAUrl() {
        assertRejected(CARDS_FILE, c -> card(c, 5).getAsJsonArray("sources").get(0).getAsJsonObject().remove("url"),
                "climate_cards.json: cards[5].sources[0].url is missing");
    }
}
