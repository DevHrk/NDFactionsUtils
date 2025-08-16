package me.nd.factionsutils.livros.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;

public class AutoRepararListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final InventoryClickEvent e) {
	    if (e.getAction() != InventoryAction.SWAP_WITH_CURSOR || !(e.getWhoClicked() instanceof Player)) {
	        return;
	    }

	    final Player p = (Player)e.getWhoClicked();
	    final ItemStack cursor = e.getCursor();
	    final ItemStack current = e.getCurrentItem();

	    if (cursor == null || cursor.getType() != Material.BOOK || !cursor.hasItemMeta() ||
	        current == null || current.getType() == Material.AIR) {
	        return;
	    }

	    final ItemMeta cursorMeta = cursor.getItemMeta();
	    final String displayName = cursorMeta.getDisplayName();
	    final String autoRepararName = m.getString("AutoReparar.Nome").replace("&", "§");
	    final String autoRepararUpgradeName = m.getString("SabedoriaD.Nome").replace("&", "§");

	    List<String> ferramentasPermitidas = Arrays.asList("_SWORD", "BOW", "_PICKAXE", "_AXE", "_HOE", "_SPADE");

	    if (!ferramentasPermitidas.stream().anyMatch(current.getType().toString()::endsWith)) {
	        p.sendMessage("§cEste encantamento serve apenas para as ferramentas");
	        return;
	    }

	    final ItemMeta currentMeta = current.getItemMeta();
	    final List<String> currentLore = currentMeta.hasLore() ? new ArrayList<>(currentMeta.getLore()) : new ArrayList<>();
	    final String autoRepararBasePrefix = m.getString("AutoReparar.item").replace("&", "§").replace("%nivel%", "");

	    if (displayName.equalsIgnoreCase(autoRepararName)) {
	        // Apply base AutoReparar enchantment
	        if (currentLore.stream().anyMatch(line -> line.startsWith(autoRepararBasePrefix))) {
	            p.sendMessage("§cEste item já possui o encantamento Auto Reparar!");
	            return;
	        }

	        currentLore.add(autoRepararBasePrefix + intToRoman(1));
	        currentMeta.setLore(currentLore);
	        current.setItemMeta(currentMeta);
	        consumeBook(cursor, e);
	        p.sendMessage("§aO encantamento Auto Reparar foi aplicado com sucesso!");
	        e.setCancelled(true);
	    } else if (displayName.equalsIgnoreCase(autoRepararUpgradeName)) {
	        // Upgrade AutoReparar enchantment
	        String currentLoreLine = currentLore.stream()
	                .filter(line -> line.startsWith(autoRepararBasePrefix))
	                .findFirst()
	                .orElse(null);

	        int currentLevel = romanToInt(currentLoreLine.replace(autoRepararBasePrefix, "").trim());
	        int maxLevels = getMaxAutoRepararLevel();

	        if (currentLevel >= maxLevels) {
	            p.sendMessage("§cEste item já atingiu o nível máximo de Auto Reparar!");
	            return;
	        }

	        currentLore.remove(currentLoreLine);
	        String newLevelRoman = intToRoman(currentLevel + 1);
	        if (newLevelRoman.isEmpty()) {
	            p.sendMessage("§cErro ao atualizar o nível do encantamento!");
	            return;
	        }

	        currentLore.add(autoRepararBasePrefix + newLevelRoman);
	        currentMeta.setLore(currentLore);
	        current.setItemMeta(currentMeta);
	        consumeBook(cursor, e);
	        p.sendMessage("§aAuto Reparar elevado para nível " + newLevelRoman + "!");
	        e.setCancelled(true);
	    }
	}
	
	@EventHandler
	public void onItemDamage(final PlayerItemDamageEvent e) {
	    final Player p = e.getPlayer();
	    final ItemStack item = e.getItem();
	    final ItemStack itemInHand = p.getInventory().getItemInHand();

	    if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
	        return;
	    }

	    final List<String> lore = item.getItemMeta().getLore();
	    final String autoRepararBasePrefix = m.getString("AutoReparar.item").replace("&", "§").replace("%nivel%", "");
	    final int nivel = lore.stream()
	            .filter(line -> line.startsWith(autoRepararBasePrefix))
	            .map(line -> romanToInt(line.replace(autoRepararBasePrefix, "").trim()))
	            .findFirst()
	            .orElse(0);

	    if (nivel > 0) {
	        final Material material = item.getType();
	        final int durability = item.getDurability();
	        final int maxDurability = item.getType().getMaxDurability();
	        final double porcentagem = (double) durability / maxDurability * 100;
	        final double[] triggerPercentages = getTriggerPercentages();

	        if (nivel <= triggerPercentages.length && porcentagem >= triggerPercentages[nivel - 1]) {
	            repararItem(p, itemInHand, material, nivel);
	        }
	    }
	}

	private void repararItem(final Player p, final ItemStack item, final Material material, int nivel) {
	    final Material repairMaterial = getRepairMaterial(material);
	    if (repairMaterial == null) {
	        return;
	    }

	    double[] repairChances = getRepairChances();
	    double repairChance = nivel <= repairChances.length ? repairChances[nivel - 1] : repairChances[repairChances.length - 1];

	    if (p.getInventory().contains(repairMaterial) && percentChance(repairChance / 100.0)) {
	        item.setDurability((short) 0);
	        p.getInventory().removeItem(new ItemStack(repairMaterial));
	    } else if (!p.getInventory().contains(repairMaterial)) {
	        final int[] taskId = new int[1];
	        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.get(), new Runnable() {
	            @Override
	            public void run() {
	                if (p.getInventory().contains(repairMaterial) && percentChance(repairChance / 100.0)) {
	                    item.setDurability((short) 0);
	                    p.getInventory().removeItem(new ItemStack(repairMaterial));
	                }
	            }
	        }, 20L, 20L).getTaskId();
	    }
	}

	private void consumeBook(ItemStack book, InventoryClickEvent event) {
	    if (book.getAmount() > 1) {
	        book.setAmount(book.getAmount() - 1);
	    } else {
	        event.setCursor(new ItemStack(Material.AIR));
	    }
	}

	private double[] getTriggerPercentages() {
	    String triggerStr = m.getString("AutoReparar.Porcentagem");
	    if (triggerStr == null || triggerStr.trim().isEmpty()) {
	        return new double[]{50.0, 40.0, 30.0}; // Lower percentage for higher levels
	    }

	    try {
	        return Arrays.stream(triggerStr.split(":"))
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .mapToDouble(Double::parseDouble)
	                .toArray();
	    } catch (NumberFormatException e) {
	        return new double[]{50.0, 40.0, 30.0};
	    }
	}

	private double[] getRepairChances() {
	    String chanceStr = m.getString("AutoReparar.Chance");
	    if (chanceStr == null || chanceStr.trim().isEmpty()) {
	        return new double[]{50.0, 75.0, 100.0}; // Higher chance for higher levels
	    }

	    try {
	        return Arrays.stream(chanceStr.split(":"))
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .mapToDouble(Double::parseDouble)
	                .toArray();
	    } catch (NumberFormatException e) {
	        return new double[]{50.0, 75.0, 100.0};
	    }
	}

	private int getMaxAutoRepararLevel() {
	    String lvlStr = m.getString("AutoReparar.LvL");
	    if (lvlStr == null || lvlStr.trim().isEmpty()) {
	        return 3;
	    }

	    return (int) Arrays.stream(lvlStr.split(":"))
	            .map(String::trim)
	            .filter(s -> !s.isEmpty())
	            .count();
	}

	private String intToRoman(int level) {
	    String lvlStr = m.getString("AutoReparar.LvL");
	    if (lvlStr == null || lvlStr.trim().isEmpty()) {
	        return "";
	    }
	    String[] levels = lvlStr.split(":");
	    if (level <= 0 || level > levels.length) {
	        return "";
	    }
	    return levels[level - 1].trim();
	}

	private int romanToInt(String roman) {
	    String lvlStr = m.getString("AutoReparar.LvL");
	    if (lvlStr == null || lvlStr.trim().isEmpty()) {
	        return 0;
	    }
	    String[] levels = lvlStr.split(":");
	    for (int i = 0; i < levels.length; i++) {
	        if (levels[i].trim().equalsIgnoreCase(roman)) {
	            return i + 1;
	        }
	    }
	    return 0;
	}

	private Material getRepairMaterial(final Material material) {
	    switch (material) {
	        case DIAMOND_SWORD:
	        case DIAMOND_AXE:
	        case DIAMOND_PICKAXE:
	        case DIAMOND_HOE:
	        case DIAMOND_SPADE:
	            return Material.DIAMOND;
	        case GOLD_SWORD:
	        case GOLD_AXE:
	        case GOLD_PICKAXE:
	        case GOLD_HOE:
	        case GOLD_SPADE:
	            return Material.GOLD_INGOT;
	        case IRON_SWORD:
	        case IRON_AXE:
	        case IRON_PICKAXE:
	        case IRON_HOE:
	        case IRON_SPADE:
	            return Material.IRON_INGOT;
	        case STONE_SWORD:
	        case STONE_AXE:
	        case STONE_PICKAXE:
	        case STONE_HOE:
	        case STONE_SPADE:
	            return Material.COBBLESTONE;
	        case WOOD_SWORD:
	        case WOOD_AXE:
	        case WOOD_PICKAXE:
	        case WOOD_HOE:
	        case WOOD_SPADE:
	            return Material.LOG;
	        default:
	            return null;
	    }
	}

	public static Boolean percentChance(final double chance) {
	    return Math.random() <= chance;
	}
}