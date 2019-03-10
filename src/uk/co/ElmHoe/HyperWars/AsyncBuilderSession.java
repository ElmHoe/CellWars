package uk.co.ElmHoe.HyperWars;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class AsyncBuilderSession {
	
	private List<BlockData> blocks;
	private int blocksPerTick = 500;
	private World world;
	private Arena arena;
	private int count = 0;
	
	public AsyncBuilderSession(List<BlockData> blocks, int blocksPerTick, Arena arena){
		this.blocks = blocks;
		this.blocksPerTick = blocksPerTick;
		this.world = arena.getWorld();
		this.arena = arena;
		arena.setRegenerating(true);
	}
	
	public void tick(){
		Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
		    public void run() {
		    	int ittCount = 0;
				for(int i = count; i < blocks.size(); i++){
					blocks.get(i).setBlock(world);
					ittCount++;
					if(ittCount >= blocksPerTick){
						break;
					}
				}
				count += blocksPerTick;
				if(!arena.isRegenerating()){
					arena.setRegenerating(true);
				}
		    }
		}, 1);
	}
	
	public int getCount(){return count;}
	public int getSize(){return blocks.size();}
	public boolean isDone(){
		if(count >= blocks.size() - 1){
			arena.setRegenerating(false);
			return true;
		}
		return false;
	}
	
	public Arena getArena(){return arena;}
	
	public void done(){
		Bukkit.getScheduler().runTaskLater(CellWars.plugin, new Runnable() {
		    public void run() {
				for(int i = 0; i < world.getEntities().size(); i++){
					Entity e = world.getEntities().get(i);
					Location loc = e.getLocation();
					if((loc.getBlockX() >= arena.getLower().getBlockX() && loc.getBlockX() <= arena.getUpper().getBlockX())){
						if((loc.getBlockZ() >= arena.getLower().getBlockZ() && loc.getBlockZ() <= arena.getUpper().getBlockZ())){
							e.remove();
						}
					}
				}
		    }
		}, 1);
		arena.setRegenerating(false);
	}
}
