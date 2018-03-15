package net.minespree.skywars.game.events;

import net.minespree.babel.Babel;
import net.minespree.babel.BabelMessage;
import net.minespree.babel.ComplexBabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.feather.data.gamedata.GameRegistry;
import net.minespree.feather.player.PlayerManager;
import net.minespree.feather.player.implementations.KittedPlayer;
import net.minespree.rise.RisePlugin;
import net.minespree.skywars.states.CageState;
import net.minespree.skywars.states.MainState;
import net.minespree.skywars.states.SkywarsState;
import net.minespree.wizard.util.Chat;
import net.minespree.wizard.util.MessageUtil;
import net.minespree.wizard.util.SetupUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;

public class CageGameEvent extends GameEvent {

    private final static BabelMessage SW_CAGES_RELEASED = Babel.translate("sw_cages_released");
    private final static BabelMessage SW_FIGHT = Babel.translate("sw_fight");

    private final static String[] COLOURS = new String[] {Chat.RED, Chat.GOLD, Chat.YELLOW, Chat.AQUA, Chat.CYAN, Chat.BLUE};

    public CageGameEvent(SkywarsGameMap map) {
        super(map, Babel.translate("sw_cage_event"), CageState.CAGE_RELEASE_TIME);
    }

    @Override
    public void occur(SkywarsState s) {
        CageState state = (CageState) s;
        state.getCageBlocks().forEach(location -> location.getBlock().setType(Material.AIR));
        state.getCageBlocks().clear();
        RisePlugin.getPlugin().getGameStateManager().changeState(new MainState(state.getTimeInProgress()));
        SW_CAGES_RELEASED.broadcast();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(!RisePlugin.getPlugin().getGameManager().getSpectatorHandler().isSpectator(player)) {
                SetupUtil.setupPlayer(player, false);
                KittedPlayer kp = (KittedPlayer) PlayerManager.getInstance().getPlayer(player);
                kp.getDefaultKit(GameRegistry.Type.SKYWARS).getTier().set(player);
            }
        });
        RisePlugin.getPlugin().getGameManager().getSpectatorHandler().setShowKits(false);
    }

    @Override
    public void tick(SkywarsState state) {
        if(state.getTimeInProgress() >= eventTime - 5) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 1F);
                ComplexBabelMessage message = new ComplexBabelMessage();
                int time = eventTime - state.getTimeInProgress();
                message.append(Chat.BOLD + COLOURS[time]);
                if(time == 0) {
                    message.append(SW_FIGHT);
                } else message.append(time + "");
                MessageUtil.sendTitle(player, message, 0, 30, time == 0 ? 20 : 0);
            });
        }
    }
}
