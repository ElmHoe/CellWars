package uk.co.ElmHoe.CellWars;

import java.util.HashMap;
import java.util.Map;

public enum Message {
	ARENA_JOIN("arenaJoin"),  ARENA_LEAVE("arenaLeave"),  PLAYER_DEATH("playerDeath"),  END_OF_GAME("endOfGame"),  START_OF_GAME("startOfGame"),  COUNTDOWN("countdown"), PLAYER_DEATH_OTHER("playerDeathOther");
	  
	  private String configName;
	  private static Map<String, Message> BY_NAME = new HashMap<String, Message>();
	  
	  private Message(String string)
	  {
	    this.configName = string;
	  }
	  
	  public String configName()
	  {
	    return this.configName;
	  }
	  
	  public static Message getByName(String name)
	  {
	    if (BY_NAME.containsKey(name)) {
	      return (Message)BY_NAME.get(name);
	    }
	    return null;
	  }
	  
	  public static void init()
	  {
	    for (Message msg : values()) {
	      BY_NAME.put(msg.configName(), msg);
	    }
	  }
}
