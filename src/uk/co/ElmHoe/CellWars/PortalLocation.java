package uk.co.ElmHoe.CellWars;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class PortalLocation {
	
	private int x;
	private int y;
	private int z;
	private World world;
	
	
	public PortalLocation(int x, int y, int z, World world){
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}
	
	public PortalLocation(Location loc){
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.world = loc.getWorld();
	}
	
	public void setMaterial(Material material){
		Location loc = new Location(world,x,y,z);
		loc.getBlock().setType(material);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof PortalLocation){
			PortalLocation loc = (PortalLocation)obj;
			if(loc.getX() == this.getX()){
				if(loc.getY() == this.getY()){
					if(loc.getZ() == this.getZ()){
						if(loc.getWorld().getName().equals(this.getWorld().getName())){
							return true;
						}
					}
				}
			}
			return false;
		}else if(obj instanceof Location){
			Location loc = (Location)obj;
			if(loc.getBlockX() == this.getX()){
				if(loc.getBlockY() == this.getY()){
					if(loc.getBlockZ() == this.getZ()){
						if(loc.getWorld().getName().equals(this.getWorld().getName())){
							return true;
						}
					}
				}
			}
			return false;
		}else{
			return false;
		}
	}
	
	public int getX(){return x;}
	public int getY(){return y;}
	public int getZ(){return z;}
	public World getWorld(){return world;}
}
