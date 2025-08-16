package me.nd.factionsutils.listeners.itens;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factionsutils.utils.misc.ItemBuilder;
import me.nd.factionsutils.hologram.HologramLibrary;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.dados.RedStoneDAO;
import me.nd.factionsutils.hologram.Hologram;
import me.nd.factionsutils.itens.RelogioRedstone;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RelogioDeRedstoneListener implements Listener {

    private static final String DELAY_TAG = "§§Delay:";
    private static final String METADATA_KEY = "RedstoneClock";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final HashMap<UUID, Double> pendingDelays = new HashMap<>();
    private static final HashMap<UUID, Inventory> openMenus = new HashMap<>();
    private static final HashMap<UUID, Location> playerEditingBlock = new HashMap<>();
    private static final HashMap<Location, RedstoneClock> activeClocks = new HashMap<>();
    private static final HashMap<Location, Hologram> blockHolograms = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (!block.hasMetadata(METADATA_KEY)) {
            return;
        }

        event.setCancelled(true);
        openConfigMenu(player, item, block.getLocation());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.isSimilar(RelogioRedstone.RELOGIO)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        double delay = getDelay(item);

        block.setMetadata(METADATA_KEY, new FixedMetadataValue(Main.get(), true));

        // Initialize clock but do not start it
        RedstoneClock clock = new RedstoneClock(block, delay);
        activeClocks.put(loc, clock);

        // Set block to STAINED_GLASS to indicate "off" state
        block.setType(Material.STAINED_GLASS);
        block.setData((byte) 8);

        Hologram hologram = HologramLibrary.createHologram(
                loc.clone().add(0.5, 0.2, 0.5),
                "§eDelay: §f" + DECIMAL_FORMAT.format(delay) + "s",
                "§eEstado: §cDesligado"
        );
        blockHolograms.put(loc, hologram);

        RedStoneDAO.saveRedstoneClock(loc, delay, false); // Save as inactive

        player.sendMessage("§aRelógio de redstone colocado (desligado). Configure para ativar!");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.hasMetadata(METADATA_KEY)) return;

        cleanupBlock(block.getLocation());
        event.getPlayer().sendMessage("§cRelógio de redstone desativado!");
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.hasMetadata(METADATA_KEY)) {
                cleanupBlock(block.getLocation());
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.hasMetadata(METADATA_KEY)) {
                cleanupBlock(block.getLocation());
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.hasMetadata(METADATA_KEY)) {
                cleanupBlock(block.getLocation());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        if (!inv.equals(openMenus.get(player.getUniqueId()))) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        double currentDelay = pendingDelays.getOrDefault(player.getUniqueId(), RelogioRedstone.DEFAULT_DELAY);
        Location blockLoc = playerEditingBlock.get(player.getUniqueId());

        if (slot == 10) { // Increase
            if (currentDelay < RelogioRedstone.MAX_DELAY) {
                currentDelay = Math.min(RelogioRedstone.MAX_DELAY, currentDelay + 0.1);
                updateMenu(player, currentDelay);
            }
        } else if (slot == 28) { // Decrease
            if (currentDelay > RelogioRedstone.MIN_DELAY) {
                currentDelay = Math.max(RelogioRedstone.MIN_DELAY, currentDelay - 0.1);
                updateMenu(player, currentDelay);
            }
        } else if (slot == 24) { // Invasion
            currentDelay = RelogioRedstone.INVASION_DELAY;
            updateMenu(player, currentDelay);
        } else if (slot == 25) { // Counter
            currentDelay = RelogioRedstone.COUNTER_DELAY;
            updateMenu(player, currentDelay);
        } else if (slot == 21) { // Confirm
            if (blockLoc != null) {
                RedstoneClock clockObj = activeClocks.get(blockLoc);
                if (clockObj != null) {
                    clockObj.updateDelay(currentDelay);
                    clockObj.start(); // Start the clock
                    Hologram hologram = blockHolograms.get(blockLoc);
                    if (hologram != null) {
                        hologram.setLines(
                                "§eDelay: §f" + DECIMAL_FORMAT.format(currentDelay) + "s",
                                "§eEstado: §aLigado"
                        );
                    }
                    RedStoneDAO.saveRedstoneClock(blockLoc, currentDelay, true);
                    player.sendMessage("§aDelay do bloco atualizado para " + DECIMAL_FORMAT.format(currentDelay) + "s e ativado!");
                } else {
                    player.sendMessage("§cErro: Bloco não encontrado ou não é um relógio de redstone!");
                }
                // Update the item in hand if it's the redstone clock
                ItemStack clock = player.getInventory().getItemInHand();
                if (clock != null && clock.isSimilar(RelogioRedstone.RELOGIO)) {
                    setDelay(clock, currentDelay);
                    player.getInventory().setItemInHand(clock);
                }
            } else {
                player.sendMessage("§cErro: Nenhum bloco selecionado para configuração!");
            }
            cleanup(player);
            player.closeInventory();
        } else if (slot == 22) { // Cancel (Desligar)
            if (blockLoc != null) {
                RedstoneClock clockObj = activeClocks.get(blockLoc);
                if (clockObj != null) {
                    clockObj.stop(); // Stop the clock
                    Block block = blockLoc.getBlock();
                    block.setType(Material.STAINED_GLASS);
                    block.setData((byte) 8);
                    Hologram hologram = blockHolograms.get(blockLoc);
                    if (hologram != null) {
                        hologram.setLines(
                                "§eDelay: §f" + DECIMAL_FORMAT.format(currentDelay) + "s",
                                "§cEstado: Desligado"
                        );
                    }
                    RedStoneDAO.saveRedstoneClock(blockLoc, currentDelay, false);
                    player.sendMessage("§cRelógio de redstone desligado!");
                }
            }
            cleanup(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (openMenus.containsKey(player.getUniqueId())) {
            cleanup(player);
        }
    }

    private void openConfigMenu(Player player, ItemStack clock, Location blockLoc) {
        Inventory inv = Bukkit.createInventory(null, 45, "§8Relógio de Redstone");

        double currentDelay = RedStoneDAO.loadRedstoneClockDelay(blockLoc);
        RedstoneClock clockObj = activeClocks.get(blockLoc);
        boolean isActive = clockObj != null && clockObj.isRunning();
        if (clockObj != null) {
            currentDelay = clockObj.delaySeconds;
        }

        pendingDelays.put(player.getUniqueId(), currentDelay);
        playerEditingBlock.put(player.getUniqueId(), blockLoc);

        ItemStack display = new ItemBuilder(Material.REDSTONE)
                .setName("§eDelay Atual: §f" + DECIMAL_FORMAT.format(currentDelay) + "s")
                .setLore(
                        "§7Ajuste o delay usando os botões abaixo.",
                        "§7Estado: " + (isActive ? "§aLigado" : "§cDesligado")
                )
                .toItemStack();
        inv.setItem(13, display);

        ItemStack increase = new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§c§l+")
                .setLore("§7Clique para aumentar o delay em 0.1s.")
                .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzOGQwOGJkMDQwNWMwNWY0N2VhODZkNjY2NDM0MzRmZGQyZThjNDZmZjFlNmY4ODJiYjliZjg5MWM3ZDNhNSJ9fX0=")
                .toItemStack();
        inv.setItem(10, increase);

        ItemStack decrease = new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§c§l-")
                .setLore("§7Clique para diminuir o delay em 0.1s.")
                .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ2YjEyOTNkYjcyOWQwMTBmNTM0Y2UxMzYxYmJjNTVhZTVhOGM4ZjgzYTE5NDdhZmU3YTg2NzMyZWZjMiJ9fX0=")
                .toItemStack();
        inv.setItem(28, decrease);

        ItemStack invasion = new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§cConfiguração de Invasão")
                .setLore(
                        "§7Esta opção define o delay mínimo para",
                        "§7canhões de Invasão (" + DECIMAL_FORMAT.format(RelogioRedstone.INVASION_DELAY) + "s)."
                )
                .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTRmOTBjN2JkNjBiZmQwZGZjMzE4MDhkMDQ4NGQ4YzJkYjk5NTlmNjhkZjkxZmJmMjk0MjNhM2RhNjI0MjlhNiJ9fX0=")
                .toItemStack();
        inv.setItem(24, invasion);

        ItemStack counter = new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§cConfiguração de Counter")
                .setLore(
                        "§7Esta opção define o delay mínimo para",
                        "§7canhões de Counter (" + DECIMAL_FORMAT.format(RelogioRedstone.COUNTER_DELAY) + "s)."
                )
                .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTc5ZmM5MmI0Zjk1NjA5MGQ0YTY3MGM1YmUwNmE1MjZiM2JkYmYxMGU0Y2U2NjkwOTY4ODU3NDA3ZTMxNGVkZCJ9fX0=")
                .toItemStack();
        inv.setItem(25, counter);

        ItemStack confirm = new ItemBuilder(Material.STAINED_CLAY, 1, (byte) 5)
                .setName("§aCONFIRMAR")
                .setLore(
                        "§7Clique para confirmar o delay e",
                        "§7ativar o relógio de redstone"
                )
                .toItemStack();
        inv.setItem(21, confirm);

        ItemStack cancel = new ItemBuilder(Material.STAINED_CLAY, 1, (byte) 14)
                .setName("§cDESLIGAR")
                .setLore(
                        "§7Clique para desligar o",
                        "§7relógio de redstone"
                )
                .toItemStack();
        inv.setItem(22, cancel);

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), inv);
    }

    private void updateMenu(Player player, double delay) {
        pendingDelays.put(player.getUniqueId(), delay);
        Inventory inv = openMenus.get(player.getUniqueId());
        if (inv != null) {
            ItemStack display = new ItemBuilder(Material.REDSTONE)
                    .setName("§eDelay Atual: §f" + DECIMAL_FORMAT.format(delay) + "s")
                    .setLore(
                            "§7Ajuste o delay usando os botões abaixo.",
                            "§7Estado: " + (activeClocks.get(playerEditingBlock.get(player.getUniqueId())).isRunning() ? "§aLigado" : "§cDesligado")
                    )
                    .toItemStack();
            inv.setItem(13, display);
        }
    }

    private double getDelay(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return RelogioRedstone.DEFAULT_DELAY;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return RelogioRedstone.DEFAULT_DELAY;
        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.startsWith(DELAY_TAG)) {
                try {
                    return Double.parseDouble(line.replace(DELAY_TAG, ""));
                } catch (NumberFormatException e) {
                    return RelogioRedstone.DEFAULT_DELAY;
                }
            }
        }
        return RelogioRedstone.DEFAULT_DELAY;
    }

    private void setDelay(ItemStack item, double delay) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> line.startsWith(DELAY_TAG));
        lore.add(DELAY_TAG + DECIMAL_FORMAT.format(delay));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void cleanup(Player player) {
        pendingDelays.remove(player.getUniqueId());
        openMenus.remove(player.getUniqueId());
        playerEditingBlock.remove(player.getUniqueId());
    }

    private static void cleanupBlock(Location loc) {
        RedstoneClock clock = activeClocks.remove(loc);
        if (clock != null) {
            clock.stop();
        }
        Hologram hologram = blockHolograms.remove(loc);
        if (hologram != null) {
            HologramLibrary.removeHologram(hologram);
        }
        Block block = loc.getBlock();
        if (block.hasMetadata(METADATA_KEY)) {
            block.removeMetadata(METADATA_KEY, Main.get());
        }
        RedStoneDAO.removeRedstoneClock(loc);
    }

    public void loadRedstoneClocks() {
        try (PreparedStatement ps = RedStoneDAO.connection.prepareStatement(
                "SELECT location, delay, active FROM redstone_clocks")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String locString = rs.getString("location");
                double delay = rs.getDouble("delay");
                boolean active = rs.getBoolean("active");
                Location loc = RedStoneDAO.stringToLocation(locString);
                Block block = loc.getBlock();

                if (block.hasMetadata(METADATA_KEY)) {
                    RedstoneClock clock = new RedstoneClock(block, delay);
                    activeClocks.put(loc, clock);
                    if (active) {
                        clock.start();
                    } else {
                        block.setType(Material.STAINED_GLASS);
                        block.setData((byte) 8);
                    }

                    Hologram hologram = HologramLibrary.createHologram(
                            loc.clone().add(0.5, 0.2, 0.5),
                            "§eDelay: §f" + DECIMAL_FORMAT.format(delay) + "s",
                            "§" + (active ? "eEstado: §aLigado" : "eEstado: §cDesligado")
                    );
                    blockHolograms.put(loc, hologram);
                } else {
                    RedStoneDAO.removeRedstoneClock(loc);
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao carregar relógios de redstone do SQLite:");
            e.printStackTrace();
        }
    }

    private static class RedstoneClock {
        private final Block block;
        private double delaySeconds;
        private BukkitRunnable task;
        private boolean isRedstone;
        private boolean isRunning;

        public RedstoneClock(Block block, double delaySeconds) {
            this.block = block;
            this.delaySeconds = delaySeconds;
            this.isRedstone = false;
            this.isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void start() {
            if (task != null) {
                task.cancel();
            }
            isRunning = true;
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!block.getChunk().isLoaded() || !block.hasMetadata(METADATA_KEY)) {
                        cancel();
                        cleanupBlock(block.getLocation());
                        return;
                    }

                    if (isRedstone) {
                        block.setType(Material.STAINED_GLASS);
                        block.setData((byte) 8);
                        isRedstone = false;
                    } else {
                        block.setType(Material.REDSTONE_BLOCK);
                        isRedstone = true;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (block.getType() == Material.REDSTONE_BLOCK) {
                                    block.setType(Material.STAINED_GLASS);
                                    block.setData((byte) 8);
                                    isRedstone = false;
                                }
                            }
                        }.runTaskLater(Main.get(), 1L);
                    }
                }
            };
            long delayTicks = (long) (delaySeconds * 20);
            task.runTaskTimer(Main.get(), delayTicks, delayTicks);
        }

        public void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (block.getType() == Material.REDSTONE_BLOCK) {
                block.setType(Material.STAINED_GLASS);
                block.setData((byte) 8);
            }
            isRedstone = false;
            isRunning = false;
        }

        public void updateDelay(double newDelay) {
            this.delaySeconds = newDelay;
            if (isRunning) {
                start(); // Restart with new delay if running
            }
        }
    }
}