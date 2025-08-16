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

public class RelogioRedstone implements Especial {
	public static ItemStack RELOGIO;
	static FileConfiguration m = Main.get().getConfig(); 
    public static double MIN_DELAY;
    public static double MAX_DELAY;
    public static double DEFAULT_DELAY;
    public static double INVASION_DELAY;
    public static double COUNTER_DELAY;
    
    
    static {
        MIN_DELAY = m.getDouble("Relogio_Redstone.min", 0.7);
        MAX_DELAY = m.getDouble("Relogio_Redstone.max", 1.8);
        DEFAULT_DELAY = m.getDouble("Relogio_Redstone.delay", 0.7);
        INVASION_DELAY = m.getDouble("Relogio_Redstone.invasion", 0.7);
        COUNTER_DELAY = m.getDouble("Relogio_Redstone.counter", 1.5);
    }
    
    public static RelogioRedstone get() {
        return new RelogioRedstone();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Relogio_Redstone.Id");
    }
    
    @Override
    public String getName() {
        return "RelogioDeRedstone";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Relogio_Redstone.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Relogio_Redstone.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Relogio_Redstone.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Relogio_Redstone.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Relogio_Redstone.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Relogio_Redstone.Data"));
   		regen.setItemMeta(regenmeta);
   		RELOGIO = regen;
   		return regen;
       }
   }