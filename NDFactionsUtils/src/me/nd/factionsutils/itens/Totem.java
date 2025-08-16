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

public class Totem implements Especial
{
	public static ItemStack TOTEM_DA_MORTE;
	static FileConfiguration m = Main.get().getConfig(); 
    public static Totem get() {
        return new Totem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Totem_Da_Morte.Id");
    }
    
    @Override
    public String getName() {
        return "Totem";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Totem_Da_Morte.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Totem_Da_Morte.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Totem_Da_Morte.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Totem_Da_Morte.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Totem_Da_Morte.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Totem_Da_Morte.Data"));
   		regen.setItemMeta(regenmeta);
   		TOTEM_DA_MORTE = regen;
   		return regen;
       }
   }
