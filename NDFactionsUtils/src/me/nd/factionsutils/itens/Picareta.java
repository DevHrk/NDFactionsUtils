package me.nd.factionsutils.itens;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.especial.Especial;
import me.nd.factionsutils.utils.Reflect;

public class Picareta implements Especial
{
	public static ItemStack ARMADILHA;
	static FileConfiguration m = Main.get().getConfig(); 
    public static Picareta get() {
        return new Picareta();
    }
    
    @Override
    public Integer getId() {
        return 278;
    }
    
    @Override
    public String getName() {
        return "Picareta";
    }
    
	@Override
       public ItemStack getItem() {

		int idPicareta = 278; // ID da picareta de dima
		boolean flags = m.getBoolean("Picareta.Flags");
		ArrayList<String> lore2 = new ArrayList<>();
   			ItemStack Picareta = new ItemStack(idPicareta);
   			try
   			{
   				Class<?> CraftItemStackClass = Reflect.getOBClass("inventory.CraftItemStack");
   				Class<?> NBTTagCompoundClass = Reflect.getNMSClass("NBTTagCompound");
   				Class<?> ItemStackClass = Reflect.getNMSClass("ItemStack");
   				Method asNMSCopy = CraftItemStackClass.getDeclaredMethod("asNMSCopy", Picareta.getClass());
   				Method asBukkitCopy = CraftItemStackClass.getDeclaredMethod("asBukkitCopy", ItemStackClass);
   				Method setBoolean = NBTTagCompoundClass.getDeclaredMethod("setBoolean", String.class, boolean.class);
   				Method setTag = ItemStackClass.getDeclaredMethod("setTag", NBTTagCompoundClass);
   				Method getItemMeta = CraftItemStackClass.getDeclaredMethod("getItemMeta", ItemStackClass);
   				Method setItemMeta = CraftItemStackClass.getDeclaredMethod("setItemMeta", ItemStackClass, ItemMeta.class);
   				Object NBTTagCompound = NBTTagCompoundClass.newInstance();
   				Object CraftItemStack = asNMSCopy.invoke(null, Picareta);
   				setBoolean.invoke(NBTTagCompound, "Unbreakable", true);
   				setTag.invoke(CraftItemStack, NBTTagCompound);
   				ItemMeta PicaretaMeta = (ItemMeta) getItemMeta.invoke(null, CraftItemStack);
   				if (flags) {for (ItemFlag flag : ItemFlag.values()) {PicaretaMeta.addItemFlags(flag);}}
   		   		for (String lore12 : m.getStringList("Picareta.Lore")) {
   		   			lore2.add(lore12.replace("&", "§"));
   				PicaretaMeta.setDisplayName(m.getString("Picareta.Nome").replace("&", "§"));
   				PicaretaMeta.setLore(lore2);
   				PicaretaMeta.addEnchant(Enchantment.SILK_TOUCH, 2, true);
   				setItemMeta.invoke(null, CraftItemStack, PicaretaMeta);
   				Picareta = (ItemStack) asBukkitCopy.invoke(null, CraftItemStack);
   		   		} 
   			}
   			catch (NullPointerException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) 
   			{
   				e.printStackTrace();
   			}
   			ARMADILHA = Picareta;
			return Picareta;
    }
}
   	
   