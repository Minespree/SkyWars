package net.minespree.skywars.game.events;

import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.skywars.states.SkywarsState;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import net.minespree.wizard.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class LuckyChestCloseEvent extends GameEvent {

    private final static BabelMessage MEGA_CHEST_INFO = Babel.translate("sw_mega_chest_info");

    private Block luckyChest;
    private PublicFloatingText infoText, luckyChestText;

    public LuckyChestCloseEvent(SkywarsGameMap map, int eventTime, Block luckyChest, PublicFloatingText luckyChestText) {
        super(map, null, eventTime, true);

        this.luckyChest = luckyChest;
        this.infoText = new PublicFloatingText(luckyChestText.getLocation().clone().add(0, 0.25, 0));
        this.luckyChestText = luckyChestText;

        infoText.setText(MEGA_CHEST_INFO);
    }

    @Override
    public void tick(SkywarsState state) {
        if(state.getMapData().getChestInventory() != null) {
            infoText.show();
            luckyChestText.show();
            luckyChestText.setText(Babel.messageStatic(Chat.GREEN + (eventTime - state.getTimeInProgress())));
        } else {
            SkywarsState.getEvents().remove(this);
            stop(state);
            occur(state);
        }
    }

    @Override
    public void occur(SkywarsState state) {
        state.getUsing().forEach(uuid -> Bukkit.getPlayer(uuid).closeInventory());
        state.getUsing().clear();
        infoText.remove();
        luckyChestText.setText(LuckyChestGameEvent.LUCKY_CHEST_CLOSED);
        state.getMapData().setChestInventory(null);
        state.sendUpdate(luckyChest, 0);
    }

}
