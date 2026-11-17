package netzerobelfast.model;

import netzerobelfast.config.GameConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The character ability hooks, and NFR-07 AC-2: their values come from the data. */
class CharacterHooksTest {

    private final List<Character> cast = Character.fromAll(Games.CONFIG.rules());
    private final Board board = new Board(Games.CONFIG.board());
    private final OwnableSquare green = (OwnableSquare) board.square(0);    // Cave Hill Woodland, £10M
    private final OwnableSquare polluting = (OwnableSquare) board.square(4); // Motorway Expansion, £6M

    private Character character(String id) {
        return cast.stream().filter(c -> c.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void defaultsAreTheNormalValues() {
        Character industrialist = character("INDUSTRIALIST");
        assertEquals(10, industrialist.buyPrice(green));
        assertEquals(6, industrialist.buyPrice(polluting));
        assertEquals(List.of(3, 5, 7), List.of(industrialist.resilienceCost(1), industrialist.resilienceCost(2), industrialist.resilienceCost(3)));
        assertEquals(0.0, industrialist.resilienceCut(0));
        assertEquals(0.5, industrialist.resilienceCut(2));
        assertEquals(1, industrialist.gliderFare());
        assertEquals(0, industrialist.gliderRideGP());
        assertEquals(2, industrialist.lobbyCost());
        assertFalse(industrialist.lobbyCancelsMeter());
        assertEquals(2, ((Industrialist) industrialist).convertedBonusIncome());
    }

    @Test
    void eachAbilityChangesItsOwnHook() {
        assertEquals(7, character("TECH_ENTREPRENEUR").buyPrice(green));
        assertEquals(6, character("TECH_ENTREPRENEUR").buyPrice(polluting));
        assertEquals(1, character("COUNCIL_PLANNER").resilienceCost(1));
        assertEquals(0, character("STUDENT_COMMUTER").gliderFare());
        assertEquals(1, character("STUDENT_COMMUTER").gliderRideGP());
        assertEquals(0, character("COMMUNITY_ACTIVIST").lobbyCost());
        assertTrue(character("COMMUNITY_ACTIVIST").lobbyCancelsMeter());
        assertEquals(1, ((Farmer) character("FARMER")).natureBonusGP());
    }

    /** Loads the real data with {@code from} replaced by {@code to} in config.json, and makes the characters. */
    private static List<Character> withConfigChange(Path folder, String from, String to) throws Exception {
        for (String name : new String[] {GameConfig.CONFIG_FILE, GameConfig.BOARD_FILE, GameConfig.CARDS_FILE}) {
            Files.write(folder.resolve(name), CharacterHooksTest.class.getResourceAsStream("/data/" + name).readAllBytes());
        }
        Path config = folder.resolve(GameConfig.CONFIG_FILE);
        String text = Files.readString(config, StandardCharsets.UTF_8);
        if (!text.contains(from)) {
            throw new AssertionError("config.json doesn't contain " + from);
        }
        Files.writeString(config, text.replace(from, to), StandardCharsets.UTF_8);
        return Character.fromAll(GameConfig.load(folder).rules());
    }

    @Test
    void nfr07_ac2_abilityValuesComeFromTheData(@TempDir Path folder) throws Exception {
        List<Character> changed = withConfigChange(folder, "\"greenProjectDiscount\": 3", "\"greenProjectDiscount\": 4");
        assertEquals(6, changed.get(1).buyPrice(green), "Tech Entrepreneur, £4M off £10M");
    }

    @Test
    void discountsNeverGoBelowZero(@TempDir Path folder) throws Exception {
        // A £5M discount would make the £3M level cost -£2M; it costs £0 instead.
        Character planner = withConfigChange(folder, "\"resilienceDiscount\": 2", "\"resilienceDiscount\": 5").get(2);
        assertEquals(0, planner.resilienceCost(1));
        assertEquals(0, planner.resilienceCost(2));
        assertEquals(2, planner.resilienceCost(3));
    }
}
