package fr.monowii.parkour2.events;

import fr.monowii.parkour2.parkour.CheckpointInfo;
import fr.monowii.parkour2.parkour.Parkour;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerParkourCheckpointEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Parkour parkour;
    private CheckpointInfo checkpointInfo;
    private boolean cancelled;

    public PlayerParkourCheckpointEvent(Player player, Parkour parkour, CheckpointInfo checkpointInfo) {
        this.player = player;
        this.parkour = parkour;
        this.checkpointInfo = checkpointInfo;
    }

    public Player getPlayer() {
        return player;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public CheckpointInfo getCheckpointInfo() {
        return checkpointInfo;
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
