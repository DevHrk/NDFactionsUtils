package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class RagnarokListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	public ArrayList<String> Ragnarok = new ArrayList<>();
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("Ragnarok.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        if (!e.getCurrentItem().getType().toString().endsWith("_AXE")) {
	            p.sendMessage("§cEste encantamento serve apenas para machados");
	            return;
	        }

	        final ItemStack derreter = e.getCurrentItem();
	        final ItemMeta derreterMeta = derreter.getItemMeta();
	        final ArrayList<String> derreterLore = new ArrayList<String>();

	        if (derreterMeta.getLore() != null) {
	            derreterLore.addAll(derreterMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (derreterLore.contains(m.getString("Ragnarok.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Ragnarok");
	            return;
	        }

	        derreterLore.add(m.getString("Ragnarok.item").replace("&", "§"));
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
	public void AoMudar(final PlayerItemHeldEvent e) {
	    final Player p = e.getPlayer();
	    final ItemStack item = p.getInventory().getItem(e.getNewSlot());

	    if (item != null && item.hasItemMeta()) {
	        final ItemMeta meta = item.getItemMeta();
	        final String ragnarokItem = m.getString("Ragnarok.item");
	        if (ragnarokItem != null) {
	            final String ragnarokItemFormatted = ragnarokItem.replace("&", "§");
	            if (meta.getLore() != null && meta.getLore().contains(ragnarokItemFormatted)) {
	                final Integer speedLevel = m.getInt("Ragnarok.Speed_Level");
	                final Integer strenghtLevel = m.getInt("Ragnarok.Strenght_Level");
	                if (speedLevel != null && strenghtLevel != null) {
	                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speedLevel - 1));
	                    p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, strenghtLevel - 1));
	                    Ragnarok.add(p.getName());
	                    return;
	                }
	            }
	        }
	    }
	    removerEfeitos(p);
	}

	private void removerEfeitos(Player p) {
	    if (Ragnarok.contains(p.getName())) {
	        p.removePotionEffect(PotionEffectType.SPEED);
	        p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
	        Ragnarok.remove(p.getName());
	    }
	}
    
    
}
