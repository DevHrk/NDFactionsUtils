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

public class ShieldListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Campo_de_Força.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_CHESTPLATE")) {
	            p.sendMessage("§cEste encantamento serve apenas para o peitoral");
	            return;
	        }

	        final ItemStack Campo_de_Força = e.getCurrentItem();
	        final ItemMeta Campo_de_ForçaMeta = Campo_de_Força.getItemMeta();
	        final ArrayList<String> Campo_de_ForçaLore = new ArrayList<String>();

	        if (Campo_de_ForçaMeta.getLore() != null) {
	            Campo_de_ForçaLore.addAll(Campo_de_ForçaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (Campo_de_ForçaLore.contains(m.getString("Campo_de_Força.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Campo de Força");
	            return;
	        }

	        Campo_de_ForçaLore.add(m.getString("Campo_de_Força.item").replace("&", "§"));
	        Campo_de_ForçaMeta.setLore(Campo_de_ForçaLore);
	        Campo_de_Força.setItemMeta(Campo_de_ForçaMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}

    @EventHandler
    public void Bater2(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            final Player p = (Player)e.getEntity();
            if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().hasItemMeta() && p.getInventory().getChestplate().getItemMeta().getLore().contains(m.getString("Campo_de_Força.item").replace("&", "§")) && percentChance(m.getInt("Campo_de_Força.Porcentagem") / 100)) {
                e.setDamage(0.0);
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
