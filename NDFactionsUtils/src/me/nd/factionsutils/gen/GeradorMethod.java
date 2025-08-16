package me.nd.factionsutils.gen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import me.nd.factionsutils.Main;

public class GeradorMethod {
	static FileConfiguration m3 = Main.get().getConfig(); 
    public static void createGeradorStructure(Location loc, Material m, Material m2) {
        loc.subtract(2.0, 0.0, 2.0);
        int disBlockX = loc.getBlockX();
        int disBlockY = loc.getBlockY();
        int disBlockZ = loc.getBlockZ();
        for (int x = m3.getInt("Gerador.settings.AlturaX"); x <= m3.getInt("Gerador.settings.LarguraX"); ++x) {
            for (int y = m3.getInt("Gerador.settings.AlturaY"); y <= m3.getInt("Gerador.settings.LarguraY"); ++y) {
                for (int z = m3.getInt("Gerador.settings.AlturaZ"); z <= m3.getInt("Gerador.settings.LarguraZ"); ++z) {
                    Block blockEnder = loc.getWorld().getBlockAt(disBlockX + x, disBlockY, disBlockZ + z);
                    Block blockGlass = loc.getWorld().getBlockAt(disBlockX + x, disBlockY + y, disBlockZ + z);
                    blockGlass.setType(m);
                    blockEnder.setType(m2);
                }
            }
        }
        loc.add(2.0, 0.0, 2.0);
        loc.getBlock().setType(Material.getMaterial(m3.getInt("Gerador.Id")));
    }
}
