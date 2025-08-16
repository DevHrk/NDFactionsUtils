package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;

import org.bukkit.Material;
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


public class ExecucaoListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Execucao.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_SWORD")) {
	            p.sendMessage("§cEste encantamento serve apenas para a espada");
	            return;
	        }

	        final ItemStack Execucao = e.getCurrentItem();
	        final ItemMeta ExecucaoMeta = Execucao.getItemMeta();
	        final ArrayList<String> ExecucaoLore = new ArrayList<String>();

	        if (ExecucaoMeta.getLore() != null) {
	            ExecucaoLore.addAll(ExecucaoMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (ExecucaoLore.contains(m.getString("Execucao.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Execucao");
	            return;
	        }

	        ExecucaoLore.add(m.getString("Execucao.item").replace("&", "§"));
	        ExecucaoMeta.setLore(ExecucaoLore);
	        Execucao.setItemMeta(ExecucaoMeta);

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
	    if (!(e.getDamager() instanceof Player)) {
	        return; // Sai do método se o damager não for um jogador
	    }
		final Player p = (Player)e.getDamager();
	    final ItemStack sword = p.getInventory().getItemInHand();

	    if (sword != null && sword.hasItemMeta() && sword.getItemMeta().getLore().contains(m.getString("Execucao.item").replace("&", "§"))&& percentChance(m.getInt("Execucao.Porcentagem") / 100.0)) {
	        final LivingEntity target = (LivingEntity)e.getEntity();
	        if (target.getHealth() - e.getDamage() <= m.getDouble("Execucao.VidaMinima")) {
	            target.setHealth(0);
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
