package me.nd.factionsutils.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class Agrupar extends Commands {

    public Agrupar() {
        super("agrupar");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }

        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();
        boolean grouped = false;

        // Map to track potions by their type and data for stacking
        Map<String, ItemStack> potionMap = new HashMap<>();

        // Iterate through inventory to collect potions
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.POTION && isStackablePotion(item)) {
                // Generate a unique key based on potion type and data
                String key = generatePotionKey(item);
                ItemStack existing = potionMap.get(key);

                if (existing == null) {
                    potionMap.put(key, item.clone());
                } else {
                    existing.setAmount(existing.getAmount() + item.getAmount());
                    potionMap.put(key, existing);
                }
                // Clear the slot (we'll re-add items later)
                inventory.setItem(i, null);
                grouped = true;
            }
        }

        // Re-add grouped potions to inventory
        for (ItemStack potion : potionMap.values()) {
            int amount = potion.getAmount();
            potion.setAmount(Math.min(amount, 64)); // Respect max stack size
            while (amount > 0) {
                inventory.addItem(potion.clone());
                amount -= potion.getAmount();
                if (amount > 0) {
                    potion.setAmount(Math.min(amount, 64));
                }
            }
        }

        player.updateInventory();

        if (grouped) {
            player.sendMessage("§aSuas poções foram agrupadas com sucesso!");
        } else {
            player.sendMessage("§cNenhuma poção agrupável foi encontrada no seu inventário!");
        }
    }

    private boolean isStackablePotion(ItemStack item) {
        // In 1.8.8, potions stack if they have identical type and data
        return item != null && item.getType() == Material.POTION;
    }

    private String generatePotionKey(ItemStack item) {
        // Create a unique key based on potion type and data
        // In 1.8.8, potion data (short) determines effect and whether it's splash
        return item.getType().name() + ":" + item.getDurability();
    }
}