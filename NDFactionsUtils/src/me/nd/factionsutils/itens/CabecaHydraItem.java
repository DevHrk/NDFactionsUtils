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

public class CabecaHydraItem implements Especial{
	public static ItemStack CABECAHYDRA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static CabecaHydraItem get() {
        return new CabecaHydraItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Cabeça_de_Hydra.Id");
    }
    
    @Override
    public String getName() {
        return "CabecaHydra";
    }
    
   	@Override
       public ItemStack getItem() {
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Cabeça_de_Hydra.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Cabeça_de_Hydra.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Cabeça_de_Hydra.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Cabeça_de_Hydra.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Cabeça_de_Hydra.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Cabeça_de_Hydra.Data"));
   		regen.setItemMeta(regenmeta);
   		CABECAHYDRA = regen;
   		return regen;
       }
   }