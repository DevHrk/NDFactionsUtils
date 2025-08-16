package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.block.*;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.LauncherItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.inventory.*;

import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.metadata.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.util.*;

import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;

public class LauncherListener implements Listener
{
	
	FileConfiguration m = Main.get().getConfig(); 
     
    @EventHandler
    void onInteract(PlayerInteractEvent e) {
        SConfig m1 = Main.get().getConfig("Mensagens");
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getInventory().getItemInHand();

        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) return;

        if (itemInHand.isSimilar(LauncherItem.ARMADILHA)) {
            if (!Utils.isZonaProtegida(p.getLocation())) {
                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getY() < 220.0) {
                if (p.hasMetadata("launcherCooldown")) {
                    Integer remainingTime = (Integer) p.getMetadata("launcherCooldown").get(0).value();
                    MessageUtils.send(p, m1.getStringList("Lancador.cooldown").stream().map(message -> message.replace("{tempo}", String.valueOf(remainingTime))).collect(Collectors.toList()));
                    e.setCancelled(true);
                    return;
                }

                int cooldownTime = m.getInt("Lancador.Tempo"); // 30 seconds cooldown
                p.setMetadata("launcherCooldown", new FixedMetadataValue(Main.get(), cooldownTime));
                BukkitRunnable cooldownRunnable = new BukkitRunnable() {
                    int remainingTime = cooldownTime;
                    @Override
                    public void run() {
                        if (remainingTime <= 0) {
                            p.removeMetadata("launcherCooldown", Main.get());
                            this.cancel();
                            return;
                        }
                        remainingTime--;
                        p.setMetadata("launcherCooldown", new FixedMetadataValue(Main.get(), remainingTime));
                    }
                };
                cooldownRunnable.runTaskTimer(Main.get(), 0, 20); // update every 1 second

                // Now, execute the impulsion action
                ItemStack laucherItem = LauncherItem.get().getItem();
                ItemMeta laucherMeta = laucherItem.getItemMeta();
                Vector vel = p.getVelocity().setY(m.getInt("Lancador.Velocidade"));

                laucherMeta.setDisplayName(m.getString("Lancador.Nome").replace("&", "§"));
                laucherItem.setItemMeta(laucherMeta);

                e.setCancelled(true);
                p.playSound(p.getLocation(), Sound.ITEM_BREAK, 20.0f, 20.0f);
                p.playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 5.0f, 1.0f);
                p.setVelocity(vel);
                MessageUtils.send(p, m1.getStringList("Lancador.acabou"));
                API.removeItem(p.getPlayer());

                p.setMetadata("launcher", new FixedMetadataValue(Main.get(), true));
            }
        }
    }
	
    @EventHandler
     void onDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            Player player = (Player)e.getEntity();
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL && player.hasMetadata("launcher") && ((MetadataValue)player.getMetadata("launcher").get(0)).asBoolean()) {
                e.setCancelled(true);
                e.setDamage(0.0);
                player.removeMetadata("launcher", (Plugin)Main.get());
            }
        }
    }

    @EventHandler
     void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("launcher")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
     void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("launcher")) {
            player.removeMetadata("launcher", (Plugin)Main.get());
        }
    }
}

