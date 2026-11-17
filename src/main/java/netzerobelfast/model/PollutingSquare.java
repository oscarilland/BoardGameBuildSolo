package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;

import java.util.List;

/**
 * A Polluting square, such as a gas plant (analysis model: {@code PollutingSquare}). Cheap and
 * high-earning, but it raises the Climate Meter and costs its owner carbon tax.
 *
 * <p>It remembers whether it's been lobbied this round, and by whom, because the Activist's
 * lobbying also cancels its meter rise. The effects happen at the end of the round (plan 005).
 */
public final class PollutingSquare extends OwnableSquare {

    private Player lobbiedBy;

    PollutingSquare(int id, String name, String district, int price) {
        super(id, name, district, price, null);
    }

    @Override
    public List<Action> landOn(Player player, Game game) {
        if (owner() == null) {
            return List.of(Action.on(Kind.BUY, player.character().buyPrice(this), id()));
        }
        if (owner() != player && lobbiedBy == null) {
            return List.of(Action.on(Kind.LOBBY, player.character().lobbyCost(), id()));
        }
        return List.of();
    }

    /** True if someone has lobbied it this round: it earns no money at the end of the round (FR-06). */
    public boolean lobbiedThisRound() {
        return lobbiedBy != null;
    }

    /** True if it was lobbied this round by a character whose lobbying cancels the meter rise (the Activist). */
    public boolean meterRiseCancelled() {
        return lobbiedBy != null && lobbiedBy.character().lobbyCancelsMeter();
    }

    void lobby(Player by) {
        this.lobbiedBy = by;
    }

    /** Lobbying lasts one round. */
    void clearLobbying() {
        this.lobbiedBy = null;
    }

    /**
     * The Green Project this square becomes when its owner converts it (FR-07): same id, name,
     * district, price and owner; not a nature project; remembering who converted it.
     */
    GreenProjectSquare convert(Player by) {
        return new GreenProjectSquare(id(), name(), district(), price(), false, owner(), by);
    }
}
