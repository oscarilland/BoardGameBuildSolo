package netzerobelfast.model;

import netzerobelfast.model.Board.Heading;

/**
 * A player: their name, the character they play as, their money and Green Points, and where
 * their counter is.
 *
 * <p>Only {@link Game} changes a player, through package-private methods, so the rules can't
 * be bypassed from the UI. The rest of the analysis model's attributes (Resilience level,
 * bankruptcy, loans) arrive with the plans that use them.
 */
public final class Player {

    private final String name;
    private final Character character;
    private int money;
    private int greenPoints;
    private int position;
    private Heading heading;

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

    /** Direction of travel if on the City Centre route; null on the loop, or after a Glider ride (Q-18). */
    public Heading heading() {
        return heading;
    }

    void moveTo(int newPosition, Heading newHeading) {
        this.position = newPosition;
        this.heading = newHeading;
    }

    @Override
    public String toString() {
        return name + " (" + character.name() + ")";
    }
}
