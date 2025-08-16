package me.nd.factionsutils.itens;

import org.bukkit.inventory.*;
import org.bukkit.*;

import me.nd.factionsutils.manager.especial.Especial;
import me.nd.factionsutils.utils.ItemBuilder;

import java.util.*;
import org.bukkit.enchantments.*;

public class CoinItem implements Especial
{
    public static CoinItem get() {
        return new CoinItem();
    }
    
    @Override
    public Integer getId() {
        return 175;
    }
    
    @Override
    public String getName() {
        return "Coin";
    }
    
    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Material.DOUBLE_PLANT).name("§6Moeda").lore(Arrays.asList("§7Utilize est\u00e1 moeda para dar", "§7upgrade no level de sua fac\u00e7\u00e3o", "§7atrav\u00e9s do §f\"/f upgrades\"§7.")).enchant(Enchantment.DURABILITY, 1).removeAttributes().build();
    }
}
