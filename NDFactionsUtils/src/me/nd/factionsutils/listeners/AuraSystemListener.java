package me.nd.factionsutils.listeners;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.command.AuraSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AuraSystemListener implements Listener {
    private static final FileConfiguration auraConfig = AuraSystem.getAuraConfig();
    private static final Connection connection = AuraSystem.getConnection();
    private static final Object dbLock = AuraSystem.getDbLock();

    public AuraSystemListener(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Location location = event.getBlockPlaced().getLocation();

        if (!AuraSystem.isAuraCrystal(item)) {
            return;
        }

        NDPlayer ndPlayer = DataManager.players.get(player.getName());
        if (ndPlayer == null || !ndPlayer.hasFaction()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.No_Faction")));
            event.setCancelled(true);
            return;
        }

        NDFaction faction = AuraSystem.getFactionAtLocation(location);
        if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.Need_Claim")));
            event.setCancelled(true);
            return;
        }

        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM aura_nodes WHERE faction_id = ? AND active = 1")) {
                ps.setString(1, faction.getNome());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) >= AuraSystem.getMaxNodesPerFaction()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.MaxNodes")));
                    event.setCancelled(true);
                    return;
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao verificar contagem de nós:");
                e.printStackTrace();
                event.setCancelled(true);
                return;
            }
        }

        AuraSystem.handleAuraNodePlace(player, location);
        event.getBlockPlaced().setType(Material.PRISMARINE); // Visual representation of the node
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        Location location = block.getLocation();

        if (block.getType() != Material.PRISMARINE) {
            return;
        }

        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT id, faction_id, active, durability FROM aura_nodes WHERE location = ?")) {
                ps.setString(1, AuraSystem.serializeLocation(location));
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return;
                }

                boolean isActive = rs.getBoolean("active");
                String factionId = rs.getString("faction_id");
                int durability = rs.getInt("durability");

                NDPlayer ndPlayer = DataManager.players.get(player.getName());
                if (ndPlayer == null || !ndPlayer.hasFaction()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.No_Faction")));
                    return;
                }

                NDFaction faction = DataManager.factions.get(factionId);
                if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.Need_Claim")));
                    return;
                }

                if (!isActive) {
                    long cooldownHours = auraConfig.getInt("Settings.NodeCooldownHours", 24);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&cEste nó está em cooldown por " + cooldownHours + " horas."));
                    return;
                }

                if (durability <= 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste nó está destruído."));
                    return;
                }

                // Check if the item is a catalyzer or link
                for (Map.Entry<String, AuraSystem.AuraCatalyzerData> entry : AuraSystem.getCatalyzers().entrySet()) {
                    ItemStack testItem = AuraSystem.createCatalyzerItem(entry.getKey());
                    if (testItem != null && testItem.isSimilar(item)) {
                        handleCatalyzerUse(player, location, item, entry.getValue());
                        event.setCancelled(true);
                        return;
                    }
                }

                for (Map.Entry<String, AuraSystem.AuraLinkData> entry : AuraSystem.getLinks().entrySet()) {
                    ItemStack testItem = AuraSystem.createLinkItem(entry.getKey());
                    if (testItem != null && testItem.isSimilar(item)) {
                        handleLinkUse(player, location, item, entry.getValue());
                        event.setCancelled(true);
                        return;
                    }
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao verificar nó de aura:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao interagir com o nó."));
            }
        }
    }

    private void handleCatalyzerUse(Player player, Location location, ItemStack item, AuraSystem.AuraCatalyzerData catalyzer) {
        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT effects, energy_cost FROM aura_nodes WHERE location = ?")) {
                    ps.setString(1, AuraSystem.serializeLocation(location));
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNó não encontrado."));
                        return;
                    }

                    String effects = rs.getString("effects");
                    int currentEnergyCost = rs.getInt("energy_cost");
                    NDFaction faction = AuraSystem.getFactionAtLocation(location);
                    int availableEnergy = AuraSystem.getFactionEnergy(faction);

                    List<String> effectList = effects.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(effects.split(",")));
                    String normalizedEffect = catalyzer.effect.trim().toLowerCase();
                    int newLevel = 1;
                    for (String effect : effectList) {
                        if (effect.startsWith(normalizedEffect.split(":")[0])) {
                            newLevel = Integer.parseInt(effect.split(":")[1]) + 1;
                            if (newLevel > catalyzer.maxLevel) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste efeito atingiu o nível máximo."));
                                return;
                            }
                            effectList.remove(effect);
                            break;
                        }
                    }

                    int newEnergyCost = currentEnergyCost + (catalyzer.energyCost * newLevel);
                    if (newEnergyCost > availableEnergy) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEnergia insuficiente para ativar este efeito."));
                        return;
                    }

                    effectList.add(normalizedEffect.split(":")[0] + ":" + newLevel);
                    String newEffects = String.join(",", effectList);

                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE aura_nodes SET effects = ?, energy_cost = ? WHERE location = ?")) {
                        updatePs.setString(1, newEffects);
                        updatePs.setInt(2, newEnergyCost);
                        updatePs.setString(3, AuraSystem.serializeLocation(location));
                        updatePs.executeUpdate();

                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
                        }
                        player.updateInventory();

                        connection.commit();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                auraConfig.getString("Messages.EffectApplied").replace("%effect%", normalizedEffect)));
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao aplicar catalisador:");
                    e.printStackTrace();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao aplicar o catalisador."));
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao iniciar transação:");
                e.printStackTrace();
            }
        }
    }

    private void handleLinkUse(Player player, Location location, ItemStack item, AuraSystem.AuraLinkData link) {
        // Placeholder for linking nodes (to be implemented)
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cConexão de nós ainda não implementada."));
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.PRISMARINE) {
            return;
        }

        Player player = event.getPlayer();
        Location location = block.getLocation();

        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT faction_id, durability, active FROM aura_nodes WHERE location = ?")) {
                ps.setString(1, AuraSystem.serializeLocation(location));
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return;
                }

                @SuppressWarnings("unused")
				String factionId = rs.getString("faction_id");
                int durability = rs.getInt("durability");
                boolean isActive = rs.getBoolean("active");

                NDPlayer ndPlayer = DataManager.players.get(player.getName());
                NDFaction faction = AuraSystem.getFactionAtLocation(location);

                if (faction != null && ndPlayer != null && ndPlayer.getFaction() != null &&
                        ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                    return; // Members of the owning faction cannot damage their own nodes
                }

                if (!isActive) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste nó está em cooldown."));
                    event.setCancelled(true);
                    return;
                }

                durability -= 10; // Reduce durability by 10 per hit
                if (durability <= 0) {
                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE aura_nodes SET durability = 0, active = 0 WHERE location = ?")) {
                        updatePs.setString(1, AuraSystem.serializeLocation(location));
                        updatePs.executeUpdate();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNó de aura destruído!"));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try (PreparedStatement resetPs = connection.prepareStatement(
                                        "UPDATE aura_nodes SET active = 1, durability = 100 WHERE location = ?")) {
                                    resetPs.setString(1, AuraSystem.serializeLocation(location));
                                    resetPs.executeUpdate();
                                } catch (SQLException e) {
                                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao reativar nó:");
                                    e.printStackTrace();
                                }
                            }
                        }.runTaskLater(Main.getPlugin(Main.class), auraConfig.getInt("Settings.NodeCooldownHours", 24) * 20 * 3600);
                    }
                } else {
                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE aura_nodes SET durability = ? WHERE location = ?")) {
                        updatePs.setInt(1, durability);
                        updatePs.setString(2, AuraSystem.serializeLocation(location));
                        updatePs.executeUpdate();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDurabilidade do nó reduzida para " + durability + "."));
                    }
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao processar dano ao nó:");
                e.printStackTrace();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.PRISMARINE) {
            return;
        }

        Player player = event.getPlayer();
        Location location = block.getLocation();

        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT faction_id, active, durability FROM aura_nodes WHERE location = ?")) {
                    ps.setString(1, AuraSystem.serializeLocation(location));
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        return;
                    }

                    String factionId = rs.getString("faction_id");
                    boolean isActive = rs.getBoolean("active");

                    NDPlayer ndPlayer = DataManager.players.get(player.getName());
                    if (ndPlayer == null || !ndPlayer.hasFaction()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.No_Faction")));
                        event.setCancelled(true);
                        return;
                    }

                    NDFaction faction = DataManager.factions.get(factionId);
                    if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.Need_Claim")));
                        event.setCancelled(true);
                        return;
                    }

                    if (!isActive) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste nó está em cooldown."));
                        event.setCancelled(true);
                        return;
                    }

                    try (PreparedStatement deletePs = connection.prepareStatement(
                            "DELETE FROM aura_nodes WHERE location = ?")) {
                        deletePs.setString(1, AuraSystem.serializeLocation(location));
                        deletePs.executeUpdate();
                        connection.commit();
                        player.getInventory().addItem(AuraSystem.createAuraCrystal());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aNó de aura retirado com sucesso!"));
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao remover nó de aura:");
                    e.printStackTrace();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao retirar o nó."));
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao iniciar transação:");
                e.printStackTrace();
                event.setCancelled(true);
            }
        }
    }
}