package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.scheduler.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.MeteorItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MeteorListener implements Listener {
    FileConfiguration m = Main.get().getConfig();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getInventory().getItemInHand();
        SConfig m1 = Main.get().getConfig("Mensagens");

        if (itemInHand == null || !itemInHand.isSimilar(MeteorItem.ARMADILHA) || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = e.getClickedBlock();
        Location location = block.getLocation();
        NDPlayer mp = DataManager.players.get(p.getName());
        Terra terra = new Terra(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
        NDFaction chunkFaction = getFactionOwningTerra(terra);

        if (!Utils.isZonaProtegida(p.getLocation())) {
            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        if (!Utils.isZonaLivre(p.getLocation())) {
            p.sendMessage(m1.getString("Mensagens.TerritorioLivre").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        NDFaction playerFaction = mp.getFaction();
        if (chunkFaction != null && chunkFaction.equals(playerFaction)) {
            MessageUtils.send(p, m1.getStringList("Meteoro.Territorio"));
            e.setCancelled(true);
            return;
        }

        MessageUtils.send(p, m1.getStringList("Meteoro.Utilizado"));
        API.removeItem(p);

        if (chunkFaction != null) {
            for (Player onlinePlayer : chunkFaction.getAllOnline()) {
                MessageUtils.send(onlinePlayer, m1.getStringList("Meteoro.Tittle"));
            }
        }
        
        int meteorTime = m.getInt("Meteoro.Setting.Tempo");
        Map<Integer, Integer> tntSpawns = m.getStringList("Meteoro.Setting.TnTSpawn")
                .stream()
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(parts -> Integer.parseInt(parts[0]), parts -> Integer.parseInt(parts[1])));

        new BukkitRunnable() {
            int time = meteorTime;

            public void run() {
                time--;
                if (tntSpawns.containsKey(time)) {
                    location.getWorld().strikeLightningEffect(location);
                    IntStream.range(0, tntSpawns.get(time)).forEach(i -> location.getWorld().spawnEntity(location.add(0.5, 0.0, 0.5), EntityType.PRIMED_TNT));
                }
                if (time == 0) {
                    location.getWorld().strikeLightning(location);
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 0L, 20L);

        e.setCancelled(true);
    }

    private NDFaction getFactionOwningTerra(Terra terra) {
        for (NDFaction faction : DataManager.factions.values()) {
            if (faction.ownsTerritory(terra)) {
                return faction;
            }
        }
        return null;
    }
}