package net.minespree.skywars;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.minespree.babel.Babel;
import net.minespree.feather.command.system.CommandManager;
import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.player.stats.local.SessionStatRegistry;
import net.minespree.feather.player.stats.local.StatType;
import net.minespree.rise.RisePlugin;
import net.minespree.rise.control.Gamemode;
import net.minespree.rise.util.InformationBook;
import net.minespree.skywars.game.SkyWarsMapData;
import net.minespree.skywars.game.commands.TestingCommands;
import net.minespree.skywars.game.kits.BlockKitExtension;
import net.minespree.skywars.states.CageState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;

public final class SkyWarsPlugin extends JavaPlugin {

    @Getter
    private static SkyWarsPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        CommandManager.getInstance().registerClass(TestingCommands.class);

        // tell Rise we're ready to roll
        Gamemode gamemode = Gamemode.builder()
                .plugin(this)
                .features(ImmutableList.of())
                .extraDataLoader(map -> new SkyWarsMapData())
                .spawnHandler(() -> null)
                .initialGameState(CageState::new)
                .game(GameRegistry.Type.SKYWARS)
                .statisticSize(27)
                .statisticMap(new HashMap<String, StatType>() {
                    {put("kills", new StatType("sw_kills", SessionStatRegistry.Sorter.HIGHEST_SCORE, false, 8, 2, GameRegistry.Type.SKYWARS));}
                    {put("assists", new StatType("sw_assists", SessionStatRegistry.Sorter.HIGHEST_SCORE, true, 5, 3, GameRegistry.Type.SKYWARS));}
                    {put("deaths", new StatType("sw_deaths", SessionStatRegistry.Sorter.HIGHEST_SCORE, true, 0, 4, GameRegistry.Type.SKYWARS));}
                    {put("chestsopened", new StatType("sw_chestsopened", SessionStatRegistry.Sorter.HIGHEST_SCORE, false, 0, 5, GameRegistry.Type.SKYWARS));}
                    {put("arrowshit", new StatType("sw_arrowshit", SessionStatRegistry.Sorter.HIGHEST_SCORE, false, 0, 6, GameRegistry.Type.SKYWARS));}
                    {put("arrowsshot", new StatType("sw_arrowsshot", SessionStatRegistry.Sorter.HIGHEST_SCORE, false, 0, 12, GameRegistry.Type.SKYWARS));}
                    {put("blocksbroken", new StatType("sw_blocksbroken", SessionStatRegistry.Sorter.HIGHEST_SCORE, false, 0, 13, GameRegistry.Type.SKYWARS));}
                    {put("blocksplaced", new StatType("sw_blocksplaced", SessionStatRegistry.Sorter.HIGHEST_SCORE, false, 0, 14, GameRegistry.Type.SKYWARS));}
                    {put("win", new StatType("sw_win", SessionStatRegistry.Sorter.HIGHEST_SCORE, true, 15, -1, GameRegistry.Type.SKYWARS));}
                    {put("loss", new StatType("sw_loss", SessionStatRegistry.Sorter.HIGHEST_SCORE, true, 0, -1, GameRegistry.Type.SKYWARS));}
                    {put("gamesPlayed", new StatType("sw_gameplayed", SessionStatRegistry.Sorter.HIGHEST_SCORE, true, 10, -1, GameRegistry.Type.SKYWARS));}
                    {put("timePlayed", new StatType("sw_timeplayed", SessionStatRegistry.Sorter.HIGHEST_SCORE,true, 0, -1,
                            (p, o) -> p.getPersistentStats().getLongStatistics(GameRegistry.Type.SKYWARS).increment("sw_timeplayed", (Long) o)));}
                })
                .informationBook(new InformationBook(Babel.translate("skywars_information_title"), "skywars_information_page1", "skywars_information_page2"))
                .kitExtensions(Collections.singletonList(new BlockKitExtension()))
                .build();
        RisePlugin.getPlugin().getGameManager().setGamemode(gamemode);
    }

}
