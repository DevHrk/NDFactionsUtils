package me.nd.factionsutils.livros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Livros;
import me.nd.factionsutils.plugin.SConfig;

public class Rajada implements Livros {
	public static ItemStack AGRUPADOR;
	private static final SConfig m = Main.get().getConfig("Livros");
	
    public static Rajada get() {
        return new Rajada();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Rajada.Id");
    }
    
    @Override
    public String getName() {
        return "Rajada";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Rajada.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Rajada.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Rajada.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Rajada.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Rajada.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Rajada.Data"));
   		regen.setItemMeta(regenmeta);
   		AGRUPADOR = regen;
   		return regen;
       }
   }