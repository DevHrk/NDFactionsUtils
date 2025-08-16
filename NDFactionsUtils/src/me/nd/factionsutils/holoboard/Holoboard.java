package me.nd.factionsutils.holoboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.holoboard.listholo.*;
import me.nd.factionsutils.hologram.BukkitUtils;
import me.nd.factionsutils.hologram.Hologram;
import me.nd.factionsutils.hologram.HologramLibrary;
import me.nd.factionsutils.plugin.SConfig;

import java.util.*;

public abstract class Holoboard {

  private static final SConfig CONFIG = Main.get().getConfig("DataBase","hologramas");
  private static final List<Holoboard> LEADERBOARDS = new ArrayList<>();

  protected String id;
  protected Location location;
  protected Hologram hologram;
  protected boolean monthly;
  protected String creator;

  public Holoboard(Location location, String id, String creator) {
    this.location = location;
    this.id = id;
    this.creator = creator;

  }
  
  public static void setupLeaderboards() {
	    for (String serialized : CONFIG.getStringList("board-list")) {
	      if (serialized.split("; ").length > 7) { // Verifique se o criador está presente na configuração
	        String id = serialized.split("; ")[6];
	        String type = serialized.split("; ")[7];
	        String creator = serialized.split("; ")[8]; // Deserializar o criador
	        Holoboard board = buildByType(BukkitUtils.deserializeLocation(serialized), id, type, creator); // Passar o criador para buildByType
	        if (board == null) {
	          return;
	        }
	        LEADERBOARDS.add(board);
	      }
	    }
	    Bukkit.getScheduler().runTaskTimer(Main.get(), () ->
	            listLeaderboards().forEach(Holoboard::update), 0, 120);
	  }

  public static void add(Location location, String id, String type, String creator) {
	    List<String> list = CONFIG.getStringList("board-list");
	    list.add(BukkitUtils.serializeLocation(location) + "; " + id + "; " + type.toLowerCase() + "; " + creator);
	    CONFIG.set("board-list", list);

	    Holoboard board = buildByType(location, id, type, creator);
	    LEADERBOARDS.add(board);
	    if (board != null) {
	        board.update();
	    }
  }

  public static void remove(Holoboard board) {
	    if (board == null) {
	        return;
	    }
	    LEADERBOARDS.remove(board);
	    List<String> list = CONFIG.getStringList("board-list");
	    list.remove(BukkitUtils.serializeLocation(board.getLocation()) + "; " + board.getId() + "; " + board.getType() + "; " + board.getCreator());
	    CONFIG.set("board-list", list);
	    
	    board.destroy();
	}
  
  public static Holoboard getById(String id) {
    return LEADERBOARDS.stream().filter(board -> board.getId().equals(id)).findFirst().orElse(null);
  }
  
  public static Holoboard getByCreator(String name) {
	    return LEADERBOARDS.stream().filter(board -> board.getCreator().equals(name)).findFirst().orElse(null);
	  }
  
  public static Holoboard getByLocation(Location loc) {
	    return LEADERBOARDS.stream().filter(board -> board.getLocation().equals(loc)).findFirst().orElse(null);
	  }

  public static Holoboard getByType(String type) {
	    return LEADERBOARDS.stream().filter(board -> board.getType().equals(type)).findFirst().orElse(null);
	  }
  
  public static Collection<Holoboard> listLeaderboards() {
    return LEADERBOARDS;
  }

  private static Holoboard buildByType(Location location, String id, String type, String creator) {
	    if (type.equalsIgnoreCase("antitrap")) {
	      return new AntiTrapHolo(location, id, creator);
	    } else if (type.equalsIgnoreCase("caldeirao")) {
	        return new CaldeiraoHolo(location, id, creator);
	      }else if (type.equalsIgnoreCase("pulso")) {
	          return new PulsoHolo(location, id, creator);
	        }else if (type.equalsIgnoreCase("inversor")) {
	            return new InversorHolo(location, id, creator);
	          }else if (type.equalsIgnoreCase("limpador")) {
		            return new ChunkClearHolo(location, id, creator);
		          }    

	    return null;
	  }

  public abstract String getType();
  
  public abstract List<String> getHologramLines();

  public void update() {
	    List<String> lines = new ArrayList<>();
	    for (String line : this.getHologramLines()) {
	      lines.add(line);
	    }

	    Bukkit.getScheduler().runTask(Main.get(), () -> {
	      if (this.hologram == null) {
	        this.hologram = HologramLibrary.createHologram(this.location.clone(), lines);
	        return;
	      }

	      int index = 1;
	      for (String line : lines) {
	        line = line.replace("{monthly_color}", this.canSeeMonthly() ? "§a§l" : "§7")
	                .replace("{total_color}", this.canSeeMonthly() ? "§7" : "§a§l");
	        hologram.updateLine(index, line);
	        if (hologram.getLine(index).getLine().equals("")) {
	          hologram.getLine(index).setLocation(hologram.getLine(index).getLocation().add(0, Double.MAX_VALUE, 0));
	        }
	        index++;
	      }
	    });
	  }
  
  public static void clear() {
      List<String> list = CONFIG.getStringList("board-list");
      list.clear();
      CONFIG.set("board-list", list);
  }

  public void destroy() {
	  if (this.hologram != null) {
          HologramLibrary.removeHologram(this.hologram);
          this.hologram = null;
     }
  }
  
  public String getCreator() {
	    return this.creator;
	}
  
  public String getId() {
    return this.id;
  }

  public boolean canSeeMonthly() {
    return this.monthly;
  }

  public Location getLocation() {
    return location;
  }

}
