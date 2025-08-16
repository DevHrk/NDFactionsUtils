package me.nd.factionsutils.listeners.itens;

import org.bukkit.entity.*;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.SaveLifeItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class SaveLifeListener  implements Listener
{
	FileConfiguration m = Main.get().getConfig(); 
	@EventHandler
	void onDamage(EntityDamageByEntityEvent event) {
	    SConfig m1 = Main.get().getConfig("Mensagens");

	    if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
	        return;
	    }

	    if (event.isCancelled()) {
	        return;
	    }

	    Player player = (Player) event.getEntity();
	    ItemStack skull = player.getInventory().getHelmet();

	    if (skull == null || skull.getType() != Material.SKULL_ITEM || !skull.hasItemMeta()) {
	        return;
	    }

	    ItemMeta skullMeta = skull.getItemMeta();
	    String name = skullMeta.getDisplayName();

	    // Add null check for name
	    if (name == null || !name.startsWith("§f(§a") || !name.endsWith("§f) §cSalva-vida")) {
	        return;
	    }

	    int usos = 0;
	    // Assuming m is defined, e.g., Main.get().getConfig()
	    FileConfiguration m = Main.get().getConfig(); // Ensure m is defined correctly
	    usos = Character.getNumericValue(name.charAt(m.getInt("Salva-Vidas.Limite")));

	    if (usos > m.getInt("Salva-Vidas.Termina")) {
	        usos--;
	        skullMeta.setDisplayName("§f(§a" + usos + "§f) §cSalva-vida");
	        List<String> messages = replaceUsos(m1.getStringList("Salva-Vidas.Usos"), usos);
	        MessageUtils.send(player, messages);
	    } else if (usos == m.getInt("Salva-Vidas.Termina")) {
	        player.getInventory().setHelmet(null);
	        MessageUtils.send(player, m1.getStringList("Salva-Vidas.Destruido"));
	        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 20.0f, 20.0f);
	    }

	    skull.setItemMeta(skullMeta);

	    Location location = player.getLocation();
	    player.getWorld().createExplosion(location, (float) m.getDouble("Salva-Vidas.Raio"), false);

	    List<Player> players = player.getWorld().getPlayers();
	    for (Player target : players) {
	        if (location.distance(target.getLocation()) <= m.getDouble("Salva-Vidas.Raio")) {
	            target.damage(m.getDouble("Salva-Vidas.DanoExplosion"));
	        }
	    }
	}
	public List<String> replaceUsos(List<String> messages, int usos) {
	    return messages.stream().map(message -> message.replace("{usos}", "" + usos)).collect(Collectors.toList());
	}
	  @EventHandler
	     void onExplode(EntityDamageEvent event) {
	        if (!(event.getEntity() instanceof Player)) {
	            return;
	        }
	        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
	            return;
	        }
	         Player player = (Player)event.getEntity();
	        if (player.getInventory().getHelmet() == null || !player.getInventory().getHelmet().hasItemMeta() || !player.getInventory().getHelmet().isSimilar(SaveLifeItem.ARMADILHA)) {
	            return;
	        }
	        event.setDamage(m.getInt("Salva-Vidas.Dano"));
	        event.setCancelled(true);
	    }
	}