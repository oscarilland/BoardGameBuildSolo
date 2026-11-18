package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A player: their name, the character they play as, their money and Green Points, where their
 * counter is, their Resilience level and their co-funding deal.
 *
 * <p>Only the model changes a player, through package-private methods, so the rules can't be
 * bypassed from the UI. There are two ways to pay: {@link #pay} for a choice (refused if
 * unaffordable) and {@link #payCompulsory} for something the rules force (can bankrupt).
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
    private boolean bankrupt;
    private int turnsBankrupt;
    private int loanRepayments;
    private int pollutingSquareRounds;

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
     * True from when a compulsory payment takes more than the player had (FR-13 AC-1) until they
     * return. While bankrupt they don't move or act, and their squares earn GP but no money.
     */
    public boolean bankrupt() {
        return bankrupt;
    }

    /**
     * How long they've been bankrupt, counted as the balance simulation did (Q-14): 1 when they go
     * bankrupt, +1 for each turn they sit out. At the data's {@code maxRounds} they return automatically.
     */
    public int turnsBankrupt() {
        return turnsBankrupt;
    }

    /** £M to be deducted at the end of the game: £15M for each return from bankruptcy (FR-13 AC-6). */
    public int loanRepayments() {
        return loanRepayments;
    }

    /**
     * Each round this player owned a Polluting square counts once per square: owning 2 for 6
     * rounds counts 12. Their share of the city's total is shown in the end-of-game report (FR-16).
     */
    public int pollutingSquareRounds() {
        return pollutingSquareRounds;
    }

    void addPollutingSquareRounds(int squares) {
        pollutingSquareRounds += squares;
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

    /**
     * A payment the player must make: an Event card cost (and, from plan 005, a disaster). If it's
     * more than they have, their money drops to £0, the rest is written off, and they're bankrupt
     * (FR-13 AC-1). Paying exactly what they have leaves £0 and isn't bankruptcy (AC-2).
     *
     * @return the amount actually paid, which is less than {@code amount} if some was written off
     */
    int payCompulsory(int amount) {
        if (amount <= money) {
            money -= amount;
            return amount;
        }
        int paid = money;
        money = 0;
        if (!bankrupt) {
            bankrupt = true;
            turnsBankrupt = 1;
        }
        return paid;
    }

    /** A turn spent bankrupt without returning. */
    void missTurn() {
        turnsBankrupt++;
    }

    /** Back in the game with a loan, which costs {@code repayment} at the end (FR-13 AC-6). */
    void returnFromBankruptcy(int loan, int repayment) {
        bankrupt = false;
        turnsBankrupt = 0;
        money += loan;
        loanRepayments += repayment;
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
