package net.minespree.skywars.game.loot;

import lombok.Getter;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.wizard.util.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class ChestItem {

    private static final Random RANDOM = new Random();

    private String id;
    private ItemType type;
    private ItemBuilder builder;
    private double weight;
    private int min, max;
    private List<String> guaranteed, overrides;

    public ChestItem(String id, ItemType type, ItemBuilder builder, double weight, int min, int max, List<String> guaranteed, List<String> overrides) {
        this.id = id;
        this.type = type;
        this.builder = builder;
        this.weight = weight;
        this.min = min;
        this.max = max;
        this.guaranteed  = guaranteed;
        this.overrides = overrides;
    }

    public ChestItem(SkywarsGameMap.ItemData data) {
        this(data.getId(), ItemType.valueOf(data.getItemType().toUpperCase()), data.getBuilder(), data.getWeight(), data.getMin(), data.getMax(), data.getGuaranteed(), data.getOverrides());
    }

    ItemStack toItem() {
        ItemStack item = builder.build().clone();
        if(min != max) {
            item.setAmount(ThreadLocalRandom.current().nextInt(min, max));
        }
        return item;
    }

}
