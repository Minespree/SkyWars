package net.minespree.skywars.game.loot;

import net.minespree.cartographer.util.GameLocation;
import net.minespree.skywars.game.SkyWarsMapData;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class IslandLootTable extends LootTable {

    private final static int MIN_PER_CHEST = 4;
    private final static int MAX_PER_CHEST = 6;

    @Override
    public void carryOut(int tier, int tierFill, SkyWarsMapData map) {
        for (SkyWarsMapData.Island island : map.getIslands()) {
            List<ItemStack> items = generateInventory(tierFill, map);
            if (items.size() == 0)
                return;
            Collections.shuffle(items);
            int chests = island.getChests().size();
            int amountPerChest = (int) Math.ceil((double) items.size() / (double) chests);
            int index = 0;
            for (GameLocation location : island.getChests()) {
                if (index >= items.size() - 1 || index + amountPerChest >= items.size() - 1) {
                    addToInventory(items.subList(index, items.size()), ((Chest) location.toLocation().getBlock().getState()).getBlockInventory());
                    break;
                } else {
                    addToInventory(items.subList(index, index + amountPerChest), ((Chest) location.toLocation().getBlock().getState()).getBlockInventory());
                }
                index += amountPerChest;
            }
        }
    }

    @Override
    public List<ItemStack> generateInventory(int tier, SkyWarsMapData map) {
        List<ChestItem> items = new ArrayList<>();
        for (int i = 0; i < map.getPlayersPerTeam(); i++) {
            for (ItemType type : ItemType.values()) {
                if(type.isGuaranteed()) {
                    ChestItem item = decideFromWeight(fromType(type));
                    items.add(item);
                    for (String id : item.getGuaranteed()) {
                        ChestItem guaranteed = get(tier, id);
                        if(guaranteed != null) {
                            items.add(guaranteed);
                        }
                    }
                }
            }
        }
        int size = map.getIslands().stream().findFirst().get().getChests().size() * map.getPlayersPerTeam(); // Assuming there will always be same chests.
        int itemsLeft = ThreadLocalRandom.current().nextInt(MIN_PER_CHEST * size, MAX_PER_CHEST * size) - items.size();
        int i = 0;
        while (i < itemsLeft) {
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

    private List<ChestItem> fromType(ItemType type) {
        return chestItems.getOrDefault(1, Collections.emptyList()).stream().filter(item -> item.getType() == type).collect(Collectors.toList());
    }

}
