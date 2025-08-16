package me.nd.factionsutils.listeners.tnt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Runnable extends BukkitRunnable {

    private static final Set<TNTPrimed> trackedTnt = ConcurrentHashMap.newKeySet();

    public static void track(TNTPrimed tnt) {
        trackedTnt.add(tnt);
    }

    @Override
    public void run() {
        Iterator<TNTPrimed> iterator = trackedTnt.iterator();

        while (iterator.hasNext()) {
            TNTPrimed tnt = iterator.next();

            // Verificação se ainda existe
            if (!tnt.isValid() || tnt.isDead()) {
                iterator.remove();
                continue;
            }

            // Marcar evento customizado uma única vez
            Location loc = tnt.getLocation();
            Bukkit.getPluginManager().callEvent(new EventosInstance(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), tnt));
            iterator.remove(); // Executa uma vez por TNT
        }
    }
}
