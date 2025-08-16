package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.*;
import java.util.*;

public class TntThrowItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack ARMADILHA;
    public static TntThrowItem get() {
        return new TntThrowItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("TnT-Arremessavel.Id");
    }
    
    @Override
    public String getName() {
        return "Arremessavel";
    }
    
	@Override
    public ItemStack getItem() {
    	
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("TnT-Arremessavel.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("TnT-Arremessavel.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("TnT-Arremessavel.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("TnT-Arremessavel.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("TnT-Arremessavel.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("TnT-Arremessavel.Data"));
   		regen.setItemMeta(regenmeta);
   		
   		ARMADILHA = regen;
   		return regen;
    }
}
