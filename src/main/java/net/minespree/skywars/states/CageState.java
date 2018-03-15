package net.minespree.skywars.states;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.feather.data.gamedata.kits.KitExtension;
import net.minespree.feather.data.gamedata.kits.KitManager;
import net.minespree.pirate.cosmetics.CosmeticInstance;
import net.minespree.pirate.cosmetics.CosmeticManager;
import net.minespree.pirate.cosmetics.CosmeticType;
import net.minespree.pirate.cosmetics.games.skywars.CageCosmetic;
import net.minespree.rise.RisePlugin;
import net.minespree.rise.states.GameState;
import net.minespree.rise.states.WaitingState;
import net.minespree.skywars.game.SkyWarsMapData;
import net.minespree.skywars.game.events.CageGameEvent;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import net.minespree.wizard.util.FacingUtil;
import net.minespree.wizard.util.SetupUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class CageState extends SkywarsState {

    public final static int CAGE_RELEASE_TIME = 10;

    @Getter
    private final List<Location> cageBlocks = new ArrayList<>();
    private final Map<UUID, SkyWarsMapData.Island> homeIslands = new HashMap<>();

    public CageState() {
        super(0);
    }

    @Override
    public void onStart(GameState gameState) {
        super.onStart(gameState);
        mapData.getScoreboard().onStart();
        events.clear();
        gamemode.getKitExtensions().forEach(KitExtension::start);

        World world = RisePlugin.getPlugin().getMapManager().getCurrentWorld().get();
        world.setTime(map.getTime());
        world.setGameRuleValue("doDaylightCycle", "false");

        for (GameLocation location : map.getTieredChests().getOrDefault(2, Collections.emptyList())) {
            Block block = location.toLocation().getBlock();
            if(block.getType() == Material.TRAPPED_CHEST) {
                byte data = block.getState().getData().getData();
                block.setType(Material.CHEST);
                block.setData(data);
            }
        }
        for (int i = 1; i < 3; i++) {
            map.getTieredChests().getOrDefault(i, Collections.emptyList())
                    .forEach(location -> mapData.getChests().put((Chest) location.toLocation().getBlock().getState(), new PublicFloatingText(location.toLocation().add(0.5, -1.0, 0.5))));
        }
        mapData.getChests().values().forEach(PublicFloatingText::hide);

        // assign islands to players
        List<SkyWarsMapData.Island> islandsUsed = new ArrayList<>();
        List<SkyWarsMapData.Island> islandsToUse = new ArrayList<>(mapData.getIslands());
        List<Player> players = Lists.newArrayList(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        if(map.isTeam()) {

        } else {
            for (Player player : players) {
                SetupUtil.setupPlayer(player, false);
                player.getInventory().setItem(0, WaitingState.getKitSelector().build(player));
                SkyWarsMapData.Island island = islandsToUse.remove(0);
                Location loc = island.getCages().get(0).toLocation();
                cageBlocks.addAll(((CageCosmetic) CosmeticManager.getCosmeticManager()
                        .getSelectedCosmetic(player, CosmeticType.SW_CAGE)
                        .orElse(CosmeticInstance.DEFAULT_CAGE.getCosmetic()))
                        .build(loc, FacingUtil.getFacing(loc)));
                player.setFallDistance(0.0f);
                player.teleport(loc);
                islandsUsed.add(island);

                homeIslands.put(player.getUniqueId(), island);
                mapData.getAlive().add(player.getUniqueId());
            }
        }

        mapData.getDefTable().carryOut(1, 1, mapData);
        mapData.getDefTable().carryOut(2, 2, mapData);
        mapData.getIslandTable().carryOut(1, 1, mapData);

        events.add(new CageGameEvent(map));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().getType() == Material.IRON_SWORD) {
            if(KitManager.getInstance().getLoadedKits().containsKey(gamemode.getGame())) {
                KitManager.getInstance().open(event.getPlayer(), game.getGamemode().getGame());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

}
