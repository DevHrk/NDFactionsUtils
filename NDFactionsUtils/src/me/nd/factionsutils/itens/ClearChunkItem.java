package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.*;
import java.util.*;

public class ClearChunkItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack CLEARCHUNK;
    public static ClearChunkItem get() {
        return new ClearChunkItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Limpador.Id");
    }
    
    @Override
    public String getName() {
        return "Limpador";
    }
    
	@Override
    public ItemStack getItem() {
		
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Limpador.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Limpador.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Limpador.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Limpador.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Limpador.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Limpador.Data"));
   		regen.setItemMeta(regenmeta);
   		CLEARCHUNK = regen;
   		return regen;
    }
}
