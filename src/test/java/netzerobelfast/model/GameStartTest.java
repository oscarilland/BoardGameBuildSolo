package netzerobelfast.model;

import netzerobelfast.config.GameConfig;
import netzerobelfast.config.Rules.CharacterDef;
import netzerobelfast.model.Game.Phase;
import netzerobelfast.model.Game.PlayerSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starting a game: FR-01 (players, names, characters) and FR-02 (starting values, first player). */
class GameStartTest {

    private static IllegalArgumentException rejected(List<PlayerSetup> setups) {
        return assertThrows(IllegalArgumentException.class, () -> Games.start(new Random(1), setups));
    }

    // ---- FR-01 ---------------------------------------------------------------

    @Test
    void fr01_ac1_acceptsFourToSixPlayers() {
        for (int count = 4; count <= 6; count++) {
            Game game = Games.start(new Random(1), Games.SIX.subList(0, count));
            assertEquals(count, game.players().size());
        }
    }

    @Test
    void fr01_ac1_rejectsThreeOrSevenPlayers() {
        assertEquals("A game needs 4 to 6 players, not 3.", rejected(Games.SIX.subList(0, 3)).getMessage());

        List<PlayerSetup> seven = new ArrayList<>(Games.SIX);
        seven.add(new PlayerSetup("Gail", "FARMER"));
        assertEquals("A game needs 4 to 6 players, not 7.", rejected(seven).getMessage());
    }

    @Test
    void fr01_ac2_charactersMustDiffer() {
        List<PlayerSetup> setups = List.of(
                new PlayerSetup("Aoife", "FARMER"), new PlayerSetup("Ben", "FARMER"),
                new PlayerSetup("Chloe", "INDUSTRIALIST"), new PlayerSetup("Dev", "COUNCIL_PLANNER"));
        assertEquals("Two players can't both be the Farmer.", rejected(setups).getMessage());
    }

    @Test
    void fr01_ac2_unknownCharacterIsRejected() {
        List<PlayerSetup> setups = new ArrayList<>(Games.FOUR);
        setups.set(3, new PlayerSetup("Dev", "PIRATE"));
        assertEquals("There's no character called \"PIRATE\".", rejected(setups).getMessage());
    }

    @Test
    void fr01_ac3_namesAreTrimmed() {
        List<PlayerSetup> setups = new ArrayList<>(Games.FOUR);
        setups.set(0, new PlayerSetup("  Aoife  ", "INDUSTRIALIST"));
        assertEquals("Aoife", Games.start(new Random(1), setups).players().get(0).name());
    }

    @Test
    void fr01_ac3_blankNamesAreRejected() {
        for (String blank : new String[] {"", "   ", null}) {
            List<PlayerSetup> setups = new ArrayList<>(Games.FOUR);
            setups.set(1, new PlayerSetup(blank, "TECH_ENTREPRENEUR"));
            assertEquals("Every player needs a name.", rejected(setups).getMessage());
        }
    }

    @Test
    void fr01_ac3_namesAreAtMostTwentyCharacters() {
        List<PlayerSetup> setups = new ArrayList<>(Games.FOUR);
        setups.set(0, new PlayerSetup("A".repeat(20), "INDUSTRIALIST"));
        assertEquals(20, Games.start(new Random(1), setups).players().get(0).name().length());

        setups.set(0, new PlayerSetup("A".repeat(21), "INDUSTRIALIST"));
        assertEquals("\"" + "A".repeat(21) + "\" is too long: names can be up to 20 characters.",
                rejected(setups).getMessage());

        // Spaces round the name don't count towards the limit.
        setups.set(0, new PlayerSetup("  " + "A".repeat(20) + "  ", "INDUSTRIALIST"));
        assertEquals(20, Games.start(new Random(1), setups).players().get(0).name().length());
    }

    @Test
    void fr01_ac4_namesMustBeUniqueIgnoringCase() {
        List<PlayerSetup> setups = new ArrayList<>(Games.FOUR);
        setups.set(0, new PlayerSetup("Sam", "INDUSTRIALIST"));
        setups.set(1, new PlayerSetup("sam ", "TECH_ENTREPRENEUR"));
        assertEquals("Two players can't both be called \"sam\".", rejected(setups).getMessage());
    }

    // ---- FR-02 ---------------------------------------------------------------

    @Test
    void fr02_ac1_startingValuesComeFromTheCharacter() {
        Game game = Games.start(new Random(1), Games.SIX);
        for (Player player : game.players()) {
            CharacterDef data = Games.CONFIG.rules().characters().stream()
                    .filter(c -> c.id().equals(player.character().id())).findFirst().orElseThrow();
            assertEquals(data.startMoney(), player.money(), player.name());
            assertEquals(data.startGP(), player.greenPoints(), player.name());
        }
        Player industrialist = game.players().get(0);
        assertEquals(40, industrialist.money());
        assertEquals(8, industrialist.greenPoints());
        Player activist = game.players().get(5);
        assertEquals(32, activist.money());
        assertEquals(18, activist.greenPoints());
    }

    @Test
    void fr02_ac2_changingTheDataChangesStartingMoney(@TempDir Path folder) throws Exception {
        for (String name : new String[] {GameConfig.CONFIG_FILE, GameConfig.BOARD_FILE, GameConfig.CARDS_FILE}) {
            Files.write(folder.resolve(name), GameStartTest.class.getResourceAsStream("/data/" + name).readAllBytes());
        }
        Path config = folder.resolve(GameConfig.CONFIG_FILE);
        String text = Files.readString(config, StandardCharsets.UTF_8);
        Files.writeString(config, text.replaceFirst("\"startMoney\": 40", "\"startMoney\": 41"), StandardCharsets.UTF_8);

        Game game = Game.start(GameConfig.load(folder), new Dice(new Random(1)), Games.FOUR);
        assertEquals(41, game.players().get(0).money());
    }

    @Test
    void fr02_ac3_firstPlayerIsRepeatableWithASeed() {
        String first = Games.start(new Random(99), Games.SIX).firstPlayer().name();
        for (int i = 0; i < 5; i++) {
            assertEquals(first, Games.start(new Random(99), Games.SIX).firstPlayer().name());
        }
    }

    @Test
    void fr02_ac3_eachSeatGoesFirstAboutEquallyOften() {
        int[] counts = new int[6];
        for (int seed = 0; seed < 600; seed++) {
            Game game = Games.start(new Random(seed), Games.SIX);
            counts[game.players().indexOf(game.firstPlayer())]++;
        }
        for (int seat = 0; seat < 6; seat++) {
            assertTrue(counts[seat] >= 70 && counts[seat] <= 130, "seat " + seat + " went first " + counts[seat] + " times");
        }
    }

    @Test
    void fr02_ac3_theFirstPlayerStarts() {
        Game game = Games.start(new ScriptedRandom().firstPlayer(2), Games.FOUR);
        assertEquals("Chloe", game.firstPlayer().name());
        assertEquals("Chloe", game.currentPlayer().name());
    }

    @Test
    void fr02_ac4_turnsFollowSeatOrderAndWrap() {
        // Seat 2 (Chloe) goes first: Chloe, Dev, Aoife, Ben, then round 2 starts with Chloe again.
        Game game = Games.start(new ScriptedRandom().firstPlayer(2).rolls(3, 3, 3, 3, 3), Games.FOUR);
        List<String> order = new ArrayList<>();
        for (int turn = 0; turn < 4; turn++) {
            order.add(game.currentPlayer().name());
            assertEquals(1, game.round());
            game.roll();
            game.endTurn();
        }
        assertEquals(List.of("Chloe", "Dev", "Aoife", "Ben"), order);
        assertEquals("Chloe", game.currentPlayer().name());
        assertEquals(2, game.round());
        assertEquals(2028, game.year());
    }

    @Test
    void fr02_ac5_ac6_newGameState() {
        Game game = Games.start(new Random(3), Games.FOUR);
        assertEquals(2026, game.year());
        assertEquals(1, game.round());
        assertEquals(Phase.AWAITING_ROLL, game.phase());
        assertTrue(game.lastMove().isEmpty());
        for (Player player : game.players()) {
            assertEquals(0, player.position(), player.name());
        }
    }

    @Test
    void playersKeepTheirSeatOrder() {
        Game game = Games.start(new Random(5), Games.FOUR);
        assertEquals(List.of("Aoife", "Ben", "Chloe", "Dev"), game.players().stream().map(Player::name).toList());
        assertEquals("Aoife (Industrialist)", game.players().get(0).toString());
    }
}
