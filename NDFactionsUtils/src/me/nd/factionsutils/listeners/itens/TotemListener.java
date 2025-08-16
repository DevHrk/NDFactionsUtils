package me.nd.factionsutils.listeners.itens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.itens.Totem;

public class TotemListener implements Listener {

	@EventHandler
	void aoMorrer(PlayerDeathEvent e) {
	    for (ItemStack item : e.getDrops()) {
	        if (item!= null && item.isSimilar(Totem.TOTEM_DA_MORTE)) {
	            e.setKeepInventory(true);
	            e.getDrops().clear();
	            e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), Totem.TOTEM_DA_MORTE);
	            removeItem(e.getEntity());
	            return;
	        }
	    }
	}

	private void removeItem(Player p) {
		for (ItemStack item : p.getInventory().getContents()) {
			if (item != null && item.isSimilar(Totem.TOTEM_DA_MORTE)) {
				if (item.getAmount() < 2) {
					p.getInventory().removeItem(item);
				} else {
					item.setAmount(item.getAmount() - 1);
				}
				return;
			}
		}
	}
}