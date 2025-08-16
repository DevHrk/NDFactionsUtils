package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
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

public class SuperAreaListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("SuperArea.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_PICKAXE")) {
	            p.sendMessage("§cEste encantamento serve apenas para picaretas");
	            return;
	        }

	        final ItemStack SuperArea = e.getCurrentItem();
	        final ItemMeta SuperAreaMeta = SuperArea.getItemMeta();
	        final ArrayList<String> SuperAreaLore = new ArrayList<String>();

	        if (SuperAreaMeta.getLore() != null) {
	            SuperAreaLore.addAll(SuperAreaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (SuperAreaLore.contains(m.getString("SuperArea.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro SuperArea");
	            return;
	        }

	        SuperAreaLore.add(m.getString("SuperArea.item").replace("&", "§"));
	        SuperAreaMeta.setLore(SuperAreaLore);
	        SuperArea.setItemMeta(SuperAreaMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}
	
    
	@EventHandler
	public void onBreak(final BlockBreakEvent e) {
	    final Player p = e.getPlayer();
	    final ItemStack item = p.getInventory().getItemInHand();

	    if (item == null || item.getType() == Material.AIR) {
	        return;
	    }

	    final ItemMeta itemMeta = item.getItemMeta();
	    final List<String> lore = itemMeta.getLore();

	    if (lore == null || !lore.contains(m.getString("SuperArea.item").replace("&", "§"))) {
	        return;
	    }

	    final Block block = e.getBlock();
	    final Material material = block.getType();

	    // Verifica se o bloco é quebrável
	    if (!material.isBlock() || material == Material.BEDROCK || material == Material.OBSIDIAN) {
	        return;
	    }
	    
	    // Verifica se a habilidade irá acontecer
	    final Random random = new Random();
	    final int chance = random.nextInt(100);
	    if (chance > m.getInt("SuperArea.Chance")) {
	        return;
	    }

	    // Quebra os blocos próximos
	    for (int x = -m.getInt("SuperArea.Lado"); x <= m.getInt("SuperArea.Lado"); x++) {
	        for (int y = -m.getInt("SuperArea.Cima"); y <= m.getInt("SuperArea.Cima"); y++) {
	            for (int z = -m.getInt("SuperArea.Lado"); z <= m.getInt("SuperArea.Lado"); z++) {
	                final Block nearbyBlock = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);

	                if (nearbyBlock.getType() == material) {
	                    nearbyBlock.breakNaturally(item);
	                }
	            }
	        }
	    }
	}
}
