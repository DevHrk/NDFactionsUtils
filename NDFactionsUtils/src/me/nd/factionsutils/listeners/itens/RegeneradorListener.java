package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.player.*;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.RegeneradorItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;

public class RegeneradorListener implements Listener
{
	FileConfiguration m = Main.get().getConfig(); 
	 @EventHandler
     void onInteract(PlayerInteractEvent e) {
   	    Player p = e.getPlayer();
   	    SConfig m1 = Main.get().getConfig("Mensagens");
        if(e.getAction() == Action.RIGHT_CLICK_AIR && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (p.getInventory().getItemInHand().isSimilar(RegeneradorItem.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
			p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
			e.setCancelled(true);
			return;
        }
        }
	 }
	 
    @EventHandler
     void onDrinkPotion(PlayerItemConsumeEvent e) {
   	    Player p = e.getPlayer();
        NDPlayer mp = DataManager.players.get(p.getName());
        SConfig m1 = Main.get().getConfig("Mensagens");
        if (p.getInventory().getItemInHand().isSimilar(RegeneradorItem.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
			p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
			e.setCancelled(true);
			return;
        }
        
        if (!mp.hasFaction()) {
        	e.setCancelled(true);
        	return;
        }
        
        if (p.getInventory().getItemInHand().isSimilar(RegeneradorItem.ARMADILHA)) {
            e.setCancelled(true);
            mp.getFaction().getAllOnline().forEach(todos -> {
                todos.resetMaxHealth();
                todos.setHealth(m.getInt("Regenerador.vida"));
                MessageUtils.send(todos, m1.getStringList("Regenerador.Regenerado"));
            });
            API.removeItem(p);  
            MessageUtils.send(p, m1.getStringList("Regenerador.regen"));
        }
    }
}
