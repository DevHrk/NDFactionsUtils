package me.nd.factionsutils.listeners.itens;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.CabecaHydraItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class CabecaHydraListener implements Listener {
	FileConfiguration m = Main.get().getConfig(); 
	  @EventHandler
	   void onClick(PlayerInteractEvent e) {
		SConfig m1 = Main.get().getConfig("Mensagens");
	    Player p = e.getPlayer();
	    ItemStack itemInHand = p.getItemInHand();
	    
	    if (itemInHand.isSimilar(CabecaHydraItem.CABECAHYDRA) && !Utils.isZonaProtegida(p.getLocation())) {
	    	p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	    	e.setCancelled(true);
	    	return;
	    }
	    
	    if (itemInHand.isSimilar(CabecaHydraItem.CABECAHYDRA)) {
	        API.removeItem(p);
	        API.dispatchCommands(p, m.getStringList("Cabeça_de_Hydra.Efeito"));
	        MessageUtils.send(p, m1.getStringList("CabecaHydra.Ativou"));
	    }
	    if (itemInHand.isSimilar(CabecaHydraItem.CABECAHYDRA) && (e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            e.setCancelled(true);
        }
   }
}
