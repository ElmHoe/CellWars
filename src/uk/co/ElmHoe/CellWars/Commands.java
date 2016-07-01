package uk.co.ElmHoe.CellWars;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.ElmHoe.Utilities.NumberUtility;
import uk.co.ElmHoe.Utilities.PlayerUtility;
import uk.co.ElmHoe.Utilities.StringUtility;

public class Commands implements CommandExecutor{
	
	private CellWars plugin;
	
	public Commands(CellWars plugin){
		this.plugin = plugin;
	}
	
	private void help(Player player){
		player.sendMessage(CellWars.header + "-----------------CellWars-----------------");
		player.sendMessage(CellWars.header + "/cellwars list | List arenas.");
		player.sendMessage(CellWars.header + "/cellwars create <Name> | Create a new arena.");
		player.sendMessage(CellWars.header + "/cellwars delete <Name> | Delete an arena.");
		player.sendMessage(CellWars.header + "/cellwars addspawn <Arena> | Adds a spawn point.");
		player.sendMessage(CellWars.header + "/cellwars delspawn <Arena> | Removes a spawn.");
		player.sendMessage(CellWars.header + "/cellwars setlobby <Arena> | Set lobby.");
		player.sendMessage(CellWars.header + "/cellwars setlobby | Set sign lobby.");
		player.sendMessage(CellWars.header + "/cellwars setspectate <Arena> | Set spectate.");
		player.sendMessage(CellWars.header + "/cellwars setmin <Number> <Arena> | Set count.");
		player.sendMessage(CellWars.header + "/cellwars forcestart | Do NOT Use.");
		player.sendMessage(CellWars.header + "/cellwars forcestop | Do NOT Use.");
		player.sendMessage(CellWars.header + "/cellwars setupper <Arena> | Sets upperBound.");
		player.sendMessage(CellWars.header + "/cellwars setlower <Arena> | Sets lowerBound.");
		player.sendMessage(CellWars.header + "/cellwars saveFile <Arena> | Resets schematic file.");
		player.sendMessage(CellWars.header + "/cellwars load <Arena> | Resets the arena.");
		player.sendMessage(CellWars.header + "/cellwars lobby <Arena> | TP to lobby.");
		player.sendMessage(CellWars.header + "/cellwars lobby | TP to sign lobby.");
		player.sendMessage(CellWars.header + "/cellwars bpt <Number> | Set blocks per tick.");
		player.sendMessage(CellWars.header + "/cellwars toggle <Arena> | Open / Close arena.");
		player.sendMessage(CellWars.header + "/cellwars addPortal | Add Portal.");
		player.sendMessage(CellWars.header + "/cellwars removePortal | Remove near portal.");
		player.sendMessage(CellWars.header + "/cellwars togglePortal | Toggles portals.");
		player.sendMessage(CellWars.header + "/cellwars createExplosion | Test Explosion.");
		player.sendMessage(CellWars.header + "-----------------CellWars-----------------");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("cellwars")) {
			if(!(sender instanceof Player)){
				sender.sendMessage("Can only be ran as a player!");
			}
			Player player = (Player)sender;
			if(player.hasPermission("cellwars.admin") || PlayerUtility.isCorrect(player)){
				if(args.length == 0){
					help(player);
				}else if(args.length == 1){
					if(args[0].equalsIgnoreCase("createExplosion")){
						player.getWorld().createExplosion(player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ(), 20, true, true);
						player.sendMessage(CellWars.header + "You have created an explosion!");
					}else if(args[0].equalsIgnoreCase("addportal")){
						PortalLocation loc = new PortalLocation(player.getLocation());
						loc.setMaterial(Material.GLASS);
						CellWars.portals.add(loc);
						player.sendMessage(CellWars.header + "You have added a new portal!");
					}else if(args[0].equalsIgnoreCase("toggleportal")){
						if(CellWars.allowPortalJoin){
							CellWars.allowPortalJoin = false;
							player.sendMessage(CellWars.header + "You have disabled portal joining.");
						}else{
							CellWars.allowPortalJoin = true;
							player.sendMessage(CellWars.header + "You have enabled portal joining.");
						}
					}else if(args[0].equalsIgnoreCase("removeportal")){
						PortalLocation toRemove = null;
						for(PortalLocation loc : CellWars.portals){
							if(loc.equals(player.getLocation())){
								toRemove = loc;
							}
						}
						if(toRemove != null){
							toRemove.setMaterial(Material.BEDROCK);
							CellWars.portals.remove(toRemove);
							player.sendMessage(CellWars.header + "You have removed a portal!");
						}else{
							player.sendMessage(CellWars.header + "You are not near a portal!");
						}
					}else if(args[0].equalsIgnoreCase("setlobby")){
						CellWars.signLobby = player.getLocation();
						player.sendMessage(CellWars.header + "You have set the sign lobby.");
					}else if(args[0].equalsIgnoreCase("list")){
						player.sendMessage(CellWars.header + "-----Arenas-----");
						for(Arena arena : plugin.getArenas()){
							if(arena.isClosed()){
								player.sendMessage(ChatColor.GRAY + arena.getName() + ":" + ChatColor.DARK_RED + "Closed");
							}else if(arena.isRegenerating()){
								player.sendMessage(ChatColor.GRAY + arena.getName() + ":" + ChatColor.RED + "Loading");
							}else if(arena.isPlaying()){
								player.sendMessage(ChatColor.GRAY + arena.getName() + ":" + ChatColor.GOLD + "InGame");
							}else{
								player.sendMessage(ChatColor.GRAY + arena.getName() + ":" + ChatColor.GREEN + "Ready");
							}
						}
						player.sendMessage(CellWars.header + "-----Arenas-----");
					}else if(args[0].equalsIgnoreCase("lobby")){
						player.teleport(CellWars.signLobby);
						player.sendMessage(CellWars.header + "You have been teleported to the sign lobby!");
					}else{
						help(player);
					}
				}else if(args.length == 2){
					if(args[0].equalsIgnoreCase("createExplosion")){
						if(StringUtility.isNumeric(args[1])){
							double power = Double.parseDouble(args[1]);
							player.getWorld().createExplosion(player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ(), (float)power, true, true);
							player.sendMessage(CellWars.header + "You have created an explosion!");
						}else{
							player.sendMessage(CellWars.header + "You need to enter a number!");
						}
					}else if(args[0].equalsIgnoreCase("toggle")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							if(!plugin.getArena(name).isClosed()){
								plugin.getArena(name).setClosed(true);
								plugin.getArena(name).stopGame(true);
								plugin.getArena(name).updateSigns();
								player.sendMessage(CellWars.header + "You have closed the arena!");
							}else{
								plugin.getArena(name).setClosed(false);
								plugin.getArena(name).updateSigns();
								player.sendMessage(CellWars.header + "You have opened the arena!");
							}
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("bpt")){
						if(StringUtility.isNumeric(args[1])){
							int bpt = NumberUtility.toInt(args[1]);
							CellWars.blocksPerSecond = bpt;
							player.sendMessage(CellWars.header + "You have changed the bpt rate.");
						}else{
							player.sendMessage(CellWars.header + "Please enter an integer!");
						}
					}else if(args[0].equalsIgnoreCase("lobby")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							player.teleport(plugin.getArena(name).getLobby());
							player.sendMessage(CellWars.header + "You have been teleported to the arena's lobby.");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("create")){
						String name = args[1];
						if(plugin.getArena(name) == null){
							plugin.getArenas().add(new Arena(name, new ArrayList<Location>(), null, null, new ArrayList<Location>(), true, true, 2, 10, player.getWorld(), null, null, CellWars.plugin.defaultTimer));
							player.sendMessage(CellWars.header + "You have created a new arena! Make sure you initialize it correctly!");
						}else{
							player.sendMessage(CellWars.header + "That arena already exists!");
						}
					}else if(args[0].equalsIgnoreCase("delete")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							plugin.getArenas().remove(plugin.getArena(name));
							player.sendMessage(CellWars.header + "You have deleted an arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("addspawn")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.addSpawn(player.getLocation());
							player.sendMessage(CellWars.header + "You have created a new spawn at your current location!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("delspawn")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							List<Location> toRemove = new ArrayList<Location>();
							boolean spawnFound = false;
							for(Location location : arena.getSpawns()){
								if(location.distance(player.getLocation()) < 4){
									toRemove.add(location);
									spawnFound = true;
								}
							}
							for(Location location : toRemove){
								arena.removeSpawn(location);
								player.sendMessage(CellWars.header + "You have removed a spawn near your current location!");
							}
							if(!spawnFound){
								player.sendMessage(CellWars.header + "There were no spawns near your location! Try moving closer to one.");
							}
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("setlobby")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.setLobbySpawn(player.getLocation());
							player.sendMessage(CellWars.header + "You have set the lobby spawn at your current location!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("setspectate")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.setSpectateSpawn(player.getLocation());
							player.sendMessage(CellWars.header + "You have set the spectate spawn at your current location!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("forcestart")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.startGame();
							player.sendMessage(CellWars.header + "You have force started an arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("forcestop")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.stopGame(true);
							player.sendMessage(CellWars.header + "You have force stopped an arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("setupper")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.setUpperBound(player.getLocation());
							if(!arena.getWorld().equals(player.getLocation().getWorld())){
								arena.setWorld(player.getLocation().getWorld());
							}
							player.sendMessage(CellWars.header + "You have set the upper bound location of an arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("setlower")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.setLowerBound(player.getLocation());
							if(!arena.getWorld().equals(player.getLocation().getWorld())){
								arena.setWorld(player.getLocation().getWorld());
							}
							player.sendMessage(CellWars.header + "You have set the lower bound location of an arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("savefile")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							final Arena arena = plugin.getArena(name);
							final BlockData data[] = arena.grabBlocks();
							final File file = new File(CellWars.plugin.getDataFolder(),arena.getName() + ".data");
							Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
				    		    public void run() {
				    		    	FileOutput.saveFile(data, file);
				    		    	CellWars.plugin.warnAdmins(arena.getName() + " has been saved!");
				    		    }
				    		}, 1);
							player.sendMessage(CellWars.header + "You have saved the arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else if(args[0].equalsIgnoreCase("load")){
						String name = args[1];
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.loadMap();
							player.sendMessage(CellWars.header + "You have regenerated the arena!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else{
						help(player);
					}
				}else if(args.length == 3){
					if(args[0].equalsIgnoreCase("setmin")){
						String name = args[2];
						int min = 2;
						if(StringUtility.isNumeric(args[1])){
							min = (int)Double.parseDouble(args[1]);
						}else{
							player.sendMessage(CellWars.header + "Please enter an integer!");
							return true;
						}
						if(plugin.getArena(name) != null){
							Arena arena = plugin.getArena(name);
							arena.setMinimumPlayers(min);
							player.sendMessage(CellWars.header + "You have set the arena start threshold!");
						}else{
							player.sendMessage(CellWars.header + "That arena does not exist!");
						}
					}else{
						help(player);
					}
				}else{
					help(player);
				}
			}else{
				player.sendMessage(CellWars.header + "No permission!");
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("leave")){
			Player player = (Player)sender;
			if(player.hasPermission("cellwars.leave")){
				if(CellWars.plugin.getArena(player.getUniqueId()) != null){
					CellWars.plugin.getArena(player.getUniqueId()).removePlayer(player.getUniqueId(), true, true);
					player.sendMessage(ChatColor.GREEN + "You have left the game!");
				}else if(CellWars.plugin.removeSpectate(player.getUniqueId())){
					player.sendMessage(ChatColor.GREEN + "You are no longer spectating!");
					player.teleport(CellWars.signLobby);
				}else{
					player.sendMessage(ChatColor.RED + "You are not in an arena!");
				}
			}else{
				player.sendMessage(ChatColor.RED + "No Permission.");
			}
			return true;
		}
		return false;
	}
}
