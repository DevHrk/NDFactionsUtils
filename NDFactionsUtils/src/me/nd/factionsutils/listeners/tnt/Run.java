package me.nd.factionsutils.listeners.tnt;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factions.Main;

public class Run
extends BukkitRunnable {
    private final Block block;
    public final TntListenner eventos;

    public void run() {
        this.block.removeMetadata("CanhaoTnT", (Plugin)Main.get());
    }

    public Run(TntListenner eventos, Block block) {
        this.eventos = eventos;
        this.block = block;
    }
}
