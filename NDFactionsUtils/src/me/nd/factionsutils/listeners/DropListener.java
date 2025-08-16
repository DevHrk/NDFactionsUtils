package me.nd.factionsutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropListener implements Listener {

    private FileConfiguration config;
    private final Random random = new Random();

    public DropListener() {
        loadConfig();
        Main.get().getServer().getPluginManager().registerEvents(this, Main.get());
    }

    private void loadConfig() {
        File configFile = new File(Main.get().getDataFolder(), "drops.yml");
        if (!configFile.exists()) {
            Main.get().saveResource("drops.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntityType();
        ConfigurationSection dropsSection = config.getConfigurationSection("drops." + entityType.name());

        if (dropsSection == null) {
            Main.get().getLogger().info("No drop configuration found for entity: " + entityType.name());
            return;
        }

        // Clear vanilla drops if custom drops are configured
        event.getDrops().clear();

        // Get Looting level from player's held item
        ItemStack weapon = player.getInventory().getItemInHand();
        int lootingLevel = weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS) 
                ? weapon.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS) 
                : 0;
        double lootingMultiplier = 1.0 + (lootingLevel * 0.5); // Exemplo: Looting I = +50%, II = +100%, III = +150%

        // Handle multiple drop configurations for the same entity
        for (String key : dropsSection.getKeys(false)) {
            ConfigurationSection dropSection = dropsSection.getConfigurationSection(key);
            if (dropSection == null) {
                Main.get().getLogger().warning("Invalid drop section: " + key + " for entity: " + entityType.name());
                continue;
            }

            double baseChance = dropSection.getDouble("Chance", 0.0);
            double adjustedChance = baseChance * lootingMultiplier; // Apply Looting multiplier
            if (random.nextDouble() > adjustedChance) {
                Main.get().getLogger().info("Drop failed for " + key + " on " + entityType.name() + 
                        " (base chance: " + baseChance + ", adjusted chance: " + adjustedChance + ")");
                continue;
            }

            // Handle item drop if configured
            if (dropSection.getBoolean("GiveConfiguratedItem", false)) {
                ItemStack item = createItem(dropSection);
                if (item != null) {
                    event.getDrops().add(item);
                    Main.get().getLogger().info("Added drop: " + item.getType().name() + " for entity: " + entityType.name());
                } else {
                    Main.get().getLogger().warning("Failed to create item for drop: " + key + " for entity: " + entityType.name());
                }
            }

            // Execute commands if they exist
            List<String> commands = dropSection.getStringList("Commands");
            for (String command : commands) {
                command = command.replace("{player_name}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                Main.get().getLogger().info("Executed command: " + command + " for entity: " + entityType.name());
            }
        }
    }

    private ItemStack createItem(ConfigurationSection section) {
        try {
            String materialName = section.getString("Material");
            if (materialName == null) {
                return null;
            }

            org.bukkit.Material material;
            try {
                material = org.bukkit.Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                return null;
            }

            ItemStack item = new ItemStack(material);
            item.setAmount(section.getInt("Amount", 1));
            item.setDurability((short) section.getInt("Durability", 0));

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Set name
                String name = section.getString("Name");
                if (name != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                }

                // Set lores
                List<String> lores = section.getStringList("Lores");
                if (!lores.isEmpty()) {
                    List<String> coloredLores = new ArrayList<>();
                    for (String lore : lores) {
                        coloredLores.add(ChatColor.translateAlternateColorCodes('&', lore));
                    }
                    meta.setLore(coloredLores);
                }

                // Set glow (using enchantment without visible effect)
                if (section.getBoolean("Glow", false)) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                }

                item.setItemMeta(meta);
            }

            // Apply enchantments
            List<String> enchantments = section.getStringList("Enchantments");
            for (String ench : enchantments) {
                String[] parts = ench.split(":");
                if (parts.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(parts[0]);
                    if (enchantment == null) {
                        continue;
                    }
                    int level;
                    try {
                        level = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    item.addUnsafeEnchantment(enchantment, level);
                }
            }

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}