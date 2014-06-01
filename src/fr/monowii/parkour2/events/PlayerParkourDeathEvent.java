package fr.monowii.parkour2.events;

import fr.monowii.parkour2.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerParkourDeathEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Level level;
    private ParkourDeathCause cause;
    private boolean cancelled;

    public PlayerParkourDeathEvent(Player player, Level level, ParkourDeathCause cause) {
        this.player = player;
        this.level = level;
        this.cause = cause;
    }

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public ParkourDeathCause getCause() {
        return cause;
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
