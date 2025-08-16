package me.nd.factionsutils.command;

import me.nd.factionsutils.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class Plantacao extends Commands implements Listener {

    private static File plantationsFile = new File(Main.get().getDataFolder(), "plantations.yml");
    private static FileConfiguration plantationsConfig;
    private static final HashMap<String, PlantationData> plantations = new HashMap<>();
    private static final HashMap<UUID, Map<Material, Long>> breakCooldowns = new HashMap<>();
    private static boolean growthEventActive = false;
    private static boolean tradeEventActive = false;
    private static final Random random = new Random();
    private static long nextGrowthEventTime = 0;
    private static long eventEndTime = 0;

    public static class PlantationData {
        String id;
        String displayName;
        Material material;
        String name;
        List<String> lore;
        double dropChance;
        ItemStack dropItem;

        public PlantationData(String id, String displayName, Material material, String name, List<String> lore, double dropChance, ItemStack dropItem) {
            this.id = id;
            this.displayName = displayName;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.dropChance = dropChance;
            this.dropItem = dropItem;
        }
    }

    public Plantacao() {
        super("plantacao");
        loadPlantationsConfig();
        Bukkit.getPluginManager().registerEvents(this, Main.get());
        startGrowthEventScheduler();
    }

    private void loadPlantationsConfig() {
        if (!plantationsFile.exists()) {
            try {
                plantationsFile.getParentFile().mkdirs();
                plantationsFile.createNewFile();
                FileConfiguration defaultConfig = new YamlConfiguration();
                // Default plantations
                defaultConfig.set("plantations.sugar_cane.display-name", "&aCana-de-açúcar Especial");
                defaultConfig.set("plantations.sugar_cane.item.material", "SUGAR_CANE");
                defaultConfig.set("plantations.sugar_cane.item.name", "&aCana-de-açúcar Especial");
                defaultConfig.set("plantations.sugar_cane.item.lore", Arrays.asList(
                    "&7Planta especial que pode ser replantada.",
                    "&7Chance de drop: &f{dropChance}%",
                    "&7Item de drop: &f{dropItem}"
                ));
                defaultConfig.set("plantations.sugar_cane.drop-chance", 10.0);
                defaultConfig.set("plantations.sugar_cane.drop-item.material", "EMERALD");
                defaultConfig.set("plantations.sugar_cane.drop-item.amount", 1);

                defaultConfig.set("plantations.pumpkin_seeds.display-name", "&6Semente de Abóbora Especial");
                defaultConfig.set("plantations.pumpkin_seeds.item.material", "PUMPKIN_SEEDS");
                defaultConfig.set("plantations.pumpkin_seeds.item.name", "&6Semente de Abóbora Especial");
                defaultConfig.set("plantations.pumpkin_seeds.item.lore", Arrays.asList(
                    "&7Planta especial que pode ser replantada.",
                    "&7Chance de drop: &f{dropChance}%",
                    "&7Item de drop: &f{dropItem}"
                ));
                defaultConfig.set("plantations.pumpkin_seeds.drop-chance", 15.0);
                defaultConfig.set("plantations.pumpkin_seeds.drop-item.material", "GOLD_INGOT");
                defaultConfig.set("plantations.pumpkin_seeds.drop-item.amount", 2);

                defaultConfig.set("plantations.melon_seeds.display-name", "&cSemente de Melancia Especial");
                defaultConfig.set("plantations.melon_seeds.item.material", "MELON_SEEDS");
                defaultConfig.set("plantations.melon_seeds.item.name", "&cSemente de Melancia Especial");
                defaultConfig.set("plantations.melon_seeds.item.lore", Arrays.asList(
                    "&7Planta especial que pode ser replantada.",
                    "&7Chance de drop: &f{dropChance}%",
                    "&7Item de drop: &f{dropItem}"
                ));
                defaultConfig.set("plantations.melon_seeds.drop-chance", 12.0);
                defaultConfig.set("plantations.melon_seeds.drop-item.material", "DIAMOND");
                defaultConfig.set("plantations.melon_seeds.drop-item.amount", 1);

                defaultConfig.set("plantations.nether_wart.display-name", "&4Fungo do Nether Especial");
                defaultConfig.set("plantations.nether_wart.item.material", "NETHER_WARTS");
                defaultConfig.set("plantations.nether_wart.item.name", "&4Fungo do Nether Especial");
                defaultConfig.set("plantations.nether_wart.item.lore", Arrays.asList(
                    "&7Planta especial que pode ser replantada.",
                    "&7Chance de drop: &f{dropChance}%",
                    "&7Item de drop: &f{dropItem}"
                ));
                defaultConfig.set("plantations.nether_wart.drop-chance", 8.0);
                defaultConfig.set("plantations.nether_wart.drop-item.material", "BLAZE_ROD");
                defaultConfig.set("plantations.nether_wart.drop-item.amount", 1);

                // Growth event settings
                defaultConfig.set("growth-event.chance", 35.0);
                defaultConfig.set("growth-event.duration-minutes", 5);
                defaultConfig.set("growth-event.trade-duration-minutes", 5);
                defaultConfig.set("growth-event.interval-minutes", 60);
                defaultConfig.set("growth-event.tnt-radius", 4.0);
                defaultConfig.set("growth-event.tnt-multiplier", 2.0);

                // Trade menu settings
                defaultConfig.set("trade-menu.sugar_cane.cost.material", "EMERALD");
                defaultConfig.set("trade-menu.sugar_cane.cost.amount", 10);
                defaultConfig.set("trade-menu.pumpkin_seeds.cost.material", "GOLD_INGOT");
                defaultConfig.set("trade-menu.pumpkin_seeds.cost.amount", 15);
                defaultConfig.set("trade-menu.melon_seeds.cost.material", "DIAMOND");
                defaultConfig.set("trade-menu.melon_seeds.cost.amount", 5);
                defaultConfig.set("trade-menu.nether_wart.cost.material", "BLAZE_ROD");
                defaultConfig.set("trade-menu.nether_wart.cost.amount", 8);

                // Messages
                defaultConfig.set("messages.syntax", "&cUse: /{command} {syntax}");
                defaultConfig.set("messages.target", "&cJogador {player} não encontrado.");
                defaultConfig.set("messages.permission", "&cVocê não tem permissão para fazer isto.");
                defaultConfig.set("messages.console", "&cApenas jogadores in-game podem realizar esta ação.");
                defaultConfig.set("messages.reload", "&aConfigurações recarregadas com sucesso.");
                defaultConfig.set("messages.inv-full", "&cO seu inventário está cheio.");
                defaultConfig.set("messages.help", String.join("\n",
                    "&aComandos de Plantação:",
                    "&a> /plantacao give <player> <plant>",
                    "&a> /plantacao trade <plant>",
                    "&a> /plantacao reload"
                ));
                defaultConfig.set("messages.plant-give", "&aVocê deu &71x {plant}&a para o jogador &7{player}&a.");
                defaultConfig.set("messages.plant-received", "&aVocê recebeu &71x {plant}&a.");
                defaultConfig.set("messages.plant-list", String.join("\n",
                    "&cPlanta não encontrada.",
                    "&cPlantas disponíveis: &f{list}"
                ));
                defaultConfig.set("messages.trade-opened", "&aTroca disponível apenas durante o evento de crescimento.");
                defaultConfig.set("messages.trade-success", "&aVocê trocou {cost} por 1x {plant}.");
                defaultConfig.set("messages.trade-fail", "&cVocê não tem {cost} suficiente para trocar por {plant}.");
                defaultConfig.set("messages.growth-event-start", "&aEvento de crescimento iniciado! As plantas crescem instantaneamente por 5 minutos!");
                defaultConfig.set("messages.trade-phase", "&aFase de troca iniciada! Use /plantacao trade <plant> por mais 5 minutos!");
                defaultConfig.set("messages.growth-event-end", "&cEvento de crescimento terminado.");
                defaultConfig.set("messages.break-cooldown", "&cVocê deve esperar 3 segundos antes de quebrar outra {plant}.");
                defaultConfig.set("messages.invalid-plant", "&cVocê só pode plantar itens especiais obtidos com /plantacao.");
                defaultConfig.set("messages.event-time-remaining", "&aEvento de crescimento ativo! Tempo restante: &f{minutes} minutos e {seconds} segundos.");
                defaultConfig.set("messages.trade-time-remaining", "&aFase de troca ativa! Tempo restante: &f{minutes} minutos e {seconds} segundos.");
                defaultConfig.set("messages.event-not-active", "&cNenhum evento de crescimento ativo no momento.");

                defaultConfig.save(plantationsFile);
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao criar plantations.yml:");
                e.printStackTrace();
            }
        }
        plantationsConfig = YamlConfiguration.loadConfiguration(plantationsFile);
        loadPlantations();
    }

    private void loadPlantations() {
        plantations.clear();
        ConfigurationSection plantationsSection = plantationsConfig.getConfigurationSection("plantations");
        if (plantationsSection == null) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Seção 'plantations' não encontrada em plantations.yml.");
            return;
        }
        for (String key : plantationsSection.getKeys(false)) {
            ConfigurationSection plantation = plantationsSection.getConfigurationSection(key);
            if (plantation == null) continue;
            String displayName = plantation.getString("display-name", "");
            ConfigurationSection itemSection = plantation.getConfigurationSection("item");
            if (itemSection == null) continue;
            Material material = Material.getMaterial(itemSection.getString("material", "STONE").toUpperCase());
            if (material == null) {
                Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Material inválido para plantação: " + key);
                continue;
            }
            String name = itemSection.getString("name", "");
            List<String> lore = itemSection.getStringList("lore");
            double dropChance = plantation.getDouble("drop-chance", 0.0);
            ConfigurationSection dropItemSection = plantation.getConfigurationSection("drop-item");
            ItemStack dropItem = null;
            if (dropItemSection != null) {
                Material dropMaterial = Material.getMaterial(dropItemSection.getString("material", "STONE").toUpperCase());
                int amount = dropItemSection.getInt("amount", 1);
                if (dropMaterial != null) {
                    dropItem = new ItemStack(dropMaterial, amount);
                }
            }
            plantations.put(key, new PlantationData(key, displayName, material, name, lore, dropChance, dropItem));
        }
    }

    public static ItemStack createPlantationItem(String plantId) {
        PlantationData plant = plantations.get(plantId);
        if (plant == null) return null;
        ItemStack item = new ItemStack(plant.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plant.name));
        List<String> formattedLore = new ArrayList<>();
        if (plant.lore != null && !plant.lore.isEmpty()) {
            for (String line : plant.lore) {
                String formattedLine = line.replace("{dropChance}", String.format("%.1f", plant.dropChance))
                                         .replace("{dropItem}", plant.dropItem != null ? plant.dropItem.getType().name() : "Nenhum");
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', formattedLine));
            }
            meta.setLore(formattedLore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void performTrade(Player player, String plantId) {
        if (!tradeEventActive) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.trade-opened")));
            return;
        }
        PlantationData plant = plantations.get(plantId);
        if (plant == null) {
            String list = String.join(", ", plantations.keySet());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.plant-list").replace("{list}", list)));
            return;
        }
        ItemStack costItem = new ItemStack(
            Material.getMaterial(plantationsConfig.getString("trade-menu." + plant.id + ".cost.material", "STONE").toUpperCase()),
            plantationsConfig.getInt("trade-menu." + plant.id + ".cost.amount", 1)
        );
        if (player.getInventory().containsAtLeast(costItem, costItem.getAmount())) {
            player.getInventory().removeItem(costItem);
            ItemStack plantItem = createPlantationItem(plant.id);
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.inv-full")));
                return;
            }
            player.getInventory().addItem(plantItem);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.trade-success")
                    .replace("{cost}", costItem.getAmount() + "x " + costItem.getType().name())
                    .replace("{plant}", plant.displayName)));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.trade-fail")
                    .replace("{cost}", costItem.getAmount() + "x " + costItem.getType().name())
                    .replace("{plant}", plant.displayName)));
        }
    }

    private void startGrowthEventScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long intervalTicks = 20 * 60 * Math.max(1, plantationsConfig.getInt("growth-event.interval-minutes", 60));
                long currentTime = System.currentTimeMillis();
                if (!growthEventActive && !tradeEventActive) {
                    nextGrowthEventTime = currentTime + (intervalTicks * 50); // Convert ticks to milliseconds
                    if (random.nextDouble() * 100 <= plantationsConfig.getDouble("growth-event.chance", 35.0)) {
                        growthEventActive = true;
                        tradeEventActive = true;
                        eventEndTime = currentTime + (20 * 60 * 10 * 50); // 10 minutes total
                        Bukkit.getConsoleSender().sendMessage("§a[NDFactionUtils] Evento de crescimento iniciado!");
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.growth-event-start")));
                        // Start a repeating task to grow plants every second during the growth phase
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!growthEventActive) {
                                    cancel();
                                    return;
                                }
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    for (Block b : getNearbyBlocks(p.getLocation(), 16)) {
                                        if (isValidPlant(b.getType())) {
                                            growPlant(b);
                                        }
                                    }
                                }
                            }
                        }.runTaskTimer(Main.get(), 0, 20); // Run every 20 ticks (1 second)
                        // Switch to trade-only phase after 5 minutes
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                growthEventActive = false;
                                Bukkit.getConsoleSender().sendMessage("§a[NDFactionUtils] Fase de troca iniciada!");
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.trade-phase")));
                            }
                        }.runTaskLater(Main.get(), 20 * 60 * Math.max(1, plantationsConfig.getInt("growth-event.duration-minutes", 5)));
                        // End event after 10 minutes
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                growthEventActive = false;
                                tradeEventActive = false;
                                eventEndTime = 0;
                                nextGrowthEventTime = System.currentTimeMillis() + (intervalTicks * 50);
                                Bukkit.getConsoleSender().sendMessage("§a[NDFactionUtils] Evento de crescimento finalizado!");
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.growth-event-end")));
                            }
                        }.runTaskLater(Main.get(), 20 * 60 * Math.max(1, plantationsConfig.getInt("growth-event.duration-minutes", 5) + plantationsConfig.getInt("growth-event.trade-duration-minutes", 5)));
                    }
                }
            }
        }.runTaskTimer(Main.get(), 0, 20 * 60 * Math.max(1, plantationsConfig.getInt("growth-event.interval-minutes", 60)));
    }

    private List<Block> getNearbyBlocks(Location loc, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                    if (isValidPlant(block.getType())) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    private boolean isValidPlant(Material material) {
        return material == Material.SUGAR_CANE || material == Material.PUMPKIN_STEM || material == Material.MELON_STEM || material == Material.NETHER_WARTS;
    }

    private void growPlant(Block block) {
        if (block.getType() == Material.SUGAR_CANE) {
            Block below = block.getRelative(0, -1, 0);
            // Check for water proximity for sugar cane
            boolean hasWater = false;
            for (Block adjacent : new Block[] {
                block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0),
                block.getRelative(0, 0, 1), block.getRelative(0, 0, -1)
            }) {
                if (adjacent.getType() == Material.WATER || adjacent.getType() == Material.STATIONARY_WATER) {
                    hasWater = true;
                    break;
                }
            }
            if ((below.getType() == Material.DIRT || below.getType() == Material.GRASS || below.getType() == Material.SAND) && hasWater) {
                // Grow to exactly 3 blocks high
                for (int y = 0; y < 3; y++) {
                    Block target = block.getWorld().getBlockAt(block.getX(), block.getY() + y, block.getZ());
                    if (y == 0 && target.getType() != Material.SUGAR_CANE) {
                        target.setType(Material.SUGAR_CANE);
                    } else if (y > 0 && target.getType() == Material.AIR) {
                        target.setType(Material.SUGAR_CANE);
                    }
                }
            }
        } else if (block.getType() == Material.PUMPKIN_STEM || block.getType() == Material.MELON_STEM) {
            if (block.getRelative(0, -1, 0).getType() == Material.SOIL) {
                block.setData((byte) 7); // Fully grown stem
                // Instantly spawn pumpkin or melon
                Block[] adjacent = {
                    block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0),
                    block.getRelative(0, 0, 1), block.getRelative(0, 0, -1)
                };
                for (Block b : adjacent) {
                    if (b.getType() == Material.AIR && b.getRelative(0, -1, 0).getType() == Material.SOIL) {
                        b.setType(block.getType() == Material.PUMPKIN_STEM ? Material.PUMPKIN : Material.MELON);
                        break;
                    }
                }
            }
        } else if (block.getType() == Material.NETHER_WARTS) {
            if (block.getRelative(0, -1, 0).getType() == Material.SOUL_SAND) {
                block.setData((byte) 3); // Fully grown Nether Wart
            }
        }
    }

    private void createFakeExplosionTNT(Location l, int maxPower) {
        double radius = plantationsConfig.getDouble("growth-event.tnt-radius", 4.0);
        double multiplier = growthEventActive ? plantationsConfig.getDouble("growth-event.tnt-multiplier", 2.0) : 1.0;
        for (double x = l.getX() - radius; x <= l.getX() + radius; x++) {
            for (double y = l.getY() - radius; y <= l.getY() + radius; y++) {
                for (double z = l.getZ() - radius; z <= l.getZ() + radius; z++) {
                    Block b = l.getWorld().getBlockAt((int) x, (int) y, (int) z);
                    if (y >= 1 && isValidPlant(b.getType())) {
                        double distance = l.distance(new Location(l.getWorld(), x, y, z));
                        int power = (int) (maxPower * (growthEventActive ? multiplier : 1.0));
                        if (distance < radius / 2) {
                            power = (int) (maxPower * multiplier);
                        } else {
                            power = (int) (Math.random() * maxPower * multiplier);
                        }
                        if (power > 0) {
                            b.breakNaturally();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Material type = item.getType();
        PlantationData plantation = null;
        for (PlantationData p : plantations.values()) {
            if (p.material == type) {
                plantation = p;
                break;
            }
        }
        if (plantation == null) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.invalid-plant", "&cVocê só pode plantar itens especiais obtidos com /plantacao.")));
            return;
        }
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        String expectedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plantation.name));
        if (!displayName.equals(expectedName)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.invalid-plant", "&cVocê só pode plantar itens especiais obtidos com /plantacao.")));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (isValidPlant(block.getType())) {
            Map<Material, Long> cooldowns = breakCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
            Long lastBreak = cooldowns.getOrDefault(block.getType(), 0L);
            if (System.currentTimeMillis() - lastBreak < 3000) {
                event.setCancelled(true);
                String plantName = block.getType() == Material.SUGAR_CANE ? "Cana-de-açúcar" :
                                  block.getType() == Material.PUMPKIN_STEM ? "Semente de Abóbora" :
                                  block.getType() == Material.MELON_STEM ? "Semente de Melancia" :
                                  "Fungo do Nether";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plantationsConfig.getString("messages.break-cooldown").replace("{plant}", plantName)));
                return;
            }
            cooldowns.put(block.getType(), System.currentTimeMillis());
            for (PlantationData plant : plantations.values()) {
                if (plant.material == block.getType() && random.nextDouble() * 100 <= plant.dropChance && plant.dropItem != null) {
                    block.getWorld().dropItemNaturally(block.getLocation(), plant.dropItem);
                }
            }
            ItemStack plantItem = createPlantationItem(getPlantId(block.getType()));
            if (plantItem != null) {
                event.setCancelled(true);
                if (growthEventActive) {
                    // Drop fruit items during growth event
                    if (block.getType() == Material.SUGAR_CANE) {
                        // Drop 2-3 sugar cane items
                        int amount = 2 + random.nextInt(2); // Random 2 or 3
                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SUGAR_CANE, amount));
                    } else if (block.getType() == Material.PUMPKIN_STEM || block.getType() == Material.MELON_STEM) {
                        // Drop 1 pumpkin or melon item
                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType() == Material.PUMPKIN_STEM ? Material.PUMPKIN : Material.MELON, 1));
                        // Spawn the fruit block instantly
                        Block[] adjacent = {
                            block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0),
                            block.getRelative(0, 0, 1), block.getRelative(0, 0, -1)
                        };
                        for (Block b : adjacent) {
                            if (b.getType() == Material.AIR && b.getRelative(0, -1, 0).getType() == Material.SOIL) {
                                b.setType(block.getType() == Material.PUMPKIN_STEM ? Material.PUMPKIN : Material.MELON);
                                break;
                            }
                        }
                    }
                    growPlant(block); // Regrow the plant instantly
                }
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), plantItem);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == org.bukkit.entity.EntityType.PRIMED_TNT) {
            createFakeExplosionTNT(event.getLocation(), 4);
            event.blockList().clear();
        }
    }

    private String getPlantId(Material material) {
        for (PlantationData plant : plantations.values()) {
            if (plant.material == material) return plant.id;
        }
        return null;
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            String helpMessage = plantationsConfig.getString("messages.help", "");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpMessage));
            long currentTime = System.currentTimeMillis();
            if (growthEventActive || tradeEventActive) {
                long timeRemaining = eventEndTime - currentTime;
                long secondsTotal = timeRemaining / 1000;
                long minutes = secondsTotal / 60;
                long seconds = secondsTotal % 60;
                String eventMessage = growthEventActive ?
                    plantationsConfig.getString("messages.event-time-remaining",
                        "&aEvento de crescimento ativo! Tempo restante: &f{minutes} minutos e {seconds} segundos.") :
                    plantationsConfig.getString("messages.trade-time-remaining",
                        "&aFase de troca ativa! Tempo restante: &f{minutes} minutos e {seconds} segundos.");
                eventMessage = eventMessage.replace("{minutes}", String.valueOf(minutes))
                                          .replace("{seconds}", String.valueOf(seconds));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', eventMessage));
            } else {
                long timeRemaining = nextGrowthEventTime - currentTime;
                String nextEventMessage;
                if (timeRemaining > 0) {
                    long secondsTotal = timeRemaining / 1000;
                    long minutes = secondsTotal / 60;
                    long seconds = secondsTotal % 60;
                    nextEventMessage = plantationsConfig.getString("messages.next-event",
                        "&aPróximo evento de crescimento em: &f{minutes} minutos e {seconds} segundos.");
                    nextEventMessage = nextEventMessage.replace("{minutes}", String.valueOf(minutes))
                                                      .replace("{seconds}", String.valueOf(seconds));
                } else {
                    nextEventMessage = "&aAguardando inicialização do próximo evento...";
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', nextEventMessage));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("plantacao.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.permission")));
                return;
            }
            loadPlantationsConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.reload")));
            return;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("plantacao.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.permission")));
                return;
            }
            if (args.length != 3) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plantationsConfig.getString("messages.syntax").replace("{command}", label).replace("{syntax}", "give <player> <plant>")));
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plantationsConfig.getString("messages.target").replace("{player}", args[1])));
                return;
            }
            PlantationData plant = plantations.get(args[2]);
            if (plant == null) {
                String list = String.join(", ", plantations.keySet());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plantationsConfig.getString("messages.plant-list").replace("{list}", list)));
                return;
            }
            ItemStack plantItem = createPlantationItem(args[2]);
            if (target.getInventory().firstEmpty() == -1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.inv-full")));
                return;
            }
            target.getInventory().addItem(plantItem);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.plant-give").replace("{plant}", plant.displayName).replace("{player}", target.getName())));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plantationsConfig.getString("messages.plant-received").replace("{plant}", plant.displayName)));
            return;
        }

        if (args[0].equalsIgnoreCase("trade")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plantationsConfig.getString("messages.console")));
                return;
            }
            if (args.length != 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plantationsConfig.getString("messages.syntax").replace("{command}", label).replace("{syntax}", "trade <plant>")));
                return;
            }
            performTrade((Player) sender, args[1]);
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            plantationsConfig.getString("messages.syntax").replace("{command}", label).replace("{syntax}", "give <player> <plant> | trade <plant> | reload")));
    }
}