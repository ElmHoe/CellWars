package uk.co.ElmHoe.CellWars;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import uk.co.ElmHoe.Utilities.PlayerUtility;

public class Kit {
	
	private String name;
	private Map<Integer, ItemStack> items;
	private int scoreThreshold;
	private ItemStack inShop;
	private String permission;
	
	public Kit(String name, Map<Integer, ItemStack> items, int threshold, ItemStack inShop, String permission){
		this.name = name;
		this.items = items;
		this.scoreThreshold = threshold;
		this.inShop = inShop;
		this.permission = permission;
	}
	
	public void giveKit(UUID uuid){
		if(PlayerUtility.getPlayer(uuid) != null){
			Player player = PlayerUtility.getPlayer(uuid);
			for(int key : items.keySet()){
				player.getInventory().setItem(key, items.get(key));
			}
			player.updateInventory();
		}
	}
	
	public ItemStack getItemAtSlot(int slot){if(items.containsKey(slot)){return items.get(slot);}return null;}
	public int getThreshold(){return this.scoreThreshold;}
	public Map<Integer, ItemStack> getItems(){return items;}
	public String getName(){return name;}
	public ItemStack getInShop(){return inShop.clone();}
	public String getPermission(){return permission;}
}
