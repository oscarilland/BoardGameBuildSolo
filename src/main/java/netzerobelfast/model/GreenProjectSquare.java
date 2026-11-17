package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;

import java.util.List;

/**
 * A Green Project, such as offshore wind or a woodland (analysis model: {@code GreenProjectSquare}).
 * Dearer than a Polluting square; it earns money and Green Points and lowers the Climate Meter.
 */
public final class GreenProjectSquare extends OwnableSquare {

    private final boolean nature;
    private final Player convertedBy;

    GreenProjectSquare(int id, String name, String district, int price, boolean nature, Player owner, Player convertedBy) {
        super(id, name, district, price, owner);
        this.nature = nature;
        this.convertedBy = convertedBy;
    }

    @Override
    public List<Action> landOn(Player player, Game game) {
        if (owner() == null) {
            return List.of(Action.on(Kind.BUY, player.character().buyPrice(this), id()));
        }
        boolean alreadyFunding = player.coFundingDeal() != null && player.coFundingDeal().square() == id();
        if (owner() != player && !alreadyFunding) {
            return List.of(Action.on(Kind.CO_FUND, game.rules().actions().coFundCost(), id()));
        }
        return List.of();
    }

    /** Nature projects (woodland, peatland) matter for the Farmer and for Droughts. */
    public boolean nature() {
        return nature;
    }

    /** Who converted it from a Polluting square, or null if it was always green. The Industrialist earns a bonus on these. */
    public Player convertedBy() {
        return convertedBy;
    }
}
