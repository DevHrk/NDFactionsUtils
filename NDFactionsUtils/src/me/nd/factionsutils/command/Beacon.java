package me.nd.factionsutils.command;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.dados.SQlite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Beacon extends Commands {
    private static File beaconsFile = new File(Main.get().getDataFolder(), "beacons.yml");
    private static FileConfiguration beaconsConfig;
    private static final Map<String, ActivatorData> activators = new HashMap<>();
    private static final Map<String, KeyData> keys = new HashMap<>();
    private static Connection connection = SQlite.getConnection();
    private static final String SUPREME_BEACON_TAG = "&0[SupremeBeacon]";
    private static final int MAX_RADIUS = 100;
    private static final int MAX_RADIUS_UPGRADES = 5;
    private static final int MAX_EFFECTS = 3;
    private static final Object dbLock = new Object(); // Synchronization lock for database access

    public static class ActivatorData {
        String id;
        String name;
        List<String> lore;
        Material material;
        short data;
        String effect;

        public ActivatorData(String id, String name, List<String> lore, Material material, short data, String effect) {
            this.id = id;
            this.name = name;
            this.lore = lore;
            this.material = material;
            this.data = data;
            this.effect = effect;
        }
    }

    public static class KeyData {
        String id;
        String name;
        List<String> lore;
        Material material;
        short data;
        String type;
        int value;

        public KeyData(String id, String name, List<String> lore, Material material, short data, String type, int value) {
            this.id = id;
            this.name = name;
            this.lore = lore;
            this.material = material;
            this.data = data;
            this.type = type;
            this.value = value;
        }
    }

    public Beacon() {
        super("beacon");
        loadBeaconsConfig();
        startEffectTask();
    }

    public static FileConfiguration getBeaconsConfig() {
        return beaconsConfig;
    }

    private void loadBeaconsConfig() {
        if (!beaconsFile.exists()) {
            try {
                beaconsFile.getParentFile().mkdirs();
                beaconsFile.createNewFile();
                FileConfiguration defaultConfig = new YamlConfiguration();
                defaultConfig.set("Settings.Raio", 20);
                defaultConfig.set("Settings.Time", 12);
                defaultConfig.set("Beacon.name", "&bSinalizador Supremo");
                defaultConfig.set("Beacon.lore", Arrays.asList(
                        "&7Este sinalizador não é destruido por",
                        "&7explosões e possui efeitos adicionais.",
                        "",
                        "&7Ao ser danificado por explosões, este",
                        "&7Sinalizador é desativado por 12 horas."
                ));
                defaultConfig.set("Ativadores.Velocidade.id", 1);
                defaultConfig.set("Ativadores.Velocidade.data", 0);
                defaultConfig.set("Ativadores.Velocidade.efeito", "speed:1");
                defaultConfig.set("Ativadores.Velocidade.material", "SUGAR");
                defaultConfig.set("Ativadores.Velocidade.name", "&eAtivador de velocidade I");
                defaultConfig.set("Ativadores.Velocidade.lore", Arrays.asList(
                        "&7Clique em um beacon",
                        "&7para liberar o efeito",
                        "&7de velocidade."
                ));
                defaultConfig.set("Chaves.MultiEfeitos.id", 1);
                defaultConfig.set("Chaves.MultiEfeitos.data", 0);
                defaultConfig.set("Chaves.MultiEfeitos.type", "multi_effects");
                defaultConfig.set("Chaves.MultiEfeitos.Quantia", 1);
                defaultConfig.set("Chaves.MultiEfeitos.material", "NETHER_STAR");
                defaultConfig.set("Chaves.MultiEfeitos.name", "&eAtivar MultiEfeitos");
                defaultConfig.set("Chaves.MultiEfeitos.lore", Arrays.asList(
                        "",
                        "&7Clique em um beacon",
                        "&7para liberar mais espaço",
                        "&7para colocar efeitos."
                ));
                defaultConfig.set("Chaves.Raio.id", 1);
                defaultConfig.set("Chaves.Raio.data", 0);
                defaultConfig.set("Chaves.Raio.type", "radius");
                defaultConfig.set("Chaves.Raio.raio", 20);
                defaultConfig.set("Chaves.Raio.material", "COMPASS");
                defaultConfig.set("Chaves.Raio.name", "&eAtivar Raio de funcionamento");
                defaultConfig.set("Chaves.Raio.lore", Arrays.asList(
                        "",
                        "&7Clique em um beacon",
                        "&7para liberar mais espaço",
                        "&7para colocar efeitos."
                ));
                defaultConfig.set("Messages.No_Perm", "&cVocê não tem permissão para executar esse comando.");
                defaultConfig.set("Messages.No_Faction", "&cVocê precisa de uma facção para colocar o beacon.");
                defaultConfig.set("Messages.Need_Claim", "&cVocê precisa estar no território da sua facção.");
                defaultConfig.set("Messages.Player_Not_Found", "&cPlayer não encontrado.");
                defaultConfig.set("Messages.Gived", "&aBeacon givado com sucesso!");
                defaultConfig.set("Messages.Placed", "&aBeacon colocado com sucesso!");
                defaultConfig.set("Messages.Withdrawn", "&aBeacon retirado com sucesso!");
                defaultConfig.set("Messages.Broken", Arrays.asList(
                        "",
                        " &cOps, este beacon está quebrado!",
                        " &cAinda falta %time%",
                        " &cPara poder retirar o beacon.",
                        ""
                ));
                defaultConfig.set("Messages.ZonaProtegida", "&cNão pode usar ativadores em zona protegida.");
                defaultConfig.set("Messages.MaxEffects", "&cEste beacon já atingiu o limite de efeitos.");
                defaultConfig.set("Messages.MaxRadius", "&cEste beacon já atingiu o limite de raio.");
                defaultConfig.set("Messages.EffectApplied", "&aEfeito %effect% adicionado ao beacon.");
                defaultConfig.set("Messages.EffectToggled", "&aEfeito %effect% %state%.");
                defaultConfig.set("Messages.MultiEffectsApplied", "&aCapacidade de efeitos aumentada para %limit%.");
                defaultConfig.set("Messages.RadiusApplied", "&aRaio do beacon aumentado para %radius% blocos.");
                defaultConfig.set("Messages.NoPyramid", "&cO beacon precisa de uma pirâmide válida para funcionar.");
                defaultConfig.save(beaconsFile);
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao criar beacons.yml:");
                e.printStackTrace();
            }
        }
        beaconsConfig = YamlConfiguration.loadConfiguration(beaconsFile);
        loadActivators();
        loadKeys();
    }

    private void loadActivators() {
        activators.clear();
        ConfigurationSection activatorsSection = beaconsConfig.getConfigurationSection("Ativadores");
        if (activatorsSection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] No activators section found in beacons.yml");
            return;
        }

        for (String key : activatorsSection.getKeys(false)) {
            ConfigurationSection activator = activatorsSection.getConfigurationSection(key);
            if (activator == null) continue;

            String name = activator.getString("name", "");
            List<String> lore = activator.getStringList("lore");
            Material material = Material.getMaterial(activator.getString("material", "STONE").toUpperCase());
            short data = (short) activator.getInt("data", 0);
            String effect = activator.getString("efeito", "").trim().toLowerCase();
            if (material == null) continue;

            activators.put(key, new ActivatorData(key, name, lore, material, data, effect));
        }
    }

    private void loadKeys() {
        keys.clear();
        ConfigurationSection keysSection = beaconsConfig.getConfigurationSection("Chaves");
        if (keysSection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] No keys section found in beacons.yml");
            return;
        }

        for (String key : keysSection.getKeys(false)) {
            ConfigurationSection keyData = keysSection.getConfigurationSection(key);
            if (keyData == null) continue;

            String id = keyData.getString("id", "");
            String name = keyData.getString("name", "");
            List<String> lore = keyData.getStringList("lore");
            Material material = Material.getMaterial(keyData.getString("material", "STONE").toUpperCase());
            short data = (short) keyData.getInt("data", 0);
            String type = keyData.getString("type", "");
            int value = type.equals("multi_effects") ? keyData.getInt("Quantia", 1) : keyData.getInt("raio", 20);
            if (material == null) continue;

            keys.put(key, new KeyData(id, name, lore, material, data, type, value));
        }
    }

    public static ItemStack createBeaconItem() {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Beacon.name", "&bSinalizador Supremo")));
        List<String> lore = new ArrayList<>();
        for (String line : beaconsConfig.getStringList("Beacon.lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        lore.add(ChatColor.translateAlternateColorCodes('&', SUPREME_BEACON_TAG));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSupremeBeacon(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return false;
        }
        List<String> lore = meta.getLore();
        return lore != null && lore.contains(ChatColor.translateAlternateColorCodes('&', SUPREME_BEACON_TAG));
    }

    public static ItemStack createActivatorItem(String activatorId) {
        ActivatorData activator = activators.get(activatorId);
        if (activator == null) return null;

        ItemStack item = new ItemStack(activator.material, 1, activator.data);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', activator.name));
        List<String> formattedLore = new ArrayList<>();
        if (activator.lore != null && !activator.lore.isEmpty()) {
            for (String line : activator.lore) {
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(formattedLore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createKeyItem(String keyId) {
        KeyData key = keys.get(keyId);
        if (key == null) return null;

        ItemStack item = new ItemStack(key.material, 1, key.data);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', key.name));
        List<String> formattedLore = new ArrayList<>();
        if (key.lore != null && !key.lore.isEmpty()) {
            for (String line : key.lore) {
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(formattedLore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isValidPyramid(Location location) {
        Block beaconBlock = location.getBlock();
        if (beaconBlock.getType() != Material.BEACON) return false;

        World world = location.getWorld();
        int beaconX = location.getBlockX();
        int beaconY = location.getBlockY();
        int beaconZ = location.getBlockZ();

        // Check for a 3x3 pyramid layer directly beneath the beacon
        int y = beaconY - 1; // One layer below the beacon
        if (y < 0) return false;

        for (int x = beaconX - 1; x <= beaconX + 1; x++) {
            for (int z = beaconZ - 1; z <= beaconZ + 1; z++) {
                Block block = world.getBlockAt(x, y, z);
                Material type = block.getType();
                if (type != Material.DIAMOND_BLOCK && type != Material.EMERALD_BLOCK &&
                    type != Material.GOLD_BLOCK && type != Material.IRON_BLOCK) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startEffectTask() {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot start beacon effect task.");
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (dbLock) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "SELECT location, active_effects, radius FROM beacons WHERE is_disabled = 0")) {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            String locString = rs.getString("location");
                            String activeEffects = rs.getString("active_effects");
                            int radius = rs.getInt("radius");
                            if (activeEffects == null || activeEffects.isEmpty()) continue;

                            Location location = deserializeLocation(locString);
                            if (location == null || !isValidPyramid(location)) continue;

                            NDFaction faction = getFactionAtLocation(location);
                            if (faction == null) continue;

                            String[] effectList = activeEffects.split(",");
                            for (String effect : effectList) {
                                if (effect.isEmpty()) continue;
                                String[] effectParts = effect.trim().split(":");
                                PotionEffectType effectType = PotionEffectType.getByName(effectParts[0].toUpperCase());
                                int level = effectParts.length > 1 ? Integer.parseInt(effectParts[1]) - 1 : 0;
                                if (effectType == null) continue;

                                for (Player player : location.getWorld().getPlayers()) {
                                    if (player.getLocation().distanceSquared(location) <= radius * radius) {
                                        NDPlayer ndPlayer = DataManager.players.get(player.getName());
                                        if (ndPlayer != null && faction.getAll().contains(ndPlayer)) {
                                            player.addPotionEffect(new PotionEffect(effectType, 200, level, true, false));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao aplicar efeitos de beacon:");
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 100L); // Run every 5 seconds
    }

    public static void disableBeacon(Location location) {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot disable beacon.");
            return;
        }
        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE beacons SET is_disabled = 1, disabled_until = ? WHERE location = ?")) {
                long disableTime = beaconsConfig.getInt("Settings.Time", 72) * 3600000L;
                long disabledUntil = System.currentTimeMillis() + disableTime;
                ps.setLong(1, disabledUntil);
                ps.setString(2, serializeLocation(location));
                ps.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao desativar beacon:");
                e.printStackTrace();
            }
        }
    }

    public static boolean canWithdrawBeacon(Location location) {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot check beacon withdrawal.");
            return false;
        }
        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT is_disabled, disabled_until FROM beacons WHERE location = ?")) {
                ps.setString(1, serializeLocation(location));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    boolean isDisabled = rs.getBoolean("is_disabled");
                    long disabledUntil = rs.getLong("disabled_until");
                    return !isDisabled || System.currentTimeMillis() >= disabledUntil;
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao verificar beacon:");
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String serializeLocation(Location location) {
        return String.format("%s,%d,%d,%d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    private static Location deserializeLocation(String locString) {
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

    public static NDFaction getFactionAtLocation(Location location) {
        if (DataManager.factions == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] DataManager.factions is null.");
            return null;
        }
        Terra terra = new Terra(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
        for (NDFaction faction : DataManager.factions.values()) {
            if (faction.ownsTerritory(terra)) {
                return faction;
            }
        }
        return null;
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("beacon.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.No_Perm")));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n",
                    "&aBeacon comandos:",
                    "&a> /beacon give <player> [activator|key]",
                    "&a> /beacon reload")));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            loadBeaconsConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aConfigurações recarregadas com sucesso."));
            return;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUse: /" + label + " give <player> [activator|key]"));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.Player_Not_Found").replace("{player}", args[1])));
                return;
            }

            if (target.getInventory().firstEmpty() == -1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cO inventário do jogador está cheio."));
                return;
            }

            ItemStack item;
            String itemName;
            if (args.length == 3) {
                if (activators.containsKey(args[2])) {
                    item = createActivatorItem(args[2]);
                    itemName = activators.get(args[2]).name;
                } else if (keys.containsKey(args[2])) {
                    item = createKeyItem(args[2]);
                    itemName = keys.get(args[2]).name;
                } else {
                    item = createBeaconItem();
                    itemName = beaconsConfig.getString("Beacon.name", "&bSinalizador Supremo");
                }
            } else {
                item = createBeaconItem();
                itemName = beaconsConfig.getString("Beacon.name", "&bSinalizador Supremo");
            }

            target.getInventory().addItem(item);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.Gived").replace("{item}", itemName).replace("{player}", target.getName())));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aVocê recebeu 1x " + itemName + "&a."));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUse: /" + label + " give <player> [activator|key] | reload"));
    }

    public static void handleBeaconPlace(Player player, Location location) {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot place beacon.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao colocar o beacon."));
            return;
        }
        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO beacons (location, is_disabled, disabled_until, effects, active_effect, radius, max_effects, radius_upgrades, active_effects) VALUES (?, 0, 0, '', '', ?, 1, 0, '')")) {
                ps.setString(1, serializeLocation(location));
                ps.setInt(2, beaconsConfig.getInt("Settings.Raio", 200));
                ps.executeUpdate();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.Placed")));
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao registrar beacon:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao colocar o beacon."));
            }
        }
    }

    @SuppressWarnings("unused")
	public static void handleActivatorUse(Player player, Location beaconLocation, ItemStack activatorItem) {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot apply activator.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao usar o ativador."));
            return;
        }
        if (activatorItem == null || activatorItem.getAmount() <= 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cItem inválido ou quantidade insuficiente."));
            return;
        }

        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false); // Start transaction
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT is_disabled, disabled_until, effects, active_effects, max_effects FROM beacons WHERE location = ?")) {
                    ps.setString(1, serializeLocation(beaconLocation));
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBeacon não encontrado."));
                        return;
                    }

                    boolean isDisabled = rs.getBoolean("is_disabled");
                    long disabledUntil = rs.getLong("disabled_until");
                    String effects = rs.getString("effects") != null ? rs.getString("effects").trim() : "";
                    String activeEffects = rs.getString("active_effects") != null ? rs.getString("active_effects").trim() : "";
                    int maxEffects = rs.getInt("max_effects");

                    NDPlayer ndPlayer = DataManager.players.get(player.getName());
                    if (ndPlayer == null || !ndPlayer.hasFaction()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê precisa de uma facção para usar o ativador."));
                        return;
                    }

                    NDFaction faction = getFactionAtLocation(beaconLocation);
                    if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste beacon não pertence à sua facção."));
                        return;
                    }

                    if (isDisabled && System.currentTimeMillis() < disabledUntil) {
                        long remaining = disabledUntil - System.currentTimeMillis();
                        String timeFormatted = formatTime(remaining);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.join("\n", beaconsConfig.getStringList("Messages.Broken")).replace("%time%", timeFormatted)));
                        return;
                    }

                    if (!isValidPyramid(beaconLocation)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.NoPyramid")));
                        return;
                    }

                    String activatorId = null;
                    for (Map.Entry<String, ActivatorData> entry : activators.entrySet()) {
                        ItemStack testItem = createActivatorItem(entry.getKey());
                        if (testItem != null && testItem.isSimilar(activatorItem)) {
                            activatorId = entry.getKey();
                            break;
                        }
                    }

                    if (activatorId == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAtivador inválido."));
                        return;
                    }

                    ActivatorData activator = activators.get(activatorId);
                    String normalizedEffect = activator.effect.trim().toLowerCase();
                    List<String> effectList = effects.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(effects.split(",")));
                    effectList.replaceAll(String::trim);
                    List<String> activeEffectList = activeEffects.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(activeEffects.split(",")));
                    activeEffectList.replaceAll(String::trim);

                    if (effectList.size() >= maxEffects && !effectList.contains(normalizedEffect)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.MaxEffects")));
                        return;
                    }

                    if (!effectList.contains(normalizedEffect)) {
                        effectList.add(normalizedEffect);
                        activeEffectList.add(normalizedEffect);
                    }
                    String newEffects = String.join(",", effectList);
                    String newActiveEffects = String.join(",", activeEffectList);

                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE beacons SET effects = ?, active_effects = ? WHERE location = ?")) {
                        updatePs.setString(1, newEffects);
                        updatePs.setString(2, newActiveEffects);
                        updatePs.setString(3, serializeLocation(beaconLocation));
                        int rowsAffected = updatePs.executeUpdate();

                        // Remove item safely
                        if (activatorItem.getAmount() > 1) {
                            activatorItem.setAmount(activatorItem.getAmount() - 1);
                        } else {
                            player.getInventory().remove(activatorItem); // Remove entire stack if amount is 1
                        }
                        player.updateInventory(); // Ensure inventory sync (optional, depending on Bukkit version)

                        connection.commit();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                beaconsConfig.getString("Messages.EffectApplied").replace("%effect%", normalizedEffect)));
                    }
                }
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error during rollback:");
                    rollbackEx.printStackTrace();
                }
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error applying activator:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao aplicar o ativador."));
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error restoring auto-commit:");
                    autoCommitEx.printStackTrace();
                }
            }
        }
    }

    public static void handleKeyUse(Player player, Location beaconLocation, ItemStack keyItem) {
        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao usar a chave."));
            return;
        }
        if (keyItem == null || keyItem.getAmount() <= 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cItem inválido ou quantidade insuficiente."));
            return;
        }

        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false); // Start transaction
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT is_disabled, disabled_until, radius, max_effects, radius_upgrades FROM beacons WHERE location = ?")) {
                    ps.setString(1, serializeLocation(beaconLocation));
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBeacon não encontrado."));
                        connection.rollback();
                        return;
                    }

                    boolean isDisabled = rs.getBoolean("is_disabled");
                    long disabledUntil = rs.getLong("disabled_until");
                    int currentRadius = rs.getInt("radius");
                    int currentMaxEffects = rs.getInt("max_effects");
                    int radiusUpgrades = rs.getInt("radius_upgrades");

                    NDPlayer ndPlayer = DataManager.players.get(player.getName());
                    if (ndPlayer == null || !ndPlayer.hasFaction()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê precisa de uma facção para usar a chave."));
                        return;
                    }

                    NDFaction faction = getFactionAtLocation(beaconLocation);
                    if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste beacon não pertence à sua facção."));
                        return;
                    }

                    if (isDisabled && System.currentTimeMillis() < disabledUntil) {
                        long remaining = disabledUntil - System.currentTimeMillis();
                        String timeFormatted = formatTime(remaining);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.join("\n", beaconsConfig.getStringList("Messages.Broken")).replace("%time%", timeFormatted)));
                        return;
                    }

                    if (!isValidPyramid(beaconLocation)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.NoPyramid")));
                        return;
                    }

                    String keyId = null;
                    for (Map.Entry<String, KeyData> entry : keys.entrySet()) {
                        ItemStack testItem = createKeyItem(entry.getKey());
                        if (testItem != null && testItem.isSimilar(keyItem)) {
                            keyId = entry.getKey();
                            break;
                        }
                    }

                    if (keyId == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cChave inválida."));
                        return;
                    }

                    KeyData key = keys.get(keyId);
                    try (PreparedStatement updatePs = connection.prepareStatement(
                            key.type.equals("multi_effects") ?
                                    "UPDATE beacons SET max_effects = ? WHERE location = ?" :
                                    "UPDATE beacons SET radius = ?, radius_upgrades = ? WHERE location = ?")) {
                        if (key.type.equals("multi_effects")) {
                            int newMaxEffects = currentMaxEffects + key.value;
                            if (newMaxEffects > MAX_EFFECTS) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.MaxEffects")));
                                return;
                            }
                            updatePs.setInt(1, newMaxEffects);
                            updatePs.setString(2, serializeLocation(beaconLocation));
                            updatePs.executeUpdate();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    beaconsConfig.getString("Messages.MultiEffectsApplied").replace("%limit%", String.valueOf(newMaxEffects))));
                        } else if (key.type.equals("radius")) {
                            if (radiusUpgrades >= MAX_RADIUS_UPGRADES || currentRadius + key.value > MAX_RADIUS) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.MaxRadius")));
                                return;
                            }
                            updatePs.setInt(1, currentRadius + key.value);
                            updatePs.setInt(2, radiusUpgrades + 1);
                            updatePs.setString(3, serializeLocation(beaconLocation));
                            updatePs.executeUpdate();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    beaconsConfig.getString("Messages.RadiusApplied").replace("%radius%", String.valueOf(currentRadius + key.value))));
                        }

                        // Remove item safely
                        if (keyItem.getAmount() > 1) {
                            keyItem.setAmount(keyItem.getAmount() - 1);
                        } else {
                            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null); // Remove item from held slot
                        }
                        player.updateInventory(); // Ensure inventory sync (optional, depending on Bukkit version)

                        connection.commit();
                    }
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Transaction rolled back due to error.");
                    } catch (SQLException rollbackEx) {
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error during rollback:");
                        rollbackEx.printStackTrace();
                    }
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error applying key:");
                    e.printStackTrace();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao aplicar a chave."));
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException autoCommitEx) {
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error restoring auto-commit:");
                        autoCommitEx.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Error starting transaction:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao usar a chave."));
            }
        }
    }

    public static void handleBeaconBreak(Player player, Location location) {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Database connection is null. Cannot break beacon.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao tentar quebrar o beacon."));
            return;
        }
        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false); // Start transaction
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT is_disabled, disabled_until FROM beacons WHERE location = ?")) {
                    ps.setString(1, serializeLocation(location));
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cBeacon não encontrado."));
                        return;
                    }

                    boolean isDisabled = rs.getBoolean("is_disabled");
                    long disabledUntil = rs.getLong("disabled_until");

                    NDPlayer ndPlayer = DataManager.players.get(player.getName());
                    if (ndPlayer == null || !ndPlayer.hasFaction()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê precisa de uma facção para retirar o beacon."));
                        return;
                    }

                    NDFaction faction = getFactionAtLocation(location);
                    if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste beacon não pertence à sua facção."));
                        return;
                    }

                    if (isDisabled && System.currentTimeMillis() < disabledUntil) {
                        long remaining = disabledUntil - System.currentTimeMillis();
                        String timeFormatted = formatTime(remaining);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.join("\n", beaconsConfig.getStringList("Messages.Broken")).replace("%time%", timeFormatted)));
                        
                        return;
                    }

                    try (PreparedStatement deletePs = connection.prepareStatement(
                            "DELETE FROM beacons WHERE location = ?")) {
                        deletePs.setString(1, serializeLocation(location));
                        deletePs.executeUpdate();
                        connection.commit();
                        player.getInventory().addItem(createBeaconItem());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', beaconsConfig.getString("Messages.Withdrawn")));
                    }
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao realizar rollback:");
                        rollbackEx.printStackTrace();
                    }
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao remover beacon:");
                    e.printStackTrace();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao retirar o beacon."));
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException autoCommitEx) {
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao restaurar auto-commit:");
                        autoCommitEx.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao iniciar transação:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao tentar quebrar o beacon."));
            }
        }
    }

    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    public static Map<String, ActivatorData> getActivators() {
        return new HashMap<>(activators);
    }

    public static Map<String, KeyData> getKeys() {
        return new HashMap<>(keys);
    }
}