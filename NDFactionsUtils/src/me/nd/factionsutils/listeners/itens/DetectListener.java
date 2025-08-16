package me.nd.factionsutils.listeners.itens;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.Detect;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class DetectListener implements Listener {

	FileConfiguration m = Main.get().getConfig(); 
	
	@EventHandler
	void aoClicar(PlayerInteractEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Player p = e.getPlayer();
	    ItemStack itemInHand = p.getInventory().getItemInHand();
	    
	    if (e.getAction() != Action.RIGHT_CLICK_BLOCK || itemInHand == null || e.getClickedBlock() == null) return;
	    
	    if(itemInHand.isSimilar(Detect.ARMADILHA)) {
	        if(!Utils.isZonaProtegida(p.getLocation())) {
	            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	            e.setCancelled(true);
	        } else {
	            Location loc = e.getClickedBlock().getLocation();
	            long worldSeed = loc.getWorld().getSeed();
	            Chunk playerChunk = loc.getWorld().getChunkAt(loc);
	            int xChunk = playerChunk.getX();
	            int zChunk = playerChunk.getZ();
	            Random random = new Random(worldSeed + xChunk * xChunk * 4987142 + xChunk * 5947611 + zChunk * zChunk * 4392871L + zChunk * 389711 ^ 0x3AD8025F);
	            String key = random.nextInt(10) == 0 ? "DetectSlimeChunk.Esta" : "DetectSlimeChunk.NaoEsta";
	            MessageUtils.send(p, m1.getStringList(key));
	        }
	    }
	}
}
