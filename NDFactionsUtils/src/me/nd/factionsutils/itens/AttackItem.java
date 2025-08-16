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

public class AttackItem implements Especial{
	public static ItemStack REMOVEDORATTACK;
	static FileConfiguration m = Main.get().getConfig(); 
    public static AttackItem get() {
        return new AttackItem();
    }
    
    @Override
    public Integer getId() {
        return m.getInt("Remover-Attack.Id");
    }
    
    @Override
    public String getName() {
        return "RemoverAttack";
    }
    
   	@Override
       public ItemStack getItem() {
   		ArrayList<String> lore2 = new ArrayList<>();
   		ItemStack regen = new ItemStack(m.getInt("Remover-Attack.Id"));
   		ItemMeta regenmeta = regen.getItemMeta();

   		m.getStringList("Remover-Attack.Lore").stream().map(lore12 -> lore12.replace("&", "§")).forEach(lore2::add);

   		regenmeta.setDisplayName(m.getString("Remover-Attack.Nome").replace("&", "§"));
   		regenmeta.setLore(lore2);

   		Optional.ofNullable(m.getBoolean("Remover-Attack.Glow")).filter(Boolean::booleanValue).ifPresent(b -> {
   	        regenmeta.addEnchant(Enchantment.DURABILITY, 1, true);
   	        regenmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);  });

   		Optional.ofNullable(m.getBoolean("Remover-Attack.Flags")).filter(Boolean::booleanValue).ifPresent(b -> 
  		 Arrays.stream(ItemFlag.values()).forEach(regenmeta::addItemFlags));

   		regen.setDurability((short) m.getInt("Remover-Attack.Data"));
   		regen.setItemMeta(regenmeta);
   		REMOVEDORATTACK = regen;
   		return regen;
       }
   }

