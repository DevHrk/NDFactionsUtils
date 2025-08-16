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

public class SalvacaoItem implements Especial{
	public static ItemStack ARMADILHA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static SalvacaoItem get() {
        return new SalvacaoItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Salvação.Id");
    }
    
    @Override
    public String getName() {
        return "Salvacao";
    }
    
   	@Override
       public ItemStack getItem() {
    	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Salvação.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Salvação.Lore").stream().map(lore12 -> lore12.replace("&", "§").replace("{raio}", String.valueOf(m.getInt("Salvação.Raio"))).replace("{blocos}", String.valueOf(m.getDouble("Salvação.Blocos")))).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Salvação.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Salvação.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Salvação.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Salvação.Data"));
   		regen.setItemMeta(regenmeta);
   		ARMADILHA = regen;
   		return regen;
       }
   }