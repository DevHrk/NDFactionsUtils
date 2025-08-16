package me.nd.factionsutils.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.dados.SQlite;
import me.nd.factionsutils.utils.misc.ItemBuilder;

public class EChest extends Commands implements Listener {

    private static final Connection connection = SQlite.getConnection();
    private final Map<UUID, Integer> openVaults = new HashMap<>();
    private final Map<UUID, Integer> managingChest = new HashMap<>();
    private final Map<UUID, Integer> expandingChest = new HashMap<>();
    private final Map<UUID, Integer> renamingChest = new HashMap<>();

    public EChest() {
        super("ec");
        Main.get().getServer().getPluginManager().registerEvents(this, Main.get());
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }
        Player player = (Player) sender;
        openChestMenu(player);
    }

    // Initialize chest metadata for a new Enderchest
    private void initializeChestMetadata(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO ender_chest_metadata (uuid, chest_number, custom_name, size, icon) " +
                "VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ps.setString(3, "Ec #" + chestNumber);
            ps.setInt(4, 27);
            ps.setString(5, "CHEST");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Abre o menu principal de Enderchests
    private void openChestMenu(Player player) {
        int unlockedChests = getUnlockedChests(player);
        Inventory inv = Bukkit.createInventory(null, 54, "Menu de Enderchest");

        // Adiciona os Enderchests desbloqueados
        for (int i = 1; i <= unlockedChests; i++) {
            initializeChestMetadata(player, i); // Ensure metadata exists
            int slot = (i - 1) % 45; // Slots 0 a 44 para Enderchests
            String customName = getChestCustomName(player, i);
            int usedSlots = getUsedSlots(player, i);
            int totalSlots = getChestSize(player, i);
            String icon = getChestIcon(player, i);
            Material iconMaterial = Material.getMaterial(icon != null ? icon : "CHEST");

            ItemStack chest = new ItemBuilder(iconMaterial != null ? iconMaterial : Material.CHEST)
                    .setName("§e" + customName)
                    .setLore(
                            "",
                            "§f > §bEnderchest #" + i + "§f.",
                            "§f > §7Slots: §c" + usedSlots + "§7/§a" + totalSlots + "§f.",
                            "",
                            "§7Botão §fesquerdo§7 para abrir o Enderchest.",
                            "§7Botão §fdireito§7 para gerenciar o Enderchest."
                    )
                    .toItemStack();
            inv.setItem(slot, chest);
        }

        // Item para comprar novo Enderchest
        int money = 1000; // Placeholder: integre com Vault
        int cash = 50; // Placeholder: integre com PlayerPoints
        int desconto = 10; // Placeholder: integre com seu sistema de descontos
        ItemStack buy = new ItemBuilder(Material.CHEST)
                .setName("§6Comprar Enderchest")
                .setLore(
                        "§7Clique para comprar",
                        "§7um novo Enderchest.",
                        "",
                        "§7Preços:",
                        "§f > §a" + money + " coins.",
                        "§f > §6" + cash + " pontos.",
                        "",
                        "§fSeu desconto: §a" + desconto + "%§f."
                )
                .toItemStack();
        inv.setItem(30, buy);

        player.openInventory(inv);
    }

    // Menu de gerenciamento do Enderchest
    private void openManageMenu(Player player, int chestNumber) {
        Inventory inv = Bukkit.createInventory(null, 27, "Enderchest #" + chestNumber + " - Ações");
        String currentName = getChestCustomName(player, chestNumber);
        int currentLines = getChestSize(player, chestNumber) / 9;

        // Item para renomear
        ItemStack nameItem = new ItemBuilder(Material.NAME_TAG)
                .setName("§aNome do Enderchest")
                .setLore(
                        "§7Nome do Enderchest que ficará",
                        "§7no /ec.",
                        "",
                        "§7 Atual: " + currentName,
                        "",
                        "§7Clique para renomear."
                )
                .toItemStack();
        inv.setItem(11, nameItem);

        // Item para mudar ícone
        ItemStack iconItem = new ItemBuilder(Material.WRITTEN_BOOK)
                .setName("§aÍcone do Enderchest")
                .setLore(
                        "§7Mude o ícone que ficará",
                        "§7no /ec.",
                        "",
                        "§7Clique para alterar o ícone."
                )
                .toItemStack();
        inv.setItem(13, iconItem);

        // Item para expandir
        ItemStack expandItem = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                .setSkullTexture("3857d6b17901fd3f0109bd9bdcc28021b65947fce0a958327247d26d92915b85")
                .setName("§aExpandir Enderchest")
                .setLore(
                        "§7Expanda a quantia de",
                        "§7linhas do Enderchest.",
                        "",
                        "§7 Atual: §b" + currentLines + "§7.",
                        "",
                        "§7Clique para expandir."
                )
                .toItemStack();
        inv.setItem(15, expandItem);

        managingChest.put(player.getUniqueId(), chestNumber);
        player.openInventory(inv);
    }

    // Menu de confirmação para expansão
    private void openExpandMenu(Player player, int chestNumber) {
        Inventory inv = Bukkit.createInventory(null, 27, "Expandir Enderchest #" + chestNumber);
        int money = 500; // Placeholder: integre com Vault
        int playerPoints = 20; // Placeholder: integre com PlayerPoints
        int desconto = 10; // Placeholder: integre com descontos

        // Item para confirmar
        ItemStack confirm = new ItemBuilder(Material.WOOL, 1, (short) 5)
                .setName("§aConfirmar")
                .setLore("§7Clique para confirmar e expandir.")
                .toItemStack();
        inv.setItem(11, confirm);

        // Item para cancelar
        ItemStack cancel = new ItemBuilder(Material.WOOL, 1, (short) 14)
                .setName("§cCancelar")
                .setLore("§7Clique para cancelar e retornar.")
                .toItemStack();
        inv.setItem(15, cancel);

        // Item de expansão
        ItemStack expand = new ItemBuilder(Material.CHEST)
                .setName("§aExpansão")
                .setLore(
                        "",
                        "§7Preços:",
                        "§f > §a" + money + " coins.",
                        "§f > §6" + playerPoints + " pontos.",
                        "",
                        "§fSeu desconto: §a" + desconto + "%§f."
                )
                .toItemStack();
        inv.setItem(13, expand);

        expandingChest.put(player.getUniqueId(), chestNumber);
        player.openInventory(inv);
    }

    // Menu de seleção de ícones
    private void openIconMenu(Player player, int chestNumber) {
        Inventory inv = Bukkit.createInventory(null, 27, "Selecionar Ícone - Enderchest #" + chestNumber);
        Material[] icons = {
                Material.DIAMOND,
                Material.EMERALD,
                Material.GOLD_INGOT,
                Material.IRON_INGOT,
                Material.REDSTONE,
                Material.LAPIS_BLOCK,
                Material.CHEST,
                Material.ENDER_CHEST
        };

        for (int i = 0; i < icons.length; i++) {
            ItemStack icon = new ItemBuilder(icons[i])
                    .setName("§aÍcone: " + icons[i].name())
                    .setLore("§7Clique para selecionar este ícone.")
                    .toItemStack();
            inv.setItem(i + 9, icon); // Slots 9 a 16
        }

        managingChest.put(player.getUniqueId(), chestNumber);
        player.openInventory(inv);
    }

    // Verifica se o jogador desbloqueou o Enderchest
    private boolean hasChestUnlocked(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT unlocked_chests FROM ender_chest_access WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int unlockedChests = rs.getInt("unlocked_chests");
                return chestNumber <= unlockedChests;
            } else {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT OR IGNORE INTO ender_chest_access (uuid, unlocked_chests) VALUES (?, 1)")) {
                    insert.setString(1, player.getUniqueId().toString());
                    insert.executeUpdate();
                }
                return chestNumber == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao verificar Enderchests desbloqueados!");
            return false;
        }
    }

    // Obtém o número de Enderchests desbloqueados
    private int getUnlockedChests(Player player) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT unlocked_chests FROM ender_chest_access WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("unlocked_chests");
            }
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    // Obtém o nome personalizado do Enderchest
    private String getChestCustomName(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT custom_name FROM ender_chest_metadata WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("custom_name");
                return name != null ? name : "Ec #" + chestNumber;
            }
            initializeChestMetadata(player, chestNumber);
            return "Ec #" + chestNumber;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ec #" + chestNumber;
        }
    }

    // Define o nome personalizado do Enderchest
    private void setChestCustomName(Player player, int chestNumber, String name) {
        initializeChestMetadata(player, chestNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chest_metadata SET custom_name = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, name);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao renomear o Enderchest!");
        }
        // Update ender_chests table for consistency
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chests SET custom_name = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, name);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtém o tamanho do Enderchest
    private int getChestSize(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT size FROM ender_chest_metadata WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("size");
            }
            initializeChestMetadata(player, chestNumber);
            return 27;
        } catch (SQLException e) {
            e.printStackTrace();
            return 27;
        }
    }

    // Define o tamanho do Enderchest
    private void setChestSize(Player player, int chestNumber, int size) {
        initializeChestMetadata(player, chestNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chest_metadata SET size = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setInt(1, size);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao expandir o Enderchest!");
        }
        // Update ender_chests table for consistency
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chests SET size = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setInt(1, size);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtém o ícone do Enderchest
    private String getChestIcon(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT icon FROM ender_chest_metadata WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("icon");
            }
            initializeChestMetadata(player, chestNumber);
            return "CHEST";
        } catch (SQLException e) {
            e.printStackTrace();
            return "CHEST";
        }
    }

    // Define o ícone do Enderchest
    private void setChestIcon(Player player, int chestNumber, String icon) {
        initializeChestMetadata(player, chestNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chest_metadata SET icon = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, icon);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao alterar o ícone do Enderchest!");
        }
        // Update ender_chests table for consistency
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE ender_chests SET icon = ? WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, icon);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, chestNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtém slots usados
    private int getUsedSlots(Player player, int chestNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM ender_chests WHERE uuid = ? AND chest_number = ? AND item IS NOT NULL")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Abre o inventário do Enderchest
    private void openEnderChest(Player player, int chestNumber) {
        int size = getChestSize(player, chestNumber);
        Inventory enderChest = Bukkit.createInventory(null, size, "Ec #" + chestNumber);
        loadEnderChestContents(player, chestNumber, enderChest);
        openVaults.put(player.getUniqueId(), chestNumber);
        player.openInventory(enderChest);
    }

    // Carrega os itens do Enderchest
    private void loadEnderChestContents(Player player, int chestNumber, Inventory enderChest) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT slot, item FROM ender_chests WHERE uuid = ? AND chest_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, chestNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int slot = rs.getInt("slot");
                String itemData = rs.getString("item");
                if (itemData != null && !itemData.isEmpty()) {
                    ItemStack item = itemFromString(itemData);
                    if (item != null && slot >= 0 && slot < enderChest.getSize()) {
                        enderChest.setItem(slot, item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao carregar o conteúdo do Enderchest!");
        }
    }

    // Salva os itens do Enderchest
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (!title.startsWith("Ec #")) return;

        int chestNumber = openVaults.remove(player.getUniqueId());
        if (chestNumber == 0) return;

        try {
            // Limpa o conteúdo anterior do Enderchest
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM ender_chests WHERE uuid = ? AND chest_number = ?")) {
                delete.setString(1, player.getUniqueId().toString());
                delete.setInt(2, chestNumber);
                delete.executeUpdate();
            }

            // Salva os itens atuais
            Inventory inventory = event.getInventory();
            String customName = getChestCustomName(player, chestNumber);
            int size = inventory.getSize();
            String icon = getChestIcon(player, chestNumber);
            for (int slot = 0; slot < size; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO ender_chests (uuid, chest_number, slot, item, custom_name, size, icon) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        insert.setString(1, player.getUniqueId().toString());
                        insert.setInt(2, chestNumber);
                        insert.setInt(3, slot);
                        insert.setString(4, itemToString(item));
                        insert.setString(5, customName);
                        insert.setInt(6, size);
                        insert.setString(7, icon);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao salvar o conteúdo do Enderchest!");
        }
    }

    // Gerencia cliques nos menus
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        int slot = event.getSlot();

        // Menu principal de Enderchests
        if (title.equals("Menu de Enderchest")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            int chestNumber = slot + 1;
            if (hasChestUnlocked(player, chestNumber)) {
                if (event.isLeftClick()) {
                    openEnderChest(player, chestNumber);
                } else if (event.isRightClick()) {
                    openManageMenu(player, chestNumber);
                }
            } else if (slot == 30) {
                // Placeholder: integre com economia
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE ender_chest_access SET unlocked_chests = unlocked_chests + 1 WHERE uuid = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ps.executeUpdate();
                    initializeChestMetadata(player, getUnlockedChests(player)); // Initialize new Enderchest
                    player.sendMessage("§aNovo Enderchest comprado com sucesso!");
                    openChestMenu(player);
                } catch (SQLException e) {
                    e.printStackTrace();
                    player.sendMessage("§cErro ao comprar novo Enderchest!");
                }
            }
        }
        // Menu de gerenciamento
        else if (title.startsWith("Enderchest #")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            int chestNumber = managingChest.getOrDefault(player.getUniqueId(), 0);
            if (chestNumber == 0) return;

            if (slot == 11) {
                renamingChest.put(player.getUniqueId(), chestNumber);
                player.sendMessage("§aDigite o novo nome do Enderchest no chat (máximo 32 caracteres):");
                player.closeInventory();
            } else if (slot == 13) {
                openIconMenu(player, chestNumber);
            } else if (slot == 15) {
                openExpandMenu(player, chestNumber);
            }
        }
        // Menu de expansão
        else if (title.startsWith("Expandir Enderchest #")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            int chestNumber = expandingChest.getOrDefault(player.getUniqueId(), 0);
            if (chestNumber == 0) return;

            if (slot == 11) {
                // Confirmar expansão
                int currentSize = getChestSize(player, chestNumber);
                if (currentSize >= 54) {
                    player.sendMessage("§cO Enderchest já está no tamanho máximo (54 slots)!");
                    player.closeInventory();
                    return;
                }
                int newSize = currentSize + 9;
                // Placeholder: integre com economia
                setChestSize(player, chestNumber, newSize);
                player.sendMessage("§aEnderchest #" + chestNumber + " expandido para " + newSize + " slots!");
                openManageMenu(player, chestNumber);
            } else if (slot == 15) {
                openManageMenu(player, chestNumber);
            }
        }
        // Menu de ícones
        else if (title.startsWith("Selecionar Ícone - Enderchest #")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            int chestNumber = managingChest.getOrDefault(player.getUniqueId(), 0);
            if (chestNumber == 0) return;

            Material selectedIcon = event.getCurrentItem().getType();
            setChestIcon(player, chestNumber, selectedIcon.name());
            player.sendMessage("§aÍcone do Enderchest #" + chestNumber + " alterado para " + selectedIcon.name() + "!");
            openManageMenu(player, chestNumber);
        }
    }

    // Captura o nome do Enderchest no chat
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!renamingChest.containsKey(uuid)) return;

        event.setCancelled(true);
        int chestNumber = renamingChest.remove(uuid);
        String newName = event.getMessage().trim();
        if (newName.length() > 32) {
            player.sendMessage("§cO nome do Enderchest não pode exceder 32 caracteres!");
            return;
        }

        // Atualiza o nome no banco
        setChestCustomName(player, chestNumber, newName);
        Bukkit.getScheduler().runTask(Main.get(), () -> {
            player.sendMessage("§aEnderchest #" + chestNumber + " renomeado para: " + newName);
            openManageMenu(player, chestNumber);
        });
    }

    // Converte ItemStack para String
    private String itemToString(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        return item.getType().name() + ";" + item.getAmount();
    }

    // Converte String para ItemStack
    private ItemStack itemFromString(String data) {
        if (data == null || data.isEmpty()) return null;
        String[] parts = data.split(";");
        try {
            Material material = Material.valueOf(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            return new ItemStack(material, amount);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}