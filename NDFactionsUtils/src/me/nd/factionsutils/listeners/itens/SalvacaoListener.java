package me.nd.factionsutils.listeners.itens;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.SalvacaoItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class SalvacaoListener implements Listener {
	
	 FileConfiguration m = Main.get().getConfig(); 
	 @EventHandler
	 void onClick3(PlayerInteractEvent e) {
	     SConfig m1 = Main.get().getConfig("Mensagens");
	     int radius = m.getInt("Salvação.Raio");
	     Player p = e.getPlayer();
	     
	     if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
	     if (!p.getInventory().getItemInHand().isSimilar(SalvacaoItem.ARMADILHA)) return;

	     if (!Utils.isZonaProtegida(p.getLocation())) {
	         p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	         e.setCancelled(true);
	         return;
	     }

	     List<Entity> nearbyEntities = p.getNearbyEntities(radius, radius, radius).stream().filter(entity -> entity instanceof Player).collect(Collectors.toList());

	     if (nearbyEntities.isEmpty()) {
	    	 MessageUtils.send(p, m1.getStringList("Salvação.Nao-Ha"));
	         return;
	     }

	     for (Entity s : nearbyEntities) {
	         Player t = (Player) s;
	         NDPlayer mt = DataManager.players.get(t.getName());
	         NDFaction fact = mt.getFaction();
	         NDPlayer mp = DataManager.players.get(p.getName());
	         NDFaction facp = mp.getFaction();

	         if (fact == null || fact != facp) {

	         Location playerCenterLocation = p.getEyeLocation();
	         Location playerToThrowLocation = t.getEyeLocation();
	         Vector throwVector = playerToThrowLocation.subtract(playerCenterLocation).toVector();
	         throwVector.normalize();
	         throwVector.multiply(Double.parseDouble(m.getString("Salvação.Blocos")));
	         throwVector.setY(0.0D);
	         t.setVelocity(throwVector);

	         MessageUtils.send(t, m1.getStringList("Salvação.Player-Acertado"));
	         MessageUtils.send(p, m1.getStringList("Salvação.Funcionou"));
	         API.removeItem(p);
	         e.setCancelled(true);
	         return;
	         }
	     }
	 }

}
