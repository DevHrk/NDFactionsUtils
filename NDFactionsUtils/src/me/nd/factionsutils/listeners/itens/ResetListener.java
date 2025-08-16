package me.nd.factionsutils.listeners.itens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.factions.MassiveFactions;
import me.nd.factionsutils.factions.SpawnFireWork;
import me.nd.factionsutils.itens.ResetKdr;
import me.nd.factionsutils.plugin.SConfig;

public class ResetListener implements Listener  {
	
	@EventHandler
	void aoClicar(PlayerInteractEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    if (e.getItem() == null) return;
	    Player p = e.getPlayer();
	    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        if (p.getInventory().getItemInHand().isSimilar(ResetKdr.RESET_KDR)) {
	            if (!Utils.isZonaProtegida(p.getLocation())) {
	                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	                e.setCancelled(true);
	            } else if (MassiveFactions.resetKDR(e.getPlayer())) {
	                SpawnFireWork.small(e.getPlayer());
	                API.removeItem(e.getPlayer());
	                e.setCancelled(true);
	            }
	        }
	    }
	}
}
