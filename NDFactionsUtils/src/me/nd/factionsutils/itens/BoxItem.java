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

public class BoxItem implements Especial{
	public static ItemStack ARMADILHA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static BoxItem get() {
        return new BoxItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("BoxInsta.Id");
    }
    
    @Override
    public String getName() {
        return "Box";
    }
    
   	@Override
       public ItemStack getItem() {
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("BoxInsta.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("BoxInsta.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("BoxInsta.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("BoxInsta.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("BoxInsta.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("BoxInsta.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }
   }