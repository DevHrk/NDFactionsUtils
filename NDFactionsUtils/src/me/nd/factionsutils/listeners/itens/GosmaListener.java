package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.api.ActionbarAPI;
import me.nd.factionsutils.itens.GosmaItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class GosmaListener implements Listener {
	FileConfiguration m = Main.get().getConfig(); 
    public static ArrayList<String> frozen = new ArrayList<String>();
    
    private final Map<Player, Long> cooldowns = new HashMap<>();

    @EventHandler
    void aoTomarDando(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();
        ItemStack hand = p.getInventory().getItemInHand();
        SConfig m1 = Main.get().getConfig("Mensagens");

        if (!hand.isSimilar(GosmaItem.GOSMA)) {
            return;
        }

        if (!Utils.isZonaProtegida(p.getLocation())) {
            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        if (!hand.getItemMeta().hasEnchants()) {
            return;
        }

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        long cooldownEnd = cooldowns.getOrDefault(p.getName(), 0L);
        long cooldownDuration = 8000L; // 8 seconds in milliseconds

        if (currentTime < cooldownEnd) {
            long remainingSeconds = (cooldownEnd - currentTime) / 1000L;
            ActionbarAPI.sendActionBarMessage(p, "§cDelay de §f" +String.valueOf(remainingSeconds) +"s §cPara utilizar novamente");
            e.setCancelled(true);
            return;
        }

        // Set new cooldown
        cooldowns.put(p, currentTime + cooldownDuration);

        API.removeItem(p);
        frozen.add(damaged.getName());
        // Apply strong slowness effect for Spigot 1.8.9
        damaged.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * m.getInt("Gosma.DuracaoFreeze"), 255));
        MessageUtils.send(damaged, m1.getStringList("GosmaMaligna.Tittle"));

        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            frozen.remove(damaged.getName());
            // Remove slowness effect when unfrozen
            damaged.removePotionEffect(PotionEffectType.SLOW);
            MessageUtils.send(damaged, m1.getStringList("GosmaMaligna.Saiu"));
        }, 20L * m.getInt("Gosma.DuracaoFreeze"));
    }
}
	