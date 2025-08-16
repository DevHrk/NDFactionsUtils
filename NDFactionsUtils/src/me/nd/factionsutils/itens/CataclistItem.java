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

public class CataclistItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack ARMADILHA;
    public static CataclistItem get() {
        return new CataclistItem();
    }
    
    @Override
    public Integer getId() {
        return 95;
    }
    
    @Override
    public String getName() {
        return "Cataclist";
    }
    
	@Override
       public ItemStack getItem() {
   		
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(Material.STAINED_GLASS);
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Cataclist.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Cataclist.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Cataclist.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Cataclist.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) 14);
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }

}
