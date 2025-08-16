package me.nd.factionsutils.livros;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Livros;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.enchantments.*;
import java.util.*;

public class ImpetoDeBatalha implements Livros
{
	private static final SConfig m = Main.get().getConfig("Livros");
	public static ItemStack ARMADILHA;
    public static ImpetoDeBatalha get() {
        return new ImpetoDeBatalha();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("ImpetoDeBatalha.Id");
    }
    
    @Override
    public String getName() {
        return "ImpetoDeBatalha";
    }
    
	@Override
    public ItemStack getItem() {
		
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("ImpetoDeBatalha.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("ImpetoDeBatalha.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{max}", String.valueOf(m.getInt("LancadorSupremo.Usos"))).replace("{uso}", String.valueOf(m.getInt("LancadorSupremo.Usos")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("ImpetoDeBatalha.Nome").replace("&", "§").replace("{max}", String.valueOf(m.getInt("LancadorSupremo.Usos"))).replace("{uso}", String.valueOf(m.getInt("LancadorSupremo.Usos"))));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("ImpetoDeBatalha.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("ImpetoDeBatalha.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("ImpetoDeBatalha.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
    }
}
