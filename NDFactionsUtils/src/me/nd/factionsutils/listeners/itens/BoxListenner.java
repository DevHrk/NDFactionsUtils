package me.nd.factionsutils.listeners.itens;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.BoxItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class BoxListenner implements Listener {

	FileConfiguration m = Main.get().getConfig(); 
	
	@EventHandler
	public void Bater3(PlayerInteractEvent e) {
	    Player t = e.getPlayer();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    NDPlayer p = DataManager.players.get(t.getName());
	    if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
	    	Terra terra = new Terra(t.getWorld(), t.getLocation().getChunk().getX(), t.getLocation().getChunk().getZ());
	        if (t.getInventory().getItemInHand().isSimilar(BoxItem.ARMADILHA) && p.getFaction() == null) {
	    		e.setCancelled(true);
	    		t.sendMessage("§cVocê não pertence a uma facção!");
	    		return;
	    	}
	        if (t.getInventory().getItemInHand().isSimilar(BoxItem.ARMADILHA) && p.getFaction() != null && !p.getFaction().ownsTerritory(terra)) {
	        	MessageUtils.send(t, m1.getStringList("BoxInsta.ApenasFac"));
	            e.setCancelled(true);
	            return;
	        }
	    	
	        if (t.getInventory().getItemInHand().isSimilar(BoxItem.ARMADILHA) && !API.isPlaying(t)) {
	        	if (cooldown.containsKey(t.getUniqueId())) {
	        	    long timeLeft = cooldown.get(t.getUniqueId()) - System.currentTimeMillis();
	        	    if (timeLeft > 0) {
	        	        String tempo = API.formatTime(timeLeft);
	        	        MessageUtils.send(t, m1.getStringList("BoxInsta.Cooldown").stream().map(message -> message.replace("{tempo}", tempo)).collect(Collectors.toList()));
	        	        return;
	        	    }
	        	}
	            API.removeItem(t);
	            MessageUtils.send(t, m1.getStringList("BoxInsta.Utilizado"));
	            API.dispatchCommands(t, m.getStringList("BoxInsta.Schematic"));
	            cooldown.put(t.getUniqueId(), System.currentTimeMillis() + (Integer.valueOf(m.getInt("BoxInsta.Tempo")) * 1000));
	        }
	    }
	}
	
	@EventHandler
	public void Bater3(BlockPlaceEvent e) {
	    Player p = e.getPlayer();
	        if (p.getInventory().getItemInHand().isSimilar(BoxItem.ARMADILHA)) {
	            e.setCancelled(true);
	            return;
	        }
	}

	private Map<UUID, Long> cooldown = new HashMap<>();

	public void removeCooldown(UUID uuid) {
	    cooldown.remove(uuid);
	}

}
