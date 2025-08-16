package me.nd.factionsutils.listeners.itens;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.Picareta;
import me.nd.factionsutils.plugin.SConfig;

public class PicaretaListener implements Listener {
	FileConfiguration m = Main.get().getConfig(); 
	@EventHandler(ignoreCancelled = true)
	void aoQuebrar(BlockDamageEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Player p = e.getPlayer();
	    if (e.getBlock().getType() == Material.BEDROCK) {
	        if (e.getBlock().getY() > m1.getInt("Picareta.CamadaMinima")) {
	            if (p.getInventory().getItemInHand().isSimilar(Picareta.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
	                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	                e.setCancelled(true);
	            } else if (e.getItemInHand().isSimilar(Picareta.ARMADILHA)) {
	                e.setInstaBreak(true);
	            }
	        }
	    }
	}
}