package me.nd.factionsutils.holoboard.listholo;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.holoboard.Holoboard;

import java.util.List;

public class CaldeiraoHolo extends Holoboard {
  
	FileConfiguration m = Main.get().getConfig(); 
	
  public CaldeiraoHolo(Location location, String id, String creator) {
    super(location, id, creator);
  }
  
  @Override
  public String getType() {
    return "caldeirao";
  }
  
  @Override
  public List<String> getHologramLines() {
    return m.getStringList("Caldeirao.holograma");
  }
}
