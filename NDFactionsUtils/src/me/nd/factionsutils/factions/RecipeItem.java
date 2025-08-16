package me.nd.factionsutils.factions;

import org.bukkit.enchantments.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.utils.ItemBuilder;

public class RecipeItem
{
	public RecipeItem() {
    	FileConfiguration m = Main.get().getConfig();
        ShapedRecipe r = new ShapedRecipe(new ItemBuilder(Material.DOUBLE_PLANT).name(m.getString("Moeda.Nome").replace("&", "§")).lore((m.getStringList("Moeda.Lore"))).enchant(Enchantment.DURABILITY, 1).removeAttributes().build());
        r.shape(new String[] { "** ", "** ", "   " });
        r.setIngredient('*', Material.INK_SACK, 11);
        Bukkit.addRecipe((Recipe)r);
    }
}
