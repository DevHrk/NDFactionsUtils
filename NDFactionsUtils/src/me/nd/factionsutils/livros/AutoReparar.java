package me.nd.factionsutils.livros;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Livros;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.enchantments.*;
import java.util.*;

public class AutoReparar implements Livros
{
	private static final SConfig m = Main.get().getConfig("Livros");
	public static ItemStack ARMADILHA;
    public static AutoReparar get() {
        return new AutoReparar();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("AutoReparar.Id");
    }
    
    @Override
    public String getName() {
        return "AutoReparar";
    }
    
	@Override
    public ItemStack getItem() {
		
		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("AutoReparar.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("AutoReparar.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{max}", String.valueOf(m.getInt("LancadorSupremo.Usos"))).replace("{uso}", String.valueOf(m.getInt("LancadorSupremo.Usos")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("AutoReparar.Nome").replace("&", "§").replace("{max}", String.valueOf(m.getInt("LancadorSupremo.Usos"))).replace("{uso}", String.valueOf(m.getInt("LancadorSupremo.Usos"))));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("AutoReparar.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("AutoReparar.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("AutoReparar.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
    }
}
