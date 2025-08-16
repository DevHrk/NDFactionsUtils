package me.nd.factionsutils.itens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.SkullAPI;
import me.nd.factionsutils.manager.especial.Especial;

public class SaveLifeItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack ARMADILHA;
    public static SaveLifeItem get() {
        return new SaveLifeItem();
    }
    
    @Override
    public Integer getId() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "SaveLife";
    }
    
    @Override
    public ItemStack getItem() {
    	
    	ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(SkullAPI.getSkull(m.getString("Salva-Vidas.URL-Skull")));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Salva-Vidas.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Salva-Vidas.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Salva-Vidas.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Salva-Vidas.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Salva-Vidas.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
    }
}
