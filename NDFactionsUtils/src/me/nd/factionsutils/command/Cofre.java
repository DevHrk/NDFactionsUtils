package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.dados.SQlite;
import me.nd.factionsutils.utils.misc.ItemBuilder;
import me.nd.factionutils.cash.CashManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cofre extends Commands implements Listener {

    private static FileConfiguration config = Main.get().getConfig();
    private static Connection connection = SQlite.getConnection();
    private static final HashMap<UUID, Integer> selectedVaults = new HashMap<>();
    private static final HashMap<UUID, String> openedChestLocations = new HashMap<>();
    private static final ArrayList<UUID> restrictedPlayers = new ArrayList<>();
    private static final HashMap<UUID, Integer> menuContext = new HashMap<>();
    
    public Cofre() {
        super("cofre");
    }

    public static int getUnlockedVaults(Player player) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT unlocked_vaults FROM player_vaults WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int vaults = rs.getInt("unlocked_vaults");
                rs.close();
                ps.close();
                return Math.min(vaults, 3);
            }
            rs.close();
            ps.close();
            PreparedStatement insert = connection.prepareStatement("INSERT INTO player_vaults (uuid, unlocked_vaults) VALUES (?, 1)");
            insert.setString(1, player.getUniqueId().toString());
            insert.executeUpdate();
            insert.close();
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static void unlockVault(Player player, int vaultNumber) {
        if (vaultNumber < 1 || vaultNumber > 3) {
            player.sendMessage("§cNúmero de cofre inválido! Use 1, 2 ou 3.");
            return;
        }
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE player_vaults SET unlocked_vaults = ? WHERE uuid = ?");
            ps.setInt(1, vaultNumber);
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao desbloquear o cofre!");
        }
    }

    public static boolean hasVaultAccess(Player player, String ownerUUID, int vaultNumber) {
        if (player.getUniqueId().toString().equals(ownerUUID)) return true;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT accepted FROM invites WHERE inviter_uuid = ? AND invitee_uuid = ? AND vault_number = ?");
            ps.setString(1, ownerUUID);
            ps.setString(2, player.getUniqueId().toString());
            ps.setInt(3, vaultNumber);
            ResultSet rs = ps.executeQuery();
            boolean access = rs.next() && rs.getBoolean("accepted");
            rs.close();
            ps.close();
            return access;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void invitePlayer(Player inviter, Player invitee, int vaultNumber) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO invites (inviter_uuid, invitee_uuid, vault_number, accepted) VALUES (?, ?, ?, 0)");
            ps.setString(1, inviter.getUniqueId().toString());
            ps.setString(2, invitee.getUniqueId().toString());
            ps.setInt(3, vaultNumber);
            ps.executeUpdate();
            ps.close();
            inviter.sendMessage("§aConvite enviado para " + invitee.getName() + " para o Cofre #" + vaultNumber + "!");
            invitee.sendMessage("§aVocê recebeu um convite de " + inviter.getName() + " para o Cofre #" + vaultNumber + "!");
        } catch (SQLException e) {
            e.printStackTrace();
            inviter.sendMessage("§cErro ao enviar convite!");
        }
    }

    private void makePlayerInvisible(Player player, int vaultNumber) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId())) {
                Integer otherVault = selectedVaults.get(other.getUniqueId());
                String otherOwnerUUID = other.getUniqueId().toString();
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT inviter_uuid FROM invites WHERE invitee_uuid = ? AND vault_number = ? AND accepted = 1")) {
                    ps.setString(1, other.getUniqueId().toString());
                    ps.setInt(2, vaultNumber);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        otherOwnerUUID = rs.getString("inviter_uuid");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (otherVault == null || otherVault != vaultNumber || !hasVaultAccess(other, otherOwnerUUID, vaultNumber)) {
                    other.hidePlayer(player);
                } else {
                    other.showPlayer(player);
                }
            }
        }
    }

    private static void restorePlayerVisibility(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId())) {
                other.showPlayer(player);
            }
        }
    }

    public static void acceptInvite(Player invitee, String inviterUUID, int vaultNumber) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE invites SET accepted = 1 WHERE inviter_uuid = ? AND invitee_uuid = ? AND vault_number = ?");
            ps.setString(1, inviterUUID);
            ps.setString(2, invitee.getUniqueId().toString());
            ps.setInt(3, vaultNumber);
            ps.executeUpdate();
            ps.close();
            invitee.sendMessage("§aConvite aceito! Você agora tem acesso ao Cofre #" + vaultNumber + ".");
        } catch (SQLException e) {
            e.printStackTrace();
            invitee.sendMessage("§cErro ao aceitar convite!");
        }
    }

    public static void declineInvite(Player invitee, String inviterUUID, int vaultNumber) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM invites WHERE inviter_uuid = ? AND invitee_uuid = ? AND vault_number = ?");
            ps.setString(1, inviterUUID);
            ps.setString(2, invitee.getUniqueId().toString());
            ps.setInt(3, vaultNumber);
            ps.executeUpdate();
            ps.close();
            invitee.sendMessage("§cConvite recusado para o Cofre #" + vaultNumber + ".");
        } catch (SQLException e) {
            e.printStackTrace();
            invitee.sendMessage("§cErro ao recusar convite!");
        }
    }

    public static void revokeInvite(Player inviter, String inviteeUUID, int vaultNumber) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM invites WHERE inviter_uuid = ? AND invitee_uuid = ? AND vault_number = ?");
            ps.setString(1, inviter.getUniqueId().toString());
            ps.setString(2, inviteeUUID);
            ps.setInt(3, vaultNumber);
            ps.executeUpdate();
            ps.close();
            inviter.sendMessage("§cConvite revogado para o Cofre #" + vaultNumber + ".");
            Player invitee = Bukkit.getPlayer(UUID.fromString(inviteeUUID));
            if (invitee != null && invitee.isOnline()) {
                invitee.sendMessage("§cSeu acesso ao Cofre #" + vaultNumber + " de " + inviter.getName() + " foi revogado!");
                selectedVaults.remove(invitee.getUniqueId());
                openedChestLocations.remove(invitee.getUniqueId());
                restrictedPlayers.remove(invitee.getUniqueId());
                restorePlayerVisibility(invitee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            inviter.sendMessage("§cErro ao revogar convite!");
        }
    }

    public static void setVaultEntrySpawn(Player player) {
        if (!player.hasPermission("factionsutils.admin")) {
            player.sendMessage("§cVocê não tem permissão para definir o spawn de entrada!");
            return;
        }
        Location location = player.getLocation();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO vault_entry_spawn (id, location) VALUES (1, ?)");
            String locStr = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
            ps.setString(1, locStr);
            ps.executeUpdate();
            ps.close();
            player.sendMessage("§aSpawn de entrada dos cofres definido com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao definir o spawn de entrada!");
        }
    }

    public static Location getVaultEntrySpawn() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT location FROM vault_entry_spawn WHERE id = 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String locStr = rs.getString("location");
                String[] parts = locStr.split(",");
                return new Location(
                        Bukkit.getWorld(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3])
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setVaultExitSpawn(Player player) {
        if (!player.hasPermission("factionsutils.admin")) {
            player.sendMessage("§cVocê não tem permissão para definir o spawn de saída!");
            return;
        }
        Location location = player.getLocation();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO vault_exit_spawn (id, location) VALUES (1, ?)");
            String locStr = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
            ps.setString(1, locStr);
            ps.executeUpdate();
            ps.close();
            player.sendMessage("§aSpawn de saída dos cofres definido com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao definir o spawn de saída!");
        }
    }

    public static Location getVaultExitSpawn() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT location FROM vault_exit_spawn WHERE id = 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String locStr = rs.getString("location");
                String[] parts = locStr.split(",");
                return new Location(
                        Bukkit.getWorld(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3])
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveVaultContents(Player owner, int vaultNumber, String location, Inventory inventory) {
        String ownerUUID = owner != null ? owner.getUniqueId().toString() : null;
        try {
            PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM vault_contents WHERE uuid = ? AND vault_number = ? AND location = ?");
            delete.setString(1, ownerUUID);
            delete.setInt(2, vaultNumber);
            delete.setString(3, location);
            delete.executeUpdate();
            delete.close();

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    Map<String, Object> serializedItem = item.serialize();
                    String jsonItem = new Gson().toJson(serializedItem);
                    PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO vault_contents (uuid, vault_number, location, slot, item) VALUES (?, ?, ?, ?, ?)");
                    insert.setString(1, ownerUUID);
                    insert.setInt(2, vaultNumber);
                    insert.setString(3, location);
                    insert.setInt(4, slot);
                    insert.setString(5, jsonItem);
                    insert.executeUpdate();
                    insert.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Inventory loadVaultContents(String ownerUUID, int vaultNumber, String location) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT is_double FROM vault_chests WHERE location = ? AND vault_number = ?")) {
            ps.setString(1, location);
            ps.setInt(2, vaultNumber);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, "Cofre - " + vaultNumber);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT slot, item FROM vault_contents WHERE uuid = ? AND vault_number = ? AND location = ?")) {
            ps.setString(1, ownerUUID);
            ps.setInt(2, vaultNumber);
            ps.setString(3, location);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int slot = rs.getInt("slot");
                String jsonItem = rs.getString("item");
                if (jsonItem != null && slot < size) {
                    Map<String, Object> serializedItem = new Gson().fromJson(jsonItem, Map.class);
                    ItemStack item = ItemStack.deserialize(serializedItem);
                    inv.setItem(slot, item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inv;
    }
    private static final HashMap<UUID, Integer> iconMenuPage = new HashMap<>();
    // Updated openIconSelectionMenu method with over 100 icons and pagination
    private void openIconSelectionMenu(Player player, int vaultNumber, int page) {
        Inventory inv = Bukkit.createInventory(null, 27, "Cofre #" + vaultNumber + " - Escolher Ícone");
        Material[] iconOptions = {
            Material.CHEST, Material.ENDER_CHEST, Material.TRAPPED_CHEST,
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
            Material.REDSTONE, Material.LAPIS_ORE, Material.COAL,
            Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK,
            Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.COAL_BLOCK,
            Material.NETHER_BRICK_ITEM,
            Material.OBSIDIAN, Material.BEDROCK,
            Material.STONE,
            Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
            Material.SANDSTONE,
            Material.RED_SANDSTONE,
            Material.QUARTZ_BLOCK, 
            Material.PRISMARINE,
            Material.SEA_LANTERN, Material.GLOWSTONE, 
            Material.IRON_SWORD, Material.DIAMOND_SWORD,
            Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.IRON_AXE, Material.DIAMOND_AXE,
            Material.IRON_HOE, Material.DIAMOND_HOE,
            Material.IRON_HELMET, Material.DIAMOND_HELMET, 
            Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, 
            Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, 
            Material.IRON_BOOTS, Material.DIAMOND_BOOTS,
            Material.BOW, 
            Material.FISHING_ROD, Material.FLINT_AND_STEEL, Material.SHEARS,
            Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.MILK_BUCKET,
            Material.COMPASS,
            Material.ENDER_PEARL, Material.BLAZE_ROD, Material.NETHER_STAR,
             Material.BEACON,
            Material.DRAGON_EGG
        };

        int iconsPerPage = 21; // Slots 0-20 for icons
        int totalPages = (int) Math.ceil((double) iconOptions.length / iconsPerPage);
        page = Math.max(0, Math.min(page, totalPages - 1)); // Clamp page number
        iconMenuPage.put(player.getUniqueId(), page);

        int startIndex = page * iconsPerPage;
        int endIndex = Math.min(startIndex + iconsPerPage, iconOptions.length);

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Material material = iconOptions[i];
            ItemStack item = new ItemBuilder(material)
                    .setName("§a" + material.name().replace("_", " "))
                    .setLore("§7Clique para definir como ícone do Cofre #" + vaultNumber)
                    .toItemStack();
            inv.setItem(slot++, item);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack prevPage = new ItemBuilder(Material.ARROW)
                    .setName("§ePágina Anterior")
                    .setLore("§7Clique para ver a página anterior")
                    .toItemStack();
            inv.setItem(25, prevPage);
        }
        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemBuilder(Material.ARROW)
                    .setName("§ePróxima Página")
                    .setLore("§7Clique para ver a próxima página")
                    .toItemStack();
            inv.setItem(26, nextPage);
        }

        menuContext.put(player.getUniqueId(), vaultNumber);
        player.openInventory(inv);
    }

    // Overloaded method to default to page 0
    private void openIconSelectionMenu(Player player, int vaultNumber) {
        openIconSelectionMenu(player, vaultNumber, 0);
    }
    
    private void setVaultIcon(Player player, int vaultNumber, Material icon) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO vault_icons (uuid, vault_number, icon) VALUES (?, ?, ?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, vaultNumber);
            ps.setString(3, icon.name());
            ps.executeUpdate();
            ps.close();
            player.sendMessage("§aÍcone do Cofre #" + vaultNumber + " alterado para " + icon.name().replace("_", " ") + "!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao alterar o ícone do cofre!");
        }
    }

    private Material getVaultIcon(Player player, int vaultNumber) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT icon FROM vault_icons WHERE uuid = ? AND vault_number = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, vaultNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String iconName = rs.getString("icon");
                return Material.getMaterial(iconName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Material.CHEST; // Ícone padrão
    }

    private void openVaultActionMenu(Player player, int vaultNumber) {
        Inventory inv = Bukkit.createInventory(null, 27, "Cofre #" + vaultNumber + " - Ações");
        ItemStack teleport = new ItemBuilder(Material.ENDER_PEARL)
                .setName("§aTeleportar")
                .setLore("§7Clique para teleportar ao cofre #" + vaultNumber)
                .toItemStack();
        ItemStack changeIcon = new ItemBuilder(Material.NAME_TAG)
                .setName("§eMudar Ícone")
                .setLore("§7Clique para mudar o ícone do cofre #" + vaultNumber)
                .toItemStack();
        inv.setItem(11, teleport);
        inv.setItem(15, changeIcon);
        menuContext.put(player.getUniqueId(), vaultNumber);
        player.openInventory(inv);
    }
    
    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openVaultMenu(player);
        } else if (args[0].equalsIgnoreCase("convidar") && args.length == 2) {
            int vaultNumber = getUnlockedVaults(player);
            if (vaultNumber < 1 || vaultNumber > 3) {
                player.sendMessage("§cCofre inválido!");
                return;
            }
            Player invitee = Bukkit.getPlayer(args[1]);
            if (invitee == null || !invitee.isOnline()) {
                player.sendMessage("§cJogador não encontrado ou offline!");
                return;
            }
            if (invitee.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cVocê não pode convidar a si mesmo!");
                return;
            }
            invitePlayer(player, invitee, vaultNumber);
        } else if (args[0].equalsIgnoreCase("expulsar") && args.length == 2) {
            int vaultNumber = getUnlockedVaults(player);
            if (vaultNumber < 1 || vaultNumber > 3) {
                player.sendMessage("§cCofre inválido!");
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cJogador não encontrado!");
                return;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cVocê não pode expulsar a si mesmo!");
                return;
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM invites WHERE inviter_uuid = ? AND invitee_uuid = ? AND vault_number = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, target.getUniqueId().toString());
                ps.setInt(3, vaultNumber);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    player.sendMessage("§cEste jogador não tem convite ou acesso ao seu Cofre #" + vaultNumber + "!");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage("§cErro ao verificar convite!");
                return;
            }
            revokeInvite(player, target.getUniqueId().toString(), vaultNumber);
            restrictedPlayers.remove(target.getUniqueId());
            selectedVaults.remove(target.getUniqueId());
            openedChestLocations.remove(target.getUniqueId());
            player.sendMessage("§aJogador " + target.getName() + " expulso do Cofre #" + vaultNumber + "!");
            if (target.isOnline()) {
                target.sendMessage("§cVocê foi expulso do Cofre #" + vaultNumber + " de " + player.getName() + "!");
            }
        } else if (args[0].equalsIgnoreCase("setarea")) {
            openAreaMenu(player);
        } else if (args[0].equalsIgnoreCase("addchest")) {
            if (!player.hasPermission("factionsutils.admin")) {
                player.sendMessage("§cVocê não tem permissão para adicionar baús!");
                return;
            }
            Integer vaultNumber = selectedVaults.get(player.getUniqueId());
            if (vaultNumber == null) {
                player.sendMessage("§cVocê deve selecionar um cofre primeiro no menu!");
                return;
            }
            int unlockedVaults = getUnlockedVaults(player);
            if (vaultNumber > unlockedVaults) {
                player.sendMessage("§cVocê não tem acesso ao Cofre #" + vaultNumber + "!");
                return;
            }
            Block target = player.getTargetBlock((HashSet<Material>) null, 5);
            if (target == null || target.getType() != Material.CHEST) {
                player.sendMessage("§cVocê deve estar olhando para um baú!");
                return;
            }
            addVaultChest(player, target.getLocation(), vaultNumber);
        } else if (args[0].equalsIgnoreCase("setspawn") && args.length == 2) {
            if (!player.hasPermission("factionsutils.admin")) {
                player.sendMessage("§cVocê não tem permissão para setar o spawn");
                return;
            }
            if (args[1].equalsIgnoreCase("saida")) {
                setVaultExitSpawn(player);
            } else if (args[1].equalsIgnoreCase("entrada")) {
                setVaultEntrySpawn(player);
            } else {
                player.sendMessage("§cUso: /cofre setspawn [saida|entrada]");
            }
        } else if (args[0].equalsIgnoreCase("sair")) {
            selectedVaults.remove(player.getUniqueId());
            openedChestLocations.remove(player.getUniqueId());
            restrictedPlayers.remove(player.getUniqueId());
            String locationStr = openedChestLocations.get(player.getUniqueId());
            Integer vaultNumber = selectedVaults.getOrDefault(player.getUniqueId(), 1);
            markVaultAsClosed(locationStr, vaultNumber, player.getUniqueId());
            restorePlayerVisibility(player);
            Location exitSpawn = getVaultExitSpawn();
            if (exitSpawn != null) {
                player.teleport(exitSpawn);
                player.sendMessage("§aTeleportado para o spawn de saída!");
            } else {
                player.sendMessage("§aSeleção de cofre removida com sucesso!");
            }
        } else if (args[0].matches("\\d+")) {
            int vaultNumber = Integer.parseInt(args[0]);
            int unlockedVaults = getUnlockedVaults(player);
            if (vaultNumber < 1 || vaultNumber > unlockedVaults) {
                player.sendMessage("§cVocê não tem acesso ao Cofre #" + vaultNumber + "!");
                return;
            }
            openVaultActionMenu(player, vaultNumber);
        } else {
            player.sendMessage("§eComandos Disponiveis:");
            player.sendMessage(" §8- §f/cofre convidar <jogador> - §7Convida para o cofre");
            player.sendMessage(" §8- §f/cofre expulsar <jogador> - §7Expulsa do cofre e retira do convidados");
            player.sendMessage(" §8- §f/cofre sair - §7Sai do cofre");
        }
    }

    private void openAreaMenu(Player player) {
        if (!player.hasPermission("factionsutils.admin")) {
            player.sendMessage("§cVocê não tem permissão para definir a área dos cofres!");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, "Definir Área dos Cofres");
        ItemStack pos1 = new ItemBuilder(Material.REDSTONE_TORCH_ON)
                .setName("§cSelecionar Posição 1")
                .setLore("§7Clique para definir a primeira posição da área.")
                .toItemStack();
        ItemStack pos2 = new ItemBuilder(Material.REDSTONE_TORCH_ON)
                .setName("§cSelecionar Posição 2")
                .setLore("§7Clique para definir a segunda posição da área.")
                .toItemStack();
        ItemStack confirm = new ItemBuilder(Material.EMERALD)
                .setName("§aConfirmar Área")
                .setLore("§7Clique para salvar a área selecionada.")
                .toItemStack();
        inv.setItem(11, pos1);
        inv.setItem(15, pos2);
        inv.setItem(13, confirm);
        player.openInventory(inv);
    }

    private static final HashMap<UUID, Location> pos1Selections = new HashMap<>();
    private static final HashMap<UUID, Location> pos2Selections = new HashMap<>();

    public static void setVaultArea(Player player, Location pos1, Location pos2) {
        if (!player.hasPermission("factionsutils.admin")) {
            player.sendMessage("§cVocê não tem permissão para definir a área dos cofres!");
            return;
        }
        try {
            String pos1Str = pos1.getWorld().getName() + "," + pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ();
            String pos2Str = pos2.getWorld().getName() + "," + pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ();
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO vault_area (id, pos1, pos2) VALUES (1, ?, ?)");
            ps.setString(1, pos1Str);
            ps.setString(2, pos2Str);
            ps.executeUpdate();
            ps.close();
            player.sendMessage("§aÁrea dos cofres definida com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao definir área!");
        }
    }

    public static String getVaultArea() {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT pos1, pos2 FROM vault_area WHERE id = 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String area = rs.getString("pos1") + ";" + rs.getString("pos2");
                return area;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addVaultChest(Player player, Location location, int vaultNumber) {
        if (!player.hasPermission("factionsutils.admin")) {
            player.sendMessage("§cVocê não tem permissão para adicionar baús!");
            return;
        }
        String area = getVaultArea();
        if (area == null) {
            player.sendMessage("§cA área dos cofres ainda não foi definida! Use /cofre setarea.");
            return;
        }
        String[] pos1 = area.split(";")[0].split(",");
        String[] pos2 = area.split(";")[1].split(",");
        Location areaPos1 = new Location(Bukkit.getWorld(pos1[0]), Double.parseDouble(pos1[1]), Double.parseDouble(pos1[2]), Double.parseDouble(pos1[3]));
        Location areaPos2 = new Location(Bukkit.getWorld(pos2[0]), Double.parseDouble(pos2[1]), Double.parseDouble(pos2[2]), Double.parseDouble(pos2[3]));
        if (!isLocationInRegion(location, areaPos1, areaPos2)) {
            player.sendMessage("§cO baú deve estar dentro da área definida para os cofres!");
            return;
        }

        Block block = location.getBlock();
        ChestLocations chestLocations = getChestLocations(block);
        String primaryLocStr = getLocationString(chestLocations.primaryLocation);

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT vault_number FROM vault_chests WHERE location = ?")) {
            ps.setString(1, primaryLocStr);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao verificar baú existente!");
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO vault_chests (location, vault_number, is_double) VALUES (?, ?, ?)")) {
            ps.setString(1, primaryLocStr);
            ps.setInt(2, vaultNumber);
            ps.setBoolean(3, chestLocations.isDouble);
            ps.executeUpdate();
            player.sendMessage("§aBaú " + (chestLocations.isDouble ? "duplo" : "simples") + " adicionado ao Cofre #" + vaultNumber + "!");
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao adicionar baú!");
        }
    }

    private static boolean isLocationInRegion(Location loc, Location pos1, Location pos2) {
        if (!loc.getWorld().equals(pos1.getWorld()) || !loc.getWorld().equals(pos2.getWorld())) return false;
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    private static int getVaultChestNumber(Location location) {
        ChestLocations chestLocations = getChestLocations(location.getBlock());
        String primaryLocStr = getLocationString(chestLocations.primaryLocation);

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT vault_number FROM vault_chests WHERE location = ?")) {
            ps.setString(1, primaryLocStr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("vault_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static String getLocationString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void openVaultMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Escolha um cofre");
        int unlockedVaults = getUnlockedVaults(player);
        String currency = config.getString("currency", "cash");

        ItemStack info = new ItemBuilder(Material.BOOK)
                .setName("§eInformações")
                .setLore(
                        "§7Com os cofres você pode guardar",
                        "§7seus itens em segurança, sem ter",
                        "§7medo de ser invadido!",
                        "",
                        "§7Clique em um cofre para teleportar",
                        "§7e carregar seus itens nos baús."
                )
                .toItemStack();
        inv.setItem(31, info);

        ItemStack sentInvites = new ItemBuilder(Material.PAPER)
                .setName("§6Convites enviados")
                .setLore(
                        "§7Use §f/cofre convidar [nick] §7para",
                        "§7convidar alguém para o seu cofre.",
                        "",
                        "§eClique para ver os convites enviados."
                )
                .toItemStack();
        inv.setItem(30, sentInvites);

        ItemStack receivedInvites = new ItemBuilder(Material.MAP)
                .setName("§aConvites recebidos")
                .setLore(
                        "§7Aceite convites e faça parte de",
                        "§7outros cofres.",
                        "",
                        "§eClique para ver os convites recebidos."
                )
                .toItemStack();
        inv.setItem(32, receivedInvites);

        int index = 1;
        for (int slot = 12; slot <= 14 && index <= 3; slot++) {
            int currentIndex = index;
            ItemBuilder builder;
            if (index <= unlockedVaults) {
                Material icon = getVaultIcon(player, index);
                builder = new ItemBuilder(icon)
                        .setName("§cCofre #" + index)
                        .setLore(
                                "",
                                "§7Clique para abrir opções do Cofre #" + currentIndex + "."
                        );
            } else {
                double cost = config.getDouble("vault" + index + "-cost", index == 2 ? 1000.0 : 2000.0);
                builder = new ItemBuilder(Material.BARRIER)
                        .setName("§cCofre #" + index + " (Bloqueado)")
                        .setLore(
                                "",
                                "§7Custo para desbloquear: §f" + cost + " " + (currency.equals("cash") ? "cash" : "dinheiro"),
                                "§eClique para comprar."
                        );
            }
            inv.setItem(slot, builder.toItemStack());
            index++;
        }

        player.openInventory(inv);
    }

    private void openSentInvitesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Convites Enviados");
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT invitee_uuid, vault_number FROM invites WHERE inviter_uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            int slot = 0;
            boolean hasInvites = false;
            while (rs.next() && slot < 27) {
                hasInvites = true;
                String inviteeUUID = rs.getString("invitee_uuid");
                int vaultNumber = rs.getInt("vault_number");
                Player invitee = Bukkit.getPlayer(UUID.fromString(inviteeUUID));
                String inviteeName = invitee != null ? invitee.getName() : "Desconhecido";
                ItemStack item = new ItemBuilder(Material.SKULL_ITEM)
                        .setName("§e" + inviteeName)
                        .setLore(
                                "§7Cofre #" + vaultNumber,
                                "§cClique para revogar convite."
                        )
                        .setSkullOwner(inviteeName)
                        .toItemStack();
                inv.setItem(slot++, item);
            }
            if (!hasInvites) {
                ItemStack web = new ItemBuilder(Material.WEB)
                        .setName("§7Sem Convites")
                        .setLore("§cVocê não enviou convites no momento.")
                        .toItemStack();
                inv.setItem(13, web);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ItemStack web = new ItemBuilder(Material.WEB)
                    .setName("§7Erro")
                    .setLore("§cOcorreu um erro ao carregar os convites.")
                    .toItemStack();
            inv.setItem(13, web);
        }
        player.openInventory(inv);
    }

    private void openReceivedInvitesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Convites Recebidos");
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT inviter_uuid, vault_number, accepted FROM invites WHERE invitee_uuid = ? AND inviter_uuid != ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            int slot = 0;
            boolean hasInvites = false;
            while (rs.next() && slot < 27) {
                hasInvites = true;
                String inviterUUID = rs.getString("inviter_uuid");
                int vaultNumber = rs.getInt("vault_number");
                boolean accepted = rs.getBoolean("accepted");
                Player inviter = Bukkit.getPlayer(UUID.fromString(inviterUUID));
                String inviterName = inviter != null ? inviter.getName() : "Desconhecido";
                ItemBuilder builder = new ItemBuilder(accepted ? Material.EMERALD : Material.PAPER)
                        .setName("§e" + inviterName + " - Cofre #" + vaultNumber);
                if (accepted) {
                    builder.setLore("§7Clique para teleportar e acessar o Cofre #" + vaultNumber + ".");
                } else {
                    builder.setLore(
                            "§aClique esquerdo para aceitar.",
                            "§cClique direito para recusar."
                    );
                }
                inv.setItem(slot++, builder.toItemStack());
            }
            if (!hasInvites) {
                ItemStack web = new ItemBuilder(Material.WEB)
                        .setName("§7Sem Convites")
                        .setLore("§cVocê não tem convites no momento.")
                        .toItemStack();
                inv.setItem(13, web);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ItemStack web = new ItemBuilder(Material.WEB)
                    .setName("§7Erro")
                    .setLore("§cOcorreu um erro ao carregar os convites.")
                    .toItemStack();
            inv.setItem(13, web);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        Player player = event.getPlayer();
        ChestLocations chestLocations = getChestLocations(block);
        String locationStr = getLocationString(chestLocations.primaryLocation);
        int vaultNumber = getVaultChestNumber(block.getLocation());

        if (vaultNumber == -1) return;

        event.setCancelled(true);

        Integer selectedVault = selectedVaults.get(player.getUniqueId());
        if (selectedVault == null) {
            player.sendMessage("§cSelecione um cofre no menu primeiro!");
            return;
        }

        // Verifica se o jogador tem acesso ao cofre específico
        String ownerUUID = player.getUniqueId().toString();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT inviter_uuid FROM invites WHERE invitee_uuid = ? AND vault_number = ? AND accepted = 1")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, selectedVault);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ownerUUID = rs.getString("inviter_uuid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cErro ao verificar acesso ao cofre!");
            return;
        }

        if (!hasVaultAccess(player, ownerUUID, selectedVault)) {
            player.sendMessage("§cVocê não tem acesso ao Cofre #" + selectedVault + "!");
            selectedVaults.remove(player.getUniqueId());
            restrictedPlayers.remove(player.getUniqueId());
            restorePlayerVisibility(player);
            return;
        }

        if (isVaultOpen(locationStr, selectedVault, player.getUniqueId(), ownerUUID)) {
            player.sendMessage("§cO Cofre já está sendo visualizado por outro jogador!");
            return;
        }

        openedChestLocations.put(player.getUniqueId(), locationStr);
        markVaultAsOpen(locationStr, selectedVault, player.getUniqueId());
        Inventory virtualInventory = loadVaultContents(ownerUUID, selectedVault, locationStr);
        player.openInventory(virtualInventory);
    }

    private static final Map<String, Set<UUID>> openVaults = new ConcurrentHashMap<>();

    private void markVaultAsOpen(String location, int vaultNumber, UUID playerUUID) {
        String vaultKey = location + ":" + vaultNumber;
        openVaults.compute(vaultKey, (k, v) -> {
            if (v == null) {
                v = new HashSet<>();
            }
            v.add(playerUUID);
            return v;
        });
    }

    private boolean isVaultOpen(String location, int vaultNumber, UUID playerUUID, String ownerUUID) {
        String vaultKey = location + ":" + vaultNumber;
        Set<UUID> players = openVaults.getOrDefault(vaultKey, new HashSet<>());
        if (players.isEmpty()) {
            return false;
        }
        for (UUID uuid : players) {
            if (!uuid.equals(playerUUID)) {
                Player otherPlayer = Bukkit.getPlayer(uuid);
                if (otherPlayer != null && otherPlayer.isOnline() && hasVaultAccess(otherPlayer, ownerUUID, vaultNumber)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void markVaultAsClosed(String location, int vaultNumber, UUID playerUUID) {
        String vaultKey = location + ":" + vaultNumber;
        openVaults.computeIfPresent(vaultKey, (k, v) -> {
            v.remove(playerUUID);
            return v.isEmpty() ? null : v;
        });
    }

    private static class ChestLocations {
        Location primaryLocation;
        @SuppressWarnings("unused")
		Location secondaryLocation;
        boolean isDouble;

        ChestLocations(Location primary, Location secondary, boolean isDouble) {
            this.primaryLocation = primary;
            this.secondaryLocation = secondary;
            this.isDouble = isDouble;
        }
    }

    private static ChestLocations getChestLocations(Block block) {
        Location primaryLocation = block.getLocation();
        Location secondaryLocation = null;
        boolean isDouble = false;

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (adjacent.getType() == Material.CHEST) {
                secondaryLocation = adjacent.getLocation();
                isDouble = true;
                // Ensure primary location is the "left" or "lower" chest for consistency
                if (adjacent.getX() < block.getX() || (adjacent.getX() == block.getX() && adjacent.getZ() < block.getZ())) {
                    primaryLocation = adjacent.getLocation();
                    secondaryLocation = block.getLocation();
                }
                break;
            }
        }

        return new ChestLocations(primaryLocation, secondaryLocation, isDouble);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals("Escolha um cofre") && !title.equals("Convites Enviados") && 
            !title.equals("Convites Recebidos") && !title.equals("Definir Área dos Cofres") &&
            !title.startsWith("Cofre #") && !title.endsWith("- Ações") && !title.endsWith("- Escolher Ícone")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int slot = event.getSlot();

        if (title.equals("Escolha um cofre")) {
            if (slot >= 12 && slot <= 14) {
                int vaultNumber = slot - 11;
                int unlockedVaults = getUnlockedVaults(player);
                if (vaultNumber <= unlockedVaults) {
                    openVaultActionMenu(player, vaultNumber);
                } else {
                    FileConfiguration config = Main.get().getConfig();
                    double cost = config.getDouble("vault" + vaultNumber + "-cost", vaultNumber == 2 ? 1000.0 : 2000.0);
                    String currency = config.getString("currency", "cash").toLowerCase();
                    boolean success = false;

                    if (currency.equals("cash")) {
                        long longCost = (long) cost;
                        if (CashManager.getCash(player) >= longCost) {
                            if (CashManager.removeCash(player, longCost)) {
                                success = true;
                            } else {
                                player.sendMessage("§cErro ao processar a compra com cash! Tente novamente.");
                            }
                        } else {
                            player.sendMessage("§cVocê não tem §6" + longCost + " cash §cpara desbloquear o cofre #" + vaultNumber + "!");
                        }
                    } else if (currency.equals("vault") && Main.get().getEconomy() != null) {
                        if (Main.get().getEconomy().has(player, cost)) {
                            Main.get().getEconomy().withdrawPlayer(player, cost);
                            success = true;
                        } else {
                            player.sendMessage("§cVocê não tem §6" + cost + " dinheiro §cpara desbloquear o cofre #" + vaultNumber + "!");
                        }
                    } else {
                        player.sendMessage("§cMoeda inválida configurada ou economia do Vault não está disponível!");
                    }

                    if (success) {
                        unlockVault(player, vaultNumber);
                        player.sendMessage("§aCofre #" + vaultNumber + " desbloqueado com sucesso por §6" + cost + " " + (currency.equals("cash") ? "cash" : "dinheiro") + "§a!");
                        openVaultMenu(player);
                    }
                }
            } else if (slot == 30) {
                openSentInvitesMenu(player);
            } else if (slot == 32) {
                openReceivedInvitesMenu(player);
            }
        } else if (title.endsWith("- Ações")) {
            Integer vaultNumber = menuContext.get(player.getUniqueId());
            if (vaultNumber == null) return;

            if (slot == 11) { // Teleportar
                selectedVaults.put(player.getUniqueId(), vaultNumber);
                restrictedPlayers.add(player.getUniqueId());
                makePlayerInvisible(player, vaultNumber);
                Location entrySpawn = getVaultEntrySpawn();
                if (entrySpawn != null) {
                    player.teleport(entrySpawn);
                    player.sendMessage("§aTeleportado para o spawn de entrada dos cofres!");
                } else {
                    player.sendMessage("§cO spawn de entrada dos cofres ainda não foi definido!");
                    selectedVaults.remove(player.getUniqueId());
                    restrictedPlayers.remove(player.getUniqueId());
                    restorePlayerVisibility(player);
                    return;
                }
                player.closeInventory();
                player.sendMessage("§aCofre #" + vaultNumber + " selecionado! Abra um baú para visualizar seu conteúdo.");
            } else if (slot == 15) { // Mudar ícone
                openIconSelectionMenu(player, vaultNumber);
            }
        } else if (title.endsWith("- Escolher Ícone")) {
            Integer vaultNumber = menuContext.get(player.getUniqueId());
            if (vaultNumber == null) return;

            Integer currentPage = iconMenuPage.getOrDefault(player.getUniqueId(), 0);
            if (slot == 25 && currentPage > 0) {
                // Previous page
                openIconSelectionMenu(player, vaultNumber, currentPage - 1);
            } else if (slot == 26 && clickedItem.getType() == Material.ARROW) {
                // Next page
                openIconSelectionMenu(player, vaultNumber, currentPage + 1);
            } else if (slot >= 0 && slot < 21) {
                // Icon selection
                Material[] iconOptions = {
                        Material.CHEST, Material.ENDER_CHEST, Material.TRAPPED_CHEST,
                        Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
                        Material.REDSTONE, Material.LAPIS_ORE, Material.COAL,
                        Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK,
                        Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.COAL_BLOCK,
                        Material.NETHER_BRICK_ITEM,
                        Material.OBSIDIAN, Material.BEDROCK,
                        Material.STONE,
                        Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
                        Material.SANDSTONE,
                        Material.RED_SANDSTONE,
                        Material.QUARTZ_BLOCK, 
                        Material.PRISMARINE,
                        Material.SEA_LANTERN, Material.GLOWSTONE, 
                        Material.IRON_SWORD, Material.DIAMOND_SWORD,
                        Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE,
                        Material.IRON_AXE, Material.DIAMOND_AXE,
                        Material.IRON_HOE, Material.DIAMOND_HOE,
                        Material.IRON_HELMET, Material.DIAMOND_HELMET, 
                        Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, 
                        Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, 
                        Material.IRON_BOOTS, Material.DIAMOND_BOOTS,
                        Material.BOW, 
                        Material.FISHING_ROD, Material.FLINT_AND_STEEL, Material.SHEARS,
                        Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.MILK_BUCKET,
                        Material.COMPASS,
                        Material.ENDER_PEARL, Material.BLAZE_ROD, Material.NETHER_STAR,
                        Material.BEACON,
                        Material.DRAGON_EGG
                };
                int iconsPerPage = 21;
                int index = currentPage * iconsPerPage + slot;
                if (index < iconOptions.length) {
                    Material icon = iconOptions[index];
                    setVaultIcon(player, vaultNumber, icon);
                    openVaultMenu(player);
                }
            }
        } else if (title.equals("Convites Enviados")) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT invitee_uuid, vault_number FROM invites WHERE inviter_uuid = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                List<String> inviteeUUIDs = new ArrayList<>();
                List<Integer> vaultNumbers = new ArrayList<>();
                while (rs.next()) {
                    inviteeUUIDs.add(rs.getString("invitee_uuid"));
                    vaultNumbers.add(rs.getInt("vault_number"));
                }
                if (slot < inviteeUUIDs.size()) {
                    revokeInvite(player, inviteeUUIDs.get(slot), vaultNumbers.get(slot));
                    openSentInvitesMenu(player);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (title.equals("Convites Recebidos")) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT inviter_uuid, vault_number, accepted FROM invites WHERE invitee_uuid = ? AND inviter_uuid != ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                List<String> inviterUUIDs = new ArrayList<>();
                List<Integer> vaultNumbers = new ArrayList<>();
                List<Boolean> acceptedList = new ArrayList<>();
                while (rs.next()) {
                    inviterUUIDs.add(rs.getString("inviter_uuid"));
                    vaultNumbers.add(rs.getInt("vault_number"));
                    acceptedList.add(rs.getBoolean("accepted"));
                }
                if (slot < inviterUUIDs.size()) {
                    String inviterUUID = inviterUUIDs.get(slot);
                    int vaultNumber = vaultNumbers.get(slot);
                    boolean accepted = acceptedList.get(slot);
                    if (accepted) {
                        if (hasVaultAccess(player, inviterUUID, vaultNumber)) {
                            openVaultActionMenu(player, vaultNumber);
                        } else {
                            player.sendMessage("§cAcesso ao Cofre #" + vaultNumber + " revogado!");
                            restrictedPlayers.remove(player.getUniqueId());
                            restorePlayerVisibility(player);
                        }
                    } else {
                        if (event.isLeftClick()) {
                            acceptInvite(player, inviterUUID, vaultNumber);
                            openReceivedInvitesMenu(player);
                        } else if (event.isRightClick()) {
                            declineInvite(player, inviterUUID, vaultNumber);
                            openReceivedInvitesMenu(player);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (title.equals("Definir Área dos Cofres")) {
            if (slot == 11) {
                pos1Selections.put(player.getUniqueId(), player.getLocation());
                player.sendMessage("§aPosição 1 definida! Agora selecione a Posição 2.");
            } else if (slot == 15) {
                pos2Selections.put(player.getUniqueId(), player.getLocation());
                player.sendMessage("§aPosição 2 definida! Clique em Confirmar para salvar a área.");
            } else if (slot == 13) {
                Location pos1 = pos1Selections.get(player.getUniqueId());
                Location pos2 = pos2Selections.get(player.getUniqueId());
                if (pos1 == null || pos2 == null) {
                    player.sendMessage("§cVocê precisa definir ambas as posições primeiro!");
                    return;
                }
                if (!pos1.getWorld().equals(pos2.getWorld())) {
                    player.sendMessage("§cAs posições devem estar no mesmo mundo!");
                    return;
                }
                setVaultArea(player, pos1, pos2);
                pos1Selections.remove(player.getUniqueId());
                pos2Selections.remove(player.getUniqueId());
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();

        if (title.equals("Escolha um cofre") || title.equals("Convites Enviados") || title.equals("Convites Recebidos") || title.equals("Definir Área dos Cofres")) {
            if (!selectedVaults.containsKey(player.getUniqueId())) {
                selectedVaults.remove(player.getUniqueId());
                restrictedPlayers.remove(player.getUniqueId());
                restorePlayerVisibility(player);
            }
            return;
        }

        if (!title.startsWith("Cofre -")) return;

        Integer vaultNumber = selectedVaults.getOrDefault(player.getUniqueId(), 1);
        String ownerUUID = player.getUniqueId().toString();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT inviter_uuid FROM invites WHERE invitee_uuid = ? AND vault_number = ? AND accepted = 1")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, vaultNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ownerUUID = rs.getString("inviter_uuid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (hasVaultAccess(player, ownerUUID, vaultNumber)) {
            String locationStr = openedChestLocations.get(player.getUniqueId());
            if (locationStr == null) {
                return;
            }

            Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));
            if (owner != null) {
                saveVaultContents(owner, vaultNumber, locationStr, inventory);
            } else {
                player.sendMessage("§cErro: Proprietário do cofre não está online!");
            }

            markVaultAsClosed(locationStr, vaultNumber, player.getUniqueId());
            openedChestLocations.remove(player.getUniqueId());
            restrictedPlayers.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String locationStr = openedChestLocations.get(player.getUniqueId());
        if (locationStr != null) {
            Integer vaultNumber = selectedVaults.getOrDefault(player.getUniqueId(), 1);
            markVaultAsClosed(locationStr, vaultNumber, player.getUniqueId());
            openedChestLocations.remove(player.getUniqueId());
            restrictedPlayers.remove(player.getUniqueId());
            selectedVaults.remove(player.getUniqueId());
            restorePlayerVisibility(player);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (player.hasPermission("factionsutils.admin")) {
            return;
        }

        if (restrictedPlayers.contains(player.getUniqueId())) {
            if (command.startsWith("/cofre") || command.startsWith("/l ") || command.equals("/l") ||
                command.startsWith("/g ") || command.equals("/g") || command.startsWith("/. ") || command.equals("/.") || command.startsWith("/a ") || command.equals("/a")) {
                return;
            }
            event.setCancelled(true);
            player.sendMessage("§cVocê só pode usar §e/cofre§c enquanto estiver no cofre!");
        }
    }
}