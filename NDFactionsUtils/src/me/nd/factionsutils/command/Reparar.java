package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Reparar extends Commands implements Listener {
    private final Map<Player, Long> repairCooldowns = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long COOLDOWN_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final String FRAGMENT_NAME = "§bFragmento de Reparo";
    private static final String VIP_PERMISSION = "factionsutils.repair.vip";
    private static final double VIP_DISCOUNT = 0.5; // 50% discount for VIPs
    private static final String GIVE_PERMISSION = "factionsutils.repair.give";
    
    private static class RepairInventoryHolder implements InventoryHolder {
        private final RepairInfo repairInfo;

        RepairInventoryHolder(RepairInfo repairInfo) {
            this.repairInfo = repairInfo;
        }

        RepairInfo getRepairInfo() {
            return repairInfo;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private static class RepairInfo {
        ItemStack singleItem;
        List<ItemStack> inventoryItems;
        List<ItemStack> armorItems; // New field for armor
        int fragmentsRequired;
        RepairInfo(ItemStack singleItem, List<ItemStack> inventoryItems, List<ItemStack> armorItems, int fragmentsRequired) {
            this.singleItem = singleItem != null ? singleItem.clone() : null;
            this.inventoryItems = inventoryItems != null ? new ArrayList<>(inventoryItems) : null;
            this.armorItems = armorItems != null ? new ArrayList<>(armorItems) : null;
            this.fragmentsRequired = fragmentsRequired;
        }
    }

    public Reparar() {
        super("reparar");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }

        Player player = (Player) sender;

        // Handle /reparar give {player} {quantia}
        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            if (!player.hasPermission(GIVE_PERMISSION)) {
                player.sendMessage("§cVocê não tem permissão para usar este comando!");
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cJogador " + args[1] + " não encontrado ou não está online!");
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    player.sendMessage("§cA quantidade deve ser um número positivo!");
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cA quantidade deve ser um número válido!");
                return;
            }

            ItemStack fragments = createRepairFragment();
            fragments.setAmount(amount);
            target.getInventory().addItem(fragments);
            target.sendMessage("§aVocê recebeu " + amount + " Fragmento(s) de Reparo de " + player.getName() + "!");
            player.sendMessage("§aVocê deu " + amount + " Fragmento(s) de Reparo para " + target.getName() + "!");
            return;
        }

        // Check cooldown (ignored for VIPs)
        if (!player.hasPermission(VIP_PERMISSION)) {
            Long lastRepair = repairCooldowns.get(player);
            if (lastRepair != null) {
                long timeLeft = (lastRepair + COOLDOWN_TIME - System.currentTimeMillis()) / 1000;
                if (timeLeft > 0) {
                    player.sendMessage("§cVocê deve esperar " + formatTime(timeLeft) + " antes de reparar novamente!");
                    return;
                } else {
                    repairCooldowns.remove(player);
                }
            }
        }

        // Get the item in hand
        ItemStack itemInHand = player.getInventory().getItemInHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR && isRepairable(itemInHand)) {
            // Repair single item in hand
            short currentDurability = itemInHand.getDurability();
            if (currentDurability == 0) {
                player.sendMessage("§cEste item já está completamente reparado!");
                return;
            }

            int repairCost = calculateRepairCost(itemInHand, player);
            int fragmentsRequired = Math.max(1, repairCost / 5);
            if (!hasFragments(player, fragmentsRequired)) {
                player.sendMessage("§cVocê precisa de " + fragmentsRequired + " Fragmentos de Reparo!");
                return;
            }

            Inventory confirmInv = createConfirmationInventory(player, itemInHand, null, null, fragmentsRequired);
            player.openInventory(confirmInv);
        } else {
            // Repair all repairable items in inventory and equipped armor
            int totalFragmentsRequired = 0;
            List<ItemStack> repairableItems = new ArrayList<>();
            List<ItemStack> repairableArmor = new ArrayList<>();
            StringBuilder repairSummary = new StringBuilder("§eItens reparáveis encontrados:\n");

            // Check main inventory
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && isRepairable(item) && item.getDurability() > 0) {
                    int repairCost = calculateRepairCost(item, player);
                    int fragmentsRequired = Math.max(1, repairCost / 5);
                    totalFragmentsRequired += fragmentsRequired;
                    repairableItems.add(item.clone());
                    repairSummary.append("§e- ").append(item.getType().name()).append(": ").append(fragmentsRequired).append(" Fragmentos\n");
                }
            }

            // Check equipped armor
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && isRepairable(armor) && armor.getDurability() > 0) {
                    int repairCost = calculateRepairCost(armor, player);
                    int fragmentsRequired = Math.max(1, repairCost / 5);
                    totalFragmentsRequired += fragmentsRequired;
                    repairableArmor.add(armor.clone());
                    repairSummary.append("§e- ").append(armor.getType().name()).append(" (Equipado): ").append(fragmentsRequired).append(" Fragmentos\n");
                }
            }

            if (repairableItems.isEmpty() && repairableArmor.isEmpty()) {
                player.sendMessage("§cNenhum item reparável encontrado no seu inventário ou armadura!");
                return;
            }

            repairSummary.append("§eTotal: ").append(totalFragmentsRequired).append(" Fragmentos de Reparo.");
            player.sendMessage(repairSummary.toString());

            if (!hasFragments(player, totalFragmentsRequired)) {
                player.sendMessage("§cVocê precisa de " + totalFragmentsRequired + " Fragmentos de Reparo para reparar tudo!");
                return;
            }

            Inventory confirmInv = createConfirmationInventory(player, null, repairableItems, repairableArmor, totalFragmentsRequired);
            player.openInventory(confirmInv);
        }
    }

    private Inventory createConfirmationInventory(Player player, ItemStack singleItem, List<ItemStack> inventoryItems, List<ItemStack> armorItems, int fragmentsRequired) {
        RepairInfo repairInfo = new RepairInfo(singleItem, inventoryItems, armorItems, fragmentsRequired);
        Inventory inv = Bukkit.createInventory(new RepairInventoryHolder(repairInfo), 9, "Confirmar Reparo");

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§eInformações do Reparo");
        List<String> lore = new ArrayList<>();

        if (singleItem != null) {
            lore.add("§fItem: §7" + singleItem.getType().name());
            lore.add("§fCusto: §7" + fragmentsRequired + " Fragmentos de Reparo");
        } else {
            int totalItems = (inventoryItems != null ? inventoryItems.size() : 0) + (armorItems != null ? armorItems.size() : 0);
            lore.add("§fItens a reparar: §7" + totalItems);
            lore.add("§fCusto total: §7" + fragmentsRequired + " Fragmentos de Reparo");
            if (inventoryItems != null) {
                for (ItemStack item : inventoryItems) {
                    lore.add("§7- " + item.getType().name() + ": " + Math.max(1, calculateRepairCost(item, player) / 5) + " Fragmentos");
                }
            }
            if (armorItems != null) {
                for (ItemStack armor : armorItems) {
                    lore.add("§7- Armadura (" + armor.getType().name() + "): " + Math.max(1, calculateRepairCost(armor, player) / 5) + " Fragmentos");
                }
            }
        }
        infoMeta.setLore(lore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        ItemStack confirmButton = new ItemStack(Material.EMERALD);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName("§aConfirmar");
        confirmButton.setItemMeta(confirmMeta);
        inv.setItem(2, confirmButton);

        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§cCancelar");
        cancelButton.setItemMeta(cancelMeta);
        inv.setItem(6, cancelButton);

        return inv;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof RepairInventoryHolder)) return;

        RepairInventoryHolder repairHolder = (RepairInventoryHolder) holder;
        RepairInfo repairInfo = repairHolder.getRepairInfo();
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getSlot();

        if (clickedItem == null || !clickedItem.hasItemMeta() || (slot != 2 && slot != 6)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        if (slot == 2) { // Confirm button
            if (!hasFragments(player, repairInfo.fragmentsRequired)) {
                player.sendMessage("§cErro: Fragmentos insuficientes!");
                player.closeInventory();
                return;
            }

            if (repairInfo.singleItem != null) {
                // Repair single item in hand
                ItemStack currentHandItem = player.getInventory().getItemInHand();
                if (currentHandItem == null || currentHandItem.getType() == Material.AIR || !currentHandItem.isSimilar(repairInfo.singleItem)) {
                    player.sendMessage("§cErro: O item na mão não corresponde ao item a ser reparado!");
                    player.closeInventory();
                    return;
                }
                    removeFragments(player, repairInfo.fragmentsRequired);
                    currentHandItem.setDurability((short) 0);
                    player.sendMessage("§aItem reparado com sucesso usando " + repairInfo.fragmentsRequired + " Fragmentos de Reparo!");
                    if (!player.hasPermission(VIP_PERMISSION)) {
                        repairCooldowns.put(player, System.currentTimeMillis());
                    }
                
            } else {
                // Repair inventory and armor
                boolean repairFailed = false;
                // Repair inventory items
                if (repairInfo.inventoryItems != null) {
                    for (ItemStack item : repairInfo.inventoryItems) {
                        if (item != null && item.getEnchantments().size() > 0 && random.nextDouble() < 0.1) {
                            player.sendMessage("§cO reparo de " + item.getType().name() + " falhou! Tente novamente.");
                            repairFailed = true;
                        } else if (item != null) {
                            for (int i = 0; i < player.getInventory().getSize(); i++) {
                                ItemStack invItem = player.getInventory().getItem(i);
                                if (invItem != null && invItem.isSimilar(item)) {
                                    invItem.setDurability((short) 0);
                                    break;
                                }
                            }
                        }
                    }
                }
                // Repair armor items
                if (repairInfo.armorItems != null) {
                    ItemStack[] armorContents = player.getInventory().getArmorContents();
                    for (ItemStack armor : repairInfo.armorItems) {
                        if (armor != null && armor.getEnchantments().size() > 0 && random.nextDouble() < 0.1) {
                            player.sendMessage("§cO reparo de " + armor.getType().name() + " falhou! Tente novamente.");
                            repairFailed = true;
                        } else if (armor != null) {
                            for (int i = 0; i < armorContents.length; i++) {
                                if (armorContents[i] != null && armorContents[i].isSimilar(armor)) {
                                    armorContents[i].setDurability((short) 0);
                                    break;
                                }
                            }
                        }
                    }
                    player.getInventory().setArmorContents(armorContents);
                }
                if (!repairFailed) {
                    removeFragments(player, repairInfo.fragmentsRequired);
                    player.sendMessage("§aInventário e armadura reparados com sucesso usando " + repairInfo.fragmentsRequired + " Fragmentos de Reparo!");
                    if (!player.hasPermission(VIP_PERMISSION)) {
                        repairCooldowns.put(player, System.currentTimeMillis());
                    }
                } else {
                    player.sendMessage("§cAlguns reparos falharam, mas os fragmentos não foram consumidos.");
                    player.closeInventory();
                    return;
                }
            }
            player.closeInventory();
        } else if (slot == 6) { // Cancel button
            player.sendMessage("§cReparo cancelado.");
            player.closeInventory();
        }
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;
        StringBuilder time = new StringBuilder();
        if (minutes > 0) {
            time.append(minutes).append(" minuto").append(minutes > 1 ? "s" : "");
            if (seconds > 0) time.append(" e ");
        }
        if (seconds > 0 || minutes == 0) {
            time.append(seconds).append(" segundo").append(seconds != 1 ? "s" : "");
        }
        return time.toString();
    }

    private boolean isRepairable(ItemStack item) {
        return item != null && item.getType().getMaxDurability() > 0;
    }

    private int calculateRepairCost(ItemStack item, Player player) {
        Material material = item.getType();
        int baseCost = 50;
        if (material.name().contains("DIAMOND")) {
            baseCost *= 2;
        } else if (material.name().contains("IRON")) {
            baseCost *= 1.5;
        } else if (material.name().contains("GOLD")) {
            baseCost *= 1.5;
        } else if (material.name().contains("STONE")) {
            baseCost *= 1.2;
        } else if (material.name().contains("WOOD")) {
            baseCost *= 1;
        }

        int enchantCount = item.getEnchantments().size();
        baseCost += enchantCount * 25;
        int repairCost = baseCost * item.getDurability() / item.getType().getMaxDurability();
        if (player.hasPermission(VIP_PERMISSION)) {
            repairCost = (int) (repairCost * VIP_DISCOUNT);
        }
        return Math.max(1, repairCost);
    }

    private boolean hasFragments(Player player, int amount) {
        int totalFragments = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isRepairFragment(item)) {
                totalFragments += item.getAmount();
            }
        }
        return totalFragments >= amount;
    }

    private void removeFragments(Player player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && isRepairFragment(item)) {
                int currentAmount = item.getAmount();
                if (currentAmount >= remaining) {
                    item.setAmount(currentAmount - remaining);
                    player.getInventory().setItem(i, item.getAmount() > 0 ? item : null);
                    remaining = 0;
                } else {
                    player.getInventory().setItem(i, null);
                    remaining -= currentAmount;
                }
                if (remaining == 0) {
                    break;
                }
            }
        }
        player.updateInventory();
    }

    private boolean isRepairFragment(ItemStack item) {
        return item != null && item.getType() == Material.NETHER_STAR && item.hasItemMeta() &&
               item.getItemMeta() != null && item.getItemMeta().hasDisplayName() &&
               item.getItemMeta().getDisplayName().equals(FRAGMENT_NAME);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player && random.nextDouble() < 0.015) {
            Player player = event.getEntity().getKiller();
            event.getDrops().add(createRepairFragment());
            player.sendMessage("§aVocê encontrou um Fragmento de Reparo!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.STONE) {
            return;
        }
        if (random.nextDouble() < 0.005) {
            Player player = event.getPlayer();
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), createRepairFragment());
            player.sendMessage("§aVocê encontrou um Fragmento de Reparo!");
        }
    }

    private ItemStack createRepairFragment() {
        ItemStack fragment = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = fragment.getItemMeta();
        meta.setDisplayName(FRAGMENT_NAME);
        fragment.setItemMeta(meta);
        return fragment;
    }
}