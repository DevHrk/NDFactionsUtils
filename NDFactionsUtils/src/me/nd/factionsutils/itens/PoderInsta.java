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

public class PoderInsta implements Especial
{
	public static ItemStack PODER_INSTANTANEO;
	
    public static PoderInsta get() {
        return new PoderInsta();
    }
    static FileConfiguration m = Main.get().getConfig(); 
    @Override
    public Integer getId() {
        return m.getInt("Poder_Instantaneo.Id");
    }
    
    @Override
    public String getName() {
        return "PoderInsta";
    }
    
	@Override
    public ItemStack getItem() {
	
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Poder_Instantaneo.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();
   		
   		m.getStringList("Poder_Instantaneo.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Poder_Instantaneo.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Poder_Instantaneo.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Poder_Instantaneo.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Poder_Instantaneo.Data"));
   		regen.setItemMeta(regenmeta);
   		PODER_INSTANTANEO = regen;
   		return regen;
    }
}
