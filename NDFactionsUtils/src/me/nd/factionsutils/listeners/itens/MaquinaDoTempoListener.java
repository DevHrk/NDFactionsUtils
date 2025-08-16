package me.nd.factionsutils.listeners.itens;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.MaquinaDoTempoItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class MaquinaDoTempoListener implements Listener {
	
	 FileConfiguration m = Main.get().getConfig(); 
	 public static HashMap<Player, Location> maquinadotempoLocations = new HashMap<Player, Location>();

	 @EventHandler
	 	void interagir(PlayerInteractEvent e) {
	 		SConfig m1 = Main.get().getConfig("Mensagens");
	 		Player p = e.getPlayer();
	 		int radius = m.getInt("MaquinaDoTempo.raio");
	 		int teleportRadius = m.getInt("MaquinaDoTempo.raio-teleporte");
	 		int time = 20 * m.getInt("MaquinaDoTempo.tempo");

	 		if (p.getInventory().getItemInHand().isSimilar(MaquinaDoTempoItem.ARMADILHA)) {
	 		    if (!Utils.isZonaProtegida(p.getLocation())) {
	 		        p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	 		    } else {
	 		        List<Entity> nearbyEntities = p.getNearbyEntities(radius, radius, radius).stream().filter(entity -> entity instanceof Player).collect(Collectors.toList());
	 		        if (nearbyEntities.isEmpty()) {
	 		    	 MessageUtils.send(p, m1.getStringList("MaquinaTempo.Nao-Ha"));
	 		    	 e.setCancelled(true);
	 		         return;
	 		       } 
	 		       for (Entity s : nearbyEntities) {
	 		            if (s instanceof Player) {
	 		 		        API.removeItem(p);
	 		                Player p1 = (Player)s;
	 		                NDPlayer mt = DataManager.players.get(p1.getName());
	 		                NDFaction fact = mt.getFaction();
	 		                NDPlayer mp = DataManager.players.get(p.getName());
	 		                NDFaction facp = mp.getFaction();
	 		                if (fact != facp || fact == null) {
	 		                    maquinadotempoLocations.put(p1, s.getLocation());
	 		                    MessageUtils.send(p1, m1.getStringList("MaquinaTempo.Tittle"));
	 		                    MessageUtils.send(p, m1.getStringList("MaquinaTempo.Utilizado"));
	 		                }
	 		            }
	 		        }
	 		        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {    
	 		        	List<Entity> teleport = p.getNearbyEntities(teleportRadius, teleportRadius, teleportRadius).stream().filter(entity -> entity instanceof Player).collect(Collectors.toList());
	 		            for (Entity s : teleport) {
	 		                if (s instanceof Player) {
	 		                    Player p1 = (Player)s;
	 		                    NDPlayer mt =  DataManager.players.get(p1.getName());
	 		                    NDFaction fact = mt.getFaction();
	 		                    NDPlayer mp = DataManager.players.get(p.getName());
	 		                    NDFaction facp = mp.getFaction();
	 		                    if (fact != facp || fact == null) {
	 		                        s.teleport(maquinadotempoLocations.get(s));
	 		                        maquinadotempoLocations.remove(s);
	 		                        List<String> messages = m1.getStringList("MaquinaTempo.teleportado").stream().map(message -> message.replace("{tempo}", String.valueOf(m.getInt("MaquinaDoTempo.tempo")))).collect(Collectors.toList());
	 		                        MessageUtils.send(p1, messages);
	 		                    }
	 		                }
	 		            }
	 		        }, time);
	 		    }
	 		    e.setCancelled(true);
	 		}
	 		
	 	}

	
	
}
