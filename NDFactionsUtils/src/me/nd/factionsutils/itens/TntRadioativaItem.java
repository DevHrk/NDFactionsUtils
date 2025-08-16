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
import me.nd.factionsutils.api.SkullAPI;
import me.nd.factionsutils.manager.especial.Especial;

public class TntRadioativaItem implements Especial
{
	static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack ARMADILHA;
    public static TntRadioativaItem get() {
        return new TntRadioativaItem();
    }
    
    @Override
    public Integer getId() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "TnTRadioativa";
    }
    
    @Override
    public ItemStack getItem() {
    		
    	ArrayList<String> lore2 = new ArrayList<>();
       		ItemStack regen = new ItemStack(SkullAPI.getSkull(m.getString("TnTRadioativa.URL-Skull")));
       		ItemMeta regenmeta = regen.getItemMeta();

       		m.getStringList("TnTRadioativa.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

       		regenmeta.setDisplayName(m.getString("TnTRadioativa.Nome").replace("&", "§"));
       		regenmeta.setLore(lore2);

       		Optional.ofNullable(m.getBoolean("TnTRadioativa.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
       	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
       	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

       		Optional.ofNullable(m.getBoolean("TnTRadioativa.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
      		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

       		regen.setDurability((short) m.getInt("TnTRadioativa.Data"));
       		regen.setItemMeta(regenmeta);
       		ARMADILHA = regen;
       		return regen;
        
    }
}