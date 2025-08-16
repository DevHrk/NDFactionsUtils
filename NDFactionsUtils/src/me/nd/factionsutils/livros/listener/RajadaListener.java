package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class RajadaListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Rajada.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("BOW")) {
	            p.sendMessage("§cEste encantamento serve apenas para o arco");
	            return;
	        }

	        final ItemStack Rajada = e.getCurrentItem();
	        final ItemMeta RajadaMeta = Rajada.getItemMeta();
	        final ArrayList<String> RajadaLore = new ArrayList<String>();

	        if (RajadaMeta.getLore() != null) {
	            RajadaLore.addAll(RajadaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (RajadaLore.contains(m.getString("Rajada.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Rajadas");
	            return;
	        }

	        RajadaLore.add(m.getString("Rajada.item").replace("&", "§"));
	        RajadaMeta.setLore(RajadaLore);
	        Rajada.setItemMeta(RajadaMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}

	@EventHandler
	public void onShoot(final EntityShootBowEvent e) {
	    final Player p = (Player)e.getEntity();
	    final ItemStack bow = p.getInventory().getItemInHand();
	    if (bow != null && bow.hasItemMeta() && bow.getItemMeta().getLore().contains(m.getString("Rajada.item").replace("&", "§")) && percentChance(m.getInt("Rajada.Porcentagem") / 100.0)) {
	        // Dispara uma rajada de flechas
	        for (int i = 0; i < m.getInt("Rajada.Quantidade"); i++) { // Quantidade de flechas na rajada
	            final Arrow arrow = p.launchProjectile(Arrow.class);
	            arrow.setVelocity(arrow.getVelocity().multiply(1.5)); // Velocidade da flecha
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
