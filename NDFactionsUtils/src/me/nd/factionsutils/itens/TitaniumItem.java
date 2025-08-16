package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import org.bukkit.enchantments.*;

public class TitaniumItem implements Especial
{
	public static ItemStack ARMADILHA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static SuperSocoItem get() {
        return new SuperSocoItem();
    }
    
    @Override
    public Integer getId() {
        return 112;
    }
    
    @Override
    public String getName() {
        return "Titanium";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(Material.NETHER_BRICK);
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Titanium.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{porcent}", String.valueOf(m.getInt("Titanium.Porcentagem")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Titanium.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Titanium.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Titanium.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Titanium.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }
}
