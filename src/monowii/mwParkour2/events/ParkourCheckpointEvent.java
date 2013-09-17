package monowii.mwParkour2.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourCheckpointEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	private int MapNumber;
	private int CheckpointNumber;
	private long time;
	
	public ParkourCheckpointEvent(Player player, int MapNumber, int CheckpointNumber, long time) {
		this.player = player;
		this.MapNumber = MapNumber;
		this.CheckpointNumber = CheckpointNumber;
		this.time = time;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return MapNumber;
	}
	
	public int getCheckpointNumber() {
		return CheckpointNumber;
	}
	
	public long getTime() {
		return time;
	}
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}
