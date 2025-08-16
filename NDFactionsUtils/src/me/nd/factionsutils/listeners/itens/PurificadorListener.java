package me.nd.factionsutils.listeners.itens;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.Incinerador;
import me.nd.factionsutils.itens.Purificador;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PurificadorListener implements Listener {
    FileConfiguration m = Main.get().getConfig();
    private final Map<UUID, BukkitRunnable> fireTasks = new HashMap<>();

    @EventHandler
    void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        SConfig m1 = Main.get().getConfig("Mensagens");
        
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
            if (p.getInventory().getItemInHand().isSimilar(Purificador.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                e.setCancelled(true);
                return;
            }
            if (p.getInventory().getItemInHand().isSimilar(Incinerador.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                e.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler
    void aoLancar(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {    
            Player p = (Player) e.getEntity().getShooter();
            if (e.getEntityType() == EntityType.SPLASH_POTION) {
                if (p.getItemInHand().isSimilar(Purificador.ARMADILHA)) {
                    e.getEntity().setCustomName(m.getString("Purificador.NomeJ").replace("&", "§"));
                    e.getEntity().setCustomNameVisible(true);
                    return;
                }
            
                if (p.getItemInHand().isSimilar(Incinerador.ARMADILHA)) {
                    e.getEntity().setCustomName(m.getString("Incinerador.NomeJ").replace("&", "§"));
                    e.getEntity().setCustomNameVisible(true);
                    return;
                }    
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    void aoAtingir(PotionSplashEvent e) {
        SConfig m1 = Main.get().getConfig("Mensagens");
        if (e.getEntity().isCustomNameVisible()) {
        	if (e.getEntity().getCustomName().equals(m.getString("Purificador.NomeJ").replace("&", "§"))) {
        	    // Add 50% chance
        	    if (Math.random() < 0.5) {
        	        for (LivingEntity le : e.getAffectedEntities()) {
        	            if (le instanceof Player) {
        	                Player p1 = (Player) le;
        	                Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
        	                    clearEffects(p1); // Remove potion effects
        	                    p1.setFireTicks(0); // Extinguish flames
        	                    // Cancel any existing fire task for this player
        	                    BukkitRunnable fireTask = fireTasks.remove(p1.getUniqueId());
        	                    if (fireTask != null) {
        	                        fireTask.cancel();
        	                    }
        	                }, 5);
        	                MessageUtils.send((Player) p1, m1.getStringList("Purificador.Title"));
        	            }
        	        }
        	    }
        	    return;
        	}

            if (e.getEntity().getCustomName().equals(m.getString("Incinerador.NomeJ").replace("&", "§"))) {
                for (LivingEntity le : e.getAffectedEntities()) {
                    if (le instanceof Player) {
                        Player p1 = (Player)le;
                        // Start a repeating task to keep the player burning
                        BukkitRunnable fireTask = new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!p1.isOnline() || p1.isDead()) {
                                    this.cancel(); // Stop the task if the player is offline or dead
                                    fireTasks.remove(p1.getUniqueId());
                                    return;
                                }
                                p1.setFireTicks(20 * 60); // Set fire for 60 seconds, refreshed every tick
                                p1.removePotionEffect(PotionEffectType.FIRE_RESISTANCE); // Remove fire resistance
                            }
                        };
                        // Store the fire task for this player
                        fireTasks.put(p1.getUniqueId(), fireTask);
                        fireTask.runTaskTimer(Main.get(), 0L, 1L); // Run every tick (1/20th of a second)
                        MessageUtils.send((Player) p1, m1.getStringList("Incinerador.Title"));
                       }
                }
                return;
            }
        }
    }

    private void clearEffects(LivingEntity le) {
        for (PotionEffect pe : le.getActivePotionEffects()) {
            le.removePotionEffect(pe.getType());
        }
    }
}