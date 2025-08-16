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

public class ResetKdr implements Especial
{
	public static ItemStack RESET_KDR;
	static FileConfiguration m = Main.get().getConfig(); 
    public static ResetKdr get() {
        return new ResetKdr();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Reset_KDR.Id");
    }
    
    @Override
    public String getName() {
        return "Reset";
    }
    
   	@Override
       public ItemStack getItem() {
    	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Reset_KDR.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Reset_KDR.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Reset_KDR.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Reset_KDR.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Reset_KDR.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Reset_KDR.Data"));
   		regen.setItemMeta(regenmeta);
   		RESET_KDR = regen;
   		return regen;
       }
   }
