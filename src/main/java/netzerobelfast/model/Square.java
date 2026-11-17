package netzerobelfast.model;

import java.util.List;

/**
 * A square on the board (analysis model: {@code Square} {abstract}).
 *
 * <p>Each kind of square overrides {@link #landOn} to say what a player can do there, so
 * {@link Game} treats every square the same way. This is the analysis model's main use of
 * polymorphism.
 */
public abstract class Square {

    private final int id;
    private final String name;
    private final String district;

    protected Square(int id, String name, String district) {
        this.id = id;
        this.name = name;
        this.district = district;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    /** The district code from {@code board.json}, such as {@code NORTH}. */
    public String district() {
        return district;
    }

    /**
     * The landing actions this square offers the player, before checking whether they can
     * afford them. {@link Game} adds conversions and filters out what's unaffordable.
     */
    public abstract List<Action> landOn(Player player, Game game);

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
