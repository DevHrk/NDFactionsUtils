package me.nd.factionsutils.menus.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.menus.Menu;
import me.nd.factionsutils.menus.item.MItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;
import me.nd.factionutils.cash.CashManager;

public class MenuListener implements Listener {
	
	  
	    @EventHandler
	    public void onInventoryClick(InventoryClickEvent event) {
	        SConfig m = Main.get().getConfig("Mensagens");
	        if (!(event.getWhoClicked() instanceof Player))
	            return;
	        Player player = (Player) event.getWhoClicked();
	        if (player.getOpenInventory().getTopInventory() == null)
	            return;
	        Menu menu = Main.get().getController().searchByTitle(player.getOpenInventory().getTopInventory().getTitle());
	        if (menu == null)
	            return;
	        event.setCancelled(menu.isCancelClick());
	        ItemStack item = event.getCurrentItem();
	        if (item == null || item.getType() == Material.AIR)
	            return;
	        MItem mItem = menu.getItemByStack(item);
	        if (mItem == null)
	            return;

	        // Handle right-click commands
	        if (event.getClick().toString().contains("RIGHT")) {
	            if (mItem.getCommandRight().equalsIgnoreCase(""))
	                return;
	            if (!mItem.isConsoleRight()) {
	                player.performCommand(mItem.getCommandRight());
	                return;
	            }
	        } else if (event.getClick().toString().contains("LEFT")) {
	            if (mItem.getCommandLeft().equalsIgnoreCase(""))
	                return;
	            if (!mItem.isConsoleLeft()) {
	                player.performCommand(mItem.getCommandLeft());
	                return;
	            }
	        }

	        // Check and handle cash cost
	        if (mItem.getCostCash() != 0) {
	            long cash = CashManager.getCash(player);
	            if (cash < mItem.getCostCash()) {
	            	MessageUtils.send(player, m.getString("Menu.Sem-Cash").replace("&", "§"), m.getStringList("Menu.Sem-Cash"));
	                return;
	            }
	            if (!CashManager.removeCash(player, mItem.getCostCash())) {
	            	MessageUtils.send(player, m.getString("Menu.Sem-Cash").replace("&", "§"), m.getStringList("Menu.Sem-Cash"));
	                return;
	            }
	        }

	        // Check and handle economy cost
	        if (mItem.getCost() != 0) {
	            if (!Main.get().getEconomy().has((OfflinePlayer) player, mItem.getCost())) {
	            	MessageUtils.send(player, m.getString("Menu.Sem-Money").replace("&", "§"), m.getStringList("Menu.Sem-Money"));
	                return;
	            }
	            Main.get().getEconomy().withdrawPlayer((OfflinePlayer) player, mItem.getCost());
	        }

	        // Send purchase confirmation message if there was a cost
	        if (mItem.getCostCash() > 0 || mItem.getCost() > 0)
	        	MessageUtils.send(player, m.getString("Menu.Comprado").replace("&", "§"), m.getStringList("Menu.Comprado"));

	        // Execute console commands after successful purchase
	        if (event.getClick().toString().contains("RIGHT")) {
	            if (mItem.getCommandRight().equalsIgnoreCase(""))
	                return;
	            if (mItem.isConsoleRight())
	                Bukkit.dispatchCommand((CommandSender) Bukkit.getConsoleSender(), mItem.getCommandRight().replace("{player}", player.getName()));
	        } else if (event.getClick().toString().contains("LEFT")) {
	            if (mItem.getCommandLeft().equalsIgnoreCase(""))
	                return;
	            if (mItem.isConsoleLeft())
	                Bukkit.dispatchCommand((CommandSender) Bukkit.getConsoleSender(), mItem.getCommandLeft().replace("{player}", player.getName()));
	        }
	    }
		
	}
