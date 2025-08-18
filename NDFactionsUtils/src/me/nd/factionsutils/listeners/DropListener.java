package me.nd.factionsutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
        Entity entity = event.getEntity();
        Player killer = ((LivingEntity) entity).getKiller();
        if (killer == null) return;

        EntityType entityType = entity.getType();
        ConfigurationSection dropsSection = config.getConfigurationSection("drops." + entityType.name());

        if (dropsSection == null) {
            return;
        }

        // Quantidade de mobs representados pelo stack
        int stackAmount = entity.hasMetadata("StackAmount") 
                ? entity.getMetadata("StackAmount").get(0).asInt() 
                : 1;

        // XP proporcional
        int baseXP = event.getDroppedExp();
        event.setDroppedExp(baseXP * stackAmount);

        // Clear vanilla drops se tiver configuração custom
        event.getDrops().clear();

        // Looting do item do player
        ItemStack weapon = killer.getInventory().getItemInHand();
        int lootingLevel = weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS) 
                ? weapon.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS) 
                : 0;
        double lootingMultiplier = 1.0 + (lootingLevel * 0.5);

        // Para cada configuração de drop do mob
        for (String key : dropsSection.getKeys(false)) {
            ConfigurationSection dropSection = dropsSection.getConfigurationSection(key);
            if (dropSection == null) {
                continue;
            }

            double baseChance = dropSection.getDouble("Chance", 0.0);
            double adjustedChance = baseChance * lootingMultiplier;

            // Executa o drop proporcional ao stack
            for (int i = 0; i < stackAmount; i++) {
                if (random.nextDouble() > adjustedChance) continue;

                // Item configurado
                if (dropSection.getBoolean("GiveConfiguratedItem", false)) {
                    ItemStack item = createItem(dropSection);
                    if (item != null) {
                        event.getDrops().add(item);
                    }
                }

                // Comandos
                List<String> commands = dropSection.getStringList("Commands");
                for (String command : commands) {
                    command = command.replace("{player_name}", killer.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
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