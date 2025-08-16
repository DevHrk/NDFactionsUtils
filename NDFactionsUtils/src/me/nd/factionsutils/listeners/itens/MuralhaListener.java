package me.nd.factionsutils.listeners.itens;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.MuralhaItem;
import me.nd.factionsutils.plugin.SConfig;

public class MuralhaListener implements Listener {
    FileConfiguration m = Main.get().getConfig();

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getItemInHand();
        SConfig m1 = Main.get().getConfig("Mensagens");

        // Verifica se o item é o correto
        if (item == null || !item.isSimilar(MuralhaItem.Mura)) {
            return;
        }

        NDPlayer mp = DataManager.players.get(p.getName());
        Terra terra = new Terra(e.getBlock().getWorld(), e.getBlock().getLocation().getChunk().getX(), e.getBlock().getLocation().getChunk().getZ());
        NDFaction chunkFaction = getFactionOwningTerra(terra);

        // Verifica se está em uma zona protegida
        if (!Utils.isZonaProtegida(e.getBlock().getLocation())) {
            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        // Verifica se os dados do jogador estão carregados
        if (mp == null) {
            e.setCancelled(true);
            return;
        }

        NDFaction playerFaction = mp.getFaction();
        // Bloqueia se o chunk pertence à facção do jogador
        if (playerFaction != null && chunkFaction != null && chunkFaction.equals(playerFaction)) {
            e.setCancelled(true);
            p.sendMessage("§cNão pode colocar no territorio da facção");
            return;
        }

        // Permite colocação em zona livre (chunk sem dono) ou em território de outra facção
        if (chunkFaction == null || (playerFaction == null || !chunkFaction.equals(playerFaction))) {
            API.removeItem(p);
            Location l = e.getBlockPlaced().getLocation();
            World world = l.getWorld();
            int yMin = m.getInt("Muralhas.Camada-Baixo");
            int yMax = m.getInt("Muralhas.Camada-Cima");
            Material wallMaterial = Material.getMaterial(m.getString("Muralhas.Material"));

            // Coloca os blocos da muralha
            for (int y = yMin; y <= yMax; y++) {
                Block b1 = new Location(world, l.getX(), l.getY() + y, l.getZ()).getBlock();
                if (b1.getType() == Material.AIR) {
                    b1.setType(wallMaterial);
                }
            }
        }
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