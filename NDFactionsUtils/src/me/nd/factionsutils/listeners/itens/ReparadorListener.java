package me.nd.factionsutils.listeners.itens;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.Agrupador;
import me.nd.factionsutils.itens.Reparador;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class ReparadorListener implements Listener  {
	 private static final FileConfiguration m = Main.get().getConfig(); 
	 
	 @EventHandler
	 void aoClicar(BlockPlaceEvent e) {
		 Player p = e.getPlayer();
		    if (p.getInventory().getItemInHand().isSimilar(Reparador.REPARADOR) || p.getInventory().getItemInHand().isSimilar(Agrupador.AGRUPADOR)) {
		        e.setCancelled(true);
		        return;
		    }
	 }
	 
	@EventHandler
	 void aoClicar(PlayerInteractEvent e) {
		SConfig m1 = Main.get().getConfig("Mensagens");
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

			if (e.getItem() == null) return;
			Player p = e.getPlayer();
			boolean isZNP = Utils.isZonaProtegida(p.getLocation());
			boolean agrupador = p.getInventory().getItemInHand().isSimilar(Agrupador.AGRUPADOR);
			boolean reparador = p.getInventory().getItemInHand().isSimilar(Reparador.REPARADOR);
			
			if (reparador && !isZNP) {
				p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
				e.setCancelled(true);
				return;
			}
			
			if (agrupador && !isZNP) {
				p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
				e.setCancelled(true);
				return;
			}
			
			if (reparador) {
			    List<ItemStack> armors = Arrays.asList(p.getInventory().getArmorContents());
			    double threshold = m.getDouble("Reparador.Porcentagem");
			    Optional<ItemStack> armorToRepair = armors.stream().filter(armor -> armor != null && armor.getType() != Material.AIR).filter(armor -> armor.getDurability() > armor.getType().getMaxDurability() * threshold).findFirst();
			    if (armorToRepair.isPresent()) {
			        repairArmor(p);
			        MessageUtils.send(p, m1.getStringList("Reparador.Reparado"));
			        API.removeItem(e.getPlayer());
			        e.setCancelled(true);
			    } else {
			        String messageKey = armors.stream().anyMatch(armor -> armor != null && armor.getType() != Material.AIR) ? "Reparador.TotalmenteReparada" : "Reparador.SemArmadura";
			        MessageUtils.send(p, m1.getStringList(messageKey));
			    }
			}
			
			if (agrupador && p.getInventory().contains(Material.POTION)) {
				agroupPotions(e.getPlayer());
				API.removeItem(e.getPlayer());
				MessageUtils.send(p, m1.getStringList("Agrupador.Agrupado"));
				e.setCancelled(true);
			} else if(agrupador) {
				MessageUtils.send(p, m1.getStringList("Agrupador.Nao-Ha"));
				e.setCancelled(true);
			}
		}
	}
	
	public static void agroupPotions(Player p) {
		PlayerInventory inv = p.getInventory();
		Map<ItemStack, Integer> potions = new HashMap<>();
		Arrays.stream(inv.getContents()).filter(Objects::nonNull).filter(item -> item.getType() == Material.POTION).forEach(item -> {
		        int amount = item.getAmount();
		        item.setAmount(1);
		        potions.merge(item, amount, Integer::sum);
		        inv.removeItem(item);
		    });

		potions.forEach((potion, amount) -> {
		    int limit = m.getInt("Agrupador_De_Pocoes.Limite");
		    while (amount > limit) {
		        potion.setAmount(limit);
		        inv.addItem(potion);
		        amount -= limit;
		    }
		    potion.setAmount(amount);
		    inv.addItem(potion);
		});
		p.playEffect(p.getLocation(), Effect.POTION_BREAK, 0);
	}
	
	public static void repairArmor(Player p) {
		ItemStack[] armadura = p.getInventory().getArmorContents();
		Arrays.stream(armadura).filter(Objects::nonNull).filter(i -> i.getType().getMaxDurability() != 0).forEach(i -> i.setDurability((short) 0));
		p.updateInventory();
		p.playSound(p.getLocation(), Sound.valueOf("ANVIL_USE"), 1, 1);
	}
	
}
