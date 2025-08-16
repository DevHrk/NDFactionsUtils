package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
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


public class DurezaListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Dureza.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        List<String> ferramentasPermitidas = Arrays.asList("_CHESTPLATE", "_BOOTS", "_HELMET", "_LEGGINGS");

	        if (!ferramentasPermitidas.stream().anyMatch(e.getCurrentItem().getType().toString()::endsWith)) {
	            p.sendMessage("§cEste encantamento serve apenas para a armadura");
	            return;
	        }

	        final ItemStack Dureza = e.getCurrentItem();
	        final ItemMeta DurezaMeta = Dureza.getItemMeta();
	        final ArrayList<String> DurezaLore = new ArrayList<String>();

	        if (DurezaMeta.getLore() != null) {
	            DurezaLore.addAll(DurezaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (DurezaLore.contains(m.getString("Dureza.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Protecao Divina");
	            return;
	        }

	        DurezaLore.add(m.getString("Dureza.item").replace("&", "§"));
	        DurezaMeta.setLore(DurezaLore);
	        Dureza.setItemMeta(DurezaMeta);

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
	        for (ItemStack item : p.getInventory().getArmorContents()) {
	            if (item != null && item.hasItemMeta() && item.getItemMeta().getLore().contains(m.getString("Dureza.item").replace("&", "§"))) {
	                // Chance de não perder durabilidade
	                double chance = m.getDouble("Dureza.Chance") / 100; // converte porcentagem para decimal
	                Random rand = new Random();
	                if (rand.nextDouble() < chance) {
	                    final short dura = item.getDurability();
	                    Bukkit.getScheduler().runTaskLater(Main.get(), new Runnable() {
	                        @Override
	                        public void run() {
	                            item.setDurability(dura);
	                        }
	                    }, 1L);
	                }
	            }
	        }
	    }
	}

}
