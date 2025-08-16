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

public class RelogioDoTempo implements Especial{
	public static ItemStack ARMADILHA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static RelogioDoTempo get() {
        return new RelogioDoTempo();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("RelogioDoTempo.Id");
    }
    
    @Override
    public String getName() {
        return "RelogioDoTempo";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("RelogioDoTempo.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();
   		
   		m.getStringList("Gerador.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{tempo}", String.valueOf(m.getInt("RelogioDoTempo.tempo"))).replace("{raio}", String.valueOf(m.getInt("RelogioDoTempo.raio")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("RelogioDoTempo.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("RelogioDoTempo.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("RelogioDoTempo.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("RelogioDoTempo.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }
   }
