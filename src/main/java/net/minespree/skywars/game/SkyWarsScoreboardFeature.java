package net.minespree.skywars.game;

import net.minespree.babel.Babel;
import net.minespree.feather.player.NetworkPlayer;
import net.minespree.rise.RisePlugin;
import net.minespree.rise.control.GameManager;
import net.minespree.rise.features.ScoreboardFeature;
import net.minespree.skywars.SkyWarsPlugin;
import net.minespree.skywars.game.events.GameEvent;
import net.minespree.skywars.states.SkywarsState;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkyWarsScoreboardFeature extends ScoreboardFeature {

    private GameManager gameManager;
    private SkyWarsMapData mapData;

    public SkyWarsScoreboardFeature() {
        super(SkyWarsPlugin.getPlugin(), ChatColor.GOLD + ChatColor.BOLD.toString() + "SkyWars");
    }

    @Override
    public void initialize(Player player) {
        setScore(player, "ip", ChatColor.GOLD + "play.minespree.net", 0);
        setScore(player, "gameId", ChatColor.GRAY + Bukkit.getServerName(), 1);
        setScore(player, "blank1", " ", 2);

        update(player);
    }

    @Override
    public void onStart() {
        gameManager = RisePlugin.getPlugin().getGameManager();
        mapData = (SkyWarsMapData) RisePlugin.getPlugin().getMapManager().getMapData();

        super.onStart();
    }

    @Override
    public void onStop() {}

    public void update() {
        Bukkit.getOnlinePlayers().forEach(this::update);
    }

    public void update(Player player) {
        setGameEvent(player);
        setPlayersLeft(player);
    }

    private void setGameEvent(Player player) {
        Optional<GameEvent> eventOpt = getState().getNextEvent();

        if (eventOpt.isPresent()) {
            GameEvent event = eventOpt.get();
            int time = event.getEventTime() - getState().getTimeInProgress();

            setScore(player, "eventTime", DurationFormatUtils.formatDuration(time * 1000, "mm:ss"), 3);
            setScore(player, "events", event.getEventName(), 4);
            setScore(player, "blank3", "   ", 5);
        } else {
            removeScore(player, "eventTime");
            removeScore(player, "events");
            removeScore(player, "blank3");
        }
    }

    private void setPlayersLeft(Player player) {
        int base = getState().getNextEvent().isPresent() ? 6 : 3;
        if(gameManager.isTeamHandler()) {

        } else {
            int alive = mapData.getAlive().size();
            if (alive > 3) {
                // just the players
                setScore(player, "playersLeftNum", Integer.toString(alive), base);
                setScore(player, "playersLeft", Babel.translate("sw_players_left_scoreboard"), base + 1);
                setScore(player, "blank2", "                ", base + 2);
            } else {
                setScore(player, "playersLeft", Babel.translate("sw_players_left_scoreboard"), alive + base);
                setScore(player, "blank2", "                ", base + alive + 1);
                List<Player> players = mapData.getAlive().stream()
                        .map(Bukkit::getPlayer)
                        .sorted(Comparator.comparing(HumanEntity::getName))
                        .collect(Collectors.toList());
                for (int i = 0; i < 3; i++) {
                    if (i >= players.size()) {
                        removeScore(player, "playersLeft-" + i);
                    }
                }
                for (int i = 0; i < players.size(); i++) {
                    setScore(player, "playersLeft-" + i, NetworkPlayer.of(players.get(i)).getName(), i + base);
                }
                removeScore(player, "playersLeftNum");
            }
        }
    }

    private SkywarsState getState() {
        return (SkywarsState) RisePlugin.getPlugin().getGameStateManager().getCurrentState();
    }

}
