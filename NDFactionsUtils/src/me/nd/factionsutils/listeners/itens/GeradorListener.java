package me.nd.factionsutils.listeners.itens;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.gen.GeradorMethod;
import me.nd.factionsutils.itens.GeradorItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeradorListener implements Listener {

    FileConfiguration m = Main.get().getConfig();
    SConfig mc = Main.get().getConfig("Geradores");
    private final List<Location> generatorLocations = new ArrayList<>();

    public GeradorListener() {
        // Load generator locations from config on plugin enable
        loadGeneratorLocations();
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent e) {
        SConfig m1 = Main.get().getConfig("Mensagens");
        Player p = e.getPlayer();
        Block b = e.getBlock();
        NDPlayer mp = DataManager.players.get(p.getName());
        ItemStack itemInHand = p.getInventory().getItemInHand();
        Terra terra = new Terra(e.getBlock().getWorld(), e.getBlock().getLocation().getChunk().getX(), e.getBlock().getLocation().getChunk().getZ());

        if (itemInHand.isSimilar(GeradorItem.ARMADILHA)) {
            if (mp.getFaction() == null) {
                p.sendMessage("§cVocê precisa de uma facção");
                e.setCancelled(true);
                return;
            }

            if (!mp.getFaction().ownsTerritory(terra)) {
                MessageUtils.send(p, m1.getStringList("Gerador.territorio"));
                e.setCancelled(true);
                return;
            }

            setMeta(b, true);
            Location blockLocation = e.getBlockPlaced().getLocation();
            GeradorMethod.createGeradorStructure(blockLocation, Material.valueOf(m.getString("Gerador.settings.MaterialGerador")), Material.valueOf(m.getString("Gerador.settings.MaterialAoRedo")));
            Main.get().getGeradorAPI().createItem(blockLocation, new ItemStack(b.getType()));
            saveGeneratorLocation(blockLocation); // Save location to config
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onBlockDamage(BlockDamageEvent e) {
        SConfig m1 = Main.get().getConfig("Mensagens");
        Player p = e.getPlayer();
        NDPlayer mp = DataManager.players.get(p.getName());
        Block b = e.getBlock();
        Location l = b.getLocation();
        Terra terra = new Terra(b.getWorld(), b.getLocation().getChunk().getX(), b.getLocation().getChunk().getZ());

        // Verifica se o bloco é um gerador
        if (!placed(b) || b.getType() != Material.getMaterial(m.getInt("Gerador.Id"))) {
            return;
        }

        // Verifica se o jogador ou NDPlayer é nulo
        if (p == null || mp == null) {
            Main.get().getLogger().warning("Jogador ou NDPlayer nulo no evento BlockDamageEvent.");
            e.setCancelled(true);
            return;
        }

        // Verifica se o jogador está no próprio território ou pertence à facção dona
        boolean isInOwnTerritory = mp.isInOwnTerritory();
        boolean ownsTerritory = mp.getFaction() != null && mp.getFaction().ownsTerritory(terra);
        ItemStack itemInHand = p.getItemInHand();

        // Caso 1: Jogador no próprio território ou pertencente à facção dona
        if (isInOwnTerritory || ownsTerritory) {
            // Verifica se a facção está sob ataque
            if (mp.getFaction() != null && mp.getFaction().isSobAtaque()) {
                MessageUtils.send(p, m1.getStringList("Gerador.sob_ataque"));
                e.setCancelled(true);
                return;
            }

            // Verifica o modo de jogo
            if (p.getGameMode() != GameMode.SURVIVAL) {
                MessageUtils.send(p, m1.getStringList("Gerador.criativo"));
                e.setCancelled(true);
                return;
            }

            // Verifica a picareta correta
            String pickaxeName = m.getString("Picareta-Hero.Nome", "").replace("&", "§");
            if (itemInHand == null || !itemInHand.hasItemMeta() || itemInHand.getType() != Material.DIAMOND_PICKAXE
                    || !itemInHand.getItemMeta().getDisplayName().equals(pickaxeName)) {
                MessageUtils.send(p, m1.getStringList("Gerador.picareta"));
                e.setCancelled(true);
                return;
            }

            // Quebra o gerador (100% de chance de adicionar ao inventário para aliados)
            removeMeta(b);
            Main.get().getGeradorAPI().deleteItem(l);
            GeradorMethod.createGeradorStructure(l, Material.AIR, Material.AIR);
            p.getInventory().addItem(GeradorItem.ARMADILHA.clone());
            Main.get().getLogger().info("Added GeradorItem.ARMADILHA to inventory of " + p.getName() + " at " + l.toString());
            removeGeneratorLocation(l);
            e.setInstaBreak(true);
        } else {
            // Caso 2: Inimigos (não pertencem à facção dona)
            // Verifica a picareta correta
            String pickaxeName = m.getString("Picareta-Hero.Nome", "").replace("&", "§");
            if (itemInHand == null || !itemInHand.hasItemMeta() || itemInHand.getType() != Material.DIAMOND_PICKAXE
                    || !itemInHand.getItemMeta().getDisplayName().equals(pickaxeName)) {
                MessageUtils.send(p, m1.getStringList("Gerador.picareta"));
                e.setCancelled(true);
                return;
            }

            // Chance de 60% para dropar o item
            if (random.nextDouble() < 0.6) {
                b.getWorld().dropItemNaturally(l, GeradorItem.ARMADILHA.clone());
                Main.get().getLogger().info("Dropped GeradorItem.ARMADILHA at " + l.toString() + " by enemy player " + p.getName());
            }

            // Remove o gerador
            removeMeta(b);
            Main.get().getGeradorAPI().deleteItem(l);
            GeradorMethod.createGeradorStructure(l, Material.AIR, Material.AIR);
            removeGeneratorLocation(l);
            e.setInstaBreak(true);
        }
    }
    Random random = new Random();

    public Location getGeneratorLocations(Location target) {
        if (target == null || target.getWorld() == null) {
            return null;
        }
        for (Location loc : generatorLocations) {
            if (loc.getWorld().getName().equals(target.getWorld().getName()) &&
                loc.getBlockX() == target.getBlockX() &&
                loc.getBlockY() == target.getBlockY() &&
                loc.getBlockZ() == target.getBlockZ()) {
                return loc;
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        SConfig m1 = Main.get().getConfig("Mensagens");
        NDPlayer mp = DataManager.players.get(p.getName());

        // Tipo de bloco
        String blockType = m.getString("Gerador.settings.tipo", "QUARTZ_BLOCK");
        if (!item(b) || b.getType() != Material.getMaterial(blockType)) {
            return;
        }

        // Verifica território
        if (!mp.isInOwnTerritory()) {
            MessageUtils.send(p, m1.getStringList("Gerador.territorio"));
            e.setCancelled(true);
            return;
        }

        // Remove bloco
        b.setType(Material.AIR);
        removeGeneratorLocation(b.getLocation()); // Remove location from config

        // Executa comandos com % de chance
        List<String> comandos = m.getStringList("Gerador.settings.comandos");
        for (String linha : comandos) {
            String[] partes = linha.split("\\|", 2);
            if (partes.length < 2) continue;

            try {
                double chance = Double.parseDouble(partes[0]);
                String cmd = partes[1]
                        .replace("%player%", p.getName())
                        .replace("%x%", String.valueOf((int) p.getLocation().getX()))
                        .replace("%y%", String.valueOf((int) p.getLocation().getY()))
                        .replace("%z%", String.valueOf((int) p.getLocation().getBlockZ()))
                        .replace("%w%", p.getLocation().getWorld().getName());

                if (Math.random() * 100 < chance) {
                    Main.get().getServer().dispatchCommand(
                            Main.get().getServer().getConsoleSender(),
                            cmd
                    );
                }
            } catch (NumberFormatException ex) {
                System.out.println("§c[Gerador] Comando inválido no config: " + linha);
            }
        }
    }
    
    public void setMeta(Block b, boolean place) {
        b.setMetadata("GeradorQuartz", new FixedMetadataValue(Main.get(), place));
    }

    public void removeMeta(Block b) {
        b.removeMetadata("GeradorQuartz", Main.get());
    }

    public boolean item(Block b) {
        List<MetadataValue> meta = b.getMetadata("Titanium");
        for (MetadataValue value : meta) {
            if (value.getOwningPlugin() == Main.get()) {
                return value.asBoolean();
            }
        }
        return false;
    }

    public boolean placed(Block b) {
        List<MetadataValue> meta = b.getMetadata("GeradorQuartz");
        for (MetadataValue value : meta) {
            if (value.getOwningPlugin() == Main.get()) {
                return value.asBoolean();
            }
        }
        return false;
    }

    public List<Location> getGeneratorLocations() {
        return Collections.unmodifiableList(new ArrayList<>(generatorLocations));
    }
    
    private void saveGeneratorLocation(Location location) {
        generatorLocations.add(location);
        List<String> locs = new ArrayList<>();
        for (Location loc : generatorLocations) {
            locs.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        mc.set("Gerador.locations", locs);
        Main.get().saveConfig();
    }

    private void removeGeneratorLocation(Location location) {
        generatorLocations.removeIf(loc -> loc.getWorld().getName().equals(location.getWorld().getName()) &&
                loc.getBlockX() == location.getBlockX() &&
                loc.getBlockY() == location.getBlockY() &&
                loc.getBlockZ() == location.getBlockZ());
        List<String> locs = new ArrayList<>();
        for (Location loc : generatorLocations) {
            locs.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        mc.set("Gerador.locations", locs);
        Main.get().saveConfig();
    }

    private void loadGeneratorLocations() {
        List<String> locs = mc.getStringList("Gerador.locations");
        for (String loc : locs) {
            String[] parts = loc.split(",");
            if (parts.length == 4) {
                try {
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    Location location = new Location(Main.get().getServer().getWorld(worldName), x, y, z);
                    generatorLocations.add(location);
                    Block b = location.getBlock();
                    if (b.getType() == Material.getMaterial(m.getInt("Gerador.Id"))) {
                        setMeta(b, true);
                    }
                } catch (NumberFormatException ex) {
                    Main.get().getLogger().warning("Invalid location format in config: " + loc);
                }
            }
        }
    }
}