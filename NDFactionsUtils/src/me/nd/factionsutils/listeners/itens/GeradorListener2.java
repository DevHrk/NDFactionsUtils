package me.nd.factionsutils.listeners.itens;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.dados.GeradorDAO;
import me.nd.factionsutils.gen.Gerador;
import me.nd.factionsutils.hologram.StringUtils;

import java.util.List;

public class GeradorListener2 extends BukkitRunnable {

    private final Main plugin = Main.get();
    private final Material tipoGerador;
    private final int colocarX;
    private final int larguraX;
    private final int colocarZ;
    private final int larguraZ;

    private int y = 0;

    public GeradorListener2() {
        // Cache das configs na construção da task
        tipoGerador = Material.getMaterial(plugin.getConfig().getString("Gerador.settings.tipo", "QUARTZ_BLOCK"));
        colocarX = plugin.getConfig().getInt("Gerador.settings.ColocarX");
        larguraX = plugin.getConfig().getInt("Gerador.settings.ColocarLarguraX");
        colocarZ = plugin.getConfig().getInt("Gerador.settings.ColocarZ");
        larguraZ = plugin.getConfig().getInt("Gerador.settings.ColocarLarguraZ");
    }

    @Override
    public void run() {
        if (tipoGerador == null) return;

        y = (y + 1) % 4; // Ciclo de 0 a 3

        List<Gerador> geradores = GeradorDAO.getallGeradores(); // Suponho que retorne Gerador

        for (Gerador gerador : geradores) {
            Location baseLoc = StringUtils.deserializeLocation(gerador.getLocation()); // Cuidado: isso poderia estar cacheado
            if (baseLoc == null || baseLoc.getWorld() == null) continue;

            baseLoc.subtract(2, 0, 2); // Alinha o canto

            int baseX = baseLoc.getBlockX();
            int baseY = baseLoc.getBlockY();
            int baseZ = baseLoc.getBlockZ();

            for (int x = colocarX; x <= larguraX; x++) {
                for (int z = colocarZ; z <= larguraZ; z++) {
                    Block block = baseLoc.getWorld().getBlockAt(baseX + x, baseY + y, baseZ + z);
                    if (block.getType() != tipoGerador) {
                        block.setType(tipoGerador, false); // false = sem atualização de física
                        block.setMetadata("Titanium", new FixedMetadataValue(plugin, true));
                    }
                }
            }
        }
    }
}
