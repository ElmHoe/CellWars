package uk.co.ElmHoe.CellWars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import uk.co.ElmHoe.MainClass;

public class AsyncSessionManager implements Runnable{
	
	private boolean running;
	private int id;
	private List<AsyncBuilderSession> sessions;
	
	public AsyncSessionManager(){
		sessions = new ArrayList<AsyncBuilderSession>();
		running = false;
		id = -1;
	}
	
	public void addSession(AsyncBuilderSession session){
		sessions.add(session);
		if(!running){
			start();
		}
	}
	
	public List<AsyncBuilderSession> getSessions(){
		return sessions;
	}
	
	public void start(){
		if(!running){
			id = Bukkit.getScheduler().scheduleSyncRepeatingTask(MainClass.plugin, this, 1, 1);
			running = true;
		}
	}
	
	public void stop(){
		if(running){
			Bukkit.getScheduler().cancelTask(id);
			running = false;
		}
	}
	
	public void run(){
		if(sessions.size() > 0){
			for(int i = 0; i < sessions.size(); i++){
				if(sessions.get(i).isDone()){
					sessions.get(i).done();
					sessions.remove(i);
				}else{
					sessions.get(i).tick();
					break;
				}
			}
		}else{
			this.stop();
		}
	}
	
}
