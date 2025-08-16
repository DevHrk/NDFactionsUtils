package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.*;
import java.util.*;

public class LauncherItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack ARMADILHA;
    public static LauncherItem get() {
        return new LauncherItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Lancador.Id");
    }
    
    @Override
    public String getName() {
        return "Lancador";
    }
    
	@Override
    public ItemStack getItem() {
		
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Lancador.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Lancador.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Lancador.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Lancador.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Lancador.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Lancador.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
    }
}
