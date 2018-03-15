package net.minespree.skywars.states;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockAction;
import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.babel.ComplexBabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.feather.data.damage.event.MinespreeDeathEvent;
import net.minespree.feather.data.damage.objects.KillAssist;
import net.minespree.feather.data.gamedata.kits.KitExtension;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.rise.RisePlugin;
import net.minespree.rise.states.BaseGameState;
import net.minespree.rise.states.GameState;
import net.minespree.rise.teams.Team;
import net.minespree.skywars.SkyWarsPlugin;
import net.minespree.skywars.game.SkyWarsMapData;
import net.minespree.skywars.game.events.ChestRefillEvent;
import net.minespree.skywars.game.events.GameEvent;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import net.minespree.wizard.util.Chat;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public abstract class SkywarsState extends BaseGameState {

    private static final BabelMessage PLAYERS_LEFT = Babel.translate("sw_players_left");
    private static final BabelMessage SW_LUCKY_CHEST_IS_CLOSED = Babel.translate("sw_lucky_chest_is_closed");

    @Getter
    protected static final Set<GameEvent> events = new ConcurrentSet<>();

    @Getter
    private Set<UUID> using = new HashSet<>();

    protected SkywarsGameMap map;
    @Getter
    protected SkyWarsMapData mapData;
    @Getter @Setter
    protected int timeInProgress;
    protected BukkitTask tickTask;

    public SkywarsState(int timeInProgress) {
        this.timeInProgress = timeInProgress;
    }

    @Override
    public void onStart(GameState gameState) {
        map = (SkywarsGameMap) RisePlugin.getPlugin().getMapManager().getCurrentMap();
        mapData = (SkyWarsMapData) mapManager.getMapData();

        // start task
        tickTask = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getPlugin(), () -> {
            timeInProgress++;
            tick();
        }, 20, 20);
    }

    @Override
    public void onStop(GameState gameState) {
        tickTask.cancel();
    }

    @Override
    public void onJoin(Player player) {
        spectatorHandler.setSpectator(player);
        mapData.getScoreboard().onStart(player);
    }

    @Override
    public void onQuit(Player player) {
        if(mapData.getAlive().contains(player.getUniqueId())) {
            mapData.getAlive().remove(player.getUniqueId());
        }
        if(using.contains(player.getUniqueId())) {
            using.remove(player.getUniqueId());
        }

        handlePlayersLeft();
    }

    public void tick() {
        mapData.getScoreboard().update();
        if(mapData.getDecay() != null) {
            if(timeInProgress % 2 == 0) {
                mapData.getDecay().step();
            }
        }
        for (Chest chest : mapData.getChests().keySet()) {
            if(mapData.getChestsShown().contains(chest)) {
                PublicFloatingText text = mapData.getChests().get(chest);
                Optional<GameEvent> nextRefill = getNextRefill();
                sendUpdate(chest.getBlock(), 1);
                if (nextRefill.isPresent()) {
                    text.setText(Babel.messageStatic(Chat.RED + DurationFormatUtils.formatDuration((nextRefill.get().getEventTime() - timeInProgress) * 1000, "m:ss")));
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld() != chest.getLocation().getWorld()) continue;
                        if (player.getLocation().distance(chest.getLocation()) <= 10.0) {
                            text.show(player);
                        } else {
                            text.hide(player);
                        }
                    }
                }
            } else {
                sendUpdate(chest.getBlock(), 0);
                mapData.getChests().get(chest).hide();
            }
        }
        for (GameEvent event : events) {
            getNextEvent().ifPresent(e -> e.startEvent(this));
            event.tick(this);
            if(event.getEventTime() <= timeInProgress) {
                events.remove(event);
                event.occur(this);
                event.stop(this);
            }
        }
    }

    private void handlePlayersLeft() {
        mapData.getScoreboard().update();
        int playersLeft = mapData.getAlive().size();
        if(playersLeft == 0) {
            game.endGame(new ComplexBabelMessage().append(Babel.translate("sw_winner")), null);
            gamemode.getKitExtensions().forEach(KitExtension::stop);
            return;
        }
        if(map.isTeam()) {
            List<Team> alive = new ArrayList<>();
            for (Team team : teamHandler.getTeams()) {
                if (team.getAlive().size() > 0) {
                    alive.add(team);
                }
            }
            if(alive.size() <= 1) {
                    Bukkit.getOnlinePlayers().stream().filter(player -> !alive.get(0).getPlayers().contains(player.getUniqueId()))
                            .forEach(player -> game.changeStatistic(player, "loss", 1));
                    alive.get(0).getPlayers().stream().map(Bukkit::getPlayer).forEach(player -> game.changeStatistic(player, "win", 1));
                game.endGame(new ComplexBabelMessage().append(Babel.translate("sw_winner"), alive.get(0)), alive.get(0).getName().toString());
                gamemode.getKitExtensions().forEach(KitExtension::stop);
            }
        } else if (playersLeft <= 1) {
            game.changeStatistic(Bukkit.getPlayer(mapData.getAlive().get(0)), "win", 1);
            String winner = Bukkit.getPlayer(mapData.getAlive().get(0)).getName();
            game.endGame(new ComplexBabelMessage().append(Babel.translate("sw_winner"), winner), winner);
            gamemode.getKitExtensions().forEach(KitExtension::stop);
        }
    }

    public Optional<GameEvent> getNextEvent() {
        GameEvent nearest = null;
        for (GameEvent event : events) {
            if((nearest == null || event.getEventTime() < nearest.getEventTime()) && !event.isHidden()) {
                nearest = event;
            }
        }
        return Optional.ofNullable(nearest);
    }

    private Optional<GameEvent> getNextRefill() {
        GameEvent nearest = null;
        for (GameEvent event : events) {
            if((nearest == null || event.getEventTime() < nearest.getEventTime()) && event instanceof ChestRefillEvent) {
                nearest = event;
            }
        }
        return Optional.ofNullable(nearest);
    }

    public void sendUpdate(Block block, int viewing) {
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(pos, Blocks.CHEST, 1, viewing);
        Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (gameStateManager.getCurrentState() instanceof CageState || event.getBlock().getType() == Material.ENDER_CHEST) {
            event.setCancelled(true);
            return;
        }
        game.changeStatistic(event.getPlayer(), "blocksbroken", 1);
        if(event.getBlock().getType() == Material.CHEST) {
            Chest chest = (Chest) event.getBlock().getState();
            if(mapData.getChests().containsKey(chest)) {
                mapData.getChestsShown().remove(chest);
                mapData.getChests().get(chest).remove();
                mapData.getChests().remove(chest);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Iterator<Block> blockIterator = event.blockList().iterator();
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if(block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                if(mapData.getChests().containsKey(chest)) {
                    mapData.getChestsShown().remove(chest);
                    mapData.getChests().get(chest).remove();
                    mapData.getChests().remove(chest);
                }
            } else if(block.getType() == Material.ENDER_CHEST) {
                blockIterator.remove();
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gameStateManager.getCurrentState() instanceof CageState) {
            event.setCancelled(true);
        } else {
            game.changeStatistic(event.getPlayer(), "blocksplaced", 1);
            if(event.getBlock().getType() == Material.TNT) {
                TNTPrimed tnt = (TNTPrimed) event.getPlayer().getWorld().spawnEntity(event.getBlock().getLocation(), EntityType.PRIMED_TNT);
                ((CraftTNTPrimed) tnt).getHandle().projectileSource = event.getPlayer();
                event.getBlock().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && gameStateManager.getCurrentState() instanceof CageState) {
            event.setCancelled(true);
        } else if(event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            if(((Arrow) event.getDamager()).getShooter() instanceof Player) {
                game.changeStatistic((Player) ((Arrow) event.getDamager()).getShooter(), "arrowshit", 1);
            }
        }
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event) {
        if(event.getEntity() instanceof Arrow) {
            if(event.getEntity().getShooter() instanceof Player) {
                game.changeStatistic((Player) event.getEntity().getShooter(), "arrowshot", 1);
            }
        }
    }

    @EventHandler
    public void onMinespreeDeath(MinespreeDeathEvent event) {
        event.setDrops(true);
        if(map.isTeam()) {
            teamHandler.getTeam(event.getPlayer()).kill(event.getPlayer());
        }
        for (KillAssist assist : event.getAssists()) {
            Babel.translate("assisted_killing_player").sendMessage(assist.getPlayer(), NetworkPlayer.of(event.getPlayer()).getName(), assist.getPercent());
            game.changeStatistic(assist.getPlayer(), "assists", 1);
        }
        game.changeStatistic(event.getPlayer(), "deaths", 1);
        event.getLife().getLastDamagingPlayer().ifPresent(player -> game.changeStatistic(player, "kills", 1));
        mapData.getAlive().remove(event.getPlayer().getUniqueId());
        mapData.getAlive().stream().map(Bukkit::getPlayer).forEach(player -> game.addCoins(player, 3, false));
        handlePlayersLeft();
        event.getPlayer().spigot().respawn();
        RisePlugin.getPlugin().getGameManager().getSpectatorHandler().setSpectator(event.getPlayer());
        mapData.getScoreboard().onStart(event.getPlayer());
        PLAYERS_LEFT.broadcast(mapData.getAlive().size());
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if(event.getInventory().getType() == InventoryType.ENDER_CHEST && !spectatorHandler.isSpectator((Player) event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().closeInventory();
            if(mapData.getChestInventory() != null) {
                using.add(event.getPlayer().getUniqueId());
                event.getPlayer().openInventory(mapData.getChestInventory());
            } else {
                SW_LUCKY_CHEST_IS_CLOSED.sendMessage((Player) event.getPlayer());
            }
        } else if(event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            if (mapData.getChests().containsKey(chest)) {
                game.changeStatistic((Player) event.getPlayer(), "chestsopened", 1);
                mapData.getChestsShown().add(chest);
                sendUpdate(chest.getBlock(), 1);
            }
        } else if(event.getInventory() instanceof EnchantingInventory) {
            ((EnchantingInventory) event.getInventory()).setSecondary(new ItemStack(Material.INK_SACK, 64, (short) 4));
        }
    }

    private boolean isEmpty(Inventory inventory) {
        for (ItemStack stack : inventory.getContents()) {
            if(stack != null) {
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(mapData.getChestInventory() != null && event.getInventory() != null && event.getInventory().equals(mapData.getChestInventory())) {
            using.remove(event.getPlayer().getUniqueId());
            if(using.isEmpty() && isEmpty(mapData.getChestInventory())) {
                mapData.setChestInventory(null);
            }
        } else if(event.getInventory() instanceof EnchantingInventory) {
            ((EnchantingInventory) event.getInventory()).setSecondary(null);
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ((EnchantingInventory) event.getInventory()).setSecondary(new ItemStack(Material.INK_SACK, 64, (short) 4));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getInventory() instanceof EnchantingInventory) {
            if(event.getSlot() == 1) {
                event.setCancelled(true);
            }
        }
    }

}
