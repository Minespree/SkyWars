package net.minespree.skywars.game.loot;

import lombok.Getter;
import net.minespree.skywars.game.SkyWarsMapData;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class LootTable {

    protected final static int MAX_SAME_ITEMS = 2;

    @Getter
    final static Map<Integer, List<ChestItem>> chestItems = new HashMap<>();
    private final static ChestItem ERROR_ITEM = new ChestItem("stone", ItemType.BLOCK, new ItemBuilder(Material.STONE), 1.0, 1, 16, Collections.emptyList(), Collections.emptyList());

    public static void put(int tier, ChestItem item) {
        if(chestItems.containsKey(tier)) {
            Iterator<ChestItem> chestIterator = chestItems.get(tier).iterator();
            while (chestIterator.hasNext()) {
                ChestItem chestItem = chestIterator.next();
                if(item.getOverrides() != null) {
                    for (String id : item.getOverrides()) {
                        if (chestItem.getId().equals(id)) {
                            chestIterator.remove();
                        }
                    }
                }
            }
        }
        chestItems.putIfAbsent(tier, new ArrayList<>());
        chestItems.get(tier).add(item);
    }

    public static ChestItem get(int tier, String id) {
        if(chestItems.containsKey(tier)) {
            for (ChestItem item : chestItems.get(tier)) {
                if(item.getId().equals(id)) {
                    return item;
                }
            }
        }
        return null;
    }

    ChestItem decideFromWeight(List<ChestItem> items) {
        if(items.isEmpty())
            return ERROR_ITEM;
        double total = items.stream().mapToDouble(ChestItem::getWeight).sum();
        double weight = ThreadLocalRandom.current().nextDouble() * total;
        int index = items.size();
        while (weight > 0.0) {
            index--;
            if(index <= 0)
                break;
            weight -= items.get(index).getWeight();
        }
        return items.get(index);
    }

    public void addToInventory(List<ItemStack> items, Inventory inventory) {
        inventory.clear();
        for (ItemStack item : items) {
            inventory.setItem(getSlot(inventory), item);
        }
    }

    protected int getSlot(Inventory inventory) {
        int slot = ThreadLocalRandom.current().nextInt(inventory.getSize());
        return inventory.getItem(slot) == null ? slot : getSlot(inventory);
    }

    public abstract void carryOut(int tier, int tierFill, SkyWarsMapData map);
    public abstract List<ItemStack> generateInventory(int tier, SkyWarsMapData map);

}
