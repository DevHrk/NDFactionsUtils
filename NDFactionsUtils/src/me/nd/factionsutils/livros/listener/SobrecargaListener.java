package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class SobrecargaListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Sobrecarga.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_SWORD")) {
	            p.sendMessage("§cEste encantamento serve apenas para espadas");
	            return;
	        }

	        final ItemStack derreter = e.getCurrentItem();
	        final ItemMeta derreterMeta = derreter.getItemMeta();
	        final ArrayList<String> derreterLore = new ArrayList<String>();

	        if (derreterMeta.getLore() != null) {
	            derreterLore.addAll(derreterMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (derreterLore.contains(m.getString("Sobrecarga.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Sobrecarga");
	            return;
	        }

	        derreterLore.add(m.getString("Sobrecarga.item").replace("&", "§"));
	        derreterMeta.setLore(derreterLore);
	        derreter.setItemMeta(derreterMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}
	
    @EventHandler
    public void Bater(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            final Player t = (Player)e.getDamager();
            if (t.getInventory().getItemInHand() != null && t.getInventory().getItemInHand().hasItemMeta() && t.getInventory().getItemInHand().getItemMeta().getLore().contains(m.getString("Sobrecarga.item").replace("&", "§")) && percentChance(m.getInt("Sobrecarga.Porcentagem") / 100)) {
                e.setDamage(e.getDamage() + e.getDamage() / 100.0 * m.getInt("Sobrecarga.Dano"));
            }
        }
    }
    
    public static Boolean percentChance(final double chance) {
        if (Math.random() <= chance) {
            return true;
        }
        return false;
    }
}
