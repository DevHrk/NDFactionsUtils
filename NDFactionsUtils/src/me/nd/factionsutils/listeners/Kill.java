package me.nd.factionsutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Kill {

    private final JavaPlugin plugin;

    public Kill(JavaPlugin plugin) {
        this.plugin = plugin;
        // Defer scheduling to ensure plugin is enabled
        scheduleCommands();
    }

    private void scheduleCommands() {
        // Lista de comandos a serem executados pelo console
        String[] commands = {
            "boss butcher EnDs General",
            "boss butcher EnDs General1",
            "boss butcher EnDs General2",
            "boss butcher EnDs Comandante",
            "boss butcher EnDs Comandante1"
        };

        // Agendar tarefa para rodar a cada 20 minutos (20 minutos = 20 * 60 * 20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }.runTaskTimer(plugin, 20L, 20 * 60 * 20L); // 20L = pequeno delay inicial, 20*60*20L = 20 minutos em ticks
    }
}