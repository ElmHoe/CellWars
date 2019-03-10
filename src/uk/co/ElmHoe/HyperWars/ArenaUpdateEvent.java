package uk.co.ElmHoe.HyperWars;

import java.util.List;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaUpdateEvent extends Event{

	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private List<UUID> players;
	
	public ArenaUpdateEvent(Arena arena, List<UUID> players){
		this.arena = arena;
		this.players = players;
	}
	
	public Arena getUUID(){return arena;}
	public List<UUID> getPlayers(){return players;}
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}

}