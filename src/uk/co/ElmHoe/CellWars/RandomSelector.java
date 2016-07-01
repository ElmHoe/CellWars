package uk.co.ElmHoe.CellWars;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.entity.Player;

import uk.co.ElmHoe.Utilities.PlayerUtility;

public abstract class RandomSelector {
	
	public static void randomAdd(UUID uuid, int difficulty){
		List<Arena> empty = new ArrayList<Arena>();
		List<Arena> ideal = new ArrayList<Arena>();
		List<Arena> notIdeal = new ArrayList<Arena>();
		
		for(Arena arena : CellWars.plugin.getArenas()){
			if(!arena.isPlaying() && !arena.isRegenerating() && !arena.isClosed()){
				if(arena.getPlayers().size() < arena.getSpawns().size()){
					if(arena.getPlayers().size() > 0){
						int averageScore = getAverageScore(arena.getPlayers());
						if(Math.abs(averageScore - difficulty) < 100){
							ideal.add(arena);
						}else{
							notIdeal.add(arena);
						}
					}else{
						empty.add(arena);
					}
				}
			}
		}
		
		if(ideal.size() > 0){
			ideal.get((new Random()).nextInt(ideal.size())).addPlayer(uuid);
		}else if(notIdeal.size() > 0){
			notIdeal.get((new Random()).nextInt(notIdeal.size())).addPlayer(uuid);
		}else if(empty.size() > 0){
			empty.get((new Random()).nextInt(empty.size())).addPlayer(uuid);
		}else{
			Player p = PlayerUtility.getPlayer(uuid);
			p.teleport(CellWars.signLobby);
			p.sendMessage(CellWars.header + "All of the maps were full! Please try again later.");
		}
	}
	
	public static int getAverageScore(List<PlayerData> players){
		if(players.size() == 0){
			return 0;
		}
		int count = 0;
		for(PlayerData p : players){
			count += p.getScore();
		}
		return count / players.size();
	}
	
}
