package me.nd.factionsutils.listeners.itens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.command.CriadorItem;
import me.nd.factionsutils.plugin.SConfig;

public class CriarListener implements Listener  {
	
	@EventHandler
	 void aoClicar(PlayerInteractEvent e) {
		SConfig m1 = Main.get().getConfig("Mensagens");
		SConfig m2 = Main.get().getConfig("CriadorItens");
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player p = e.getPlayer();
			if (e.getItem() == null) return;
			for (ItemStack i : CriadorItem.NEW_ITENS.keySet()) {
				for (String item : m2.getSection("Criador-Itens").getKeys(false)) {
					String perm = m2.getString("Criador-Itens." + item + ".Perm");
					if (perm == null || perm.isEmpty() || p.hasPermission(perm)) {
					    if (p.getInventory().getItemInHand().isSimilar(i)) {
					        API.dispatchCommands(e.getPlayer(), CriadorItem.NEW_ITENS.get(i));
					        API.removeItem(e.getPlayer());
					        e.setCancelled(true);
					        return;
					    }
					} else {
					    p.sendMessage(m1.getString("Mensagens.SemPermItem").replace("&", "§"));
					}
				
				if (p.getInventory().getItemInHand().isSimilar(i) && !Utils.isZonaProtegida(p.getLocation())) {
					p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
					e.setCancelled(true);
				}
			}
		}
		}
	}
	
}
