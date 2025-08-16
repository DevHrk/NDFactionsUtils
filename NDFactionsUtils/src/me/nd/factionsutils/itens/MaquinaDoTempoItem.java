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

public class MaquinaDoTempoItem implements Especial{
	public static ItemStack ARMADILHA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static MaquinaDoTempoItem get() {
        return new MaquinaDoTempoItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("MaquinaDoTempo.Id");
    }
    
    @Override
    public String getName() {
        return "MaquinaDoTempo";
    }
    
   	@Override
       public ItemStack getItem() {
   		
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("MaquinaDoTempo.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("MaquinaDoTempo.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{raio}", String.valueOf(m.getInt("MaquinaDoTempo.raio"))).replace("{tempo}", String.valueOf(m.getInt("MaquinaDoTempo.tempo")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("MaquinaDoTempo.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("MaquinaDoTempo.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("MaquinaDoTempo.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("MaquinaDoTempo.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }
   }