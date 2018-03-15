package net.minespree.skywars.game.events;

import lombok.Getter;
import lombok.Setter;
import net.minespree.babel.BabelMessage;
import net.minespree.cartographer.maps.SkywarsGameMap;
import net.minespree.skywars.SkyWarsPlugin;
import net.minespree.skywars.states.SkywarsState;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@Getter
public abstract class GameEvent implements Listener {

    private boolean started;

    protected final SkywarsGameMap map;
    protected final BabelMessage eventName;
    @Setter
    protected int eventTime;
    protected boolean hidden;

    public GameEvent(SkywarsGameMap map, BabelMessage eventName, int eventTime) {
        this(map, eventName, eventTime, false);
    }

    public GameEvent(SkywarsGameMap map, BabelMessage eventName, int eventTime, boolean hidden) {
        this.map = map;
        this.eventName = eventName;
        this.eventTime = eventTime;
        this.hidden = hidden;

        Bukkit.getPluginManager().registerEvents(this, SkyWarsPlugin.getPlugin());
    }

    public void startEvent(SkywarsState state) {
        if(started)
            return;
        started = true;
        start(state);
    }

    public void start(SkywarsState state) {}


    public abstract void occur(SkywarsState state);
    public void tick(SkywarsState state) {}

    public void stop(SkywarsState state) {
        HandlerList.unregisterAll(this);
    }

}
