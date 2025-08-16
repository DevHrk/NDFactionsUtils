package me.nd.factionsutils.manager;

import me.nd.factionsutils.livros.*;
import me.nd.factionsutils.manager.especial.Livros;

import com.google.common.collect.Lists;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class LivroManager
{
    private static List<Livros> items = Lists.newArrayList();
    
    public LivroManager() {
    	items.add(new AspectoDivino());
    	items.add(new AutoReparar());
    	items.add(new Derreter());
    	items.add(new Dureza());
    	items.add(new Execucao());
    	items.add(new Furia());
    	items.add(new ImpetoDeBatalha());
    	items.add(new LivroAleatorio());
    	items.add(new ProtecaoDivina());
    	items.add(new Ragnarok());
    	items.add(new Rajada());
    	items.add(new Shield());
    	items.add(new Sobrecarga());
    	items.add(new SuperArea());
    	items.add(new SabedoriaDoDragao());
    }

    public static List<Livros> getItems() {
        return items;
    }

    public static Livros getItem(String name) {
        for (Livros item : items) {
            if (!item.getName().equalsIgnoreCase(name)) continue;
            return item;
        }
        return null;
    }

    public static Livros getItem(Integer id) {
        for (Livros item : items) {
            if (item.getId() != id) continue;
            return item;
        }
        return null;
    }

    public static Livros getItem(ItemStack item) {
        return items.stream().filter(customItem -> customItem.getItem().isSimilar(item)).findFirst().orElse(null);
    }
}
