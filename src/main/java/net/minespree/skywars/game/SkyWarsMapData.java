package net.minespree.skywars.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minespree.cartographer.maps.GameMap;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.cartographer.util.ColourData;
import net.minespree.cartographer.util.GameArea;
import net.minespree.cartographer.util.GameLocation;
import net.minespree.rise.control.maps.MapData;
import net.minespree.rise.teams.Team;
import net.minespree.skywars.SkyWarsPlugin;
import net.minespree.skywars.game.decay.IslandDecay;
import net.minespree.skywars.game.loot.ChestItem;
import net.minespree.skywars.game.loot.DefaultLootTable;
import net.minespree.skywars.game.loot.IslandLootTable;
import net.minespree.skywars.game.loot.LootTable;
import net.minespree.wizard.floatingtext.types.PublicFloatingText;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;

import java.util.*;

@Getter
public class SkyWarsMapData implements MapData {

    private SkywarsGameMap map;
    private int playersPerTeam;
    private List<UUID> alive = Lists.newArrayList();
    private SkyWarsScoreboardFeature scoreboard = new SkyWarsScoreboardFeature();
    private List<Island> islands;
    private IslandLootTable islandTable = new IslandLootTable();
    private DefaultLootTable defTable = new DefaultLootTable();
    private Map<Chest, PublicFloatingText> chests = Maps.newConcurrentMap();
    private Set<Chest> chestsShown = Sets.newConcurrentHashSet();
    @Setter
    private IslandDecay decay;
    @Setter
    private PublicFloatingText megaText;
    @Setter
    private Inventory chestInventory;

    @Override
    public void create(GameMap gameMap) {
        map = (SkywarsGameMap) gameMap;

        LootTable.getChestItems().clear();

        islands = Lists.newArrayList();
        List<GameArea> mapIslands = Lists.newArrayList(map.getIslands().keySet());
        if(map.isTeam()) {
            int i = 0;
            playersPerTeam = map.getTeamSpawns().values().stream().findAny().orElse(Collections.emptyList()).size();
            for (ColourData data : map.getTeamSpawns().keySet()) {
                if(i < mapIslands.size()) {
                    GameArea area = mapIslands.get(i);
                    islands.add(new TeamIsland(new Team(data, map.getTeamSlots().get(data)),
                            area, map.getTeamSpawns().get(data), map.getIslands().get(area)));
                } else {
                    throw new IndexOutOfBoundsException("Who configured this map?");
                }
                i++;
            }
        } else {
            playersPerTeam = 1;
            for (int i = 0; i < map.getSoloSpawns().size(); i++) {
                GameLocation location = map.getSoloSpawns().get(i);
                if(i < mapIslands.size()) {
                    GameArea area = mapIslands.get(i);
                    islands.add(new Island(area, Collections.singletonList(location), map.getIslands().get(area)));
                } else {
                    throw new IndexOutOfBoundsException("Who configured this map?");
                }
            }
        }
        if(SkyWarsPlugin.getPlugin().getConfig().contains("items")) {
            ConfigurationSection section = SkyWarsPlugin.getPlugin().getConfig().getConfigurationSection("items");
            for (String tierStr : section.getKeys(false)) {
                int tier = Integer.parseInt(tierStr);
                for (String item : section.getConfigurationSection(tierStr).getKeys(false)) {
                    LootTable.put(tier, new ChestItem(map.loadItem(item, section.getConfigurationSection(tierStr + "." + item))));
                }
            }
        }
        map.getMapSpecificItems().forEach((tier, items) -> items.forEach(item -> LootTable.put(tier, new ChestItem(item))));
    }

    @Data @Getter
    public static class Island {

        private Team team;
        private final GameArea area;
        private final List<GameLocation> cages, chests;

    }

    @Getter
    public static class TeamIsland extends Island {

        private final Team team;

        public TeamIsland(Team team, GameArea area, List<GameLocation> cages, List<GameLocation> chests) {
            super(area, cages, chests);

            this.team = team;
        }
    }

}
