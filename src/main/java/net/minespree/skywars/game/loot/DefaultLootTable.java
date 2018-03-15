package net.minespree.skywars.game.loot;

import net.minespree.cartographer.util.GameLocation;
import net.minespree.skywars.game.SkyWarsMapData;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DefaultLootTable extends LootTable {

    private final static int MIN_SIZE = 5;
    private final static int MAX_SIZE = 8;

    private final static int MIN_SIZE_MEGA = 3;
    private final static int MAX_SIZE_MEGA = 5;

    @Override
    public void carryOut(int tier, int tierFill, SkyWarsMapData map) {
        for (GameLocation location : map.getMap().getTieredChests().get(tier)) {
            if(location.toLocation().getBlock().getType() == Material.CHEST) {
                addToInventory(generateInventory(tierFill, map), ((Chest) location.toLocation().getBlock().getState()).getBlockInventory());
            }
        }
    }

    @Override
    public List<ItemStack> generateInventory(int tier, SkyWarsMapData map) {
        List<ChestItem> items = new ArrayList<>();
        int size = ThreadLocalRandom.current().nextInt(tier == 3 ? MIN_SIZE_MEGA : MIN_SIZE, tier == 3 ? MAX_SIZE_MEGA : MAX_SIZE);
        int i = 0;
        while (i < size) {
            ChestItem item = decideFromWeight(chestItems.getOrDefault(tier, Collections.emptyList()));
            int j = 0;
            for (ChestItem chestItem : items) {
                if(chestItem.equals(item)) {
                    j++;
                }
            }
            if(j < MAX_SAME_ITEMS) {
                items.add(item);
                i++;
            }
        }
        return items.stream().map(ChestItem::toItem).collect(Collectors.toList());
    }
}
