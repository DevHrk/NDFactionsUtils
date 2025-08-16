package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.block.*;
import org.bukkit.scheduler.*;
import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.*;

import me.nd.factions.enums.Cargo;
import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.event.*;

public class GeneratorListener implements Listener
{
	FileConfiguration m = Main.get().getConfig(); 
	@EventHandler
	void onPlace(BlockPlaceEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Player p = e.getPlayer();
	    NDPlayer mp = DataManager.players.get(p.getName());
	    NDFaction fac = mp.getFaction();
	    Block b = e.getBlock();
	    Location bloc = b.getLocation();
	    ItemStack item = p.getItemInHand();
	    Terra terra = new Terra(e.getBlock().getWorld(), e.getBlock().getLocation().getChunk().getX(), e.getBlock().getLocation().getChunk().getZ());
	    
	    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
	        return;
	    }

	    for (String item1 : m.getConfigurationSection("Geradores").getKeys(false)) {
	        String itemName = m.getString("Geradores." + item1 + ".Nome").replace("&", "§");
	        if (item.getItemMeta().getDisplayName().contains(itemName)) {
	        	int limit = m.getInt("Geradores." + item1 + ".blocks"); // Adiciona a chave de limite
	        	if (mp.getFaction() == null) {
	        		e.setCancelled(true);
	        		p.sendMessage("§cVocê precisa de uma facção");
	        		return;
	        	}
	        	
	            if (mp.getFaction() != null && !mp.getFaction().ownsTerritory(terra)) {
	                e.setCancelled(true);
	                MessageUtils.send(p, m1.getStringList("GeradorDeCamada.territorio"));
	                return;
	            }
	            
	            if (mp.getFaction() != null && !mp.getFaction().isZonaProtegida(b.getLocation())) {
	                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	                e.setCancelled(true);
	                return;
	            }

	            if (mp.getCargo() == Cargo.Recruta) {
	                e.setCancelled(true);
	                MessageUtils.send(p, m1.getStringList("GeradorDeCamada.sem-perm"));
	                return;
	            }
	            
	            if (mp.getFaction() != null && fac.isSobAtaque()) {
	                e.setCancelled(true);
	                MessageUtils.send(p, m1.getStringList("GeradorDeCamada.em-attack"));
	                return;
	            }

	            new BukkitRunnable() {
	                int blocksGenerated = 0; // Contador para os blocos gerados

	                public void run() {
	                	if (limit != 0 && blocksGenerated >= limit) {
	                        this.cancel();
	                        return;
	                    }

	                    String type = m.getString("Geradores." + item1 + ".type");
	                    int camadas = m.getInt("Geradores." + item1 + ".camadas");
	                    int id = m.getInt("Geradores." + item1 + ".Id");
	                    boolean ignoreBlock = m.getBoolean("Geradores." + item1 + ".ignore_block");

	                    if (type.equalsIgnoreCase("1")) {
	                        bloc.setY(bloc.getY() + camadas);
	                    } else if (type.equalsIgnoreCase("2")) {
	                        bloc.setY(bloc.getY() - camadas);
	                    }

	                    Block next = b.getWorld().getBlockAt(bloc.getBlockX(), bloc.getBlockY(), bloc.getBlockZ());
	                    if (next.getType() == Material.AIR && bloc.getY() != 0.0) {
	                        next.setTypeId(id);
	                        blocksGenerated++; // Incrementa o contador
	                    } else if (!ignoreBlock) {
	                        this.cancel();
	                    }
	                }
	            }.runTaskTimer((Plugin)Main.get(), 0L, 20 * m.getInt("Geradores." + item1 + ".velocidade"));
	        }
	    }
	}
}
