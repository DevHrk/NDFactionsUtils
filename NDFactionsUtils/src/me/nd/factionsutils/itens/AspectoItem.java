package me.nd.factionsutils.itens;

import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;

public class AspectoItem implements Especial{
	public static ItemStack ASPECTONOTURNO;
	static FileConfiguration m = Main.get().getConfig(); 
    public static AspectoItem get() {
        return new AspectoItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Aspecto_Noturno.Id");
    }
    
    @Override
    public String getName() {
        return "AspectoN";
    }
    
   	@Override
       public ItemStack getItem() {
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Aspecto_Noturno.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Aspecto_Noturno.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{raio}", String.valueOf(m.getInt("Aspecto_Noturno.Raio")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Aspecto_Noturno.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Aspecto_Noturno.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Aspecto_Noturno.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		regen.setDurability((short) m.getInt("Aspecto_Noturno.Data"));
   		regen.setItemMeta(regenmeta);
   		ASPECTONOTURNO = regen;
   		return regen;
       }
   }
