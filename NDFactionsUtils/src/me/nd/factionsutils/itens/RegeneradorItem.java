package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import java.util.*;

public class RegeneradorItem implements Especial
{
	public static ItemStack ARMADILHA;
    public static RegeneradorItem get() {
        return new RegeneradorItem();
    }
    static FileConfiguration m = Main.get().getConfig(); 
    @Override
    public Integer getId() {
        return m.getInt("Regenerador.Id");
    }
    
    @Override
    public String getName() {
        return "Regenerador";
    }
    
   	@Override
       public ItemStack getItem() {
   		
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Regenerador.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Regenerador.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Regenerador.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Regenerador.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Regenerador.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Regenerador.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }
   }
