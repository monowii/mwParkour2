package fr.monowii.parkour2.events;

import fr.monowii.parkour2.parkour.Parkour;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerParkourFinishEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Parkour parkour;
    private long time;
    private boolean cancelled;

    public PlayerParkourFinishEvent(Player player, Parkour parkour, long time) {
        this.player = player;
        this.parkour = parkour;
        this.time = time;
    }

    public Player getPlayer() {
        return player;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public long getTime() {
        return time;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean state) {
        cancelled = state;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
