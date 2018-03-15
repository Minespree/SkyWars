package net.minespree.skywars.game.events;

import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.skywars.states.SkywarsState;

public class ChestRefillEvent extends GameEvent {

    private final static BabelMessage SW_CHEST_REFILLED = Babel.translate("sw_chest_refilled");

    public ChestRefillEvent(SkywarsGameMap map, int eventTime) {
        super(map, Babel.translate("sw_chest_refill"), eventTime);
    }

    @Override
    public void occur(SkywarsState state) {
        state.getMapData().getDefTable().carryOut(1, 2, state.getMapData());
        state.getMapData().getDefTable().carryOut(2, 2, state.getMapData());
        state.getMapData().getChestsShown().clear();
        SW_CHEST_REFILLED.broadcast();
    }

}
