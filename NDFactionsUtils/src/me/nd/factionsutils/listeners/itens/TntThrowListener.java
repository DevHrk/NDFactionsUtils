package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.*;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.TntThrowItem;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class TntThrowListener implements Listener
{
	FileConfiguration m = Main.get().getConfig(); 
	@EventHandler
    void onInteract( PlayerInteractEvent e) {
         Player p = e.getPlayer();
         SConfig m1 = Main.get().getConfig("Mensagens");
         Action action = e.getAction();
         ItemStack itemInHand = p.getItemInHand();
         if (p.getItemInHand().isSimilar(TntThrowItem.get().getItem()) && !Utils.isZonaProtegida(p.getLocation()) && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
        	 p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
			 e.setCancelled(true);
		     return;
			}
        if (p.getItemInHand().isSimilar(TntThrowItem.ARMADILHA) && p.getInventory().getItemInHand().isSimilar(TntThrowItem.get().getItem()) && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            API.removeItem(p);
            TNTPrimed tnt = (TNTPrimed)p.getWorld().spawn(p.getLocation(), (Class)TNTPrimed.class);
            tnt.setVelocity(p.getLocation().getDirection().normalize().multiply(m.getInt("TnT-Arremessavel.Velocidade")));
        }
        if (itemInHand.isSimilar(TntThrowItem.get().getItem()) && (action == Action.RIGHT_CLICK_BLOCK)) {
            e.setCancelled(true);
        }
    }
}
