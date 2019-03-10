package uk.co.ElmHoe.HyperWars;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class FileOutput {
	
	private static String sentence = "{X},{Y},{Z},{DATA},{MATERIAL}";
	
	public static void saveFile(BlockData data[], File file){
		file.getParentFile().mkdirs();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("# CellWars Arena Schematic File -- Do NOT Modify the Contents of this file! #\n");
			for(int i = 0; i < data.length; i++){
				writer.write(sentence.replace("{X}", "" + data[i].getX()).replace("{Y}", "" + data[i].getY()).replace("{Z}", "" + data[i].getZ()).replace("{DATA}", "" + data[i].getData()).replace("{MATERIAL}", "" + data[i].getMaterial().name()));
				writer.write("\n");
				//conserve ram
				writer.flush();
			}
			writer.close();
			CellWars.plugin.getLogger().log(Level.INFO, file.getPath() + " saved!");
		} catch (IOException e) {
			CellWars.plugin.getLogger().log(Level.WARNING, file.getPath() + " failed to save!");
			CellWars.plugin.warnAdmins(file.getPath() + " has failed to save!");
			e.printStackTrace();
		}
	}
	
	public static void clear(Arena arena){
		List<BlockData> blocks = new ArrayList<BlockData>();
		for(int x = arena.getLower().getBlockX(); x < arena.getUpper().getBlockX(); x++){
			for(int y = arena.getLower().getBlockY(); y < arena.getUpper().getBlockY(); y++){
				for(int z = arena.getLower().getBlockZ(); z < arena.getUpper().getBlockZ(); z++){
					blocks.add(new BlockData(x,y,z,Material.AIR,(byte)0));
				}
			}
		}
		AsyncBuilderSession session = new AsyncBuilderSession(blocks, CellWars.blocksPerSecond, arena);
		CellWars.sessionManager.addSession(session);
		CellWars.plugin.getLogger().log(Level.INFO, arena.getName() + " cleared!");
	}
	
	public static boolean loadFile(File file, Arena arena){
		if(file.exists()){
			try (BufferedReader br = new BufferedReader(new FileReader(file)))
			{
				String line;
				List<BlockData> blocks = new ArrayList<BlockData>();
				while ((line = br.readLine()) != null) {
					String sentence = line;
					if(line.length() > 0){
						if(!line.substring(0,1).equals("#")){
							String split[] = sentence.split(",");
							byte data = (byte)toInt(split[3]);
							Material material = Material.getMaterial(split[4]);
							blocks.add(new BlockData(toInt(split[0]), toInt(split[1]), toInt(split[2]), material, data));
						}
					}
				}
				AsyncBuilderSession session = new AsyncBuilderSession(blocks, CellWars.blocksPerSecond, arena);
				CellWars.sessionManager.addSession(session);
				CellWars.plugin.getLogger().log(Level.INFO, file.getPath() + " loaded!");
				return true;
			} catch (IOException e) {
				CellWars.plugin.getLogger().log(Level.WARNING, file.getPath() + " failed to load!");
				CellWars.plugin.warnAdmins(file.getPath() + " has failed to load!");
				e.printStackTrace();
				return false;
			} 
		}
		CellWars.plugin.getLogger().log(Level.WARNING, file.getPath() + " failed to load!");
		CellWars.plugin.warnAdmins(file.getPath() + " has failed to load!");
		return false;
	}
	
	private static int toInt(String string){
		try{
			return (int)Double.parseDouble(string);
		}catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void saveConfig(File file, Arena arena){
		if(!file.exists()){
	        file.getParentFile().mkdirs();
	    }
		FileConfiguration config = new YamlConfiguration();
		
		config.createSection("config.arenaData");
		config.createSection("config.arenaData.name");
		config.set("config.arenaData.name", arena.getName());
		config.createSection("config.arenaData.arenaChat");
		config.set("config.arenaData.arenaChat", arena.isArenaChat());
		config.createSection("config.arenaData.regenerate");
		config.set("config.arenaData.regenerate", arena.isRegenerate());
		config.createSection("config.arenaData.minimumPlayers");
		config.set("config.arenaData.minimumPlayers", arena.getMinimumPlayers());
		config.createSection("config.arenaData.startCountdown");
		config.set("config.arenaData.startCountdown", arena.getStartCountdown());
		config.createSection("config.arenaData.worldName");
		config.set("config.arenaData.worldName", arena.getWorld().getName());
		config.createSection("config.arenaData.upperBound");
		config.createSection("config.arenaData.upperBound.X");
		config.set("config.arenaData.upperBound.X", arena.getUpper().getBlockX());
		config.createSection("config.arenaData.upperBound.Y");
		config.set("config.arenaData.upperBound.Y", arena.getUpper().getBlockY());
		config.createSection("config.arenaData.upperBound.Z");
		config.set("config.arenaData.upperBound.Z", arena.getUpper().getBlockZ());
		config.createSection("config.arenaData.lowerBound");
		config.createSection("config.arenaData.lowerBound.X");
		config.set("config.arenaData.lowerBound.X", arena.getLower().getBlockX());
		config.createSection("config.arenaData.lowerBound.Y");
		config.set("config.arenaData.lowerBound.Y", arena.getLower().getBlockY());
		config.createSection("config.arenaData.lowerBound.Z");
		config.set("config.arenaData.lowerBound.Z", arena.getLower().getBlockZ());
		config.createSection("config.arenaData.spectate");
		config.createSection("config.arenaData.spectate.X");
		config.set("config.arenaData.spectate.X", arena.getSpectate().getX());
		config.createSection("config.arenaData.spectate.Y");
		config.set("config.arenaData.spectate.Y", arena.getSpectate().getY());
		config.createSection("config.arenaData.spectate.Z");
		config.set("config.arenaData.spectate.Z", arena.getSpectate().getZ());
		config.createSection("config.arenaData.spectate.Yaw");
		config.set("config.arenaData.spectate.Yaw", arena.getSpectate().getYaw());
		config.createSection("config.arenaData.spectate.Pitch");
		config.set("config.arenaData.spectate.Pitch", arena.getSpectate().getPitch());
		config.createSection("config.arenaData.lobby");
		config.createSection("config.arenaData.lobby.X");
		config.set("config.arenaData.lobby.X", arena.getLobby().getX());
		config.createSection("config.arenaData.lobby.Y");
		config.set("config.arenaData.lobby.Y", arena.getLobby().getY());
		config.createSection("config.arenaData.lobby.Z");
		config.set("config.arenaData.lobby.Z", arena.getLobby().getZ());
		config.createSection("config.arenaData.lobby.Yaw");
		config.set("config.arenaData.lobby.Yaw", arena.getLobby().getYaw());
		config.createSection("config.arenaData.lobby.Pitch");
		config.set("config.arenaData.lobby.Pitch", arena.getLobby().getPitch());
		config.createSection("config.arenaData.lobby.World");
		config.set("config.arenaData.lobby.World", arena.getLobby().getWorld().getName());
		config.createSection("config.arenaData.spawns");
		
		for(int i = 0; i < arena.getSpawns().size(); i++){
			config.createSection("config.arenaData.spawns." + i);
			config.createSection("config.arenaData.spawns." + i + ".X");
			config.set("config.arenaData.spawns." + i + ".X", arena.getSpawns().get(i).getX());
			config.createSection("config.arenaData.spawns." + i + ".Y");
			config.set("config.arenaData.spawns." + i + ".Y", arena.getSpawns().get(i).getY());
			config.createSection("config.arenaData.spawns." + i + ".Z");
			config.set("config.arenaData.spawns." + i + ".Z", arena.getSpawns().get(i).getZ());
			config.createSection("config.arenaData.spawns." + i + ".Yaw");
			config.set("config.arenaData.spawns." + i + ".Yaw", arena.getSpawns().get(i).getYaw());
			config.createSection("config.arenaData.spawns." + i + ".Pitch");
			config.set("config.arenaData.spawns." + i + ".Pitch", arena.getSpawns().get(i).getPitch());
		}
		
		for(int i = 0; i < arena.getSigns().size(); i++){
			config.createSection("config.arenaData.sign." + i);
			config.createSection("config.arenaData.sign." + i + ".X");
			config.set("config.arenaData.sign." + i + ".X", arena.getSigns().get(i).getBlockX());
			config.createSection("config.arenaData.sign." + i + ".Y");
			config.set("config.arenaData.sign." + i + ".Y", arena.getSigns().get(i).getBlockY());
			config.createSection("config.arenaData.sign." + i + ".Z");
			config.set("config.arenaData.sign." + i + ".Z", arena.getSigns().get(i).getBlockZ());
			config.createSection("config.arenaData.sign." + i + ".World");
			config.set("config.arenaData.sign." + i + ".World", arena.getSigns().get(i).getWorld().getName());
		}
		
		
		config.createSection("config.arenaData.lobbyTimer");
		List<String> values = new ArrayList<String>();
		for(Integer number : arena.getTimer()){
			values.add("" + number);
		}
		config.set("config.arenaData.lobbyTimer", values);
		
		try {
			config.save(file);
			CellWars.plugin.getLogger().log(Level.INFO, arena.getName() + ".yml saved!");
		} catch (IOException e) {
			CellWars.plugin.getLogger().log(Level.WARNING, arena.getName() + ".yml failed to save!");
			CellWars.plugin.warnAdmins(arena.getName() + ".yml has failed to save!");
			e.printStackTrace();
		}
	}
	
	public static Arena loadConfig(File file){
		if(file.exists()){
			FileConfiguration config = new YamlConfiguration();
			
			try {
				config.load(file);
			} catch (Exception e) {
				CellWars.plugin.warnAdmins(file.getPath() + " has failed to load into config!");
				e.printStackTrace();
			}
			
			String name = config.getString("config.arenaData.name");
			boolean arenaChat = config.getBoolean("config.arenaData.arenaChat");
			boolean regenerate = config.getBoolean("config.arenaData.regenerate");
			int minPlayers = config.getInt("config.arenaData.minimumPlayers");
			int countdown = config.getInt("config.arenaData.startCountdown");
			World world = Bukkit.getWorld(config.getString("config.arenaData.worldName"));
			Location upperBound = new Location(world, config.getInt("config.arenaData.upperBound.X"), config.getInt("config.arenaData.upperBound.Y"),
					config.getInt("config.arenaData.upperBound.Z"));
			Location lowerBound = new Location(world, config.getInt("config.arenaData.lowerBound.X"), config.getInt("config.arenaData.lowerBound.Y"),
					config.getInt("config.arenaData.lowerBound.Z"));
			Location spectate = new Location(world, config.getDouble("config.arenaData.spectate.X"), config.getDouble("config.arenaData.spectate.Y"),
					config.getDouble("config.arenaData.spectate.Z"), (float)config.getDouble("config.arenaData.spectate.Yaw"), (float)config.getDouble("config.arenaData.spectate.Pitch"));
			Location lobby = new Location(Bukkit.getWorld(config.getString("config.arenaData.lobby.World")), config.getDouble("config.arenaData.lobby.X"), config.getDouble("config.arenaData.lobby.Y"),
					config.getDouble("config.arenaData.lobby.Z"), (float)config.getDouble("config.arenaData.lobby.Yaw"), (float)config.getDouble("config.arenaData.lobby.Pitch"));
			
			List<Location> spawns = new ArrayList<Location>();
			if(config.getConfigurationSection("config.arenaData.spawns") != null){
				for(String index : config.getConfigurationSection("config.arenaData.spawns").getKeys(false)){
					Location spawn = new Location(world, config.getDouble("config.arenaData.spawns." + index + ".X"), config.getDouble("config.arenaData.spawns." + index + ".Y"),
							config.getDouble("config.arenaData.spawns." + index + ".Z"), (float)config.getDouble("config.arenaData.spawns." + index + ".Yaw"), (float)config.getDouble("config.arenaData.spawns." + index + ".Pitch"));
					spawns.add(spawn);
				}
			}
			
			List<Location> signs = new ArrayList<Location>();
			if(config.getConfigurationSection("config.arenaData.sign") != null){
				for(String index : config.getConfigurationSection("config.arenaData.sign").getKeys(false)){
					Location sign = new Location(Bukkit.getWorld(config.getString("config.arenaData.sign." + index + ".World")), config.getInt("config.arenaData.sign." + index + ".X"), config.getInt("config.arenaData.sign." + index + ".Y"),
							config.getInt("config.arenaData.sign." + index + ".Z"));
					signs.add(sign);
				}
			}
			
			List<Integer> lobbyTimer = new ArrayList<Integer>();
			for(String value : config.getStringList("config.arenaData.lobbyTimer")){
				lobbyTimer.add(Integer.parseInt(value));
			}
			
			Arena arena = new Arena(name, spawns, spectate, lobby, signs, regenerate, arenaChat, minPlayers, countdown, world, upperBound, lowerBound, lobbyTimer);
			CellWars.plugin.getLogger().log(Level.INFO, arena.getName() + ".yml loaded!");
			return arena;
		}
		CellWars.plugin.getLogger().log(Level.WARNING, file.getPath() + " failed to load!");
		CellWars.plugin.warnAdmins(file.getPath() + " has failed to load a configuration section!");
		return null;
	}
	
}
