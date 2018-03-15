package net.minespree.skywars.game.events;

import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.babel.MultiBabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.skywars.states.SkywarsState;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import net.minespree.wizard.util.FireworkUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LuckyChestGameEvent extends GameEvent {

    private static final MultiBabelMessage LUCKY_CHEST_SPAWN = Babel.translateMulti("sw_lucky_chest_spawn");
    public static final BabelMessage LUCKY_CHEST_CLOSED = Babel.translate("sw_lucky_chest_closed");

    private Inventory chestInventory;
    private Block luckyChest;
    private PublicFloatingText luckyChestText;

    public LuckyChestGameEvent(SkywarsGameMap map, int eventTime) {
        super(map, Babel.translate("sw_lucky_chest"), eventTime);
    }

    @Override
    public void start(SkywarsState state) {
        List<GameLocation> luckyChests = map.getTieredChests().get(3);
        if(!luckyChests.isEmpty()) {
            if(state.getMapData().getMegaText() != null) {
                state.getMapData().getMegaText().remove();
            }
            luckyChest = luckyChests.get(ThreadLocalRandom.current().nextInt(luckyChests.size())).toLocation().getBlock();
            luckyChestText = new PublicFloatingText(luckyChest.getLocation().add(0.5, -1.0, 0.5));
            luckyChestText.setText(LUCKY_CHEST_CLOSED, eventTime);
            chestInventory = Bukkit.createInventory(null, 27);
            state.getMapData().getDefTable().addToInventory(state.getMapData().getDefTable().generateInventory(3, state.getMapData()), chestInventory);
            state.getMapData().setMegaText(luckyChestText);
        }
        if(luckyChestText != null) {
            luckyChestText.show();
        }
    }

    @Override
    public void occur(SkywarsState state) {
        if(luckyChestText != null) {
            LUCKY_CHEST_SPAWN.broadcast();
            FireworkUtil.randomFirework(luckyChest.getLocation().add(0.5, 1.5, 0.5));
            state.getMapData().setChestInventory(chestInventory);
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F));
            SkywarsState.getEvents().add(new LuckyChestCloseEvent(map, eventTime + 30, luckyChest, luckyChestText));
        }
    }
}
