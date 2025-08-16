package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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


public class FuriaListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Furia.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_SWORD")) {
	            p.sendMessage("§cEste encantamento serve apenas para a espada");
	            return;
	        }

	        final ItemStack Furia = e.getCurrentItem();
	        final ItemMeta FuriaMeta = Furia.getItemMeta();
	        final ArrayList<String> FuriaLore = new ArrayList<String>();

	        if (FuriaMeta.getLore() != null) {
	            FuriaLore.addAll(FuriaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (FuriaLore.contains(m.getString("Furia.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Furia");
	            return;
	        }

	        FuriaLore.add(m.getString("Furia.item").replace("&", "§"));
	        FuriaMeta.setLore(FuriaLore);
	        Furia.setItemMeta(FuriaMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}

	@EventHandler
	public void onDamage(final EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
	    final Player p = (Player)e.getEntity();
	    final ItemStack sword = p.getInventory().getItemInHand();
	    if (sword != null && sword.hasItemMeta() && sword.getItemMeta().getLore().contains(m.getString("Furia.item").replace("&", "§"))) {
	        // Verifica se o jogador está sendo atacado
	        if (e.getDamager() instanceof LivingEntity && e.getDamager() != p) {
	            // Verifica se o jogador está atacando de volta
	            if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)p.getLastDamageCause()).getDamager() == e.getDamager()) {
	                // Verifica se há vários inimigos próximos
	                int enemiesNearby = 0;
	                for (Entity entity : p.getNearbyEntities(3, 3, 3)) {
	                    if (entity instanceof Player && entity != p) {
	                        enemiesNearby++;
	                    }
	                }
	                if (enemiesNearby >= m.getInt("Furia.Quantidade")) {
	                    // Causa dano em área
	                    for (Entity entity : p.getNearbyEntities(3, 3, 3)) {
	                        if (entity instanceof Player && entity != p) {
	                            ((Player)entity).damage(m.getInt("Furia.Dano"));
	                        }
	                    }
	                }
	            }
	        }
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
