package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A player: their name, the character they play as, their money and Green Points, where their
 * counter is, their Resilience level and their co-funding deal.
 *
 * <p>Only the model changes a player, through package-private methods, so the rules can't be
 * bypassed from the UI. Bankruptcy and loans arrive with plan 005.
 */
public final class Player {

    private final String name;
    private final Character character;
    private int money;
    private int greenPoints;
    private int position;
    private Heading heading;
    private int resilienceLevel;
    private CoFundingDeal coFundingDeal;

    Player(String name, Character character, int startPosition) {
        this.name = name;
        this.character = character;
        this.money = character.startMoney();
        this.greenPoints = character.startGP();
        this.position = startPosition;
    }

    public String name() {
        return name;
    }

    public Character character() {
        return character;
    }

    /** Money in £M. */
    public int money() {
        return money;
    }

    public int greenPoints() {
        return greenPoints;
    }

    /** The id of the square the counter is on. */
    public int position() {
        return position;
    }

    /** Direction of travel if on the City Centre route; null on the loop, or after a Glider ride to City Hall (Q-18). */
    public Heading heading() {
        return heading;
    }

    /** 0 (none) up to the maximum level in the data. */
    public int resilienceLevel() {
        return resilienceLevel;
    }

    /** The player's co-funding deal, or null if they don't have one. */
    public CoFundingDeal coFundingDeal() {
        return coFundingDeal;
    }

    /**
     * What this player actually pays for a disaster costing {@code amount}, after their
     * Resilience cut, rounded half up to whole £M (FR-08, Q-08). For example, level 1 (25%) on
     * £2M is £1.5M, which rounds to £2M. Exact decimals avoid floating-point surprises.
     */
    public int disasterCost(int amount) {
        BigDecimal cut = BigDecimal.valueOf(character.resilienceCut(resilienceLevel));
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.ONE.subtract(cut))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    void moveTo(int newPosition, Heading newHeading) {
        this.position = newPosition;
        this.heading = newHeading;
    }

    /**
     * A payment the player chose to make. Players can never choose something they can't
     * afford, so this refuses rather than going below £0 (compulsory payments come in plan 005).
     */
    void pay(int amount) {
        if (amount > money) {
            throw new IllegalActionException(name + " can't afford £" + amount + "M (has £" + money + "M).");
        }
        money -= amount;
    }

    void receive(int amount) {
        money += amount;
    }

    void addGP(int points) {
        greenPoints += points;
    }

    void raiseResilience() {
        resilienceLevel++;
    }

    void startDeal(CoFundingDeal deal) {
        this.coFundingDeal = deal;
    }

    @Override
    public String toString() {
        return name + " (" + character.name() + ")";
    }
}
