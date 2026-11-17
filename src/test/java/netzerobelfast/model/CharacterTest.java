package netzerobelfast.model;

import netzerobelfast.config.Rules.CharacterDef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The character factory picks the right subclass for each data id. Ability hooks are in {@link CharacterHooksTest}. */
class CharacterTest {

    @Test
    void eachIdMakesItsOwnSubclass() {
        List<Character> characters = Character.fromAll(Games.CONFIG.rules());
        assertEquals(6, characters.size());
        assertInstanceOf(Industrialist.class, characters.get(0));
        assertInstanceOf(TechEntrepreneur.class, characters.get(1));
        assertInstanceOf(CouncilPlanner.class, characters.get(2));
        assertInstanceOf(Farmer.class, characters.get(3));
        assertInstanceOf(StudentCommuter.class, characters.get(4));
        assertInstanceOf(CommunityActivist.class, characters.get(5));
    }

    @Test
    void valuesComeFromTheData() {
        Character farmer = Character.fromAll(Games.CONFIG.rules()).get(3);
        assertEquals("FARMER", farmer.id());
        assertEquals("Farmer", farmer.name());
        assertEquals("Farmer", farmer.toString());
        assertEquals(36, farmer.startMoney());
        assertEquals(13, farmer.startGP());
        assertEquals("Nature projects earn +1 GP per round", farmer.ability());
    }

    @Test
    void unknownIdIsRejected() {
        CharacterDef pirate = new CharacterDef("PIRATE", "Pirate", 10, 10, "Arr", Map.of(), Map.of());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Character.from(pirate, Games.CONFIG.rules()));
        assertEquals("config.json: character id 'PIRATE' has no matching character in the game", e.getMessage());
    }

    @Test
    void missingAbilityParameterIsRejectedWhenTheCharacterIsMade() {
        CharacterDef planner = new CharacterDef("COUNCIL_PLANNER", "Council Planner", 38, 12, "x", Map.of(), Map.of());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Character.from(planner, Games.CONFIG.rules()));
        assertEquals("config.json: character COUNCIL_PLANNER is missing params.resilienceDiscount", e.getMessage());
    }

    @Test
    void missingAbilityFlagIsRejected() {
        CharacterDef activist = new CharacterDef("COMMUNITY_ACTIVIST", "Community Activist", 32, 18, "x",
                Map.of("lobbyCost", 0), Map.of());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Character.from(activist, Games.CONFIG.rules()));
        assertEquals("config.json: character COMMUNITY_ACTIVIST is missing params.lobbyCancelsMeter", e.getMessage());
    }
}
