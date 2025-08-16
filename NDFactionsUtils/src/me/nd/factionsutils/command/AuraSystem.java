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
import java.util.UUID;

public class AuraSystem extends Commands {
    private static File auraFile = new File(Main.get().getDataFolder(), "auras.yml");
    private static FileConfiguration auraConfig;
    private static final Map<String, AuraCatalyzerData> catalyzers = new HashMap<>();
    private static final Map<String, AuraLinkData> links = new HashMap<>();
    private static Connection connection = SQlite.getConnection();
    private static final String AURA_CRYSTAL_TAG = "&0[AuraCrystal]";
    private static final int MAX_NODES_PER_FACTION = 5;
    private static final int MAX_ENERGY = 500;
    private static final int DEFAULT_RADIUS = 100;
    private static final Object dbLock = new Object();

    public static class AuraNode {
        String id;
        Location location;
        String factionId;
        List<String> effects;
        int energyCost;
        int durability;
        Map<String, Integer> effectLevels;
        List<String> linkedNodes;

        public AuraNode(String id, Location location, String factionId, List<String> effects, int energyCost, int durability, List<String> linkedNodes) {
            this.id = id;
            this.location = location;
            this.factionId = factionId;
            this.effects = effects;
            this.energyCost = energyCost;
            this.durability = durability;
            this.effectLevels = new HashMap<>();
            this.linkedNodes = linkedNodes != null ? linkedNodes : new ArrayList<>();
        }
    }

    public static class AuraCatalyzerData {
        String id;
        String name;
        List<String> lore;
        Material material;
        public String effect;
        public int energyCost;
        public int maxLevel;

        public AuraCatalyzerData(String id, String name, List<String> lore, Material material, String effect, int energyCost, int maxLevel) {
            this.id = id;
            this.name = name;
            this.lore = lore;
            this.material = material;
            this.effect = effect;
            this.energyCost = energyCost;
            this.maxLevel = maxLevel;
        }
    }

    public static class AuraLinkData {
        String id;
        String name;
        List<String> lore;
        Material material;
        int energyCostMultiplier;

        public AuraLinkData(String id, String name, List<String> lore, Material material, int energyCostMultiplier) {
            this.id = id;
            this.name = name;
            this.lore = lore;
            this.material = material;
            this.energyCostMultiplier = energyCostMultiplier;
        }
    }

    public AuraSystem() {
        super("aura");
        initializeDatabase();
        loadAuraConfig();
        startAuraTask();
    }

    private void initializeDatabase() {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Conexão com o banco de dados é nula.");
            return;
        }
        synchronized (dbLock) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS aura_nodes (" +
                    "id TEXT PRIMARY KEY, " +
                    "location TEXT NOT NULL, " +
                    "faction_id TEXT NOT NULL, " +
                    "effects TEXT DEFAULT '', " +
                    "energy_cost INTEGER DEFAULT 0, " +
                    "durability INTEGER DEFAULT 100, " +
                    "active BOOLEAN DEFAULT 1, " +
                    "linked_nodes TEXT DEFAULT ''" +
                    ")")) {
                ps.execute();
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao criar tabela aura_nodes:");
                e.printStackTrace();
            }
        }
    }

    private void loadAuraConfig() {
        if (!auraFile.exists()) {
            try {
                auraFile.getParentFile().mkdirs();
                auraFile.createNewFile();
                FileConfiguration defaultConfig = new YamlConfiguration();
                defaultConfig.set("Settings.BaseEnergyPerChunk", 5);
                defaultConfig.set("Settings.NodeCooldownHours", 24);
                defaultConfig.set("Settings.DefaultRadius", DEFAULT_RADIUS);
                defaultConfig.set("AuraCrystal.name", "&bCristal de Aura");
                defaultConfig.set("AuraCrystal.lore", Arrays.asList(
                        "&7Um cristal poderoso que projeta",
                        "&7auras benéficas para sua facção."
                ));
                defaultConfig.set("Catalyzers.Regeneration.id", "regeneration");
                defaultConfig.set("Catalyzers.Regeneration.material", "GHAST_TEAR");
                defaultConfig.set("Catalyzers.Regeneration.name", "&aCatalisador de Regeneração");
                defaultConfig.set("Catalyzers.Regeneration.lore", Arrays.asList(
                        "&7Adiciona regeneração aos membros",
                        "&7da facção no raio do nó."
                ));
                defaultConfig.set("Catalyzers.Regeneration.effect", "regeneration:1");
                defaultConfig.set("Catalyzers.Regeneration.energy_cost", 10);
                defaultConfig.set("Catalyzers.Regeneration.max_level", 3);
                defaultConfig.set("Catalyzers.Haste.id", "haste");
                defaultConfig.set("Catalyzers.Haste.material", "BLAZE_POWDER");
                defaultConfig.set("Catalyzers.Haste.name", "&eCatalisador de Pressa");
                defaultConfig.set("Catalyzers.Haste.lore", Arrays.asList(
                        "&7Adiciona pressa aos membros",
                        "&7da facção no raio do nó."
                ));
                defaultConfig.set("Catalyzers.Haste.effect", "haste:1");
                defaultConfig.set("Catalyzers.Haste.energy_cost", 15);
                defaultConfig.set("Catalyzers.Haste.max_level", 2);
                defaultConfig.set("Links.EtherThread.id", "ether_thread");
                defaultConfig.set("Links.EtherThread.material", "STRING");
                defaultConfig.set("Links.EtherThread.name", "&eFio de Éter");
                defaultConfig.set("Links.EtherThread.lore", Arrays.asList(
                        "&7Conecta dois nós de aura",
                        "&7para ampliar seus efeitos."
                ));
                defaultConfig.set("Links.EtherThread.energy_cost_multiplier", 150);
                defaultConfig.set("Messages.No_Perm", "&cVocê não tem permissão para executar esse comando.");
                defaultConfig.set("Messages.No_Faction", "&cVocê precisa de uma facção para usar o nó.");
                defaultConfig.set("Messages.Need_Claim", "&cVocê precisa estar no território da sua facção.");
                defaultConfig.set("Messages.Placed", "&aNó de aura colocado com sucesso!");
                defaultConfig.set("Messages.MaxNodes", "&cSua facção atingiu o limite de nós!");
                defaultConfig.set("Messages.EffectApplied", "&aEfeito %effect% aplicado ao nó (nível %level%).");
                defaultConfig.set("Messages.LinkCreated", "&aNós conectados com sucesso!");
                defaultConfig.set("Messages.NoNearbyNodes", "&cNenhum nó próximo encontrado para conectar.");
                defaultConfig.set("Messages.InsufficientEnergy", "&cEnergia insuficiente para ativar o nó.");
                defaultConfig.save(auraFile);
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao criar auras.yml:");
                e.printStackTrace();
            }
        }
        auraConfig = YamlConfiguration.loadConfiguration(auraFile);
        loadCatalyzers();
        loadLinks();
    }

    private void loadCatalyzers() {
        catalyzers.clear();
        ConfigurationSection catalyzerSection = auraConfig.getConfigurationSection("Catalyzers");
        if (catalyzerSection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Seção Catalyzers não encontrada em auras.yml");
            return;
        }

        for (String key : catalyzerSection.getKeys(false)) {
            ConfigurationSection catalyzer = catalyzerSection.getConfigurationSection(key);
            if (catalyzer == null) continue;
            Material material = Material.getMaterial(catalyzer.getString("material", "STONE").toUpperCase());
            if (material == null) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Material inválido para catalisador: " + key);
                continue;
            }
            catalyzers.put(key, new AuraCatalyzerData(
                    catalyzer.getString("id", key),
                    catalyzer.getString("name", ""),
                    catalyzer.getStringList("lore"),
                    material,
                    catalyzer.getString("effect", ""),
                    catalyzer.getInt("energy_cost", 10),
                    catalyzer.getInt("max_level", 3)
            ));
        }
    }

    private void loadLinks() {
        links.clear();
        ConfigurationSection linkSection = auraConfig.getConfigurationSection("Links");
        if (linkSection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Seção Links não encontrada em auras.yml");
            return;
        }

        for (String key : linkSection.getKeys(false)) {
            ConfigurationSection link = linkSection.getConfigurationSection(key);
            if (link == null) continue;
            Material material = Material.getMaterial(link.getString("material", "STRING").toUpperCase());
            if (material == null) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Material inválido para link: " + key);
                continue;
            }
            links.put(key, new AuraLinkData(
                    link.getString("id", key),
                    link.getString("name", ""),
                    link.getStringList("lore"),
                    material,
                    link.getInt("energy_cost_multiplier", 150)
            ));
        }
    }

    public static ItemStack createAuraCrystal() {
        ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("AuraCrystal.name", "&bCristal de Aura")));
        List<String> lore = new ArrayList<>();
        for (String line : auraConfig.getStringList("AuraCrystal.lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        lore.add(ChatColor.translateAlternateColorCodes('&', AURA_CRYSTAL_TAG));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isAuraCrystal(ItemStack item) {
        if (item == null || item.getType() != Material.PRISMARINE_CRYSTALS || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        return meta.getLore().contains(ChatColor.translateAlternateColorCodes('&', AURA_CRYSTAL_TAG));
    }

    public static ItemStack createCatalyzerItem(String catalyzerId) {
        AuraCatalyzerData catalyzer = catalyzers.get(catalyzerId);
        if (catalyzer == null) return null;
        ItemStack item = new ItemStack(catalyzer.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', catalyzer.name));
        List<String> formattedLore = new ArrayList<>();
        for (String line : catalyzer.lore) {
            formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createLinkItem(String linkId) {
        AuraLinkData link = links.get(linkId);
        if (link == null) return null;
        ItemStack item = new ItemStack(link.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', link.name));
        List<String> formattedLore = new ArrayList<>();
        for (String line : link.lore) {
            formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    public static Map<String, AuraCatalyzerData> getCatalyzers() {
        return new HashMap<>(catalyzers);
    }

    public static Map<String, AuraLinkData> getLinks() {
        return new HashMap<>(links);
    }

    public static FileConfiguration getAuraConfig() {
        return auraConfig;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static Object getDbLock() {
        return dbLock;
    }

    public static int getMaxNodesPerFaction() {
        return MAX_NODES_PER_FACTION;
    }

    private void startAuraTask() {
        if (connection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Conexão com o banco de dados é nula. Tarefa de aura não iniciada.");
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (dbLock) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "SELECT id, location, faction_id, effects, energy_cost, linked_nodes FROM aura_nodes WHERE active = 1")) {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            String locString = rs.getString("location");
                            String factionId = rs.getString("faction_id");
                            String effects = rs.getString("effects");
                            String linkedNodes = rs.getString("linked_nodes");
                            Location location = deserializeLocation(locString);
                            if (location == null) continue;

                            NDFaction faction = DataManager.factions.get(factionId);
                            if (faction == null) continue;

                            int availableEnergy = getFactionEnergy(faction);
                            int totalEnergyCost = rs.getInt("energy_cost");
                            if (!linkedNodes.isEmpty()) {
                                totalEnergyCost *= getLinkMultiplier(linkedNodes);
                            }
                            if (availableEnergy < totalEnergyCost) continue;

                            double radius = auraConfig.getInt("Settings.DefaultRadius", DEFAULT_RADIUS);
                            if (!linkedNodes.isEmpty()) {
                                radius *= 1.5; // Increase radius for linked nodes
                            }

                            for (String effect : effects.split(",")) {
                                if (effect.isEmpty()) continue;
                                String[] parts = effect.split(":");
                                PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                                int level = parts.length > 1 ? Integer.parseInt(parts[1]) - 1 : 0;
                                if (type == null) continue;

                                for (Player player : location.getWorld().getPlayers()) {
                                    if (player.getLocation().distanceSquared(location) <= radius * radius) {
                                        NDPlayer ndPlayer = DataManager.players.get(player.getName());
                                        if (ndPlayer != null && faction.getAll().contains(ndPlayer)) {
                                            player.addPotionEffect(new PotionEffect(type, 200, level, true, false));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao aplicar efeitos de aura:");
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0L, 100L);
    }

    public static int getFactionEnergy(NDFaction faction) {
        // Assuming NDFaction has a method getTerritories() instead of getTerras()
        int chunks = faction.getTerras() != null ? faction.getTerras().size() : 0;
        return Math.min(chunks * auraConfig.getInt("Settings.BaseEnergyPerChunk", 5), MAX_ENERGY);
    }

    private int getLinkMultiplier(String linkedNodes) {
        if (linkedNodes.isEmpty()) return 1;
        int count = linkedNodes.split(",").length + 1; // Include the current node
        return (int) (1 + (count - 1) * (auraConfig.getInt("Links.EtherThread.energy_cost_multiplier", 150) / 100.0));
    }

    public static void handleAuraNodePlace(Player player, Location location) {
        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro no banco de dados."));
            return;
        }
        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false);
                NDPlayer ndPlayer = DataManager.players.get(player.getName());
                if (ndPlayer == null || !ndPlayer.hasFaction()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.No_Faction")));
                    return;
                }

                NDFaction faction = getFactionAtLocation(location);
                if (faction == null || !ndPlayer.getFaction().getNome().equals(faction.getNome())) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.Need_Claim")));
                    return;
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT COUNT(*) FROM aura_nodes WHERE faction_id = ? AND active = 1")) {
                    ps.setString(1, faction.getNome());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next() && rs.getInt(1) >= MAX_NODES_PER_FACTION) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.MaxNodes")));
                        return;
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO aura_nodes (id, location, faction_id, effects, energy_cost, durability, active, linked_nodes) VALUES (?, ?, ?, '', 0, 100, 1, '')")) {
                    String nodeId = UUID.randomUUID().toString();
                    ps.setString(1, nodeId);
                    ps.setString(2, serializeLocation(location));
                    ps.setString(3, faction.getNome());
                    ps.executeUpdate();
                    connection.commit();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.Placed")));
                }
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao realizar rollback:");
                    rollbackEx.printStackTrace();
                }
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao registrar nó de aura:");
                e.printStackTrace();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao colocar o nó."));
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao restaurar auto-commit:");
                    autoCommitEx.printStackTrace();
                }
            }
        }
    }

    public static void handleLinkUse(Player player, Location location, ItemStack item, AuraLinkData link) {
        if (connection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro no banco de dados."));
            return;
        }
        synchronized (dbLock) {
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT id, faction_id, energy_cost, linked_nodes FROM aura_nodes WHERE location = ?")) {
                    ps.setString(1, serializeLocation(location));
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNó não encontrado."));
                        return;
                    }

                    String nodeId = rs.getString("id");
                    String factionId = rs.getString("faction_id");
                    int currentEnergyCost = rs.getInt("energy_cost");
                    String linkedNodes = rs.getString("linked_nodes");

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

                    // Find nearby nodes to link
                    String nearbyNodeId = findNearbyNode(location, factionId, nodeId);
                    if (nearbyNodeId == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.NoNearbyNodes")));
                        return;
                    }

                    int newEnergyCost = currentEnergyCost * link.energyCostMultiplier / 100;
                    if (newEnergyCost > getFactionEnergy(faction)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.InsufficientEnergy")));
                        return;
                    }

                    // Update current node
                    List<String> linkedNodeList = linkedNodes.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(linkedNodes.split(",")));
                    linkedNodeList.add(nearbyNodeId);
                    String newLinkedNodes = String.join(",", linkedNodeList);

                    try (PreparedStatement updatePs = connection.prepareStatement(
                            "UPDATE aura_nodes SET energy_cost = ?, linked_nodes = ? WHERE id = ?")) {
                        updatePs.setInt(1, newEnergyCost);
                        updatePs.setString(2, newLinkedNodes);
                        updatePs.setString(3, nodeId);
                        updatePs.executeUpdate();

                        // Update linked node
                        try (PreparedStatement updateLinkedPs = connection.prepareStatement(
                                "UPDATE aura_nodes SET linked_nodes = ? WHERE id = ?")) {
                            updateLinkedPs.setString(1, nodeId);
                            updateLinkedPs.setString(2, nearbyNodeId);
                            updateLinkedPs.executeUpdate();
                        }

                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
                        }
                        player.updateInventory();

                        connection.commit();
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.LinkCreated")));
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao conectar nós:");
                    e.printStackTrace();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao conectar os nós."));
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao iniciar transação:");
                e.printStackTrace();
            }
        }
    }

    private static String findNearbyNode(Location location, String factionId, String currentNodeId) {
        if (connection == null) return null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, location FROM aura_nodes WHERE faction_id = ? AND active = 1 AND id != ?")) {
            ps.setString(1, factionId);
            ps.setString(2, currentNodeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nodeId = rs.getString("id");
                Location nodeLocation = deserializeLocation(rs.getString("location"));
                if (nodeLocation != null && nodeLocation.distanceSquared(location) <= 10 * 16 * 10 * 16) { // 10 chunks range
                    return nodeId;
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao buscar nós próximos:");
            e.printStackTrace();
        }
        return null;
    }

    public static NDFaction getFactionAtLocation(Location location) {
        if (DataManager.factions == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] DataManager.factions é nulo.");
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

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("aura.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.No_Perm")));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n",
                    "&aComandos de Aura:",
                    "&a> /aura give <player> [crystal|catalyzer|link] [id]",
                    "&a> /aura reload")));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            loadAuraConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aConfigurações recarregadas."));
            return;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUse: /" + label + " give <player> [crystal|catalyzer|link] [id]"));
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', auraConfig.getString("Messages.Player_Not_Found")));
                return;
            }

            if (target.getInventory().firstEmpty() == -1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInventário cheio."));
                return;
            }

            ItemStack item;
            String itemName;
            if (args.length >= 3) {
                if (args[2].equalsIgnoreCase("crystal")) {
                    item = createAuraCrystal();
                    itemName = auraConfig.getString("AuraCrystal.name", "&bCristal de Aura");
                } else if (args[2].equalsIgnoreCase("catalyzer") && args.length == 4 && catalyzers.containsKey(args[3])) {
                    item = createCatalyzerItem(args[3]);
                    itemName = catalyzers.get(args[3]).name;
                } else if (args[2].equalsIgnoreCase("link") && args.length == 4 && links.containsKey(args[3])) {
                    item = createLinkItem(args[3]);
                    itemName = links.get(args[3]).name;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTipo ou ID inválido."));
                    return;
                }
            } else {
                item = createAuraCrystal();
                itemName = auraConfig.getString("AuraCrystal.name", "&bCristal de Aura");
            }

            target.getInventory().addItem(item);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aItem " + itemName + " dado para " + target.getName()));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aVocê recebeu 1x " + itemName));
        }
    }
}