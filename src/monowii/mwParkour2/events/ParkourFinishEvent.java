package monowii.mwParkour2.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourFinishEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private Player player;
	private int MapNumber;
	private long time;
	private boolean firstTime;
	
	public ParkourFinishEvent(Player player, int MapNumber, long time, boolean firstTime) {
		this.player = player;
		this.MapNumber = MapNumber;
		this.time = time;
		this.firstTime = firstTime;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return MapNumber;
	}
	
	public long getTime() {
		return time;
	}
	
	public boolean isFirstTime() {
		return firstTime;
	}
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}
