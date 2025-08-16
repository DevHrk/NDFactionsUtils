package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class DerreterListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Derreter.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_PICKAXE")) {
	            p.sendMessage("§cEste encantamento serve apenas para picaretas");
	            return;
	        }

	        final ItemStack derreter = e.getCurrentItem();
	        final ItemMeta derreterMeta = derreter.getItemMeta();
	        final ArrayList<String> derreterLore = new ArrayList<String>();

	        if (derreterMeta.getLore() != null) {
	            derreterLore.addAll(derreterMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (derreterLore.contains(m.getString("Derreter.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Derreter");
	            return;
	        }

	        derreterLore.add(m.getString("Derreter.item").replace("&", "§"));
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
	public void onBreak2(final BlockBreakEvent e) {
	    final Block bloco = e.getBlock();
	    final Player p = e.getPlayer();
	    final ItemStack item = p.getInventory().getItemInHand();

	    // Check if item is null, has item meta, and m is not null
	    if (item == null || !item.hasItemMeta() || item.getItemMeta() == null || m == null) {
	        return; // Exit early if any of these are null or invalid
	    }

	    // Get the lore string from config and check if it's null
	    String loreString = m.getString("Derreter.item");
	    if (loreString == null) {
	        return; // Exit if the config value is null
	    }
	    loreString = loreString.replace("&", "§");

	    // Check if item has lore and if it contains the configured string
	    if (item.getItemMeta().getLore() != null && item.getItemMeta().getLore().contains(loreString)) {
	        if (bloco.getType() == Material.IRON_ORE) {
	            e.setCancelled(true);
	            bloco.setType(Material.AIR);

	            int quantidade = 1;
	            if (item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) > 0) {
	                quantidade += item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
	            }

	            p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.IRON_INGOT, quantidade) });
	        } else if (bloco.getType() == Material.GOLD_ORE) {
	            e.setCancelled(true);
	            bloco.setType(Material.AIR);

	            int quantidade = 1;
	            if (item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) > 0) {
	                quantidade += item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
	            }

	            p.getInventory().addItem(new ItemStack[] { new ItemStack(Material.GOLD_INGOT, quantidade) });
	        }
	    }
	}
}
