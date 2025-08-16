package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

import java.util.*;

public class TrackerItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack RASTREADOR;
    public static TrackerItem get() {
        return new TrackerItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Rastreador.Id");
    }
    
    @Override
    public String getName() {
        return "Rastreador";
    }
    
  	@Override
      public ItemStack getItem() {
  		
  		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Rastreador.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Rastreador.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Rastreador.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Rastreador.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Rastreador.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Rastreador.Data"));
   		regen.setItemMeta(regenmeta);
   		RASTREADOR = regen;
   		return regen;
      }
  }
