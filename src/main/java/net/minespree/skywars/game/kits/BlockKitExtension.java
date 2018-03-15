package net.minespree.skywars.game.kits;

import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.data.gamedata.kits.KitExtension;
import net.minespree.feather.data.gamedata.kits.Tier;
import net.minespree.skywars.game.loot.ChestItem;
import net.minespree.skywars.game.loot.ItemType;
import net.minespree.skywars.game.loot.LootTable;
import net.minespree.wizard.util.ItemBuilder;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockKitExtension extends KitExtension {

    private static final Random RANDOM = new Random();

    public BlockKitExtension() {
        super(GameRegistry.Type.SKYWARS, "sw_noob");
    }

    @Override
    public void setKit(Player player, Tier tier) {
        Document document = tier.getData().get("block");
        int slot = document.getInteger("slot");
        int amount = document.getInteger("amount");
        List<ChestItem> blocks = new ArrayList<>();
        for (ChestItem item : LootTable.getChestItems().getOrDefault(1, Collections.emptyList())) {
            if (item.getType() == ItemType.BLOCK) {
                blocks.add(item);
            }
        }
        if (!blocks.isEmpty()) {
            ChestItem item = blocks.get(RANDOM.nextInt(blocks.size()));
            ItemBuilder builder = item.getBuilder();
            ItemStack stack = builder.build().clone();
            stack.setAmount(amount);
            player.getInventory().setItem(slot, stack);
        }
    }
}
