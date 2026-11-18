package netzerobelfast.model;

import netzerobelfast.config.Rules;
import netzerobelfast.config.Rules.CharacterDef;

import java.util.ArrayList;
import java.util.List;

/**
 * One of the six characters a player can play as (analysis model: {@code Character} {abstract}).
 *
 * <p>Each character has a subclass, so its ability can change the rules by overriding a hook
 * such as {@link #buyPrice} or {@link #lobbyCost}, without {@code Game} needing to know which
 * character it is. The defaults here read the normal values from {@code config.json}; a
 * subclass overrides a hook using its own {@code params} from the data. No ability numbers
 * are written in code (NFR-07).
 */
public abstract class Character {

    private final CharacterDef definition;
    private final Rules rules;

    protected Character(CharacterDef definition, Rules rules) {
        this.definition = definition;
        this.rules = rules;
    }

    /**
     * Makes the character for a data entry, choosing the subclass by the entry's {@code id}.
     *
     * @throws IllegalArgumentException if no subclass matches the id, or a parameter it needs is missing
     */
    public static Character from(CharacterDef definition, Rules rules) {
        return switch (definition.id()) {
            case "INDUSTRIALIST" -> new Industrialist(definition, rules);
            case "TECH_ENTREPRENEUR" -> new TechEntrepreneur(definition, rules);
            case "COUNCIL_PLANNER" -> new CouncilPlanner(definition, rules);
            case "FARMER" -> new Farmer(definition, rules);
            case "STUDENT_COMMUTER" -> new StudentCommuter(definition, rules);
            case "COMMUNITY_ACTIVIST" -> new CommunityActivist(definition, rules);
            default -> throw new IllegalArgumentException(
                    "config.json: character id '" + definition.id() + "' has no matching character in the game");
        };
    }

    /** Makes every character in the data; used at start-up so a bad character entry fails at once. */
    public static List<Character> fromAll(Rules rules) {
        List<Character> characters = new ArrayList<>();
        for (CharacterDef definition : rules.characters()) {
            characters.add(from(definition, rules));
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

    protected Rules rules() {
        return rules;
    }

    /**
     * Reads a whole-number ability parameter from this character's {@code params} in the data.
     * Subclasses call it in their constructors, so a missing parameter stops the game at start-up.
     */
    protected int number(String key) {
        Integer value = definition.numbers().get(key);
        if (value == null) {
            throw new IllegalArgumentException("config.json: character " + id() + " is missing params." + key);
        }
        return value;
    }

    /** Reads a true/false ability parameter from this character's {@code params}. */
    protected boolean flag(String key) {
        Boolean value = definition.flags().get(key);
        if (value == null) {
            throw new IllegalArgumentException("config.json: character " + id() + " is missing params." + key);
        }
        return value;
    }

    // ---- Ability hooks -------------------------------------------------------

    /** What this character pays to buy the square (FR-04). Tech Entrepreneur: less for Green Projects. */
    public int buyPrice(OwnableSquare square) {
        return square.price();
    }

    /** What this character pays for Resilience level {@code level} (1 to max). Council Planner: less. */
    public int resilienceCost(int level) {
        return rules.resilience().levelCosts().get(level - 1);
    }

    /** The share of disaster payments that Resilience level {@code level} removes; 0 at level 0. */
    public double resilienceCut(int level) {
        return level == 0 ? 0 : rules.resilience().disasterCostCut().get(level - 1);
    }

    /** The Glider fare. Student Commuter: free. */
    public int gliderFare() {
        return rules.actions().gliderFare();
    }

    /** GP earned for each Glider ride. Student Commuter: some; everyone else none. */
    public int gliderRideGP() {
        return 0;
    }

    /** The cost of lobbying. Community Activist: free. */
    public int lobbyCost() {
        return rules.actions().lobbyCost();
    }

    /** Whether this character's lobbying also stops the square adding to the Climate Meter that round. */
    public boolean lobbyCancelsMeter() {
        return false;
    }

    /**
     * Extra £M per round this character's Green Project earns (analysis model: {@code incomeBonus}).
     * Industrialist: on squares they converted themselves.
     */
    public int incomeBonus(GreenProjectSquare square, Player owner) {
        return 0;
    }

    /** Extra GP per round for each nature project this character owns. Farmer: some. */
    public int natureBonusGP() {
        return 0;
    }

    @Override
    public String toString() {
        return name();
    }
}
