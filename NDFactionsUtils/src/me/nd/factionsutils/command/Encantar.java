package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Encantar extends Commands implements Listener {

    private static final int MAX_ENCHANT_LEVEL = 5;
    private static final Map<Material, Enchantment[]> ENCHANTABLE_ITEMS = new HashMap<>();
    private static final Map<Integer, Integer> LEVEL_XP_COSTS = new HashMap<>();
    private static final double XP_COST_MULTIPLIER = 1.5; // Multiplicador de custo para novos encantamentos
    private static final Map<Enchantment, String> ENCHANTMENT_NAMES_PT = new HashMap<>();
    private static final Map<String, Enchantment> ENCHANTMENT_BY_NAME_PT = new HashMap<>();
    
    static {
        // Define XP costs for each enchantment level
        LEVEL_XP_COSTS.put(1, 10);
        LEVEL_XP_COSTS.put(2, 15);
        LEVEL_XP_COSTS.put(3, 20);
        LEVEL_XP_COSTS.put(4, 25);
        LEVEL_XP_COSTS.put(5, 30);

        // Define enchantable items and their possible enchantments
        Enchantment[] weaponEnchants = {
                Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS,
                Enchantment.FIRE_ASPECT, Enchantment.KNOCKBACK, Enchantment.LOOT_BONUS_MOBS
        };
        Enchantment[] toolEnchants = {
                Enchantment.DIG_SPEED, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH
        };
        Enchantment[] armorEnchants = {
                Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE,
                Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE,
                Enchantment.THORNS, Enchantment.OXYGEN, Enchantment.WATER_WORKER,
                Enchantment.DEPTH_STRIDER
        };
        Enchantment[] bowEnchants = {
                Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE, Enchantment.ARROW_KNOCKBACK,
                Enchantment.ARROW_INFINITE
        };

        Enchantment[] universalEnchants = {
                Enchantment.DURABILITY, Enchantment.LUCK, Enchantment.FIRE_ASPECT
        };

        // Weapons
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_SWORD, combineEnchants(weaponEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_AXE, combineEnchants(weaponEnchants, toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_SWORD, combineEnchants(weaponEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_AXE, combineEnchants(weaponEnchants, toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_SWORD, combineEnchants(weaponEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_AXE, combineEnchants(weaponEnchants, toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.STONE_SWORD, combineEnchants(weaponEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.STONE_AXE, combineEnchants(weaponEnchants, toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.WOOD_SWORD, combineEnchants(weaponEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.WOOD_AXE, combineEnchants(weaponEnchants, toolEnchants, universalEnchants));
        
        // Tools
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_PICKAXE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_HOE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_PICKAXE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_HOE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_PICKAXE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_HOE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.STONE_PICKAXE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.STONE_HOE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.WOOD_PICKAXE, combineEnchants(toolEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.WOOD_HOE, combineEnchants(toolEnchants, universalEnchants));
        
        // Armor
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_HELMET, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_CHESTPLATE, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_LEGGINGS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.DIAMOND_BOOTS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_HELMET, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_CHESTPLATE, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_LEGGINGS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.GOLD_BOOTS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_HELMET, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_CHESTPLATE, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_LEGGINGS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.IRON_BOOTS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.CHAINMAIL_HELMET, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.CHAINMAIL_CHESTPLATE, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.CHAINMAIL_LEGGINGS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.CHAINMAIL_BOOTS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.LEATHER_HELMET, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.LEATHER_CHESTPLATE, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.LEATHER_LEGGINGS, combineEnchants(armorEnchants, universalEnchants));
        ENCHANTABLE_ITEMS.put(Material.LEATHER_BOOTS, combineEnchants(armorEnchants, universalEnchants));
        // Ranged Weapons
        ENCHANTABLE_ITEMS.put(Material.BOW, combineEnchants(bowEnchants, universalEnchants));

        // Other
        ENCHANTABLE_ITEMS.put(Material.FISHING_ROD, combineEnchants(
                new Enchantment[]{Enchantment.LUCK, Enchantment.LURE}, universalEnchants));
        
        // Mapeamento de encantamentos para nomes em português
        ENCHANTMENT_NAMES_PT.put(Enchantment.DAMAGE_ALL, "Afiação");
        ENCHANTMENT_NAMES_PT.put(Enchantment.DAMAGE_UNDEAD, "Julgamento");
        ENCHANTMENT_NAMES_PT.put(Enchantment.DAMAGE_ARTHROPODS, "Ruína dos Artrópodes");
        ENCHANTMENT_NAMES_PT.put(Enchantment.FIRE_ASPECT, "Aspecto Flamejante");
        ENCHANTMENT_NAMES_PT.put(Enchantment.KNOCKBACK, "Repulsão");
        ENCHANTMENT_NAMES_PT.put(Enchantment.LOOT_BONUS_MOBS, "Saque");
        ENCHANTMENT_NAMES_PT.put(Enchantment.DIG_SPEED, "Eficiência");
        ENCHANTMENT_NAMES_PT.put(Enchantment.LOOT_BONUS_BLOCKS, "Fortuna");
        ENCHANTMENT_NAMES_PT.put(Enchantment.SILK_TOUCH, "Toque Suave");
        ENCHANTMENT_NAMES_PT.put(Enchantment.PROTECTION_ENVIRONMENTAL, "Proteção");
        ENCHANTMENT_NAMES_PT.put(Enchantment.PROTECTION_FIRE, "Proteção contra Fogo");
        ENCHANTMENT_NAMES_PT.put(Enchantment.PROTECTION_EXPLOSIONS, "Proteção contra Explosões");
        ENCHANTMENT_NAMES_PT.put(Enchantment.PROTECTION_PROJECTILE, "Proteção contra Projéteis");
        ENCHANTMENT_NAMES_PT.put(Enchantment.THORNS, "Espinhos");
        ENCHANTMENT_NAMES_PT.put(Enchantment.OXYGEN, "Respiração");
        ENCHANTMENT_NAMES_PT.put(Enchantment.WATER_WORKER, "Afinidade Aquática");
        ENCHANTMENT_NAMES_PT.put(Enchantment.DEPTH_STRIDER, "Passos Profundos");
        ENCHANTMENT_NAMES_PT.put(Enchantment.ARROW_DAMAGE, "Força");
        ENCHANTMENT_NAMES_PT.put(Enchantment.ARROW_FIRE, "Chama");
        ENCHANTMENT_NAMES_PT.put(Enchantment.ARROW_KNOCKBACK, "Impacto");
        ENCHANTMENT_NAMES_PT.put(Enchantment.ARROW_INFINITE, "Infinidade");
        ENCHANTMENT_NAMES_PT.put(Enchantment.DURABILITY, "Durabilidade");
        ENCHANTMENT_NAMES_PT.put(Enchantment.LUCK, "Sorte do Mar");
        ENCHANTMENT_NAMES_PT.put(Enchantment.LURE, "Isca");

        // Mapeamento inverso (nome em português -> encantamento)
        for (Map.Entry<Enchantment, String> entry : ENCHANTMENT_NAMES_PT.entrySet()) {
            ENCHANTMENT_BY_NAME_PT.put(entry.getValue().toUpperCase().replace(" ", "_"), entry.getKey());
        }
    }

    private static Enchantment[] combineEnchants(Enchantment[]... enchantArrays) {
        return Arrays.stream(enchantArrays)
                .flatMap(Arrays::stream)
                .toArray(Enchantment[]::new);
    }

    public Encantar() {
        super("encantar", "enc");
        Bukkit.getPluginManager().registerEvents(this, Main.get());
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player) && args.length < 2) {
            sender.sendMessage("§cUso: /encantar give <jogador> <nível>");
            return;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJogador não encontrado!");
                return;
            }
            int level;
            try {
                level = Integer.parseInt(args[2]);
                if (level != 1 && level != 2) {
                    sender.sendMessage("§cO nível deve ser 1 ou 2!");
                    return;
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                sender.sendMessage("§cUso: /encantar give <jogador> <nível>");
                return;
            }

            ItemStack upgradeItem = new ItemBuilder(Material.NETHER_STAR)
                    .setName("§6Cristal de Encantamento Nível " + level)
                    .setLore(
                            "§7Item usado para evoluir encantamentos",
                            "§7Nível: " + level,
                            "§7Use 2 cristais de nível 1",
                            "§7e 2 de nível 2 para nível 6"
                    )
                    .toItemStack();
            addOrDropItem(target, upgradeItem);
            sender.sendMessage("§aCristal de Encantamento Nível " + level + " dado a " + target.getName() + "!");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }

        Player player = (Player) sender;
        openMainEnchantMenu(player);
    }

    private void openMainEnchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Menu de Encantamento");

        // Preencher com vidro preto
        ItemStack blackGlass = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName("§r")
                .setData((short) 15) // Vidro preto no Spigot 1.8.8
                .toItemStack();
        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 13 && i != 31) {
                inv.setItem(i, blackGlass);
            }
        }

        ItemStack info = new ItemBuilder(Material.BOOK)
                .setName("§eInformações")
                .setLore(
                        "§fN. Encantamento: §e1-5",
                        "",
                        "§7Clique em um item do seu inventário",
                        "§7para colocá-lo no slot central.",
                        "§7Use 2 Cristais de nível 1",
                        "§7e 2 de nível 2 para nível 6.",
                        "",
                        "§e▎ §fEsquerdo: §7Encantar",
                        "§e▎ §fDireito: §7Devolver"
                )
                .toItemStack();

        inv.setItem(31, info);
        inv.setItem(13, new ItemStack(Material.AIR)); // Slot central vazio

        player.openInventory(inv);
    }

    private void openEnchantSelectionMenu(Player player, ItemStack item) {
        Inventory inv = Bukkit.createInventory(null, 36, "Selecionar Encantamento");

        if (!ENCHANTABLE_ITEMS.containsKey(item.getType())) {
            player.sendMessage("§cEste item não pode ser encantado!");
            return;
        }

        // Preencher com vidro preto
        ItemStack blackGlass = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName("§r")
                .setData((short) 15)
                .toItemStack();
        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 4 && (i < 10 || i > 16) && (i < 19 || i > 25)) {
                inv.setItem(i, blackGlass);
            }
        }

        Enchantment[] availableEnchants = ENCHANTABLE_ITEMS.get(item.getType());
        int slot = 10;
        boolean hasEnchantments = !item.getEnchantments().isEmpty();

        for (Enchantment enchant : availableEnchants) {
            int currentLevel = item.getEnchantmentLevel(enchant);
            // Mostrar encantamentos até nível 5 ou, se nível 5, permitir seleção para nível 6
            if (currentLevel > MAX_ENCHANT_LEVEL) continue;

            int xpCost = currentLevel < MAX_ENCHANT_LEVEL ? LEVEL_XP_COSTS.get(currentLevel + 1) : 0;
            if (hasEnchantments && currentLevel == 0) {
                xpCost = (int) (xpCost * XP_COST_MULTIPLIER); // Aumentar custo para novos encantamentos
            }

            String enchantNamePt = ENCHANTMENT_NAMES_PT.getOrDefault(enchant, enchant.getName()); // Usa nome em português
            ItemStack enchantItem = new ItemBuilder(Material.ENCHANTED_BOOK)
                    .setName("§a" + enchantNamePt)
                    .setLore(
                            "§7Nível Atual: " + currentLevel,
                            currentLevel < MAX_ENCHANT_LEVEL
                                    ? "§7Custo: " + xpCost + " (Nível " + (currentLevel + 1) + ")"
                                    : "§cNível 5 atingido. ",
                            "",
                            currentLevel < MAX_ENCHANT_LEVEL
                                    ? "§eClique para ver níveis disponíveis"
                                    : "§eClique para evoluir para nível 6"
                    )
                    .toItemStack();

            inv.setItem(slot, enchantItem);
            slot++;
            if (slot == 17) slot = 19; // Pular a linha do meio
        }

        inv.setItem(4, item); // Item no slot 4
        player.openInventory(inv);
    }

    private void openLevelSelectionMenu(Player player, ItemStack item, Enchantment enchant) {
        String enchantNamePt = ENCHANTMENT_NAMES_PT.getOrDefault(enchant, enchant.getName());
        Inventory inv = Bukkit.createInventory(null, 36, "Selecionar Nível de " + enchantNamePt);

        // Preencher com vidro preto
        ItemStack blackGlass = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName("§r")
                .setData((short) 15)
                .toItemStack();
        for (int i = 0; i < inv.getSize(); i++) {
            if (i != 4 && i != 12 && i != 13 && i != 14 && i != 15 && i != 16) {
                inv.setItem(i, blackGlass);
            }
        }

        int currentLevel = item.getEnchantmentLevel(enchant);
        boolean hasEnchantments = !item.getEnchantments().isEmpty();

        if (currentLevel < MAX_ENCHANT_LEVEL) {
            // Only allow the next level if the enchantment exists (currentLevel > 0), otherwise allow up to 4 levels
            int maxLevel = currentLevel == 0 ? Math.min(currentLevel + 4, MAX_ENCHANT_LEVEL) : currentLevel + 1;
            for (int level = currentLevel + 1; level <= maxLevel; level++) {
                // Set XP cost to 30 for SILK_TOUCH, otherwise use LEVEL_XP_COSTS
                int xpCost = enchant == Enchantment.SILK_TOUCH ? 30 : LEVEL_XP_COSTS.get(level);
                // Apply multiplier only for non-SILK_TOUCH enchantments when adding a new enchantment
                if (hasEnchantments && currentLevel == 0 && enchant != Enchantment.SILK_TOUCH) {
                    xpCost = (int) (xpCost * XP_COST_MULTIPLIER);
                }

                // Usar verde (5) se tiver XP suficiente, vermelho (14) se não tiver
                short glassData = player.getLevel() >= xpCost ? (short) 5 : (short) 14;
                ItemStack levelItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setName("§aNível " + level)
                        .setData(glassData)
                        .setLore(
                                "§7Custo: " + xpCost + " lvl de xp",
                                "",
                                player.getLevel() >= xpCost ? "§eClique para encantar" : "§cVocê não tem XP suficiente!"
                        )
                        .toItemStack();

                inv.setItem(10 + level - (currentLevel + 1), levelItem);
            }
        }

        if (currentLevel == MAX_ENCHANT_LEVEL) {
            ItemStack upgradeToSix = new ItemBuilder(Material.NETHER_STAR)
                    .setName("§6Upgrade para Nível 6")
                    .setLore(
                            "§7Coloque 2 Cristais de Nível 1",
                            "§7e 2 Cristais de Nível 2",
                            "",
                            "§eClique para evoluir"
                    )
                    .toItemStack();
            inv.setItem(16, upgradeToSix);
        }

        inv.setItem(4, item); // Item no slot 4
        player.openInventory(inv);
    }

    private void addOrDropItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            // Inventário cheio, dropar item
            player.getWorld().dropItem(player.getLocation(), item);
            player.sendMessage("§cSeu inventário está cheio! O item foi dropado no chão.");
        } else {
            // Adicionar ao inventário
            player.getInventory().addItem(item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getSlot();
        ClickType clickType = event.getClick();

        if (!inventoryTitle.equals("Menu de Encantamento") &&
                !inventoryTitle.contains("Selecionar Encantamento") &&
                !inventoryTitle.contains("Selecionar Nível")) return;

        // Cancelar o evento por padrão
        event.setCancelled(true);

        // Evitar cliques inválidos
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Verificar clique no inventário do jogador
        if (clickedInventory != null && clickedInventory.equals(player.getInventory())) {
            Inventory inv = event.getView().getTopInventory();
            if (inventoryTitle.equals("Menu de Encantamento")) {
                if (inv.getItem(13) != null && inv.getItem(13).getType() != Material.AIR) {
                    player.sendMessage("§cJá existe um item no slot de encantamento!");
                    return;
                }
                if (ENCHANTABLE_ITEMS.containsKey(clickedItem.getType())) {
                    inv.setItem(13, clickedItem);
                    player.getInventory().setItem(slot, null);
                    player.sendMessage("§aItem colocado no slot de encantamento!");
                } else {
                    player.sendMessage("§cEste item não pode ser encantado!");
                }
            } else if (inventoryTitle.contains("Selecionar Nível") && clickedItem.getType() == Material.NETHER_STAR) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String name = meta.getDisplayName();
                    int amount = clickedItem.getAmount();
                    ItemStack singleItem = new ItemStack(clickedItem);
                    singleItem.setAmount(1);
                    ItemStack remainingItem = amount > 1 ? new ItemStack(clickedItem) : null;
                    if (remainingItem != null) {
                        remainingItem.setAmount(amount - 1);
                    }

                    if (name.equals("§6Cristal de Encantamento Nível 1")) {
                        if (inv.getItem(12) == null || inv.getItem(12).getType() == Material.AIR) {
                            inv.setItem(12, singleItem);
                            player.getInventory().setItem(slot, null);
                            if (remainingItem != null) {
                                addOrDropItem(player, remainingItem);
                            }
                            player.sendMessage("§aCristal de Nível 1 colocado!");
                        } else if (inv.getItem(13) == null || inv.getItem(13).getType() == Material.AIR) {
                            inv.setItem(13, singleItem);
                            player.getInventory().setItem(slot, null);
                            if (remainingItem != null) {
                                addOrDropItem(player, remainingItem);
                            }
                            player.sendMessage("§aCristal de Nível 1 colocado!");
                        } else {
                            player.sendMessage("§cOs slots para Cristais de Nível 1 estão ocupados!");
                        }
                    } else if (name.equals("§6Cristal de Encantamento Nível 2")) {
                        if (inv.getItem(14) == null || inv.getItem(14).getType() == Material.AIR) {
                            inv.setItem(14, singleItem);
                            player.getInventory().setItem(slot, null);
                            if (remainingItem != null) {
                                addOrDropItem(player, remainingItem);
                            }
                            player.sendMessage("§aCristal de Nível 2 colocado!");
                        } else if (inv.getItem(15) == null || inv.getItem(15).getType() == Material.AIR) {
                            inv.setItem(15, singleItem);
                            player.getInventory().setItem(slot, null);
                            if (remainingItem != null) {
                                addOrDropItem(player, remainingItem);
                            }
                            player.sendMessage("§aCristal de Nível 2 colocado!");
                        } else {
                            player.sendMessage("§cOs slots para Cristais de Nível 2 estão ocupados!");
                        }
                    }
                }
            }
            return;
        }

        // Lógica para o Menu de Encantamento
        if (inventoryTitle.equals("Menu de Encantamento")) {
            Inventory inv = event.getView().getTopInventory();
            ItemStack centerItem = inv.getItem(13);
            if (slot == 13 && centerItem != null && centerItem.getType() != Material.AIR) {
                if (clickType == ClickType.LEFT) {
                    if (ENCHANTABLE_ITEMS.containsKey(centerItem.getType())) {
                        inv.setItem(13, null); // Limpar slot 13
                        openEnchantSelectionMenu(player, centerItem);
                    } else {
                        player.sendMessage("§cEste item não pode ser encantado!");
                    }
                } else if (clickType == ClickType.RIGHT) {
                    addOrDropItem(player, centerItem);
                    inv.setItem(13, new ItemStack(Material.AIR));
                    player.sendMessage("§aItem devolvido ao seu inventário!");
                }
            }
        }
        // Lógica para Selecionar Encantamento
        else if (inventoryTitle.contains("Selecionar Encantamento")) {
            Inventory inv = event.getView().getTopInventory();
            ItemStack resultItem = inv.getItem(4);
            if (resultItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    if (displayName.length() >= 2 && displayName.startsWith("§a")) {
                        String enchantNamePt = displayName.substring(2); // Remove "§a"
                        Enchantment enchant = ENCHANTMENT_BY_NAME_PT.get(enchantNamePt.toUpperCase().replace(" ", "_"));
                        if (enchant != null) {
                            inv.setItem(4, null); // Limpar slot 4 antes de abrir o próximo menu
                            openLevelSelectionMenu(player, resultItem, enchant);
                        } else {
                            player.sendMessage("§cEncantamento inválido!");
                        }
                    } else {
                        player.sendMessage("§cNome de encantamento inválido!");
                    }
                }
            } else if (slot == 4 && resultItem != null && resultItem.getType() != Material.AIR && clickType == ClickType.RIGHT) {
                addOrDropItem(player, resultItem);
                inv.setItem(4, new ItemStack(Material.AIR));
                player.closeInventory();
                player.sendMessage("§aItem devolvido ao seu inventário!");
            }
        }
        // Lógica para Selecionar Nível
        else if (inventoryTitle.contains("Selecionar Nível")) {
            Inventory inv = event.getView().getTopInventory();
            ItemStack resultItem = inv.getItem(4);
            if (resultItem != null && clickedItem != null) {
                if (clickedItem.getType() == Material.STAINED_GLASS_PANE && slot >= 10 && slot <= 15) {
                    if (clickType != ClickType.LEFT) {
                        return; // Ignorar cliques que não sejam botão esquerdo
                    }
                    ItemMeta meta = clickedItem.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        String levelStr = meta.getDisplayName().substring(8);
                        try {
                            int level = Integer.parseInt(levelStr);
                            String enchantNamePt = inventoryTitle.replace("Selecionar Nível de ", "");
                            Enchantment enchant = ENCHANTMENT_BY_NAME_PT.get(enchantNamePt.toUpperCase().replace(" ", "_"));
                            if (enchant != null) {
                                int xpCost = enchant == Enchantment.SILK_TOUCH ? 30 : LEVEL_XP_COSTS.get(level);
                                if (!resultItem.getEnchantments().isEmpty() && resultItem.getEnchantmentLevel(enchant) == 0) {
                                    xpCost = (int) (xpCost * XP_COST_MULTIPLIER);
                                }
                                if (player.getLevel() >= xpCost) {
                                    player.setLevel(player.getLevel() - xpCost);
                                    resultItem.addUnsafeEnchantment(enchant, level);
                                    player.sendMessage("§aItem encantado com " + ENCHANTMENT_NAMES_PT.get(enchant) + " nível " + level + "!");
                                    inv.setItem(4, null); // Limpar slot 4 antes de reabrir
                                    openEnchantSelectionMenu(player, resultItem);
                                } else {
                                    player.sendMessage("§cVocê não tem XP suficiente!");
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else if (clickedItem.getType() == Material.NETHER_STAR && slot == 16) {
                    ItemStack slot12 = inv.getItem(12);
                    ItemStack slot13 = inv.getItem(13);
                    ItemStack slot14 = inv.getItem(14);
                    ItemStack slot15 = inv.getItem(15);
                    String enchantNamePt = inventoryTitle.replace("Selecionar Nível de ", "");
                    Enchantment enchant = ENCHANTMENT_BY_NAME_PT.get(enchantNamePt.toUpperCase().replace(" ", "_"));

                    if (enchant != null && slot12 != null && slot13 != null && slot14 != null && slot15 != null &&
                            slot12.getType() == Material.NETHER_STAR && slot13.getType() == Material.NETHER_STAR &&
                            slot14.getType() == Material.NETHER_STAR && slot15.getType() == Material.NETHER_STAR) {
                        ItemMeta meta12 = slot12.getItemMeta();
                        ItemMeta meta13 = slot13.getItemMeta();
                        ItemMeta meta14 = slot14.getItemMeta();
                        ItemMeta meta15 = slot15.getItemMeta();
                        if (meta12 != null && meta13 != null && meta14 != null && meta15 != null &&
                                meta12.hasDisplayName() && meta13.hasDisplayName() &&
                                meta14.hasDisplayName() && meta15.hasDisplayName()) {
                            String name12 = meta12.getDisplayName();
                            String name13 = meta13.getDisplayName();
                            String name14 = meta14.getDisplayName();
                            String name15 = meta15.getDisplayName();
                            boolean validItems = name12.equals("§6Cristal de Encantamento Nível 1") &&
                                    name13.equals("§6Cristal de Encantamento Nível 1") &&
                                    name14.equals("§6Cristal de Encantamento Nível 2") &&
                                    name15.equals("§6Cristal de Encantamento Nível 2") &&
                                    slot12.getAmount() >= 1 && slot13.getAmount() >= 1 &&
                                    slot14.getAmount() >= 1 && slot15.getAmount() >= 1;
                            if (validItems) {
                                // Consumir 1 cristal de cada slot
                                inv.setItem(12, null);
                                inv.setItem(13, null);
                                inv.setItem(14, null);
                                inv.setItem(15, null);
                                resultItem.addUnsafeEnchantment(enchant, 6);
                                player.sendMessage("§aEncantamento " + ENCHANTMENT_NAMES_PT.get(enchant) + " evoluído para nível 6!");
                                addOrDropItem(player, resultItem);
                                inv.setItem(4, null); // Limpar slot 4 para evitar duplicação
                                player.closeInventory();
                            } else {
                                player.sendMessage("§cColoque 2 Cristais de Nível 1 e 2 Cristais de Nível 2!");
                            }
                        } else {
                            player.sendMessage("§cColoque Cristais de Encantamento válidos!");
                        }
                    } else {
                        player.sendMessage("§cColoque 2 Cristais de Nível 1 e 2 Cristais de Nível 2!");
                    }
                } else if (slot == 4 && resultItem != null && resultItem.getType() != Material.AIR && clickType == ClickType.RIGHT) {
                    addOrDropItem(player, resultItem);
                    inv.setItem(4, new ItemStack(Material.AIR));
                    player.closeInventory();
                    player.sendMessage("§aItem devolvido ao seu inventário!");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        String inventoryTitle = event.getView().getTitle();
        Inventory inv = event.getInventory();

        // Clear cursor item if it's a UI element (STAINED_GLASS_PANE or NETHER_STAR)
        ItemStack cursorItem = player.getItemOnCursor();
        if (cursorItem != null && (cursorItem.getType() == Material.STAINED_GLASS_PANE || cursorItem.getType() == Material.NETHER_STAR)) {
            ItemMeta meta = cursorItem.getItemMeta();
            if (meta != null && meta.hasDisplayName() && 
                (meta.getDisplayName().startsWith("§aNível ") || meta.getDisplayName().equals("§6Upgrade para Nível 6"))) {
                player.setItemOnCursor(null);
            }
        }

        if (inventoryTitle.equals("Menu de Encantamento")) {
            ItemStack centerItem = inv.getItem(13);
            if (centerItem != null && centerItem.getType() != Material.AIR) {
                addOrDropItem(player, centerItem);
                inv.setItem(13, null);
            }
        } else if (inventoryTitle.contains("Selecionar Encantamento")) {
            ItemStack resultItem = inv.getItem(4);
            if (resultItem != null && resultItem.getType() != Material.AIR) {
                addOrDropItem(player, resultItem);
                inv.setItem(4, null);
            }
        } else if (inventoryTitle.contains("Selecionar Nível")) {
            ItemStack resultItem = inv.getItem(4);
            if (resultItem != null && resultItem.getType() != Material.AIR) {
                addOrDropItem(player, resultItem);
                inv.setItem(4, null);
            }
            // Return enchantment crystals (NETHER_STAR) in slots 12, 13, 14, and 15, if they are valid
            for (int slot : new int[]{12, 13, 14, 15}) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() == Material.NETHER_STAR) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasDisplayName() && 
                        (meta.getDisplayName().equals("§6Cristal de Encantamento Nível 1") || 
                         meta.getDisplayName().equals("§6Cristal de Encantamento Nível 2"))) {
                        addOrDropItem(player, item);
                        inv.setItem(slot, null);
                    }
                }
            }
            // Clear UI elements (STAINED_GLASS_PANE and NETHER_STAR for level 6) without returning them
            for (int slot = 10; slot <= 16; slot++) {
                ItemStack item = inv.getItem(slot);
                if (item != null && (item.getType() == Material.STAINED_GLASS_PANE || 
                    (item.getType() == Material.NETHER_STAR && 
                     item.hasItemMeta() && item.getItemMeta().hasDisplayName() && 
                     item.getItemMeta().getDisplayName().equals("§6Upgrade para Nível 6")))) {
                    inv.setItem(slot, null);
                }
            }
        }
    }

    private static class ItemBuilder {
        private final ItemStack item;
        private final ItemMeta meta;

        public ItemBuilder(Material material) {
            this.item = new ItemStack(material);
            this.meta = item.getItemMeta();
        }

        public ItemBuilder setName(String name) {
            meta.setDisplayName(name);
            return this;
        }

        public ItemBuilder setLore(String... lore) {
            meta.setLore(Arrays.asList(lore));
            return this;
        }

        public ItemBuilder setData(short data) {
            item.setDurability(data);
            return this;
        }

        public ItemStack toItemStack() {
            item.setItemMeta(meta);
            return item;
        }
    }
}