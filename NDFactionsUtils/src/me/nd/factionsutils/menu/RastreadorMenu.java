package me.nd.factionsutils.menu;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.FormatterAPI;

public class RastreadorMenu {
	 public static void abrirMenu(Player p, Integer raio) {
		FileConfiguration m1 = Main.get().getConfig(); 
	        Inventory inv = Bukkit.createInventory(null, m1.getInt("Rastreador-Menu.Tamanho"), m1.getString("Rastreador-Menu.Nome").replace("&", "§"));
	        int i = 10;
	        for (Player todos : Bukkit.getOnlinePlayers()) {
	            if (todos.equals(p) || !(p.getLocation().distance(todos.getLocation()) <= (double)raio.intValue())) continue;
	            if (i == 17) {
	                i = 19;
	            }
	            if (i == 26) {
	                i = 28;
	            }
	            if (i == 35) {
	                i = 37;
	            }
	            if (i > 43) continue;
	            ItemStack skull = new ItemStack(Material.SKULL_ITEM);
	            skull.setDurability((short)3);
	            SkullMeta sm = (SkullMeta)skull.getItemMeta();
	            sm.setOwner(todos.getName());
	            sm.setDisplayName(m1.getString("Rastreador-Menu.Display").replace("&", "§").replace("{nome}", todos.getName()));
	            ArrayList<String> lore = new ArrayList<String>();
	            String fac = "Nenhuma";
	            NDPlayer m = DataManager.players.get((Object)todos.getName());
	            if (m.hasFaction()) {
	                fac = m.getFaction().getNome();
	            }
				final int money = (int)Main.economy.getBalance(p.getName());
	            for (String lorezq : m1.getStringList("Rastreador-Menu.Jogadores-lore")) {
	    	     lore.add(lorezq.replace("&", "§").replace("{fac}", fac).replace("{money}", FormatterAPI.formatNumber(money)));
	            sm.setLore(lore);
	            skull.setItemMeta((ItemMeta)sm);
	            }
	            inv.setItem(i, skull);
	            ++i;
	        
	        p.openInventory(inv);
	    }
	 }
}
