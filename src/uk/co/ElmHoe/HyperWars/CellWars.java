package uk.co.ElmHoe.HyperWars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.ElmHoe.MainClass;
import uk.co.ElmHoe.Utilities.ItemUtility;
import uk.co.ElmHoe.Utilities.NumberUtility;
import uk.co.ElmHoe.Utilities.StringUtility;

public class CellWars extends JavaPlugin implements Runnable{
	
	
	@Override
	public void run() {
		for(Arena arena : arenaData){
			if(arena.isPlaying()){
				arena.timer += 2;
				if(arena.timer > maxTime){
					arena.tellPlayers(ChatColor.RED + "The maximum time limit has been reached and the arena will now stop!");
					arena.stopGame(true);
					arena.setPlaying(false);
					arena.timer = 0;
					arena.loadMap();
				}else if(arena.getPlayers().size() <= 2 && (new Random()).nextBoolean()){
					//arena.randomTnT();
				}
			}else{
				if(arena.isRegenerating()){
					boolean glitch = true;
					for(AsyncBuilderSession session : sessionManager.getSessions()){
						if(session.getArena().equals(arena)){
							glitch = false;
							break;
						}
					}
					if(glitch){
						arena.setRegenerating(false);
					}
				}
			}
			arena.updateSigns();
		}
	}
	
	public static boolean allowPortalJoin;
	public static boolean denySignJoin;
	public static List<PortalLocation> portals = new ArrayList<PortalLocation>();
	public static Map<Message, String> messages = new HashMap<Message, String>();
	public static CellWars plugin;
	public static String header = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "CellWars" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
	public Commands commands;
	public Events events;
	private List<Arena> arenaData = new ArrayList<Arena>();
	private File configFile;
	private FileConfiguration config;
	private File kitsFile;
	private FileConfiguration kitConfig;
	private File portalFile;
	private FileConfiguration portalConfig;
	private File messageFile;
	private FileConfiguration messageConfig;
	public List<ItemStack> chestItems = new ArrayList<ItemStack>();
	public List<ItemStack> lockedChestItems = new ArrayList<ItemStack>();
	public int maxItemsPerChest;
	public int maxItemsPerLockedChest;
	public int minItemsPerChest;
	public int minItemsPerLockedChest;
	public List<Integer> defaultTimer = new ArrayList<Integer>();
	public List<Kit> kits = new ArrayList<Kit>();
	public int kitUIslots;
	public int scorePerWin;
	public int scorePerKill;
	public int scorePerGame;
	public Inventory kitSelector;
	public ItemStack menuItem;
	public ItemStack playerSelector;
	public static Location signLobby;
	public static int blocksPerSecond;
	public static AsyncSessionManager sessionManager;
	public static int maxTime;
	
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage(header + "Running on " + MainClass.getVersion());
		
		events = new Events(this);
		commands = new Commands(this);
		
		getCommand("cellwars").setExecutor(commands);
		getCommand("leave").setExecutor(commands);
		
		plugin = this;
		
		load();
		
		menuItem = new ItemStack(Material.EMERALD);
		ItemMeta menuMeta = menuItem.getItemMeta();
		menuMeta.setDisplayName(StringUtility.format("&8[&cKits&8]"));
		menuItem.setItemMeta(menuMeta);
		
		playerSelector = new ItemStack(Material.COMPASS);
		ItemMeta meta = playerSelector.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "PlayerSelector" + ChatColor.DARK_GRAY + "]");
		playerSelector.setItemMeta(meta);
		
		setupTimer();
		
		sessionManager = new AsyncSessionManager();
		
		for(Arena arena : arenaData){
			arena.loadMap();
		}
		
		CellWars.allowPortalJoin = true;
		
		Bukkit.getScheduler().runTaskTimer(this,this,40,40);
		
		Bukkit.getConsoleSender().sendMessage(header + "by HctiMitcH!");
	}
	
	public void onDisable(){
		for(Arena arena : arenaData){
			arena.stopGame(true);
		}
		save();
	}
	
	private void setupTimer(){
		defaultTimer.add(60);
		defaultTimer.add(30);
		defaultTimer.add(10);
		defaultTimer.add(5);
		defaultTimer.add(4);
		defaultTimer.add(3);
		defaultTimer.add(2);
		defaultTimer.add(1);
	}
	
	public Arena getArena(UUID uuid){
		for(Arena arena : arenaData){
			if(arena.getPlayerData(uuid) != null){
				return arena;
			}
		}
		return null;
	}
	
	public Arena getArenaFromSpectator(UUID uuid){
		for(Arena arena : arenaData){
			if(arena.getSpectators().contains(uuid)){
				return arena;
			}
		}
		return null;
	}
	
	public Arena getArena(String name){
		for(Arena arena : arenaData){
			if(arena.getName().equalsIgnoreCase(name)){
				return arena;
			}
		}
		return null;
	}
	
	public boolean removeSpectate(UUID uuid){
		boolean hasSpectated = false;
		for(Arena arena : arenaData){
			if(arena.getSpectators().contains(uuid)){
				arena.removeSpectator(uuid);
				hasSpectated = true;
			}
		}
		return hasSpectated;
	}
	
	public boolean isSpectating(UUID uuid){
		for(Arena arena : arenaData){
			if(arena.getSpectators().contains(uuid)){
				return true;
			}
		}
		return false;
	}
	
	public Arena getSpectating(UUID uuid){
		for(Arena arena : arenaData){
			if(arena.getSpectators().contains(uuid)){
				return arena;
			}
		}
		return null;
	}
	
	public List<Arena> getArenas(){
		return arenaData;
	}
	
	public void warnAdmins(String string){
		Bukkit.broadcast(CellWars.header + string, "cellwars.admin");
	}
	
	public void loadData(){
		maxTime = config.getInt("config.maxArenaTime");
		maxItemsPerChest = config.getInt("config.maxItemsPerChest");
		maxItemsPerLockedChest = config.getInt("config.maxItemsPerLockedChest");
		minItemsPerChest = config.getInt("config.minItemsPerChest");
		minItemsPerLockedChest = config.getInt("config.minItemsPerLockedChest");
		kitUIslots = kitConfig.getInt("config.kitUIslots");
		scorePerWin = config.getInt("config.scorePerWin");
		scorePerKill = config.getInt("config.scorePerKill");
		scorePerGame = config.getInt("config.scorePerGame");
		blocksPerSecond = config.getInt("config.regenBlocksPerSecond");
		denySignJoin = config.getBoolean("config.denySignJoin");
		
		if(portalConfig.getConfigurationSection("config.portals") != null){
			for(String index : portalConfig.getConfigurationSection("config.portals").getKeys(false)){
				int x = portalConfig.getInt("config.portals." + index + ".X");
				int y = portalConfig.getInt("config.portals." + index + ".Y");
				int z = portalConfig.getInt("config.portals." + index + ".Z");
				World world = Bukkit.getWorld(portalConfig.getString("config.portals." + index + ".World"));
				PortalLocation loc = new PortalLocation(x,y,z,world);
				CellWars.portals.add(loc);
			}
		}
		
		signLobby = new Location(Bukkit.getWorld(config.getString("config.signLobby.World")), 
				config.getDouble("config.signLobby.X"), config.getDouble("config.signLobby.Y"),
				config.getDouble("config.signLobby.Z"), (float)config.getDouble("config.signLobby.Yaw"),
				(float)config.getDouble("config.signLobby.Pitch"));
		
		for(String msg : messageConfig.getConfigurationSection("config.messages").getKeys(false)){
			messages.put(Message.getByName(msg), StringUtility.format(messageConfig.getString("config.messages." + msg)));
		}
		
		for(String name : config.getStringList("config.arenas")){
			File file = new File(CellWars.plugin.getDataFolder(), name + ".yml");
			Arena arena = FileOutput.loadConfig(file);
			if(arena != null){
				arenaData.add(arena);
			}else{
				this.getLogger().log(Level.WARNING, CellWars.header + name + " arena has not loaded because of a bad config!");
			}
		}
		
		chestItems = ItemUtility.getItems(config.getStringList("config.chestItems"));
		
		lockedChestItems = ItemUtility.getItems(config.getStringList("config.lockedChestItems"));
		
		kitSelector = Bukkit.createInventory(null, kitUIslots, ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Kits" + ChatColor.DARK_GRAY + "]");
		
		for(String index : kitConfig.getConfigurationSection("config.kitData").getKeys(false)){
			String name = kitConfig.getString("config.kitData." + index + ".name");
			int unlock = kitConfig.getInt("config.kitData." + index + ".unlockThreshold");
			String permission = kitConfig.getString("config.kitData." + index + ".permission");
			
			ItemStack inShop = new ItemStack(Material.getMaterial(kitConfig.getString("config.kitData." + index + ".inShop.material")));
			ItemMeta inShopMeta = inShop.getItemMeta();
			inShopMeta.setLore(StringUtility.format(kitConfig.getStringList("config.kitData." + index + ".inShop.lore")));
			inShopMeta.setDisplayName(StringUtility.format(kitConfig.getString("config.kitData." + index + ".inShop.name")));
			inShop.setDurability((short)kitConfig.getInt("config.kitData." + index + ".inShop.data"));
			inShop.setItemMeta(inShopMeta);
			
			Map<Integer, ItemStack> data = new HashMap<Integer, ItemStack>();
			
			List<ItemStack> kitItems = ItemUtility.getItems(kitConfig.getStringList("config.kitData." + index + ".itemData"));
			List<String> kitSlots = kitConfig.getStringList("config.kitData." + index + ".itemDataSlots");
			
			for(ItemStack item : kitItems){
				data.put(NumberUtility.toInt(kitSlots.get(kitItems.indexOf(item))), item);
			}
			
			Kit kit = new Kit(name, data, unlock, inShop, permission);
			
			kits.add(kit);
			
			kitSelector.setItem(Integer.parseInt(index), kit.getInShop());
			
		}
	}
	
	public void load(){
		configFile = new File(getDataFolder(), "config.yml");
		kitsFile = new File(getDataFolder(), "Kits.yml");
		portalFile = new File(getDataFolder(), "portals.yml");
		messageFile = new File(getDataFolder(), "messages.yml");
		try{
			firstRun();
		} catch (Exception e){
			e.printStackTrace();
		}
		config = new YamlConfiguration();
		kitConfig = new YamlConfiguration();
		portalConfig = new YamlConfiguration();
		messageConfig = new YamlConfiguration();
	    loadYamls();
	    Message.init();
	    loadData();
	}
	
	public void save(){
		try {
			List<String> arenaNames = new ArrayList<String>();
			for(Arena arena : arenaData){
				arenaNames.add(arena.getName());
			}
			config.set("config.arenas", arenaNames);
			
			config.set("config.signLobby.X", signLobby.getX());
			config.set("config.signLobby.Y", signLobby.getY());
			config.set("config.signLobby.Z", signLobby.getZ());
			config.set("config.signLobby.Yaw", signLobby.getYaw());
			config.set("config.signLobby.Pitch", signLobby.getPitch());
			config.set("config.signLobby.World", signLobby.getWorld().getName());
			config.set("config.regenBlocksPerSecond", blocksPerSecond);
			
			portalConfig.set("config.portals", null);
			for(PortalLocation loc : CellWars.portals){
				int id = CellWars.portals.indexOf(loc);
				portalConfig.createSection("config.portals." + id);
				portalConfig.createSection("config.portals." + id + ".X");
				portalConfig.createSection("config.portals." + id + ".Y");
				portalConfig.createSection("config.portals." + id + ".Z");
				portalConfig.createSection("config.portals." + id + ".World");
				portalConfig.set("config.portals." + id + ".X", loc.getX());
				portalConfig.set("config.portals." + id + ".Y", loc.getY());
				portalConfig.set("config.portals." + id + ".Z", loc.getZ());
				portalConfig.set("config.portals." + id + ".World", loc.getWorld().getName());
			}
			
			config.save(configFile);
			portalConfig.save(portalFile);
			for(Arena arena : arenaData){
				File file = new File(CellWars.plugin.getDataFolder(), arena.getName() + ".yml");
				FileOutput.saveConfig(file, arena);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void firstRun() throws Exception {
	    if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	    if(!kitsFile.exists()){
	        kitsFile.getParentFile().mkdirs();
	        copy(getResource("Kits.yml"), kitsFile);
	    }
	    if(!portalFile.exists()){
	        portalFile.getParentFile().mkdirs();
	        copy(getResource("portals.yml"), portalFile);
	    }
	    if(!messageFile.exists()){
	        messageFile.getParentFile().mkdirs();
	        copy(getResource("messages.yml"), messageFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void loadYamls() {
	    try {
	        config.load(configFile);
	        kitConfig.load(kitsFile);
	        portalConfig.load(portalFile);
	        messageConfig.load(messageFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}