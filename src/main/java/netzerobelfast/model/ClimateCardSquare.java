package netzerobelfast.model;

import java.util.List;

/**
 * A Climate Card square (analysis model: {@code ClimateCardSquare}). Landing here draws a card,
 * which is compulsory. Cards arrive in plan 004; until then the square offers nothing.
 */
public final class ClimateCardSquare extends Square {

    ClimateCardSquare(int id, String name, String district) {
        super(id, name, district);
    }

    @Override
    public List<Action> landOn(Player player, Game game) {
        return List.of();
    }
}
