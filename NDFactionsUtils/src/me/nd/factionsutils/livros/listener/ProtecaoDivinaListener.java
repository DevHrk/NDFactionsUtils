package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class ProtecaoDivinaListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    final Player p = (Player)e.getWhoClicked();
	    if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && e.getCursor().getType().equals((Object)Material.BOOK) && e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(m.getString("ProtecaoDivina.Nome").replace("&", "§"))) {
	        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
	            return;
	        }

	        List<String> ferramentasPermitidas = Arrays.asList("_CHESTPLATE", "_BOOTS", "_HELMET", "_LEGGINGS");

	        if (!ferramentasPermitidas.stream().anyMatch(e.getCurrentItem().getType().toString()::endsWith)) {
	            p.sendMessage("§cEste encantamento serve apenas para a armadura");
	            return;
	        }

	        final ItemStack ProtecaoDivina = e.getCurrentItem();
	        final ItemMeta ProtecaoDivinaMeta = ProtecaoDivina.getItemMeta();
	        final ArrayList<String> ProtecaoDivinaLore = new ArrayList<String>();

	        if (ProtecaoDivinaMeta.getLore() != null) {
	            ProtecaoDivinaLore.addAll(ProtecaoDivinaMeta.getLore());
	        }

	        // Verifica se o item já foi encantado com o livro
	        if (ProtecaoDivinaLore.contains(m.getString("ProtecaoDivina.item").replace("&", "§"))) {
	            p.sendMessage("§cEste item já foi encantado com o livro Protecao Divina");
	            return;
	        }

	        ProtecaoDivinaLore.add(m.getString("ProtecaoDivina.item").replace("&", "§"));
	        ProtecaoDivinaMeta.setLore(ProtecaoDivinaLore);
	        ProtecaoDivina.setItemMeta(ProtecaoDivinaMeta);

	        if (e.getCursor().getAmount() > 1) {
	            e.getCursor().setAmount(e.getCursor().getAmount() - 1);
	        } else {
	            e.setCursor(new ItemStack(Material.AIR));
	        }

	        e.setCancelled(true);
	    }
	}
	
	public static Map<Player, Integer> ataquesSimultaneos = new HashMap<>();

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
	    if (e.getEntity() instanceof Player) {
	        Player p = (Player) e.getEntity();
	        ItemStack armor = p.getInventory().getChestplate();
	        if (armor != null && armor.getItemMeta() != null && armor.getItemMeta().getLore() != null) {
	            List<String> lore = armor.getItemMeta().getLore();
	            if (lore.contains(m.getString("ProtecaoDivina.item").replace("&", "§"))) {
	                int quantidadeAtaques = ataquesSimultaneos.getOrDefault(p, 0) + 1;
	                ataquesSimultaneos.put(p, quantidadeAtaques);
	                if (quantidadeAtaques >= m.getInt("ProtecaoDivina.AtacksNecessarios")) { // ajuste o número de ataques simultâneos aqui
	                    int resistencia = 0;
	                    for (PotionEffect effect : p.getActivePotionEffects()) {
	                        if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
	                            resistencia = effect.getAmplifier();
	                            break;
	                        }
	                    }
	                    if (resistencia > 0) {
	                        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, resistencia + 1));
	                    } else {
	                        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, m.getInt("ProtecaoDivina.Resistencia_Level") - 1));
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
