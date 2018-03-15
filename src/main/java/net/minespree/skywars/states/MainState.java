package net.minespree.skywars.states;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.rise.states.GameState;
import net.minespree.skywars.game.events.ChestRefillEvent;
import net.minespree.skywars.game.events.IslandDecayEvent;
import net.minespree.skywars.game.events.LuckyChestGameEvent;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.SpawnEgg;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainState extends SkywarsState {

    private Map<UUID, UUID> entities = Maps.newHashMap();
    private List<String> damaged = Lists.newArrayList();

    public MainState(int timeInProgress) {
        super(timeInProgress);
    }

    @Override
    public void onStart(GameState gameState) {
        super.onStart(gameState);

        events.add(new LuckyChestGameEvent(map, 120));
        events.add(new ChestRefillEvent(map, 240));
        events.add(new IslandDecayEvent(map, 300));
        events.add(new LuckyChestGameEvent(map, 360));
    }

    @Override
    public void onStop(GameState gameState) {
        super.onStop(gameState);

        mapData.getChests().values().forEach(PublicFloatingText::remove);
    }

    @Override
    public void tick() {
        super.tick();

        for (UUID uuid : mapData.getAlive()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.COMPASS && !spectatorHandler.isSpectator(player)) {
                double distance = 0.0;
                Player closest = null;
                for (UUID targetUuid : mapData.getAlive()) {
                    Player target = Bukkit.getPlayer(targetUuid);
                    if (target != player && target.getWorld() == player.getWorld()) {
                        if (closest == null || (closest.getWorld() == player.getWorld() && player.getLocation().distance(target.getLocation()) < distance)) {
                            distance = player.getLocation().distance(target.getLocation());
                            closest = target;
                        }
                    }
                }
                if (closest != null) {
                    player.setCompassTarget(closest.getLocation());
                    MessageUtil.sendActionBar(player, Chat.RED + Chat.BOLD + NetworkPlayer.of(closest).getName() + " - " + String.format("%.1f", distance));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player && !damaged.contains(event.getEntity().getName()) && timeInProgress < 15) {
            damaged.add(event.getEntity().getName());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.MONSTER_EGG) {
            SpawnEgg egg = (SpawnEgg) event.getItem().getData();
            Entity entity = event.getClickedBlock().getWorld().spawnEntity(event.getClickedBlock().getLocation().clone().add(0, 1,0 ), egg.getSpawnedType());
            entities.put(entity.getUniqueId(), event.getPlayer().getUniqueId());
            event.setCancelled(true);
            event.getItem().setAmount(event.getItem().getAmount() - 1);
            if(event.getItem().getAmount() == 0) {
                event.getPlayer().setItemInHand(null);
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if(event.getTarget() != null) {
            if((entities.containsKey(event.getEntity().getUniqueId()) &&
                    entities.get(event.getEntity().getUniqueId()).equals(event.getTarget().getUniqueId())
                    || (event.getTarget() instanceof Player && spectatorHandler.isSpectator((Player) event.getTarget())))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if(event.getEntity() instanceof Player)
            return;
        event.setCancelled(true);
    }

}
