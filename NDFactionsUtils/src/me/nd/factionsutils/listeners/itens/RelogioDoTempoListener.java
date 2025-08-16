package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.RelogioDoTempo;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class RelogioDoTempoListener implements Listener {
	
	public static ArrayList<String> relogiodotempo = new ArrayList<String>();
	public static HashMap<Player, Location> relogiodotempoLocations = new HashMap<Player, Location>();
	
	@EventHandler
	void interact(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		SConfig m1 = Main.get().getConfig("Mensagens");
		FileConfiguration m = Main.get().getConfig(); 
		int radius = m.getInt("RelogioDoTempo.raio");
		int teleportRadius = m.getInt("RelogioDoTempo.raio-teleporte");
		int time = 20 * m.getInt("RelogioDoTempo.tempo");

		if (p.getInventory().getItemInHand().isSimilar(RelogioDoTempo.ARMADILHA)) {
		    if (!Utils.isZonaProtegida(p.getLocation())) {
		        p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
		    } else {
		        API.removeItem(p);
		        relogiodotempoLocations.put(p, p.getLocation());
		        for (Entity s : p.getNearbyEntities(radius, radius, radius)) {
		            if (s instanceof Player) {
		                Player p1 = (Player)s;
		                relogiodotempoLocations.put(p1, p1.getLocation());
		                NDPlayer mt = DataManager.players.get(p1.getName());
		                NDFaction fact = mt.getFaction();
		                NDPlayer mp = DataManager.players.get(p.getName());
		                NDFaction facp = mp.getFaction();
		                if (fact != facp || fact == null) {
		                	MessageUtils.send(p1, m1.getStringList("RelogioDoTempo.Tittle"));
		                    relogiodotempo.add(s.getName());
		                }
		            }
		        }
		        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {    
		            p.teleport(relogiodotempoLocations.get(p));
		            relogiodotempoLocations.remove(p);
		            MessageUtils.send(p, m1.getStringList("RelogioDoTempo.Acabou"));
		            for (Entity s : p.getNearbyEntities(teleportRadius, teleportRadius, teleportRadius)) {
		                if (s instanceof Player) {
		                    Player p1 = (Player)s;
		                    MessageUtils.send(p1, m1.getStringList("RelogioDoTempo.Acabou"));
			                NDPlayer mt = DataManager.players.get(p1.getName());
			                NDFaction fact = mt.getFaction();
			                NDPlayer mp = DataManager.players.get(p.getName());
			                NDFaction facp = mp.getFaction();
		                    p1.teleport(relogiodotempoLocations.get(p1));
		                    relogiodotempoLocations.remove(s);
		                    if (fact != facp || fact == null) {
		                        relogiodotempo.remove(s.getName());
		                    }
		                }
		            }
		        }, time);
		    }
		    e.setCancelled(true);
		}
	}
	
	@EventHandler
	 void Bater(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
		Player p = (Player)e.getEntity();
		if (relogiodotempoLocations.containsKey(p)) {
		     e.setCancelled(true);
		}
		if (relogiodotempo.contains(p.getName())) {
			e.setCancelled(true);
	       }
		}
	}
	
	@EventHandler
	 void Interagir(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		SConfig m1 = Main.get().getConfig("Mensagens");
		if (relogiodotempoLocations.containsKey(p)) {
			 MessageUtils.send(p, m1.getStringList("RelogioDoTempo.Interagir"));
		     e.setCancelled(true);
		}
		
		if (relogiodotempo.contains(p.getName())) {
			MessageUtils.send(p, m1.getStringList("RelogioDoTempo.Interagir"));
			e.setCancelled(true);
	       }
	}
	
	@EventHandler
	 void Colocar(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (relogiodotempoLocations.containsKey(p)) {
		     e.setCancelled(true);
		}
		if (relogiodotempo.contains(p.getName())) {
			e.setCancelled(true);
	       }
	}
	
	@EventHandler
	 void Quebrar(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (relogiodotempoLocations.containsKey(p)) {
		     e.setCancelled(true);
		}
		
		if (relogiodotempo.contains(p.getName())) {
			e.setCancelled(true);
	       }
	}
	
    @EventHandler
    void onPlayerMove(PlayerMoveEvent e) {
       Player p = e.getPlayer();
       SConfig m1 = Main.get().getConfig("Mensagens");
       if (relogiodotempo.contains(p.getName())) {
           e.setTo(e.getFrom());
           MessageUtils.send(p, m1.getStringList("RelogioDoTempo.ParadoNoTempo"));
       }
   }
    
}
