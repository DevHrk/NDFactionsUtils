package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.CataclistItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class CataclistListener implements Listener {
	
	FileConfiguration m = Main.get().getConfig(); 
	public static ArrayList<String> concluido = new ArrayList<String>();
	public static ArrayList<String> naoconcluido = new ArrayList<String>();
	
	@EventHandler
	void inteteracta(PlayerInteractEvent e) {
	    Player p = e.getPlayer();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    int raio = m.getInt("Cataclist.Raio");
	    List<Entity> nearbyE = p.getNearbyEntities(raio, raio, raio);
	    ItemStack itemInHand = p.getItemInHand();
	    if (e.getAction() != Action.RIGHT_CLICK_AIR || itemInHand == null || !itemInHand.isSimilar(CataclistItem.ARMADILHA)) return;
	    
	    if(!Utils.isZonaProtegida(p.getLocation())) {
	        p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	        e.setCancelled(true);
	    } else if(concluido.contains(p.getName())) {
	        MessageUtils.send(p, m1.getStringList("Cataclist.PodeInteragir"));
	    } else if(naoconcluido.contains(p.getName())) {
	        MessageUtils.send(p, m1.getStringList("Cataclist.NaoInteragir"));
	    } else {
	        naoconcluido.add(p.getName());
	        MessageUtils.send(p, m1.getStringList("Cataclist.TittleAtivando"));
	        MessageUtils.send(p, m1.getStringList("Cataclist.Ativação"));
	        for (Entity s : nearbyE) {
	            if (s instanceof Player) {
	                Player p1 = (Player)s;
	                MessageUtils.send(p1, m1.getStringList("Cataclist.Ativação"));
	                MessageUtils.send(p1, m1.getStringList("Cataclist.TittleAtivando"));
	            }
	        }
	        
	        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
	            concluido.add(p.getName());
	            naoconcluido.remove(p.getName());
	            MessageUtils.send(p, m1.getStringList("Cataclist.Ativada-Colocar"));
	            MessageUtils.send(p, m1.getStringList("Cataclist.TittleAtivada"));
	            for (Entity s : nearbyE) {
	                if (s instanceof Player) {
	                    Player p1 = (Player)s;
	                    MessageUtils.send(p1, m1.getStringList("Cataclist.TittleAtivada"));
	                }
	            }
	        }, 20L * m.getInt("Cataclist.Tempo"));
	    }
	}
	
	@EventHandler
	void interact(BlockPlaceEvent e) {
	    Player p = e.getPlayer();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    ItemStack itemInHand = p.getInventory().getItemInHand();
	    int raio = m.getInt("Cataclist.Raio");
	    List<Entity> nearbyE = p.getNearbyEntities(raio, raio, raio);
	    
	    if (itemInHand == null || !itemInHand.isSimilar(CataclistItem.ARMADILHA)) return;
	    
	    if(!Utils.isZonaProtegida(e.getBlock().getLocation())) {
	        p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	        e.setCancelled(true);
	    } else if(concluido.contains(p.getName())) {
	        API.removeItem(p);
	        concluido.remove(p.getName());
	        naoconcluido.remove(p.getName());
	        Location backLocation = new Location(Bukkit.getServer().getWorld(m.getString("Cataclist.Back.Mundo")), m.getInt("Cataclist.Back.X"), m.getInt("Cataclist.Back.Y"), m.getInt("Cataclist.Back.Z"));
	        p.teleport(backLocation);
	        p.setHealth(0);
	        for (Entity s : nearbyE) {
	            if (s instanceof Player) {
	                Player p1 = (Player)s;
	                p1.teleport(backLocation);
	                p1.setHealth(0);
	            }
	        }
	    } else {
	        MessageUtils.send(p, m1.getStringList("Cataclist.AindaNao"));
	        e.setCancelled(true);
	    }
	}
}
