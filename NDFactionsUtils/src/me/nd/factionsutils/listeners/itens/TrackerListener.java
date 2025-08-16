package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

import java.util.Optional;

import org.bukkit.*;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.TrackerItem;
import me.nd.factionsutils.menu.RastreadorMenu;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.inventory.*;

import org.bukkit.event.inventory.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;

public class TrackerListener implements Listener
{

	FileConfiguration m = Main.get().getConfig(); 
	@EventHandler
	void onInteract(PlayerInteractEvent e) {
	    Player p = e.getPlayer();
	    ItemStack itemStack = p.getItemInHand();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    
	    if (itemStack == null || itemStack.getType() == Material.AIR) {
	        return;
	    }

	    if (p.getInventory().getItemInHand().isSimilar(TrackerItem.RASTREADOR) && !Utils.isZonaProtegida(p.getLocation())) {
	        p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	        e.setCancelled(true);
	        return;
	    }

	    if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
	        return;
	    }

	    if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName() || !itemStack.getItemMeta().getDisplayName().contains(m.getString("Rastreador.Nome").replace("&", "§"))) {
	        return;
	    }

	    // Verifique se há jogadores por perto
	    int radius = m.getInt("Rastreador.Distancia"); // Defina o raio que você deseja verificar
	    boolean playerNearby = false;
	    for (Entity entity : p.getNearbyEntities(radius, radius, radius)) {
	        if (entity instanceof Player) {
	            playerNearby = true;
	            break;
	        }
	    }

	    if (!playerNearby) {
	    	MessageUtils.send(p, m1.getStringList("Rastreador.Nao-Ha"));
	        return;
	    }

	    RastreadorMenu.abrirMenu(p, m.getInt("Rastreador.Distancia"));
	    e.setCancelled(true);
	}
	@EventHandler
	void onInventoryClickEvent(InventoryClickEvent e) {
	    Optional.ofNullable(e.getCurrentItem()).ifPresent(item -> {
	        if (e.getInventory().getName().contains(m.getString("Rastreador-Menu.Nome").replace("&", "§"))) {
	            Player p = (Player) e.getWhoClicked();
	            if (e.getSlot() > 9 && e.getSlot() < 44) {
	                String nome = item.getItemMeta().getDisplayName().replace("§e", "");
	                p.closeInventory();
	                Bukkit.dispatchCommand((CommandSender) p, "f perfil " + nome);
	            }
	            e.setCancelled(true);
	        }
	    });
	}
}
