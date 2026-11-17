package netzerobelfast.model;

import netzerobelfast.model.Action.Kind;

import java.util.ArrayList;
import java.util.List;

/**
 * A Glider stop (analysis model: {@code GliderStop}). A player who lands here may pay the fare
 * to ride to any other stop; there's one action per destination.
 */
public final class GliderStop extends Square {

    GliderStop(int id, String name, String district) {
        super(id, name, district);
    }

    @Override
    public List<Action> landOn(Player player, Game game) {
        List<Action> rides = new ArrayList<>();
        for (int stop : game.board().gliderStops()) {
            if (stop != id()) {
                rides.add(new Action(Kind.RIDE_GLIDER, player.character().gliderFare(), id(), stop));
            }
        }
        return rides;
    }
}
