package me.nd.factionsutils.livros;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Livros;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.enchantments.*;
import java.util.*;

public class Shield implements Livros
{
	private static final SConfig m = Main.get().getConfig("Livros");
	public static ItemStack ARMADILHA;
    public static Shield get() {
        return new Shield();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Campo_de_Força.Id");
    }
    
    @Override
    public String getName() {
        return "Shield";
    }
    
	@Override
    public ItemStack getItem() {
		
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Campo_de_Força.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Campo_de_Força.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{max}", String.valueOf(m.getInt("LancadorSupremo.Usos"))).replace("{uso}", String.valueOf(m.getInt("LancadorSupremo.Usos")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Campo_de_Força.Nome").replace("&", "§").replace("{max}", String.valueOf(m.getInt("LancadorSupremo.Usos"))).replace("{uso}", String.valueOf(m.getInt("LancadorSupremo.Usos"))));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Campo_de_Força.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Campo_de_Força.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Campo_de_Força.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
    }
}
