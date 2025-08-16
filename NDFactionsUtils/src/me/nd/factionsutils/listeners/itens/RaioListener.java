package me.nd.factionsutils.listeners.itens;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.RaioMestre;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class RaioListener implements Listener {
	FileConfiguration m = Main.get().getConfig(); 
	@EventHandler
	void aoClicar(PlayerInteractEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        if (e.getItem() == null) return;
	        Player p = e.getPlayer();
	        if(p.getInventory().getItemInHand().isSimilar(RaioMestre.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
	            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	            e.setCancelled(true);
	            return;
	        }
	        
	        if (p.getInventory().getItemInHand().isSimilar(RaioMestre.ARMADILHA)) {
	            playLightning(e.getClickedBlock(), p);
	            API.removeItem(p);
	            MessageUtils.send(p, m1.getStringList("RaioMestre.Utilizado"));
	            e.setCancelled(true);
	        }
	    }
	}

	private void playLightning(Block b, Player player) {
	    player.getWorld().strikeLightning((b != null) ? b.getLocation() : player.getTargetBlock((Set<Material>) null, m.getInt("Raio_Mestre.Distancia")).getLocation());
	    Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.valueOf("AMBIENCE_THUNDER"), 1, 1));
	}
	
}
