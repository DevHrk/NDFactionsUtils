package me.nd.factionsutils.itens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

public class GosmaItem implements Especial{
	public static ItemStack GOSMA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static GosmaItem get() {
        return new GosmaItem();
    }
    
    @Override
    public Integer getId() {
        return 341;
    }
    
    @Override
    public String getName() {
        return "Gosma";
    }
    
   	@Override
       public ItemStack getItem() {
   		
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(Material.SLIME_BALL);
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Gosma.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Gosma.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Gosma.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Gosma.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) 0);
   		regen.setItemMeta(regenmeta);
   		GOSMA = regen;
   		return regen;
       }
   }
