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

public class Agrupador implements Especial {
	public static ItemStack AGRUPADOR;
	static FileConfiguration m = Main.get().getConfig(); 
    public static Agrupador get() {
        return new Agrupador();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Agrupador_De_Pocoes.Id");
    }
    
    @Override
    public String getName() {
        return "Agrupador";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Agrupador_De_Pocoes.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Agrupador_De_Pocoes.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Agrupador_De_Pocoes.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Agrupador_De_Pocoes.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Agrupador_De_Pocoes.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Agrupador_De_Pocoes.Data"));
   		regen.setItemMeta(regenmeta);
   		AGRUPADOR = regen;
   		return regen;
       }
   }