package netzerobelfast.model;

import netzerobelfast.config.Rules.CharacterDef;

import java.util.ArrayList;
import java.util.List;

/**
 * One of the six characters a player can play as (analysis model: {@code Character} {abstract}).
 *
 * <p>Each character has a subclass, so its ability can change the rules by overriding a
 * method, without other classes needing to know which character it is. The values (name,
 * starting money and GP, ability numbers) all come from {@code config.json} through
 * {@link CharacterDef}; the subclass only decides how they're used.
 *
 * <p>The ability hooks ({@code priceFor}, {@code incomeBonus} and so on) are added in plan 003,
 * when buying and income first exist.
 */
public abstract class Character {

    private final CharacterDef definition;

    protected Character(CharacterDef definition) {
        this.definition = definition;
    }

    /**
     * Makes the character for a data entry, choosing the subclass by the entry's {@code id}.
     *
     * @throws IllegalArgumentException if no subclass matches the id
     */
    public static Character from(CharacterDef definition) {
        return switch (definition.id()) {
            case "INDUSTRIALIST" -> new Industrialist(definition);
            case "TECH_ENTREPRENEUR" -> new TechEntrepreneur(definition);
            case "COUNCIL_PLANNER" -> new CouncilPlanner(definition);
            case "FARMER" -> new Farmer(definition);
            case "STUDENT_COMMUTER" -> new StudentCommuter(definition);
            case "COMMUNITY_ACTIVIST" -> new CommunityActivist(definition);
            default -> throw new IllegalArgumentException(
                    "config.json: character id '" + definition.id() + "' has no matching character in the game");
        };
    }

    /** Makes every character in the data; used at start-up so an unknown id fails at once. */
    public static List<Character> fromAll(List<CharacterDef> definitions) {
        List<Character> characters = new ArrayList<>();
        for (CharacterDef definition : definitions) {
            characters.add(from(definition));
        }
        return List.copyOf(characters);
    }

    public String id() {
        return definition.id();
    }

    public String name() {
        return definition.name();
    }

    public int startMoney() {
        return definition.startMoney();
    }

    public int startGP() {
        return definition.startGP();
    }

    /** The ability, in words, for the setup screen and the player panel. */
    public String ability() {
        return definition.ability();
    }

    @Override
    public String toString() {
        return name();
    }
}
