package uk.co.ElmHoe.CellWars;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import uk.co.ElmHoe.Utilities.NumberUtility;

public class BlockData {
	
	private Material material;
	private byte data;
	private int x;
	private int y;
	private int z;
	
	public BlockData(int x, int y, int z, Material material, byte data){
		this.material = material;
		this.data = data;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@SuppressWarnings("deprecation")
	public void setBlock(World world){
		Location loc = new Location(world,x,y,z);
		loc.getBlock().setType(material);
		loc.getBlock().setData(data);
		if(material.equals(Material.CHEST)){
			Chest chest = (Chest)loc.getBlock().getState();
			Inventory inv = chest.getInventory();
			inv.clear();
			int itemCount = NumberUtility.getRandom(CellWars.plugin.maxItemsPerChest - CellWars.plugin.minItemsPerChest + 1) + CellWars.plugin.minItemsPerChest;
			for(int i = 0; i < itemCount; i++){
				inv.setItem(NumberUtility.getRandom(inv.getSize()), CellWars.plugin.chestItems.get(NumberUtility.getRandom(CellWars.plugin.chestItems.size())));
			}
			if(!inv.contains(Material.WOOD) || !inv.contains(Material.LOG)){
				inv.setItem(0, new ItemStack(Material.WOOD, 32));
			}
		}else if(material.equals(Material.TRAPPED_CHEST)){
			Chest chest = (Chest)loc.getBlock().getState();
			Inventory inv = chest.getInventory();
			inv.clear();
			int itemCount = NumberUtility.getRandom(CellWars.plugin.maxItemsPerLockedChest - CellWars.plugin.minItemsPerLockedChest + 1) + CellWars.plugin.minItemsPerLockedChest;
			for(int i = 0; i < itemCount; i++){
				inv.setItem(NumberUtility.getRandom(inv.getSize()), CellWars.plugin.lockedChestItems.get(NumberUtility.getRandom(CellWars.plugin.lockedChestItems.size())));
			}
			if(!inv.contains(Material.WOOD) || !inv.contains(Material.LOG)){
				inv.setItem(0, new ItemStack(Material.WOOD, 32));
			}
		}else if(material.equals(Material.FURNACE)){
			loc.getBlock().setType(Material.AIR);
			loc.getBlock().setType(material);
			loc.getBlock().setData(data);
		}
	}
	
	public Material getMaterial(){return material;}
	public byte getData(){return data;}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getZ(){return z;}
}
