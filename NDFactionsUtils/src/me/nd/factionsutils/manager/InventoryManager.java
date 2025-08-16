package me.nd.factionsutils.manager;

import java.util.Map;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;
import me.nd.factionsutils.plugin.SConfig;

public class InventoryManager {
	private static final SConfig config = Main.get().getConfig("DataBase","saveInventory");
	
	public static void saveInventory(Player player, SelectedItems selectedItems) {
	    if (selectedItems == null || selectedItems.getSelectedItems() == null) return;

	    String playerPath = player.getName() + ".inventory";
	    if (config == null) return;

	    synchronized (config) {
	        config.set(playerPath, null); // limpa entrada antiga

	        for (Especial item : selectedItems.getSelectedItems()) {
	            if (item == null || item.getItem() == null) continue;

	            ItemStack special = item.getItem();
	            for (int i = 0; i < player.getInventory().getSize(); i++) {
	                ItemStack current = player.getInventory().getItem(i);
	                if (current != null && current.isSimilar(special)) {
	                    config.set(playerPath + "." + i, special.serialize());
	                }
	            }
	        }

	        try {
	            config.save();
	        } catch (Exception e) {
	            Bukkit.getLogger().warning("Erro ao salvar inventário de " + player.getName());
	            e.printStackTrace();
	        }
	    }
	}

	public static void loadInventory(Player player, SelectedItems selectedItems) {
	    for (Especial item : selectedItems.getSelectedItems()) {
	        for (int i = 0; i < player.getInventory().getSize(); i++) {
	            if (config.contains(player.getName() + ".inventory." + i)) {
	                ConfigurationSection section = config.getSection(player.getName() + ".inventory." + i);
	                if (section != null) {
	                    Map<String, Object> itemMap = section.getValues(true);
	                    if (itemMap != null) {
	                        ItemStack savedItem = ItemStack.deserialize(itemMap);
	                        if (item.getItem().isSimilar(savedItem)) {
	                            // Verifica se o item já existe no inventário antes de adicioná-lo
	                            boolean alreadyExists = IntStream.range(0, player.getInventory().getSize())
	                                .anyMatch(index -> savedItem.isSimilar(player.getInventory().getItem(index)));
	                            if (!alreadyExists) {
	                                player.getInventory().setItem(i, savedItem);
	                            } else {
	                                // Se o item já estiver no inventário do jogador, remova-o da configuração
	                                config.set(player.getName() + ".inventory." + i, null);
	                                config.save();
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }
	    config.save();
	}

    public static void startSavingTask(final SelectedItems selectedItems) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Salva o inventário de forma assíncrona
                    Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> saveInventory(player, selectedItems));
                }
            }
        }.runTaskTimer(Main.get(), 0L, 20L * 60 * 30); // 30 minutos para salvar
    }
    
    public static void SavingItens(final SelectedItems selectedItems) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    saveInventory(player, selectedItems);
                }
    }
    
	
	}
