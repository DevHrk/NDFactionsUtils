package me.nd.factionsutils.listeners.itens;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.Armadilha;
import me.nd.factionsutils.plugin.SConfig;

public class ArmadilhaListener implements Listener {
	  
	 FileConfiguration m = Main.get().getConfig(); 
	@EventHandler
	   void onClick2(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		SConfig m1 = Main.get().getConfig("Mensagens");
			if (p.getInventory().getItemInHand().isSimilar(Armadilha.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
			    p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
			    e.setCancelled(true);
			}
	}
	
	@EventHandler
	 void aoAcertarProjetil(ProjectileHitEvent e) {
		if (e.getEntityType() == EntityType.SNOWBALL) {
			if (e.getEntity().isCustomNameVisible()) {
				executeTrap(e.getEntity().getLocation());
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	 void aoTomarDano(EntityDamageByEntityEvent e) {
		if (e.getDamager().getType() == EntityType.SNOWBALL) {
			if (e.getDamager().isCustomNameVisible()) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler
	 void aoLancar(ProjectileLaunchEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {	
			Player p = (Player) e.getEntity().getShooter();

			if (e.getEntityType() == EntityType.SNOWBALL) {
				if (p.getItemInHand().isSimilar(Armadilha.ARMADILHA)) {
					e.getEntity().setCustomName("§bARMADILHA");
					e.getEntity().setCustomNameVisible(true);
					return;
				}
			}
		}
	}
	public static HashMap<Player, Integer> PROTECTEDS = new HashMap<>();

	@EventHandler(ignoreCancelled = true)
	 void aoTomarDando(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			if (e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if (PROTECTEDS.containsKey(p)) {
					e.setCancelled(true);
					Integer task = PROTECTEDS.get(p);
					Bukkit.getScheduler().cancelTask(task);
					PROTECTEDS.remove(p);
				}
			}
		}
	}
	
	private void executeTrap(Location l) {
		for (double x = l.getX() - 1; x <= l.getX() + 1; x++) {
			for (double z = l.getZ() - 1; z <= l.getZ() + 1; z++) {
				Block b = new Location(l.getWorld(), x, l.getY(), z).getBlock();
				if (b.getType() == Material.AIR) {
					b.setType(Material.WEB);
				}
			}
		}
		Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
				for (double x = l.getX() - 1; x <= l.getX() + 1; x++) {
					for (double z = l.getZ() - 1; z <= l.getZ() + 1; z++) {
						Block b = new Location(l.getWorld(), x, l.getY(), z).getBlock();
						if (b.getType() == Material.WEB) {
							b.setType(Material.AIR);
						}
					}
			}
		},20L * m.getInt("Armadilha.DuracaoDasTeias"));
	}
}