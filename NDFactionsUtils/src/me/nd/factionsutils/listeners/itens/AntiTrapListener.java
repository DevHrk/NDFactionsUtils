package me.nd.factionsutils.listeners.itens;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.holoboard.Holoboard;
import me.nd.factionsutils.itens.AntiTrapItem;
import me.nd.factionsutils.plugin.SConfig;

public class AntiTrapListener implements Listener {
	
	FileConfiguration m = Main.get().getConfig(); 
	public static HashSet<String> playersInArea = new HashSet<>();
	
	@EventHandler
	void onBlockPlace(BlockPlaceEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Block b = e.getBlock();
	    Player p = e.getPlayer();
	    ItemStack item = p.getItemInHand();

	    if (item == null || !item.isSimilar(AntiTrapItem.AntiTrap)) {
	        return;
	    }

	    Location location = b.getLocation().clone();
	    String playerName = p.getName();
	    boolean canPlaceBlock = Utils.isZonaProtegida(b.getLocation());
	    boolean isAntiTrapLimit = m.getBoolean("Anti-Trap.Limite");
	    Holoboard antiTrapBoard = Holoboard.getById(playerName + "antitrap");
	    if (!canPlaceBlock || (isAntiTrapLimit && antiTrapBoard != null)) {
	        String messageKey = !canPlaceBlock ? "Mensagens.ZonaProtegida" : "Mensagens.Limite";
	        p.sendMessage(m1.getString(messageKey).replace("&", "§"));
	        e.setCancelled(true);
	        return;
	    }
	    
	    b.setType(Material.NOTE_BLOCK);
	    API.removeItem(p);

	    if (isAntiTrapLimit) {
	        location.add(0.5, 0, 0.5);
	        Holoboard.add(location, playerName + "antitrap", "antitrap", playerName);
	    }

	    Bukkit.getScheduler().runTaskLater(Main.get(), () -> setMeta(b, true), 5L);
	    Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
	        Holoboard board = Holoboard.getById(playerName + "antitrap");
	        if (b != null && b.hasMetadata("AntiTrap")) {
	            if (b.getType() != Material.AIR) {
	                b.setType(Material.AIR);
	            }
	            removeMeta(b);
	            if (isAntiTrapLimit) {
	                Holoboard.remove(board);
	            }
	        } else if (board != null) {
	            // Se o bloco não existir mais, remova apenas o holograma
	            return;
	        }
	    }, 20L * m.getInt("Anti-Trap.tempo"));
	}
	
    @EventHandler
     void onExplosion(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.hasMetadata("AntiTrap")) {
                Location originalLocation = block.getLocation();
                Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
                Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
                if (boardToRemove != null) {
                    Holoboard.remove(boardToRemove);
                    block.setType(Material.AIR);
                    removeMeta(block);
                }
            }
        }
    }
    	
    @EventHandler
    void colocar1(BlockPlaceEvent e) {
        Location l = e.getBlockPlaced().getLocation();
        int raio = m.getInt("Anti-Trap.Raio");
        int altura = m.getInt("Anti-Trap.Altura");
        IntStream.rangeClosed(-raio, raio).forEach(x -> {
            IntStream.rangeClosed(-altura, altura).forEach(y -> {
                IntStream.rangeClosed(-raio, raio).forEach(z -> {
                    Block b = l.getWorld().getBlockAt(l.getBlockX() + x, l.getBlockY() + y, l.getBlockZ() + z);
                    if (b.getType() == Material.NOTE_BLOCK && b.hasMetadata("AntiTrap") && placed(b)) {
                        e.setCancelled(true);
                        return;
                    }
                });
            });
        });
    }

    	
    	@EventHandler
    	void onBreak(BlockBreakEvent e) {
    	    Player p = e.getPlayer();
    	    Block block = e.getBlock();
    	    String playerName = p.getName();
    	    if (block.hasMetadata("AntiTrap")) {
    	        // Verifique se o jogador tem um "antitrap" colocado
    	        if (m.getBoolean("Anti-Trap.Limite") && Holoboard.getById(playerName + "antitrap") != null && Holoboard.getByType("antitrap") != null) {
    	                removeAntiTrap(block, playerName);
    	                e.setCancelled(true);
    	        } else {
    	            // Se o jogador não tem um "antitrap" colocado, ele pode quebrar qualquer um
    	            removeAntiTrap(block, playerName);
    	        }
    	    }
    	}

    	void removeAntiTrap(Block block, String playerName) {
    	    Location originalLocation = block.getLocation();
    	    Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
    	    Optional.ofNullable(Holoboard.getByLocation(offsetLocation)).ifPresent(Holoboard::remove);
    	    block.setType(Material.AIR);
    	    removeMeta(block);
    	}
    	
		  public void setMeta(Block b,boolean place) {
			  b.setMetadata("AntiTrap", new FixedMetadataValue(Main.get() , place));
		  }
		  
		  public void removeMeta(Block b) {
			  b.removeMetadata("AntiTrap", Main.get());
		  }
		  
		  public boolean placed(Block b) {
			  List<MetadataValue> meta = b.getMetadata("AntiTrap");
			  for (MetadataValue value : meta) {
				  return value.asBoolean();
			  }
			  return false;
		  }
}