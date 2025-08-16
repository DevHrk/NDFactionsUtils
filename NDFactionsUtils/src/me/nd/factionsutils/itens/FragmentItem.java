package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import java.util.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.*;

public class FragmentItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
    public static FragmentItem get() {
        return new FragmentItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Fragmento.Id");
    }
    
    @Override
    public String getName() {
        return "Fragment";
    }
    
    @Override
    public ItemStack getItem() {
        
    	ArrayList<String> lore2 = new ArrayList<>();
       		ItemStack regen = new ItemStack(m.getInt("Fragmento.Id"));
       		ItemMeta regenmeta = regen.getItemMeta();

       		m.getStringList("Fragmento.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

       		regenmeta.setDisplayName(m.getString("Fragmento.Nome").replace("&", "§"));
       		regenmeta.setLore(lore2);

       		Optional.ofNullable(m.getBoolean("Fragmento.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
       	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
       	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

       		Optional.ofNullable(m.getBoolean("Fragmento.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
      		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

       		regen.setDurability((short) m.getInt("Fragmento.Data"));
       		regen.setItemMeta(regenmeta);
       		return regen;
    		
    }
}
