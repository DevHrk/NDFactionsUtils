package me.nd.factionsutils.listeners;

import me.nd.factions.api.Heads;
import me.nd.factions.enums.Cargo;
import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.command.Beacon;
import me.nd.factionsutils.dados.SQlite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeaconListener implements Listener {
    private static Connection connection = SQlite.getConnection();
    private static final Map<UUID, String> playerBeaconLocations = new HashMap<>();

    @EventHandler
    public void onBeaconPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        NDPlayer ndPlayer = DataManager.players.get(p.getName());
        if (event.getBlock().getType() == Material.BEACON) {
            ItemStack item = event.getItemInHand();
            if (!Beacon.isSupremeBeacon(item)) {
                return;
            }

            if (ndPlayer == null || !ndPlayer.hasFaction()) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Beacon.getBeaconsConfig().getString("Messages.No_Faction")));
                event.setCancelled(true);
                return;
            }
            Terra terra = new Terra(event.getBlock().getWorld(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (!ndPlayer.getFaction().ownsTerritory(terra)) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Beacon.getBeaconsConfig().getString("Messages.Need_Claim")));
                event.setCancelled(true);
                return;
            }
            if (ndPlayer.getCargo() == Cargo.Recruta || ndPlayer.getCargo() == Cargo.Membro) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê precisa ser capitão ou líder para colocar este item."));
                event.setCancelled(true);
                return;
            }
            if (connection == null) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro interno: conexão com o banco de dados não está disponível."));
                event.setCancelled(true);
                return;
            }
            Beacon.handleBeaconPlace(event.getPlayer(), event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBeaconBreak(BlockBreakEvent event) {
    	NDPlayer ndPlayer = DataManager.players.get(event.getPlayer().getName());
        if (event.getBlock().getType() == Material.BEACON) {
            if (isSupremeBeaconAtLocation(event.getBlock().getLocation())) {
            	if (ndPlayer.getCargo() == Cargo.Recruta || ndPlayer.getCargo() == Cargo.Membro) {
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê precisa ser capitão ou superior para quebrar esté beacon."));
                    event.setCancelled(true);
                    return;
                }
                Beacon.handleBeaconBreak(event.getPlayer(), event.getBlock().getLocation());
                event.getBlock().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onBeaconInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.BEACON) {
            return;
        }

        if (!isSupremeBeaconAtLocation(event.getClickedBlock().getLocation())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() == Material.AIR) {
            playerBeaconLocations.put(player.getUniqueId(), Beacon.serializeLocation(event.getClickedBlock().getLocation()));
            openBeaconMenu(player, event.getClickedBlock().getLocation());
            event.setCancelled(true);
            return;
        }
        
        if (isZonaProtegida(player.getLocation())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Beacon.getBeaconsConfig().getString("Messages.ZonaProtegida", "&cNão pode usar ativadores em zona protegida.")));
            event.setCancelled(true);
            return;
        }

        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro interno: conexão com o banco de dados não está disponível."));
            event.setCancelled(true);
            return;
        }

        for (String activatorId : Beacon.getActivators().keySet()) {
            ItemStack activatorItem = Beacon.createActivatorItem(activatorId);
            if (activatorItem != null && item.isSimilar(activatorItem)) {
                Beacon.handleActivatorUse(player, event.getClickedBlock().getLocation(), item);
                event.setCancelled(true);
                return;
            }
        }

        for (String keyId : Beacon.getKeys().keySet()) {
            ItemStack keyItem = Beacon.createKeyItem(keyId);
            if (keyId != null && item.isSimilar(keyItem)) {
                Beacon.handleKeyUse(player, event.getClickedBlock().getLocation(), item);
                event.setCancelled(true);
                return;
            }
        }
        if (item != null) {
            playerBeaconLocations.put(player.getUniqueId(), Beacon.serializeLocation(event.getClickedBlock().getLocation()));
            openBeaconMenu(player, event.getClickedBlock().getLocation());
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : event.blockList()) {
            if (block.getType() == Material.BEACON && isSupremeBeaconAtLocation(block.getLocation())) {
                Beacon.disableBeacon(block.getLocation());
                blocksToRemove.add(block);
            }
        }
        event.blockList().removeAll(blocksToRemove);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerBeaconLocations.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.translateAlternateColorCodes('&', "Sinalizador Supremo -")) &&
                !title.equals(ChatColor.translateAlternateColorCodes('&', "Selecionar Efeito do Beacon"))) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (title.startsWith(ChatColor.translateAlternateColorCodes('&', "Sinalizador Supremo -"))) {
            if (event.getSlot() == 11) {
                String locationString = playerBeaconLocations.get(player.getUniqueId());
                if (locationString != null) {
                    Location beaconLocation = deserializeLocation(locationString);
                    if (beaconLocation != null) {
                        openEffectsMenu(player, beaconLocation);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao abrir o menu de efeitos: localização inválida."));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao abrir o menu de efeitos: localização não encontrada."));
                }
            }
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', "Selecionar Efeito do Beacon"))) {
            if (clickedItem.getType() == Material.POTION || clickedItem.getType() == Material.REDSTONE) {
                String effect = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).replace("Efeito: ", "");
                String[] parts = effect.split(" ");
                effect = parts[0].toLowerCase() + ":" + (parts.length > 1 ? parts[1] : "1");
                String locationString = playerBeaconLocations.get(player.getUniqueId());
                if (locationString != null) {
                    Location beaconLocation = deserializeLocation(locationString);
                    if (beaconLocation != null) {
                        toggleEffectActiveState(player, beaconLocation, effect);
                        openEffectsMenu(player, beaconLocation); // Reopen effects menu to reflect changes
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao alternar efeito: localização inválida."));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao alternar efeito: localização não encontrada."));
                }
            }
            player.closeInventory();
        }
    }

    private void openBeaconMenu(Player player, Location beaconLocation) {
        NDPlayer ndPlayer = DataManager.players.get(player.getName());
        if (ndPlayer == null || !ndPlayer.hasFaction()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Beacon.getBeaconsConfig().getString("Messages.No_Faction")));
            return;
        }

        NDFaction faction = Beacon.getFactionAtLocation(beaconLocation);
        if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Beacon.getBeaconsConfig().getString("Messages.Need_Claim")));
            return;
        }

        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro interno: conexão com o banco de dados não está disponível."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "Sinalizador Supremo - [" + faction.getTag() + "]"));

        String activeEffects = "";
        String effects = "";
        boolean isDisabled = false;
        long disabledUntil = 0;
        int radius = 200;
        int maxEffects = 1;
        int radiusUpgrades = 0;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT active_effects, effects, is_disabled, disabled_until, radius, max_effects, radius_upgrades FROM beacons WHERE location = ?")) {
            ps.setString(1, Beacon.serializeLocation(beaconLocation));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                activeEffects = rs.getString("active_effects") != null ? rs.getString("active_effects").trim() : "";
                effects = rs.getString("effects") != null ? rs.getString("effects").trim() : "";
                isDisabled = rs.getBoolean("is_disabled");
                disabledUntil = rs.getLong("disabled_until");
                radius = rs.getInt("radius");
                maxEffects = rs.getInt("max_effects");
                radiusUpgrades = rs.getInt("radius_upgrades");
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBeacon não encontrado."));
                return;
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao verificar status do beacon:");
            e.printStackTrace();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao abrir o menu do beacon."));
            return;
        }

        ItemStack activeEffectItem;
        List<String> activeEffectList = activeEffects.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(activeEffects.split(",")));
        activeEffectList.replaceAll(String::trim);
        if (activeEffectList.isEmpty()) {
            activeEffectItem = new ItemStack(Heads.getSkull("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025"));
            ItemMeta meta = activeEffectItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cNenhum Efeito Ativo"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Use um ativador para aplicar um efeito."));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Clique para gerenciar efeitos."));
            meta.setLore(lore);
            activeEffectItem.setItemMeta(meta);
        } else {
            activeEffectItem = new ItemStack(Material.POTION);
            ItemMeta meta = activeEffectItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aEfeitos Ativos"));
            List<String> lore = new ArrayList<>();
            for (String effect : activeEffectList) {
                if (effect.isEmpty()) continue;
                String[] effectParts = effect.split(":");
                String effectName = effectParts[0].toUpperCase();
                int level = effectParts.length > 1 ? Integer.parseInt(effectParts[1]) : 1;
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7- " + effectName + " " + level));
            }
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Clique para gerenciar efeitos."));
            meta.setLore(lore);
            activeEffectItem.setItemMeta(meta);
        }
        inv.setItem(11, activeEffectItem);

        ItemStack statusItem;
        if (isDisabled && System.currentTimeMillis() < disabledUntil) {
            long remaining = disabledUntil - System.currentTimeMillis();
            String timeFormatted = Beacon.formatTime(remaining);
            statusItem = new ItemStack(Heads.getSkull("http://textures.minecraft.net/texture/c3a03c06ffe2356ce00aef5b708878d2fe4365a97bc4dae1e1542c27b2eb30dd"));
            ItemMeta meta = statusItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cBeacon Desativado"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Tempo restante: " + timeFormatted));
            meta.setLore(lore);
            statusItem.setItemMeta(meta);
        } else {
            statusItem = new ItemStack(Beacon.isValidPyramid(beaconLocation) ? Heads.getSkull("http://textures.minecraft.net/texture/954752e4dad9b3e3b1232d88bfa722704baea0fff3767349417ee76e13ef6ed2") : Heads.getSkull("http://textures.minecraft.net/texture/cde33c95fec1b8d988250f5f5b3a2485742439faeaa75ed506ea01d75e17f21"));
            ItemMeta meta = statusItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Beacon.isValidPyramid(beaconLocation) ? "&aBeacon Ativo" : "&cBeacon Inativo"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e&lInformações do Beacon:"));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&',Beacon.isValidPyramid(beaconLocation) ?
                    "&f ▸&f Status: &a✔" : "&f ▸&f Status: &c✘"));
            int effectCount = effects.isEmpty() ? 0 : (int) Arrays.stream(effects.split(",")).filter(e -> !e.isEmpty()).count();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f ▸&f Raio Atual:&e " + radius + "&7/&e100 blocos"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f ▸&f Efeitos:&e " + effectCount + "&7/&e" + maxEffects));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f ▸&f Melhorias de Raio:&e " + radiusUpgrades + "&7/&e4"));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', maxEffects < 3 ? "&a✔ Pode adicionar mais efeitos!" : "&c✘ Não é possível adicionar mais efeitos."));
            lore.add(ChatColor.translateAlternateColorCodes('&', radiusUpgrades < 5 && radius < 100 ? "&a✔ Pode aumentar o raio!" : "&c✘ Distância máxima do beacon atingida."));
            meta.setLore(lore);
            statusItem.setItemMeta(meta);
        }
        inv.setItem(15, statusItem);

        player.openInventory(inv);
    }

    private void openEffectsMenu(Player player, Location beaconLocation) {
        if (beaconLocation == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao abrir o menu de efeitos: localização inválida."));
            return;
        }

        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro interno: conexão com o banco de dados não está disponível."));
            return;
        }

        String effects = "";
        String activeEffects = "";
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT effects, active_effects FROM beacons WHERE location = ?")) {
            ps.setString(1, Beacon.serializeLocation(beaconLocation));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                effects = rs.getString("effects") != null ? rs.getString("effects").trim() : "";
                activeEffects = rs.getString("active_effects") != null ? rs.getString("active_effects").trim() : "";
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBeacon não encontrado."));
                return;
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao verificar efeitos do beacon:");
            e.printStackTrace();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao abrir o menu de efeitos."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "Selecionar Efeito do Beacon"));
        if (effects.isEmpty()) {
            ItemStack noEffects = new ItemStack(Heads.getSkull("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025"));
            ItemMeta meta = noEffects.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cNenhum Efeito Disponível"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Adicione efeitos usando ativadores."));
            meta.setLore(lore);
            noEffects.setItemMeta(meta);
            inv.setItem(13, noEffects);
        } else {
            List<String> effectList = new ArrayList<>(Arrays.asList(effects.split(",")));
            List<String> activeEffectList = activeEffects.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(activeEffects.split(",")));
            activeEffectList.replaceAll(String::trim);
            int slot = 10;
            for (String effect : effectList) {
                if (effect == null || effect.trim().isEmpty()) continue;
                String normalizedEffect = effect.trim();
                String[] effectParts = normalizedEffect.split(":");
                if (effectParts.length < 1) continue;
                String effectName = effectParts[0].toUpperCase();
                int level = effectParts.length > 1 ? Integer.parseInt(effectParts[1]) : 1;
                boolean isActive = activeEffectList.contains(normalizedEffect);

                ItemStack effectItem = new ItemStack(isActive ? Material.POTION : Material.REDSTONE);
                ItemMeta meta = effectItem.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aEfeito: " + effectName + " " + level));
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.translateAlternateColorCodes('&', isActive ? "&aAtivo" : "&cInativo"));
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Clique para " + (isActive ? "desativar" : "ativar") + " este efeito."));
                meta.setLore(lore);
                effectItem.setItemMeta(meta);
                inv.setItem(slot, effectItem);
                slot++;
                if (slot > 16) break;
            }
        }
        player.openInventory(inv);
    }

    @SuppressWarnings("unused")
	private void toggleEffectActiveState(Player player, Location beaconLocation, String effect) {
        if (beaconLocation == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao alternar efeito: localização inválida."));
            return;
        }

        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro interno: conexão com o banco de dados não está disponível."));
            return;
        }

        String normalizedEffect = effect.trim();
        try {
            connection.setAutoCommit(false); // Start transaction
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT active_effects, max_effects FROM beacons WHERE location = ?")) {
                ps.setString(1, Beacon.serializeLocation(beaconLocation));
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBeacon não encontrado."));
                    connection.rollback();
                    return;
                }

                String activeEffects = rs.getString("active_effects") != null ? rs.getString("active_effects").trim() : "";
                int maxEffects = rs.getInt("max_effects");
                List<String> activeEffectList = activeEffects.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(activeEffects.split(",")));
                activeEffectList.replaceAll(String::trim);

                boolean isActive = activeEffectList.contains(normalizedEffect);
                if (isActive) {
                    activeEffectList.remove(normalizedEffect);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Beacon.getBeaconsConfig().getString("Messages.EffectToggled", "&aEfeito %effect% %state%.")
                                    .replace("%effect%", normalizedEffect).replace("%state%", "desativado")));
                } else {
                    if (activeEffectList.size() >= maxEffects) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Beacon.getBeaconsConfig().getString("Messages.MaxEffects")));
                        connection.rollback();
                        return;
                    }
                    activeEffectList.add(normalizedEffect);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Beacon.getBeaconsConfig().getString("Messages.EffectToggled", "&aEfeito %effect% %state%.")
                                    .replace("%effect%", normalizedEffect).replace("%state%", "ativado")));
                }

                String newActiveEffects = String.join(",", activeEffectList);
                try (PreparedStatement updatePs = connection.prepareStatement(
                        "UPDATE beacons SET active_effects = ? WHERE location = ?")) {
                    updatePs.setString(1, newActiveEffects);
                    updatePs.setString(2, Beacon.serializeLocation(beaconLocation));
                    int rowsAffected = updatePs.executeUpdate();
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao alternar efeito ativo:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao alternar o efeito."));
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao iniciar transação:");
            e.printStackTrace();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao alternar o efeito."));
        }
    }

    @SuppressWarnings("unused")
    private void setActiveEffect(Player player, Location beaconLocation, String effect) {
        if (beaconLocation == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao selecionar efeito: localização inválida."));
            return;
        }
        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro interno: conexão com o banco de dados não está disponível."));
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE beacons SET active_effect = ? WHERE location = ?")) {
            ps.setString(1, effect);
            ps.setString(2, Beacon.serializeLocation(beaconLocation));
            ps.executeUpdate();
            String[] effectParts = effect.split(":");
            String effectName = effectParts[0].toUpperCase();
            int level = effectParts.length > 1 ? Integer.parseInt(effectParts[1]) : 1;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Beacon.getBeaconsConfig().getString("Messages.EffectApplied").replace("%effect%", effectName + " " + level)));
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao definir efeito ativo:");
            e.printStackTrace();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao definir o efeito ativo."));
        }
    }

    private boolean isSupremeBeaconAtLocation(Location location) {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot check beacon.");
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM beacons WHERE location = ?")) {
            ps.setString(1, Beacon.serializeLocation(location));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao verificar beacon no banco de dados:");
            e.printStackTrace();
            return false;
        }
    }

    private boolean isZonaProtegida(Location location) {
        return false; // Implement your protected zone logic here
    }

    private Location deserializeLocation(String locString) {
        if (locString == null) return null;
        String[] parts = locString.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}