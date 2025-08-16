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

public class PoderMax implements Especial
{
	public static ItemStack PODER_MAXIMO;
	
    public static PoderMax get() {
        return new PoderMax();
    }
    static FileConfiguration m = Main.get().getConfig(); 
    @Override
    public Integer getId() {
        return m.getInt("Poder_Maximo.Id");
    }
    
    @Override
    public String getName() {
        return "Poder";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Poder_Maximo.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Poder_Maximo.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Poder_Maximo.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Poder_Maximo.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Poder_Maximo.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Poder_Maximo.Data"));
   		regen.setItemMeta(regenmeta);
   		PODER_MAXIMO = regen;
   		return regen;
       }
   }