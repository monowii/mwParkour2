package fr.monowii.parkour2.events;

import fr.monowii.parkour2.level.CheckpointInfo;
import fr.monowii.parkour2.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerParkourCheckpointEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Level level;
    private CheckpointInfo checkpointInfo;
    private boolean cancelled;

    public PlayerParkourCheckpointEvent(Player player, Level level, CheckpointInfo checkpointInfo) {
        this.player = player;
        this.level = level;
        this.checkpointInfo = checkpointInfo;
    }

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
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
