package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;

import java.util.List;

/**
 * A Resilience square, such as a flood wall (analysis model: {@code ResilienceSquare}). Landing
 * here lets a player buy their own next Resilience level, which cuts what they pay for disasters.
 */
public final class ResilienceSquare extends Square {

    ResilienceSquare(int id, String name, String district) {
        super(id, name, district);
    }

    @Override
    public List<Action> landOn(Player player, Game game) {
        int next = player.resilienceLevel() + 1;
        if (next > game.rules().resilience().maxLevel()) {
            return List.of();
        }
        return List.of(Action.on(Kind.BUY_RESILIENCE, player.character().resilienceCost(next), id()));
    }
}
