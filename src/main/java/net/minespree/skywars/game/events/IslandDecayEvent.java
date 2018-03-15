package net.minespree.skywars.game.events;

import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.skywars.game.decay.NormalIslandDecay;
import net.minespree.skywars.states.SkywarsState;

public class IslandDecayEvent extends GameEvent {

    private final static BabelMessage SW_ISLAND_DECAYING = Babel.translate("sw_island_decaying");

    public IslandDecayEvent(SkywarsGameMap map, int eventTime) {
        super(map, Babel.translate("sw_island_decay"), eventTime);
    }

    @Override
    public void occur(SkywarsState state) {
        state.getMapData().setDecay(new NormalIslandDecay(state.getMapData(), map.getCentre(), map.getRadius()));
        SW_ISLAND_DECAYING.broadcast();
    }
}
