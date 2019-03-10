package uk.co.ElmHoe.HyperWars;

import java.util.UUID;

import org.bukkit.entity.Player;

import uk.co.ElmHoe.Data;
import uk.co.ElmHoe.Utilities.PlayerUtility;

public class PlayerData {
	
	private UUID uuid;
	private boolean dead;
	private Arena arena;
	private int wins;
	private int games;
	private int score;
	private int kills;
	private int deaths;
	private Kit kit;
	
	public PlayerData(UUID uuid, Arena arena){
		this.uuid = uuid;
		this.arena = arena;
		
		if(Data.getConfig().contains(uuid.toString() + ".cellwars")){
			wins = Data.getConfig().getInt(uuid.toString() + ".cellwars.wins");
			games = Data.getConfig().getInt(uuid.toString() + ".cellwars.games");
			score = Data.getConfig().getInt(uuid.toString() + ".cellwars.score");
			kills = Data.getConfig().getInt(uuid.toString() + ".cellwars.kills");
			deaths = Data.getConfig().getInt(uuid.toString() + ".cellwars.deaths");
		}else{
			wins = 0;
			games = 0;
			score = 0;
			kills = 0;
			deaths = 0;
			updateData();
		}
		
		kit = null;
		dead = false;
	}
	
	public void updateData(){
		Data.set(uuid.toString() + ".cellwars.wins", wins);
		Data.set(uuid.toString() + ".cellwars.games", games);
		Data.set(uuid.toString() + ".cellwars.score", score);
		Data.set(uuid.toString() + ".cellwars.kills", kills);
		Data.set(uuid.toString() + ".cellwars.deaths", deaths);
	}
	
	public Player getPlayer(){return PlayerUtility.getPlayer(uuid);}
	
	public void addDeath(){deaths++;updateData();}
	public void addKill(){kills++;updateData();}
	public void addWin(){wins++;updateData();}
	public void addGame(){games++;updateData();}
	public void setArena(Arena arena){this.arena = arena;}
	public void setDead(boolean bool){this.dead = bool;}
	public void addScore(int toAdd){score+=toAdd;updateData();}
	public void setScore(int toSet){score = toSet;updateData();}
	public void setKit(Kit kit){this.kit = kit;}
	
	public int getKills(){return kills;}
	public int getDeaths(){return deaths;}
	public int getScore(){return score;}
	public int getLoses(){return games - wins;}
	public int getGames(){return games;}
	public int getWins(){return wins;}
	public Arena getArena(){return arena;}
	public boolean isDead(){return dead;}
	public UUID getUUID(){return uuid;}
	public Kit getKit(){return kit;}
}
