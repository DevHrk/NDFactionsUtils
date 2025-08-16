package me.nd.factionsutils.itens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

public class MoedaItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
    public static MoedaItem get() {
        return new MoedaItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Moeda.Id");
    }
    
    @Override
    public String getName() {
        return "Moeda";
    }
    
    @Override
    public ItemStack getItem() {
    	ArrayList<String> lore2 = new ArrayList<>();
       		ItemStack regen = new ItemStack(m.getInt("Moeda.Id"));
       		ItemMeta regenmeta = regen.getItemMeta();

       		m.getStringList("Moeda.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

       		regenmeta.setDisplayName(m.getString("Moeda.Nome").replace("&", "§"));
       		regenmeta.setLore(lore2);

       		Optional.ofNullable(m.getBoolean("Moeda.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
       	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
       	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

       		Optional.ofNullable(m.getBoolean("Moeda.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
      		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

       		regen.setDurability((short) m.getInt("Moeda.Data"));
       		regen.setItemMeta(regenmeta);
       		return regen;
    }
}
