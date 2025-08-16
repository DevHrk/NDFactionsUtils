package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.IceWarItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class IceWarListener implements Listener{
	
	FileConfiguration m = Main.get().getConfig(); 
	public static ArrayList<String> congelado = new ArrayList<String>();
    double[][] coords = { {0.0, 0.0, 4.0}, {1.0, 0.0, 4.0}, {2.0, 0.0, 4.0}, {3.0, 0.0, 3.0}, {-1.0, 0.0, 4.0}, {-2.0, 0.0, 4.0}, {-3.0, 0.0, 3.0}, {0.0, 0.0, -4.0}, {1.0, 0.0, -4.0}, {2.0, 0.0, -4.0},
    	    {3.0, 0.0, -3.0}, {-1.0, 0.0, -4.0}, {-2.0, 0.0, -4.0}, {-3.0, 0.0, -3.0}, {4.0, 0.0, 2.0}, {4.0, 0.0, 1.0}, {4.0, 0.0, 0.0}, {4.0, 0.0, -1.0}, {4.0, 0.0, -2.0}, {-4.0, 0.0, 2.0},
    	    {-4.0, 0.0, 1.0}, {-4.0, 0.0, 0.0}, {-4.0, 0.0, -1.0}, {-4.0, 0.0, -2.0}, {0.0, 1.0, 5.0}, {1.0, 1.0, 5.0}, {2.0, 1.0, 5.0}, {3.0, 1.0, 4.0}, {-1.0, 1.0, 5.0}, {-2.0, 1.0, 5.0},
    	    {-3.0, 1.0, 4.0}, {0.0, 1.0, -5.0}, {1.0, 1.0, -5.0}, {2.0, 1.0, -5.0}, {3.0, 1.0, -4.0},  {-1.0, 1.0, -5.0}, {-2.0, 1.0, -5.0}, {-3.0, 1.0, -4.0}, {5.0, 1.0, 2.0}, {5.0, 1.0, 1.0},
    	    {5.0, 1.0, 0.0}, {5.0, 1.0, -1.0}, {5.0, 1.0, -2.0}, {-5.0, 1.0, 2.0}, {-5.0, 1.0, 1.0}, {-5.0, 1.0, 0.0}, {-5.0, 1.0, -1.0}, {-5.0, 1.0, -2.0}, {-4.0, 1.0, 3.0}, {4.0, 1.0, 3.0},
    	    {4.0, 1.0, -3.0}, {-4.0, 1.0, -3.0}, {0.0, 3.0, 7.0}, {1.0, 3.0, 7.0}, {2.0, 3.0, 7.0}, {3.0, 3.0, 7.0}, {-1.0, 3.0, 7.0}, {-2.0, 3.0, 7.0}, {-3.0, 3.0, 6.0}, {0.0, 3.0, -7.0},
    	    {1.0, 3.0, -7.0}, {2.0, 3.0, -7.0}, {3.0, 3.0, -6.0}, {-1.0, 3.0, -7.0}, {-2.0, 3.0, -7.0}, {-3.0, 3.0, -6.0}, {7.0, 3.0, 2.0}, {7.0, 3.0, 1.0}, {7.0, 3.0, 0.0}, {7.0, 3.0, -1.0},
    	    {7.0, 3.0, -2.0}, {-7.0, 3.0, 2.0}, {-7.0, 3.0, 1.0}, {-7.0, 3.0, 0.0}, {-7.0, 3.0, -1.0}, {-7.0, 3.0, -2.0}, {-6.0, 3.0, 3.0}, {6.0, 3.0, 3.0}, {6.0, 3.0, -3.0}, {-6.0, 3.0, -3.0},
    	    {-4.0, 3.0, 5.0}, {4.0, 3.0, 5.0}, {3.0, 3.0, 6.0}, {4.0, 3.0, -5.0}, {-4.0, 3.0, -5.0}, {5.0, 3.0, -4.0}, {5.0, 3.0, 4.0}, {-5.0, 3.0, 4.0}, {-4.0, 3.0, -5.0}, {-5.0, 3.0, -4.0}
    	};
	
	@EventHandler
    public void Bater3(PlayerInteractEvent e) {
        Player t = e.getPlayer();
        SConfig m1 = Main.get().getConfig("Mensagens");
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK ) {
        	if (t.getInventory().getItemInHand().isSimilar(IceWarItem.ARMADILHA) && !Utils.isZonaProtegida(t.getLocation())) {
        		t.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
				e.setCancelled(true);
				return;
        	}
        if (t.getInventory().getItemInHand().isSimilar(IceWarItem.ARMADILHA) && !API.isPlaying(t)) {
            API.saveLocation(t);
            API.addPlayer(t);
            API.removeItem(t);
            MessageUtils.send(t, m1.getStringList("Icewar.Utilizado"));
            for (Entity s : t.getNearbyEntities(
		         Integer.valueOf(m.getInt("Ice-War.raio")).intValue(), 
		         Integer.valueOf(m.getInt("Ice-War.raio")).intValue(), 
		         Integer.valueOf(m.getInt("Ice-War.raio")).intValue())) {
            	if (s instanceof Player) {
  	        	Player p = (Player)s;
  	            NDPlayer mt = DataManager.players.get(p.getName());
  	            NDFaction fact = mt.getFaction();
  	            NDPlayer mp = DataManager.players.get(t.getName());
  	            NDFaction facp = mp.getFaction();
  	            if (fact != facp || fact == null) {
      						congelado.add(s.getName());
      						MessageUtils.send(p, m1.getStringList("Icewar.Tittle"));
      		     	       }
            	}
            }

            	for (double[] coord : coords) {
                    Block block = t.getLocation().add(coord[0], coord[1], coord[2]).getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.valueOf(m.getString("Ice-War.Material")));
                    }
            	}
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.get(), new Runnable() {
                @Override
                public void run() {
                    t.teleport(API.getLocation(t));
                    	for (double[] coord : coords) {
                            Block block = t.getLocation().add(coord[0], coord[1], coord[2]).getBlock();
                            if (block.getType() == Material.valueOf(m.getString("Ice-War.Material"))) {
                                block.setType(Material.AIR);
                            }
                    	}
                    API.delLocation(t);
                    API.delPlayer(t);
                    for (Entity s : t.getNearbyEntities(
           		         Integer.valueOf(m.getInt("Ice-War.raio")).intValue(), 
           		         Integer.valueOf(m.getInt("Ice-War.raio")).intValue(), 
           		         Integer.valueOf(m.getInt("Ice-War.raio")).intValue())) {
                       	if (s instanceof Player) {
             	        	Player p = (Player)s;
             	        	NDPlayer mt = DataManager.players.get(p.getName());
             	            NDFaction fact = mt.getFaction();
             	            NDPlayer mp = DataManager.players.get(t.getName());
             	            NDFaction facp = mp.getFaction();
             	            if (fact != facp || fact == null) {
             	            	MessageUtils.send(p, m1.getStringList("Icewar.Saiu"));
             	            	congelado.remove(s.getName());
                 		     	       }
                       	  }
                       }
                }
            }, Integer.valueOf(m.getInt("Ice-War.tempo")) * 20);
        }
      }
	}
    @EventHandler
    void onPlayerMove(PlayerMoveEvent e) {
       Player p = e.getPlayer();
       SConfig m1 = Main.get().getConfig("Mensagens");
       if (congelado.contains(p.getName())) {
           e.setTo(e.getFrom());
           MessageUtils.send(p, m1.getStringList("Icewar.Congelado"));
       }
   }
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	 void aoExecutarComando(PlayerCommandPreprocessEvent e) {
		String cmd = e.getMessage().toLowerCase().split(" ")[0];
		SConfig m1 = Main.get().getConfig("Mensagens");  
		Player p = e.getPlayer();
		if (congelado.contains(p.getName())) {
		for (String blockedCmd : m.getStringList("Ice-War.Comandos-Bloqueados")) {
			if (blockedCmd.equals(cmd) || (cmd.split(":").length > 1 && blockedCmd.equals("/" + cmd.split(":")[1]))) {
					MessageUtils.send(p, m1.getString("Icewar.Bloqueado"), m1.getStringList("Icewar.Bloqueado"));
					e.setCancelled(true);
					return;
			 }
		   }
		}
	}
}
