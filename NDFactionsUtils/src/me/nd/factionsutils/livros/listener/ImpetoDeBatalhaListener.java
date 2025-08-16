package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


public class ImpetoDeBatalhaListener implements Listener {
	
	public static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("ImpetoDeBatalha.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_SWORD")) {
	            p.sendMessage("§cEste encantamento serve apenas para a espada");
	            return;
	        }

	        final ItemStack ImpetoDeBatalha = e.getCurrentItem();
	        final ItemMeta ImpetoDeBatalhaMeta = ImpetoDeBatalha.getItemMeta();
	        final ArrayList<String> ImpetoDeBatalhaLore = new ArrayList<String>();

	        if (ImpetoDeBatalhaMeta.getLore() != null) {
	            ImpetoDeBatalhaLore.addAll(ImpetoDeBatalhaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (ImpetoDeBatalhaLore.contains(m.getString("ImpetoDeBatalha.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro ImpetoDeBatalha");
	            return;
	        }

	        ImpetoDeBatalhaLore.add(m.getString("ImpetoDeBatalha.item").replace("&", "§"));
	        ImpetoDeBatalhaMeta.setLore(ImpetoDeBatalhaLore);
	        ImpetoDeBatalha.setItemMeta(ImpetoDeBatalhaMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}
	
	public static Map<Player, Integer> ataques = new HashMap<>();
	public static Map<Player, Long> ultimoAtaque = new HashMap<>();

	@EventHandler
	public void onAttack(final EntityDamageByEntityEvent e) {
	    if (!(e.getDamager() instanceof Player)) {
	        return;
	    }

	    final Player p = (Player) e.getDamager();
	    final ItemStack item = p.getInventory().getItemInHand();

	    if (item == null || item.getType() == Material.AIR) {
	        return;
	    }

	    final ItemMeta itemMeta = item.getItemMeta();
	    final List<String> lore = itemMeta.getLore();

	    if (lore == null || !lore.contains(m.getString("ImpetoDeBatalha.item").replace("&", "§"))) {
	        return;
	    }

	    if (!ataques.containsKey(p)) {
	        ataques.put(p, 0);
	    }

	    if (!ultimoAtaque.containsKey(p)) {
	        ultimoAtaque.put(p, System.currentTimeMillis());
	    }

	    ataques.put(p, ataques.get(p) + 1);
	    ultimoAtaque.put(p, System.currentTimeMillis());

	    if (ataques.get(p) >= m.getInt("ImpetoDeBatalha.AtacksNecessarios")) {
	        final double danoAdicional = m.getDouble("ImpetoDeBatalha.DanoAdicional");
	        e.setDamage(e.getDamage() + danoAdicional);
	        ataques.put(p, 0);
	    }
	}

    
}
