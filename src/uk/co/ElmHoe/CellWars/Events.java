package uk.co.ElmHoe.CellWars;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import uk.co.ElmHoe.Utilities.PlayerUtility;
import uk.co.ElmHoe.Utilities.StringUtility;

public class Events implements Listener{
	
	private CellWars plugin;
	
	public Events(CellWars plugin){
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this,  plugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
    	if(plugin.getArena(player.getUniqueId()) != null){
    		final Arena arena = plugin.getArena(player.getUniqueId());
    		arena.getPlayerData(player.getUniqueId()).setDead(true);
    		arena.getPlayerData(player.getUniqueId()).addDeath();
    		Player killer = PlayerUtility.getKiller(event.getDeathMessage());
    		
    		for(Player p : arena.getPlayerObjects()){
    			p.playSound(p.getLocation(), Sound.ENTITY_IRONGOLEM_DEATH, 1, 1);
    		}
    		
    		for(ItemStack itemStack : event.getDrops()){
    			if(itemStack != null){
    				player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
    			}
    		}
    		event.getDrops().clear();
    		
    		if(killer != null && killer.getUniqueId().equals(player.getUniqueId())){
    			arena.tellPlayers(CellWars.messages.get(Message.PLAYER_DEATH_OTHER).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
    			arena.tellSpectators(CellWars.messages.get(Message.PLAYER_DEATH_OTHER).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
    			event.setDeathMessage("");
        		player.getPlayer().resetMaxHealth();
        		player.getPlayer().setHealth(20.0);
        		player.getPlayer().setFoodLevel(20);
        		player.teleport(CellWars.signLobby);
        		arena.removePlayer(player.getUniqueId(), true, false);
        		Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
        		    public void run() {
        		    	arena.addSpectator(player.getUniqueId());
        		    }
        		}, 1);
        		return;
    		}
    		
    		if(killer != null){
    			if(plugin.getArena(killer.getUniqueId()) != null){
    				plugin.getArena(killer.getUniqueId()).getPlayerData(player.getKiller().getUniqueId()).addScore(plugin.scorePerKill);
    				plugin.getArena(killer.getUniqueId()).getPlayerData(player.getKiller().getUniqueId()).addKill();
    			}
    		}
    		
    		if(arena.getAlivePlayers() <= 1){
    			if(killer != null){
    				arena.tellPlayers(CellWars.messages.get(Message.PLAYER_DEATH).replace("{KILLER}", killer.getName()).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
        			arena.tellSpectators(CellWars.messages.get(Message.PLAYER_DEATH).replace("{KILLER}", killer.getName()).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
    			}else{
    				arena.tellPlayers(CellWars.messages.get(Message.PLAYER_DEATH_OTHER).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
        			arena.tellSpectators(CellWars.messages.get(Message.PLAYER_DEATH_OTHER).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
    			}
    		}else{
    			if(killer != null){
    				arena.tellPlayers(CellWars.messages.get(Message.PLAYER_DEATH).replace("{KILLER}", killer.getName()).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
        			arena.tellSpectators(CellWars.messages.get(Message.PLAYER_DEATH).replace("{KILLER}", killer.getName()).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
    			}else{
    				arena.tellPlayers(CellWars.messages.get(Message.PLAYER_DEATH_OTHER).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
        			arena.tellSpectators(CellWars.messages.get(Message.PLAYER_DEATH_OTHER).replace("{PLAYER}", player.getName()).replace("{PLAYERS}", "" + arena.getAlivePlayers()));
    			}
    		}
    		
    		event.setDeathMessage("");
    		player.getPlayer().resetMaxHealth();
    		player.getPlayer().setHealth(20.0);
    		player.getPlayer().setFoodLevel(20);
    		player.teleport(CellWars.signLobby);
    		arena.removePlayer(player.getUniqueId(), false, false);
    		Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
    		    public void run() {
    		    	arena.addSpectator(player.getUniqueId());
    		    }
    		}, 1);
    	}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(plugin.getSpectating(event.getPlayer().getUniqueId()) != null){
			event.getPlayer().teleport(plugin.getSpectating(event.getPlayer().getUniqueId()).getSpectate());
		}else{
			event.setRespawnLocation(CellWars.signLobby);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInventoryIntereact(InventoryClickEvent event) {
		Arena arena = plugin.getArena(event.getWhoClicked().getUniqueId());
		if(event.getInventory().getName().equals(plugin.kitSelector.getName())){
			if(arena != null && !arena.isPlaying()){
				event.setCancelled(true);
					if(event.getCurrentItem() != null){
						ItemStack clicked = event.getCurrentItem();
						for(Kit kit : plugin.kits){
							if(kit.getInShop().equals(clicked)){
								if(event.getWhoClicked().hasPermission(kit.getPermission())){
									PlayerData playerData = arena.getPlayerData(event.getWhoClicked().getUniqueId());
									if(playerData.getScore() >= kit.getThreshold()){
										playerData.setKit(kit);
										event.getWhoClicked().closeInventory();
										((Player)event.getWhoClicked()).sendMessage(CellWars.header + "You have selected the " + kit.getName() + " kit!");
										PlayerUtility.updateInventory(event.getWhoClicked().getUniqueId());
										return;
									}
								}
							((Player)event.getWhoClicked()).sendMessage(CellWars.header + "You do not have permission, or do not have high enough score to use this kit!");
							PlayerUtility.updateInventory(event.getWhoClicked().getUniqueId());
							break;
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInventoryIntereactMenu(InventoryClickEvent event) {
		Arena arena = plugin.getSpectating(event.getWhoClicked().getUniqueId());
		if(arena != null && arena.isPlaying()){
			if(event.getInventory().getName().equals(arena.playerMenu.getName())){
				event.setCancelled(true);
				if(event.getCurrentItem() != null){
					ItemStack clicked = event.getCurrentItem();
					if(clicked.hasItemMeta()){
						Player toTeleport = PlayerUtility.getPlayer(clicked.getItemMeta().getDisplayName());
						if(toTeleport != null){
							event.getWhoClicked().teleport(toTeleport);
						}else{
							event.getWhoClicked().teleport(arena.getSpectate());
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteractCheckForKitMenu(PlayerInteractEvent event) {
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
	    	if(event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().equals(plugin.menuItem)){
		    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
		    		event.getPlayer().openInventory(plugin.kitSelector);
		    		event.setCancelled(true);
		    		event.getPlayer().sendMessage(CellWars.header + "You have opened the kit selection menu!");
		    	}
	    	}else if(event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().equals(plugin.playerSelector)){
	    		Arena arena = plugin.getSpectating(event.getPlayer().getUniqueId());
	    		if(arena != null){
	    			event.getPlayer().openInventory(arena.playerMenu);
	    			event.getPlayer().sendMessage(CellWars.header + "You have opened the player selection menu!");
	    		}
	    	}
		}
	}	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
	    if(!event.isCancelled()){
	    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
	    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
	    		if(arena.isLocked()){
	    			event.setCancelled(true);
	    		}
	    	}
	    	if(plugin.isSpectating(event.getPlayer().getUniqueId())){
	    		event.setCancelled(true);
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(BlockBreakEvent event) {
	    if(!event.isCancelled()){
	    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
	    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
	    		if(arena.isLocked()){
	    			event.setCancelled(true);
	    		}
	    	}
	    	if(plugin.isSpectating(event.getPlayer().getUniqueId())){
	    		event.setCancelled(true);
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
	    if(!event.isCancelled()){
	    	if(event.getDamager() instanceof Player){
	    		Player p = (Player)event.getDamager();
	    		if(plugin.isSpectating(p.getUniqueId())){
	    			event.setCancelled(true);
	    		}
	    	}
	    	if(event.getEntity() instanceof Player){
	    		Player p = (Player)event.getEntity();
	    		if(plugin.isSpectating(p.getUniqueId())){
	    			event.setCancelled(true);
	    		}
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamage(EntityDamageEvent event) {
	    if(!event.isCancelled()){
	    	if(event.getEntity() instanceof Player){
	    		Player p = (Player)event.getEntity();
	    		if(plugin.isSpectating(p.getUniqueId())){
	    			event.setCancelled(true);
	    		}
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamage(FoodLevelChangeEvent event) {
	    if(!event.isCancelled()){
	    	if(event.getEntity() instanceof Player){
	    		Player p = (Player)event.getEntity();
	    		if(plugin.isSpectating(p.getUniqueId())){
	    			event.setCancelled(true);
	    		}
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleItemDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
		if(!event.isCancelled()){
	    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
	    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
	    		if(arena.isLocked()){
	    			event.setCancelled(true);
	    			PlayerUtility.updateInventory(event.getPlayer().getUniqueId());
	    		}
	    	}
	    	if(plugin.isSpectating(event.getPlayer().getUniqueId())){
	    		event.setCancelled(true);
	    		PlayerUtility.updateInventory(event.getPlayer().getUniqueId());
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleItemPickup(org.bukkit.event.player.PlayerPickupItemEvent event) {
		if(!event.isCancelled()){
	    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
	    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
	    		if(arena.isLocked()){
	    			event.setCancelled(true);
	    			PlayerUtility.updateInventory(event.getPlayer().getUniqueId());
	    		}
	    	}
	    	if(plugin.isSpectating(event.getPlayer().getUniqueId())){
	    		event.setCancelled(true);
	    		PlayerUtility.updateInventory(event.getPlayer().getUniqueId());
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(BlockPlaceEvent event) {
	    if(!event.isCancelled()){
	    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
	    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
	    		if(arena.isLocked()){
	    			event.setCancelled(true);
	    		}
	    	}
	    	if(plugin.isSpectating(event.getPlayer().getUniqueId())){
	    		event.setCancelled(true);
	    		PlayerUtility.updateInventory(event.getPlayer().getUniqueId());
	    	}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().teleport(CellWars.signLobby);
    	for(Player p : PlayerUtility.getOnlinePlayers()){
			if(!p.getUniqueId().equals(event.getPlayer().getUniqueId())){
				p.showPlayer(event.getPlayer());
				event.getPlayer().showPlayer(p);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
    	if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
    		arena.removePlayer(event.getPlayer().getUniqueId(), true, true);
    		PlayerUtility.clearInventory(event.getPlayer().getUniqueId());
    	}
    	if(plugin.getArenaFromSpectator(event.getPlayer().getUniqueId()) != null){
    		Arena arena = plugin.getArenaFromSpectator(event.getPlayer().getUniqueId());
    		arena.removeSpectator(event.getPlayer().getUniqueId());
    	}
    	for(Player p : PlayerUtility.getOnlinePlayers()){
			if(!p.getUniqueId().equals(event.getPlayer().getUniqueId())){
				p.showPlayer(event.getPlayer());
				event.getPlayer().showPlayer(p);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent event) {
		if(plugin.getArena(event.getPlayer().getUniqueId()) != null){
    		Arena arena = plugin.getArena(event.getPlayer().getUniqueId());
    		arena.removePlayer(event.getPlayer().getUniqueId(), true, true);
    		PlayerUtility.clearInventory(event.getPlayer().getUniqueId());
    	}else if(plugin.getArenaFromSpectator(event.getPlayer().getUniqueId()) != null){
    		Arena arena = plugin.getArenaFromSpectator(event.getPlayer().getUniqueId());
    		arena.removeSpectator(event.getPlayer().getUniqueId());
    	}
		for(Player p : PlayerUtility.getOnlinePlayers()){
			if(!p.getUniqueId().equals(event.getPlayer().getUniqueId())){
				p.showPlayer(event.getPlayer());
				event.getPlayer().showPlayer(p);
			}
		}
	}
	
	@EventHandler
	public void onPlace(org.bukkit.event.block.SignChangeEvent sign){
		if(sign.getLine(0).equalsIgnoreCase("[cellwars]")){
			org.bukkit.entity.Player player = sign.getPlayer();
			if(player.hasPermission("cellwars.admin")){
				sign.setLine(0, StringUtility.format(ChatColor.DARK_AQUA + "[Join]"));
				sign.setLine(1, StringUtility.format("CellWars"));
				if(plugin.getArena(sign.getLine(2)) != null){
					Arena arena = plugin.getArena(sign.getLine(2));
					sign.setLine(2, arena.getName());
					sign.setLine(3, arena.getPlayers().size() + "/" + arena.getSpawns().size());
					arena.addSign(sign.getBlock().getLocation());
				}
			}else{
				player.sendMessage(CellWars.header + "You do not have permissions to create arena signs!");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onRightClick(PlayerInteractEvent event){
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(event.getClickedBlock().getState() instanceof Sign){
				Sign sign = (Sign)event.getClickedBlock().getState();
				if(sign.getLine(1).equalsIgnoreCase("cellwars")){
					Player player = event.getPlayer();
					boolean already = false;
					Arena arena = plugin.getArena(sign.getLine(2));
					if(arena == null){
						return;
					}
					arena.addSign(sign.getLocation());
					arena.updateSigns();
					if(plugin.getArena(player.getUniqueId()) != null){
						already = true;
					}else if(plugin.isSpectating(player.getUniqueId())){
						already = true;
					}
					if(!already){
						if(arena.isClosed()){
							player.sendMessage(CellWars.header + "The arena is closed, please check back later!");
						}else if(arena.isRegenerating()){
							player.sendMessage(CellWars.header + "The arena is regenerating, please wait!");
						}else if(!arena.isPlaying()){
							if(!CellWars.denySignJoin){
								if(arena.getPlayers().size() < arena.getSpawns().size()){
									if(player.hasPermission("cellwars.join")){
										arena.addPlayer(player.getUniqueId());
									}else{
										player.sendMessage(CellWars.header + "You do not have permission!");
									}
								}else{
									player.sendMessage(CellWars.header + "The game is starting please wait.");
								}
							}else{
								player.sendMessage(CellWars.header + "Sign joining is disabled! Please use the portal.");
							}
						}else{
							if(player.hasPermission("cellwars.spectate") && arena.getSpectate() != null){
								arena.addSpectator(player.getUniqueId());
							}else{
								player.sendMessage(CellWars.header + "The arena is already in progress!");
							}
						}
					}else{
						player.sendMessage(CellWars.header + "You have already joined an arena!");
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onMove(PlayerMoveEvent event){
		if(CellWars.allowPortalJoin){
			if(CellWars.plugin.getArena(event.getPlayer().getUniqueId()) == null && !CellWars.plugin.isSpectating(event.getPlayer().getUniqueId())){
				if(event.getPlayer().getWorld().getName().equals(CellWars.signLobby.getWorld().getName())){
					if(event.getPlayer().hasPermission("cellwars.join")){
						for(PortalLocation location : CellWars.portals){
							if(location.equals(event.getPlayer().getLocation())){
								RandomSelector.randomAdd(event.getPlayer().getUniqueId(), new PlayerData(event.getPlayer().getUniqueId(), null).getScore());
								break;
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		if(!event.getPlayer().hasPermission("cellwars.chat")){
			if(plugin.getArena(player.getUniqueId()) != null){
				Arena arena = plugin.getArena(player.getUniqueId());
				if(arena.isArenaChat()){
					event.getRecipients().clear();
					for(Player playerOb : arena.getPlayerObjects()){
						event.getRecipients().add(playerOb);
					}
					for(Player p : PlayerUtility.getOnlinePlayers()){
						if(p.hasPermission("cellwars.chat")){
							event.getRecipients().add(p);
						}
					}
					for(UUID uuid : arena.getSpectators()){
						if(PlayerUtility.isOnline(uuid)){
							event.getRecipients().add(PlayerUtility.getPlayer(uuid));
						}else{
							plugin.removeSpectate(uuid);
						}
					}
				}
			}else if(plugin.isSpectating(event.getPlayer().getUniqueId())){
				Arena arena = plugin.getSpectating(event.getPlayer().getUniqueId());
				if(arena.isArenaChat()){
					event.getRecipients().clear();
					for(Player p : arena.getPlayerObjects()){
						event.getRecipients().add(p);
					}
					for(UUID uuid : arena.getSpectators()){
						if(PlayerUtility.isOnline(uuid)){
							event.getRecipients().add(PlayerUtility.getPlayer(uuid));
						}else{
							plugin.removeSpectate(uuid);
						}
					}
				}
			}else{
				event.getRecipients().clear();
				for(Player p : PlayerUtility.getOnlinePlayers()){
					if((plugin.getArena(p.getUniqueId()) == null && !plugin.isSpectating(p.getUniqueId())) || p.hasPermission("cellwars.chat")){
						event.getRecipients().add(p);
					}
				}
			}
		}
	}
}
