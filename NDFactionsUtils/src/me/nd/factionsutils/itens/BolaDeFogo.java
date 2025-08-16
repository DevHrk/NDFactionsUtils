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

public class BolaDeFogo implements Especial{
	public static ItemStack BOLADEFOGO;
	static FileConfiguration m = Main.get().getConfig(); 
    public static BolaDeFogo get() {
        return new BolaDeFogo();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Bola_de_Fogo.Id");
    }
    
    @Override
    public String getName() {
        return "BoladeFogo";
    }
    
   	@Override
       public ItemStack getItem() {
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Bola_de_Fogo.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Bola_de_Fogo.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Bola_de_Fogo.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Bola_de_Fogo.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Bola_de_Fogo.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Bola_de_Fogo.Data"));
   		regen.setItemMeta(regenmeta);
   		BOLADEFOGO = regen;
   		return regen;
       }
   }

