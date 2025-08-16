package me.nd.factionsutils.livros.listener;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.livros.LivroAleatorio;
import me.nd.factionsutils.manager.LivroManager;
import me.nd.factionsutils.manager.especial.Livros;
import me.nd.factionsutils.plugin.SConfig;

public class LivroAleatorioListener implements Listener {
	
	private static final SConfig m = Main.get().getConfig("Livros");
	
	@EventHandler
	public void onClick(final PlayerInteractEvent e) {
	    Player p = e.getPlayer();
	    ItemStack item = p.getItemInHand();
	    if (item.isSimilar(LivroAleatorio.ARMADILHA)) {
	        e.setCancelled(true);

	        // Lê a lista de itens da configuração
	        List<String> lista = m.getStringList("LivroAleatorio.Lista");

	        // Verifica se a lista não está vazia
	        if (!lista.isEmpty()) {
	            // Aleatoriza um item da lista
	            String itemAleatorio = lista.get(new Random().nextInt(lista.size()));

	            // Cria o item a ser dado ao jogador
	            Livros especial = LivroManager.getItem(itemAleatorio);
	            if (especial != null) {
	                ItemStack encantamento = especial.getItem();

	                // Dá o item ao jogador
	                p.getInventory().addItem(encantamento);
	            } else {
	                // Se o item for nulo, envia uma mensagem de erro para o jogador
	                p.sendMessage("Erro: item não encontrado.");
	            }
	        } else {
	            // Se a lista estiver vazia, envia uma mensagem de erro para o jogador
	            p.sendMessage("Erro: lista de itens vazia.");
	        }

	        // Remove o item da mão do jogador
	        removeItem(p);
	    }
	}
	public static void removeItem(Player p) {
		if (p.getItemInHand().getAmount() < 2) {
			p.setItemInHand(new ItemStack(Material.AIR));
		} else {
			ItemStack item = p.getItemInHand();
			item.setAmount(item.getAmount() - 1);
		}
	}
	
    
}
