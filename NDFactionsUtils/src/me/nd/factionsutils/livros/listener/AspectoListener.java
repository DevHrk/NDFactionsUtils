package me.nd.factionsutils.livros.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AspectoListener implements Listener {

    private static final SConfig config = Main.get().getConfig("Livros");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || cursor.getType() != Material.BOOK || !cursor.hasItemMeta() ||
            current == null || current.getType() == Material.AIR) {
            return;
        }

        ItemMeta cursorMeta = cursor.getItemMeta();
        String displayName = cursorMeta.getDisplayName();
        String aspectoDivinoName = config.getString("Aspecto_Divino.Nome").replace("&", "§");
        String sabedoriaName = config.getString("SabedoriaD.Nome").replace("&", "§");

        // Handle Aspecto Divino enchantment
        if (displayName.equalsIgnoreCase(aspectoDivinoName)) {
            applyAspectoDivino(player, current, cursor, event);
        }
        // Handle Sabedoria do Dragão (upgrade)
        else if (displayName.equalsIgnoreCase(sabedoriaName)) {
            upgradeAspectoDivino(player, current, cursor, event);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !boots.hasItemMeta() || !boots.getItemMeta().hasLore()) {
            return;
        }

        ItemMeta meta = boots.getItemMeta();
        List<String> lore = meta.getLore();
        String aspectoBaseLore = config.getString("Aspecto_Divino.item").replace("&", "§").replace("%nivel%", "");
        int nivel = lore.stream()
                .filter(line -> line.startsWith(aspectoBaseLore))
                .map(line -> romanToInt(line.replace(aspectoBaseLore, "").trim()))
                .findFirst()
                .orElse(0);

        if (nivel > 0) {
            double[] percentages = getReductionPercentages();
            if (nivel <= percentages.length) {
                double reduction = percentages[nivel - 1] / 100.0;
                double originalDamage = event.getDamage();
                event.setDamage(originalDamage * (1.0 - reduction));
            }
        }
    }

    private void applyAspectoDivino(Player player, ItemStack current, ItemStack cursor, InventoryClickEvent event) {
        if (!current.getType().toString().endsWith("_BOOTS")) {
            player.sendMessage("§cEste encantamento só pode ser aplicado em botas!");
            return;
        }

        ItemMeta meta = current.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String loreTag = config.getString("Aspecto_Divino.item").replace("&", "§").replace("%nivel%", intToRoman(1));

        if (lore.contains(loreTag)) {
            player.sendMessage("§cEstas botas já possuem Aspecto Divino!");
            return;
        }

        lore.add(loreTag);
        meta.setLore(lore);
        current.setItemMeta(meta);
        consumeBook(cursor, event);
        player.sendMessage("§aO encantamento Aspecto Divino foi aplicado com sucesso!");
        event.setCancelled(true);
    }

    private void upgradeAspectoDivino(Player player, ItemStack current, ItemStack cursor, InventoryClickEvent event) {

        ItemMeta meta = current.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        String aspectoBasePrefix = config.getString("Aspecto_Divino.item").replace("&", "§").replace("%nivel%", "");

        String currentLoreLine = lore.stream()
                .filter(line -> line.startsWith(aspectoBasePrefix))
                .findFirst()
                .orElse(null);

        String roman = currentLoreLine.substring(aspectoBasePrefix.length()).trim();
        int currentLevel = romanToInt(roman);
        int maxLevels = getMaxAspectoLevel();

        if (currentLevel >= maxLevels) {
            player.sendMessage("§cEstas botas já atingiram o nível máximo de Aspecto Divino!");
            return;
        }

        lore.remove(currentLoreLine);
        String newLevelRoman = intToRoman(currentLevel + 1);
        if (newLevelRoman.isEmpty()) {
            player.sendMessage("§cErro ao atualizar o nível do encantamento!");
            return;
        }

        lore.add(aspectoBasePrefix + newLevelRoman);
        meta.setLore(lore);
        current.setItemMeta(meta);
        consumeBook(cursor, event);
        player.sendMessage("§aAspecto Divino elevado para nível " + newLevelRoman + "!");
        event.setCancelled(true);
    }

    private void consumeBook(ItemStack book, InventoryClickEvent event) {
        if (book.getAmount() > 1) {
            book.setAmount(book.getAmount() - 1);
        } else {
            event.setCursor(new ItemStack(Material.AIR));
        }
    }

    private double[] getReductionPercentages() {
        String reductionStr = config.getString("Aspecto_Divino.Reducao");
        if (reductionStr.trim().isEmpty()) {
            return new double[]{10.0, 20.0, 30.0};
        }

        try {
            return Arrays.stream(reductionStr.split(":"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (NumberFormatException e) {
            return new double[]{10.0, 20.0, 30.0};
        }
    }

    private int getMaxAspectoLevel() {
        String lvlStr = config.getString("Aspecto_Divino.LvL");
        if (lvlStr.trim().isEmpty()) {
            return 3;
        }

        return (int) Arrays.stream(lvlStr.split(":"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
    }

    private String intToRoman(int level) {
        String lvlStr = config.getString("Aspecto_Divino.LvL");
        String[] levels = lvlStr.split(":");
        if (level <= 0 || level > levels.length) return "";
        return levels[level - 1].trim();
    }

    private int romanToInt(String roman) {
        String lvlStr = config.getString("Aspecto_Divino.LvL");
        String[] levels = lvlStr.split(":");
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].trim().equalsIgnoreCase(roman)) {
                return i + 1;
            }
        }
        return 0;
    }
}
