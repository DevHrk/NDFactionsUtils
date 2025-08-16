package me.nd.factionsutils.listeners.itens;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.holoboard.Holoboard;
import me.nd.factionsutils.itens.PulsoMagneticoItem;
import me.nd.factionsutils.plugin.SConfig;

public class PulsoListener implements Listener {
	
	FileConfiguration m = Main.get().getConfig(); 
    HashMap<Block, Boolean> redstone = new HashMap<>();
    public static HashSet<String> pulsomagnetico = new HashSet<>();
	
	@EventHandler
	void onPlace(BlockPlaceEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Block b = e.getBlock();
	    Location l = b.getLocation().clone();
	    Player p = e.getPlayer();
	    String pget = p.getName();
	    ItemStack item = p.getItemInHand();
	    boolean isPulsoLimit = m.getBoolean("Pulso.Limite");

	    if (item == null || !item.isSimilar(PulsoMagneticoItem.PULSO)) {
	        return;
	    }

	    if (!Utils.isZonaProtegida(p.getLocation())  || (isPulsoLimit && Holoboard.getById(pget + "pulso") != null && Holoboard.getByType("pulso") != null)) {
	        String messageKey = !Utils.isZonaProtegida(p.getLocation()) ? "Mensagens.ZonaProtegida" : "Mensagens.Limite";
	        p.sendMessage(m1.getString(messageKey).replace("&", "§"));
	        e.setCancelled(true);
	        return;
	    }

	    setMeta(b, true);
	    API.removeItem(p);
	    redstone.put(b, true);

	    if (isPulsoLimit) {
	        l.add(0.5, 0, 0.5);
	        Holoboard.add(l, pget + "pulso", "pulso", pget);
	    }

	    Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
	        Holoboard board = Holoboard.getById(pget + "pulso");
	        Optional.ofNullable(b).filter(block -> block.hasMetadata("pulso")).ifPresent(block -> {
	                    if (block.getType() != Material.AIR) {
	                        block.setType(Material.AIR);
	                    }
	                    redstone.put(block, false);
	                    removeMeta(block);
	                    if (isPulsoLimit) {
	                        Holoboard.remove(board);
	                    }
	                });
	    }, 20L * m.getInt("Pulso.Tempo"));
	}
    
	@EventHandler
	void onExplosion(EntityExplodeEvent event) {
	    event.blockList().stream().filter(block -> block.hasMetadata("pulso")).forEach(block -> {
	            Location originalLocation = block.getLocation();
	            Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
	            Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
	            if (boardToRemove != null) {
	                Holoboard.remove(boardToRemove);
	                block.setType(Material.AIR);
	                removeMeta(block);
	                if (redstone.containsKey(block) && redstone.get(block)) {
	                    redstone.put(block, false);
	                }
	            }
	        });
	}

	@EventHandler
	void onBreak(BlockBreakEvent e) {
	    Player p = e.getPlayer();
	    Block block = e.getBlock();
	    String playerName = p.getName();
	    if (block.hasMetadata("pulso")) {
	        // Verifique se o jogador tem um "pulso" colocado
	        if (m.getBoolean("Pulso.Limite") && Holoboard.getById(playerName + "pulso") != null && Holoboard.getByType("pulso") != null) {
	                removePulso(block, playerName);
	                e.setCancelled(true);
	        } else {
	            // Se o jogador não tem um "pulso" colocado, ele pode quebrar qualquer um
	        	removePulso(block, playerName);
	        }
	    }
	}
		    	
	@EventHandler
	void onBlockDisable(BlockRedstoneEvent e) {
	    // Só processa se o sinal ligou agora
	    if (e.getOldCurrent() > 0 || e.getNewCurrent() <= 0) return;

	    Location l = e.getBlock().getLocation();
	    World world = l.getWorld();

	    final int raio = m.getInt("Pulso.Raio");
	    final int altura = m.getInt("Pulso.Altura");

	    final int baseX = l.getBlockX();
	    final int baseY = l.getBlockY();
	    final int baseZ = l.getBlockZ();

	    // Loop 3D otimizado
	    for (int x = -raio; x <= raio; x++) {
	        for (int y = -altura; y <= altura; y++) {
	            for (int z = -raio; z <= raio; z++) {
	                Block b = world.getBlockAt(baseX + x, baseY + y, baseZ + z);

	                if (b.getType() != Material.NOTE_BLOCK) continue;
	                if (!b.hasMetadata("pulso")) continue;
	                if (!placed(b)) continue;

	                Boolean valor = redstone.get(b);
	                if (valor != null && valor) {
	                    e.setNewCurrent(0);
	                    return; // Encontrou um, para a busca
	                }
	            }
	        }
	    }
	}
				
	void removePulso(Block block, String playerName) {
	    Location originalLocation = block.getLocation();
	    Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
	    Optional.ofNullable(Holoboard.getByLocation(offsetLocation)).ifPresent(Holoboard::remove);
	    block.setType(Material.AIR);
	    removeMeta(block);
	}
				
		    	  public void setMeta(Block b,boolean place) {
					  b.setMetadata("pulso", new FixedMetadataValue(Main.get() , place));
				  }
		    	  
		    	  public void removeMeta(Block b) {
					  b.removeMetadata("pulso", Main.get());
				  }
		    	  
				  public boolean placed(Block b) {
					  List<MetadataValue> meta = b.getMetadata("pulso");
					  for (MetadataValue value : meta) {
						  return value.asBoolean();
					  }
					  return false;
				  }
}
