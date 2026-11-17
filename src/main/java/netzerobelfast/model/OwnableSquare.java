package netzerobelfast.model;

/**
 * A square a player can buy and own: a Polluting square or a Green Project (analysis model:
 * {@code OwnableSquare} {abstract}). Owned squares can't be bought by anyone else.
 */
public abstract class OwnableSquare extends Square {

    private final int price;
    private Player owner;

    protected OwnableSquare(int id, String name, String district, int price, Player owner) {
        super(id, name, district);
        this.price = price;
        this.owner = owner;
    }

    /** The full price in £M; a character's discount is applied by {@link Character#buyPrice}. */
    public int price() {
        return price;
    }

    /** The owner, or null if nobody owns it yet. */
    public Player owner() {
        return owner;
    }

    void setOwner(Player owner) {
        this.owner = owner;
    }
}
