package uk.co.ElmHoe.CellWars;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import uk.co.ElmHoe.Utilities.NumberUtility;
import uk.co.ElmHoe.Utilities.PexUtility;
import uk.co.ElmHoe.Utilities.PlayerUtility;
import uk.co.ElmHoe.Utilities.StringUtility;
import uk.co.ElmHoe.CellWars.PlayerData;
import uk.co.ElmHoe.CellWarsScoreboard.CellWarsScoreboard;
import uk.co.ElmHoe.InfoboardAPI.InfoboardAPI;

public class Arena {
	
	private String name;
	private boolean playing;
	private List<Location> spawns;
	private List<PlayerData> players;
	private Location spectate;
	private Location lobby;
	private List<Location> signs;
	private List<UUID> allPlayers;
	private boolean regenerate;
	private boolean arenaChat;
	private int minimumPlayers;
	private int startCountdown;
	private World world;
	private Location upperBound;
	private Location lowerBound;
	private List<UUID> spectators;
	private boolean lockPlayers;
	private List<Integer> lobbyTimer;
	private boolean countdown;
	private boolean regenerating;
	private boolean closed;
	private boolean stopCountdown;
	public int timer;
	public Inventory playerMenu;
	private ItemStack returnItem;
	
	public Arena(String name, List<Location> spawns, Location spectate, Location lobby, List<Location> signs, boolean regenerate, boolean arenaChat, int minimumPlayers, int countdown, World world, Location upperBound, Location lowerBound, List<Integer> lobbyTimer){
		this.name = name;
		this.spawns = spawns;
		this.spectate = spectate;
		this.lobby = lobby;
		this.signs = signs;
		this.regenerate = regenerate;
		this.arenaChat = arenaChat;
		this.minimumPlayers = minimumPlayers;
		this.startCountdown = countdown;
		this.world = world;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		this.lobbyTimer = lobbyTimer;
		
		this.setLocked(true);
		playing = false;
		closed = false;
		regenerating = false;
		this.countdown = false;
		stopCountdown = false;
		players = new ArrayList<PlayerData>();
		spectators = new ArrayList<UUID>();
		allPlayers = new ArrayList<UUID>();
		timer = 0;
		count = 60;
		this.updateSigns();
		
		returnItem = new ItemStack(Material.EMERALD);
		ItemMeta meta = returnItem.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Spectate Spawn" + ChatColor.DARK_GRAY + "]");
		returnItem.setItemMeta(meta);
		
		playerMenu = Bukkit.createInventory(null, (spawns.size() / 9 + 1) * 9, ChatColor.DARK_GRAY + "[" + ChatColor.RED + "PlayerMenu" + ChatColor.DARK_GRAY + "]");
	}
	
	private void endOfCountdown(){
		this.setLocked(false);
		tellPlayers(CellWars.messages.get(Message.START_OF_GAME));
		for(PlayerData player : players){
			allPlayers.add(player.getUUID());
		}
	}
	
	public void startGame(){
		Map<Location, UUID> taken = new HashMap<Location, UUID>();
		
		while(taken.size() < players.size()){
			for(PlayerData player : players){
				if(!taken.values().contains(player.getUUID())){
					int randomSpawn = NumberUtility.getRandom(spawns.size());
					if(!taken.containsKey(spawns.get(randomSpawn))){
						player.getPlayer().teleport(spawns.get(randomSpawn));
						PlayerUtility.clearInventory(player.getUUID());
						taken.put(spawns.get(randomSpawn), player.getUUID());
						if(player.getKit() != null){
							player.getKit().giveKit(player.getUUID());
						}
						player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
					}
				}
			}
		}
		countdown = false;
		stopCountdown = true;
		this.setPlaying(true);
		updateScoreboard();
		
		endOfCountdown();
	}
	
	public void updateScoreboard(){
		int count = 0;
		Map<String, Integer> data = new HashMap<String, Integer>();
		playerMenu.clear();
		for(PlayerData player : players){
			if(count < 16){
				data.put(StringUtility.trim(ChatColor.YELLOW + player.getPlayer().getName(), 14), player.getScore());
			}
			count++;
			
			ItemStack item = new ItemStack(Material.SKULL_ITEM,1,(short)3);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(player.getPlayer().getName());
			item.setItemMeta(meta);
			
			playerMenu.setItem(players.indexOf(player), item);
			playerMenu.setItem((spawns.size() / 9 + 1) * 9 - 1, returnItem);
		}
		
		for(PlayerData player : players){
			InfoboardAPI.getPlayer(player.getUUID()).setData(2, CellWars.header, data);
			InfoboardAPI.getPlayer(player.getUUID()).setCurrentBoard(1);
			InfoboardAPI.getPlayer(player.getUUID()).setStaticBoard(1);
		}
		
		for(UUID uuid : spectators){
			InfoboardAPI.getPlayer(uuid).setData(2, CellWars.header, data);
			InfoboardAPI.getPlayer(uuid).setCurrentBoard(1);
			InfoboardAPI.getPlayer(uuid).setStaticBoard(1);
		}
		
		ArenaUpdateEvent event = new ArenaUpdateEvent(this, allPlayers);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}
	
	/*public void countDown(final int count, final Arena arena){
		Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
		    public void run() {
		        if(count <= 0){
		        	arena.endOfCountdown();
		        }else{
		        	tellPlayers(CellWars.messages.get(Message.COUNTDOWN).replace("{COUNTDOWN}", "" + (count)));
		        	countDown(count - 1, arena);
		        }
		    }
		}, 20);
	}*/
	
	public int count;
	
	public void lobbyTimer(final Arena arena){
		if(stopCountdown){
			stopCountdown = false;
			countdown = false;
			return;
		}
		countdown = true;
		Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
		    public void run() {
		        if(arena.count <= 0){
		        	startGame();
		        	for(PlayerData player : players){
		        		player.getPlayer().setLevel(0);
		        	}
		        	return;
		        }else if(lobbyTimer.contains(Integer.valueOf(arena.count))){
		        	tellPlayers(CellWars.messages.get(Message.COUNTDOWN).replace("{COUNTDOWN}", "" + (arena.count)));
		        }
		        for(PlayerData player : players){
	        		player.getPlayer().setLevel(arena.count);
	        	}
		        if(arena.getPlayers().size() >= arena.getSpawns().size() && arena.count > 10){
		    		arena.count = 10;
		    	}else{
		    		arena.count = arena.count - 1;
		    	}
		        lobbyTimer(arena);
		    }
		}, 20);
	}
	
	public void randomTnT(){
		if(allPlayers.size() > 2){
			Random r = new Random();
			int x = r.nextInt(this.getUpper().getBlockX() - this.getLower().getBlockX()) + this.getLower().getBlockX();
			int y = r.nextInt(this.getUpper().getBlockY() - this.getLower().getBlockY()) + this.getLower().getBlockY();
			int z = r.nextInt(this.getUpper().getBlockZ() - this.getLower().getBlockZ()) + this.getLower().getBlockZ();
			Location loc = new Location(world,x,y,z);
			float power = (float)(r.nextInt(15) + 1);
			if(loc.getBlock().getType() == Material.AIR){
				loc.getBlock().setType(Material.LAVA);
				world.createExplosion(x,y,z, power, true, true);
			}else{
				if(loc.getBlock().getType() != Material.BEDROCK){
					world.createExplosion(x,y,z, power, true, true);
				}else{
					this.randomTnT();
				}
			}
		}
	}
	
	public void stopGame(boolean forced){
		List<PlayerData> toRemovePlayer = new ArrayList<PlayerData>();
		for(PlayerData player : players){
			if(!forced){
				if(!player.isDead()){
					for(Player p : PlayerUtility.getOnlinePlayers()){
						if(CellWars.plugin.getArena(p.getUniqueId()) == null && !CellWars.plugin.isSpectating(p.getUniqueId())){
							p.sendMessage(CellWars.messages.get(Message.END_OF_GAME).replace("{PLAYER}", player.getPlayer().getName()).replace("{ARENA}", this.getName()));
						}
					}
					//Bukkit.broadcastMessage(CellWars.messages.get(Message.END_OF_GAME).replace("{PLAYER}", player.getPlayer().getName()).replace("{ARENA}", this.getName()));
					tellSpectators(CellWars.messages.get(Message.END_OF_GAME).replace("{PLAYER}", player.getPlayer().getName()).replace("{ARENA}", this.getName()));
					tellPlayers(CellWars.messages.get(Message.END_OF_GAME).replace("{PLAYER}", player.getPlayer().getName()).replace("{ARENA}", this.getName()));
					player.addWin();
					player.addScore(CellWars.plugin.scorePerWin);
				}
			}
			player.getPlayer().teleport(CellWars.signLobby);
			PlayerUtility.clearInventory(player.getUUID());
			player.getPlayer().setLevel(0);
			for(PotionEffect effect : player.getPlayer().getActivePotionEffects()){
				player.getPlayer().removePotionEffect(effect.getType());
			}
			player.getPlayer().resetMaxHealth();
			toRemovePlayer.add(player);
		}
		for(PlayerData p : toRemovePlayer){
			this.removePlayer(p.getUUID(), forced, false);
		}
		
		List<UUID> toRemove = new ArrayList<UUID>();
		for(UUID uuid : spectators){
			toRemove.add(uuid);
		}
		for(UUID uuid : toRemove){
			this.removeSpectator(uuid);
		}
		players = new ArrayList<PlayerData>();
		spectators = new ArrayList<UUID>();
		this.setPlaying(false);
		stopCountdown = false;
		this.setLocked(true);
		if(regenerate && !forced){
			this.loadMap();
		}
		updateSigns();
		timer = 0;
		ArenaUpdateEvent event = new ArenaUpdateEvent(this, allPlayers);
		Bukkit.getServer().getPluginManager().callEvent(event);
		allPlayers = new ArrayList<UUID>();
	}
	
	public void loadMap(){
		final Arena arena = this;
		final File file = new File(CellWars.plugin.getDataFolder(), this.getName() + ".data");
		this.setRegenerating(true);
		Bukkit.getScheduler().runTaskLaterAsynchronously(CellWars.plugin, new Runnable() {
		    public void run() {
				FileOutput.loadFile(file, arena);
		    }
		}, 20);
	}
	
	public void tellPlayers(String message){
		for(PlayerData player : players){
			if(player.getPlayer() != null){
				player.getPlayer().sendMessage(message);
			}
		}
	}
	
	public void tellSpectators(String message){
		for(UUID uuid : this.spectators){
			if(PlayerUtility.getPlayer(uuid) != null){
				PlayerUtility.getPlayer(uuid).sendMessage(message);
			}
		}
	}
	
	public void updateSigns(){
		List<Location> toRemove = new ArrayList<Location>();
		for(Location location : signs){
			if(location.getBlock().getState() instanceof Sign){
				Sign sign = (Sign)location.getBlock().getState();
				sign.setLine(3, StringUtility.format(players.size() + "/" + spawns.size()));
				if(this.closed){
					sign.setLine(0, ChatColor.DARK_RED + "[Closed!]");
				}else if(this.isRegenerating()){
					sign.setLine(0, ChatColor.RED + "[Loading...]");
				}else if(this.isPlaying()){
					sign.setLine(0, ChatColor.GOLD + "[InGame]");
				}else{
					sign.setLine(0, ChatColor.DARK_AQUA + "[Join]");
				}
				sign.update();
			}else{
				toRemove.add(location);
			}
		}
		for(Location location : toRemove){
			signs.remove(location);
		}
	}
	
	public boolean isRegenerating(){return this.regenerating;}
	
	public void addPlayer(UUID uuid){
		if(PlayerUtility.getPlayer(uuid) != null && CellWars.plugin.getArena(uuid) == null){
			if(!allPlayers.contains(uuid)){
				allPlayers.add(uuid);
			}
			CellWars.plugin.removeSpectate(uuid);
			if(getPlayerData(uuid) == null){
				players.add(new PlayerData(uuid, this));
			}
			if(players.size() >= this.getMinimumPlayers() && !countdown){
				int max = 0;
				for(Integer i : lobbyTimer){
					if(max < i){
						max = i;
					}
				}
				count = max;
				this.lobbyTimer(this);
			}
			for(Player p : this.getPlayerObjects()){
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			}
			PlayerUtility.getPlayer(uuid).teleport(this.getLobby());
			PlayerUtility.getPlayer(uuid).getInventory().setItem(8, CellWars.plugin.menuItem);
			PlayerUtility.getPlayer(uuid).openInventory(CellWars.plugin.kitSelector);
			PlayerUtility.getPlayer(uuid).setGameMode(GameMode.SURVIVAL);
			PlayerUtility.getPlayer(uuid).resetMaxHealth();
			PlayerUtility.getPlayer(uuid).setFoodLevel(20);
			for(PotionEffect effect : PlayerUtility.getPlayer(uuid).getActivePotionEffects()){
				PlayerUtility.getPlayer(uuid).removePotionEffect(effect.getType());
			}
			updateSigns();
			this.updateScoreboard();
			this.tellPlayers(CellWars.messages.get(Message.ARENA_JOIN).replace("{MAXPLAYERS}", "" + this.spawns.size()).replace("{PLAYERS}", "" + this.players.size()).replace("{ARENA}", this.getName()).replace("{PLAYER}", PlayerUtility.getPlayer(uuid).getName()));
		}
	}
	
	public PlayerData getPlayerData(UUID uuid){
		for(PlayerData player : players){
			if(player.getUUID().equals(uuid)){
				return player;
			}
		}
		return null;
	}
	
	public void addSpectator(UUID uuid){
		Player p = PlayerUtility.getPlayer(uuid);
		if(p != null && !CellWars.plugin.isSpectating(uuid)){
			p.teleport(this.getSpectate());
			p.setAllowFlight(true);
			for(PlayerData player : players){
				player.getPlayer().hidePlayer(p);
			}
			for(UUID spectator : spectators){
				PlayerUtility.getPlayer(spectator).hidePlayer(p);
				p.hidePlayer(PlayerUtility.getPlayer(spectator));
			}
			
			p.getInventory().setItem(8, CellWars.plugin.playerSelector);
			PlayerUtility.updateInventory(p.getUniqueId());
			
			if(!spectators.contains(uuid)){
				spectators.add(uuid);
			}
			updateScoreboard();
			PexUtility.setPrefix(uuid, ChatColor.GRAY + "[" + ChatColor.RED + "Spectating" + ChatColor.GRAY + "]" + PexUtility.getPrefix(uuid));
		}
	}
	
	public void addSign(Location location){
		if(!signs.contains(location)){
			signs.add(location);
			this.updateSigns();
		}
	}
	
	public void addSpawn(Location location){
		if(!this.spawns.contains(location)){
			this.spawns.add(location);
		}
	}
	
	public void removeSpawn(Location location){
		if(this.spawns.contains(location)){
			this.spawns.remove(location);
		}
	}
	
	public void setRegenerating(boolean regen){this.regenerating = regen;this.updateSigns();}
	
	public void removePlayer(UUID uuid, boolean forced, boolean msg){
		CellWars.plugin.removeSpectate(uuid);
		InfoboardAPI.getPlayer(uuid).setCurrentBoard(0);
		InfoboardAPI.getPlayer(uuid).setStaticBoard(-1);
		InfoboardAPI.getPlayer(uuid).removeBoard(2);
		
		if(forced){
			allPlayers.remove(uuid);
		}
		
		if(this.isPlaying() && !forced){
			PlayerData p = getPlayerData(uuid);
			p.addGame();
			p.addScore(CellWars.plugin.scorePerGame);
		}
		
		if(getPlayerData(uuid) != null){
			players.remove(getPlayerData(uuid));
		}
		if(this.countdown){
			if(this.getPlayers().size() < this.getMinimumPlayers()){
				this.stopCountdown = true;
			}
		}
		if(PlayerUtility.getPlayer(uuid) != null){
			PlayerUtility.getPlayer(uuid).teleport(CellWars.signLobby);
			Player player = PlayerUtility.getPlayer(uuid);
			PlayerUtility.clearInventory(player.getUniqueId());
			player.setLevel(0);
			for(PotionEffect effect : player.getActivePotionEffects()){
				player.removePotionEffect(effect.getType());
			}
			player.resetMaxHealth();
			player.setHealth(20.0);
			player.setFoodLevel(20);
			CellWarsScoreboard.getFromUUID(uuid).updateData();
		}
		if(this.getAlivePlayers() <= 1 && this.isPlaying()){
			Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
    		    public void run() {
    		    	stopGame(false);
    		    }
    		}, 5);
		}
		updateSigns();
		this.updateScoreboard();
		if(msg)
		this.tellPlayers(CellWars.messages.get(Message.ARENA_LEAVE).replace("{MAXPLAYERS}", "" + this.spawns.size()).replace("{PLAYERS}", "" + this.players.size()).replace("{ARENA}", this.getName()).replace("{PLAYER}", PlayerUtility.getPlayer(uuid).getName()));
	}
	
	public void removeSpectator(UUID uuid){
		if(spectators.contains(uuid)){
			spectators.remove(uuid);
		}
		
		InfoboardAPI.getPlayer(uuid).setCurrentBoard(0);
		InfoboardAPI.getPlayer(uuid).setStaticBoard(-1);
		InfoboardAPI.getPlayer(uuid).removeBoard(2);
		
		if(PlayerUtility.getPlayer(uuid) != null){
			PlayerUtility.getPlayer(uuid).teleport(CellWars.signLobby);
			Player spectator = PlayerUtility.getPlayer(uuid);
			for(Player p : PlayerUtility.getOnlinePlayers()){
				if(!p.getUniqueId().equals(spectator.getUniqueId())){
					p.showPlayer(spectator);
					spectator.showPlayer(p);
				}
			}
			spectator.setAllowFlight(false);
			PexUtility.resetPrefix(uuid);
			CellWarsScoreboard.getFromUUID(uuid).updateData();
			PlayerUtility.getPlayer(uuid).setFoodLevel(20);
		}
		PlayerUtility.clearInventory(uuid);
	}
	
	public int getAlivePlayers(){
		int count = 0;
		for(PlayerData player : players){
			if(!player.isDead()){
				count++;
			}
		}
		return count;
	}
	
	public Player[] getPlayerObjects(){
		Player array[] = new Player[this.players.size()];
		for(PlayerData player : players){
			array[players.indexOf(player)] = player.getPlayer();
		}
		return array;
	}
	
	@SuppressWarnings("deprecation")
	public BlockData[] grabBlocks(){
		BlockData data[] = new BlockData[(upperBound.getBlockX() - lowerBound.getBlockX() + 1) * (upperBound.getBlockY() - lowerBound.getBlockY() + 1) * (upperBound.getBlockZ() - lowerBound.getBlockZ() + 1)];
		int count = 0;
		for(int x = lowerBound.getBlockX(); x <= upperBound.getBlockX(); x++){
			for(int y = lowerBound.getBlockY(); y <= upperBound.getBlockY(); y++){
				for(int z = lowerBound.getBlockZ(); z <= upperBound.getBlockZ(); z++){
					Location loc = new Location(world,x,y,z);
					data[count] = new BlockData(x, y, z, loc.getBlock().getType(), loc.getBlock().getData());
					count++;
				}
			}
		}
		return data;
	}
	
	public void setLowerBound(Location lower){this.lowerBound = lower;}
	public void setUpperBound(Location upper){this.upperBound = upper;}
	public void setCountdown(int count){this.startCountdown = count;}
	public void setMinimumPlayers(int minPlayers){this.minimumPlayers = minPlayers;}
	public void setArenaChat(boolean chat){this.arenaChat = chat;}
	public void setRegenerate(boolean regen){this.regenerate = regen;}
	public void setPlaying(boolean playing){this.playing = playing;}
	public void setLocked(boolean lock){this.lockPlayers = lock;}
	public void setLobbySpawn(Location location){this.lobby = location;}
	public void setSpectateSpawn(Location location){this.spectate = location;}
	public void setWorld(World world){this.world = world;}
	public void setClosed(boolean closed){this.closed = closed;}
	
	public boolean hasLobbyCounted(){return countdown;}
	public List<Integer> getTimer(){return this.lobbyTimer;}
	public List<UUID> getSpectators(){return spectators;}
	public Location getLower(){return lowerBound;}
	public Location getUpper(){return upperBound;}
	public World getWorld(){return world;}
	public int getStartCountdown(){return startCountdown;}
	public int getMinimumPlayers(){return minimumPlayers;}
	public boolean isArenaChat(){return arenaChat;}
	public boolean isRegenerate(){return regenerate;}
	public List<Location> getSigns(){return signs;}
	public Location getLobby(){return lobby;}
	public Location getSpectate(){return spectate;}
	public List<PlayerData> getPlayers(){return players;}
	public List<Location> getSpawns(){return spawns;}
	public boolean isPlaying(){return playing;}
	public String getName(){return name;}
	public boolean isLocked(){return this.lockPlayers;}
	public boolean isClosed(){return this.closed;}
}
