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

public class AntiTrapItem implements Especial
{static FileConfiguration m = Main.get().getConfig(); 
	public static ItemStack AntiTrap;
    public static AntiTrapItem get() {
        return new AntiTrapItem();
    }
    
    @Override
    public Integer getId() {
        return 25;
    }
    
    @Override
    public String getName() {
        return "AntiTrap";
    }
    
   	@Override
       public ItemStack getItem() {
       	
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(25);
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Anti-Trap.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Anti-Trap.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Anti-Trap.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Anti-Trap.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		regen.setDurability((short) m.getInt("Anti-Trap.Data"));
   		regen.setItemMeta(regenmeta);
   		AntiTrap = regen;
   		return regen;
       }
   }

