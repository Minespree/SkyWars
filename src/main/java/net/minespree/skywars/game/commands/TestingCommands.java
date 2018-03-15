package net.minespree.skywars.game.commands;

import net.minespree.feather.command.system.annotation.Command;
import net.minespree.feather.player.rank.Rank;
import net.minespree.rise.RisePlugin;
import net.minespree.rise.states.GameState;
import net.minespree.skywars.game.events.GameEvent;
import net.minespree.skywars.states.SkywarsState;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TestingCommands {

    @Command(names = "hurryevent", requiredRank = Rank.ADMIN, hideFromHelp = true)
    public static void nextEvent(Player player) {
        GameState state = RisePlugin.getPlugin().getGameStateManager().getCurrentState();
        if(state instanceof SkywarsState) {
            Optional<GameEvent> nextEvent = ((SkywarsState) state).getNextEvent();
            nextEvent.ifPresent(event -> ((SkywarsState) state).setTimeInProgress(event.getEventTime() - 1));
        }
    }

}
