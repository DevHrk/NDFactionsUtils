package me.nd.factionsutils.listeners.itens;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.SchematicAPI;
import me.nd.factionsutils.itens.Schematic;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class SchematicListenner implements Listener {
	
	private Location start = null;
	private Location end = null;
	private Player structureCreator = null;
	private boolean isNamingStructure = false;
	private boolean isNamingStructuredelete = false;

	@EventHandler
	public void onPlayerInteract(BlockBreakEvent e) {
	    Player p = e.getPlayer();
	    ItemStack item = p.getItemInHand();
	    if (item.isSimilar(Schematic.ARMADILHA)) {
	       e.setCancelled(true);
	    }
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
	    Player p = e.getPlayer();
	    ItemStack item = p.getItemInHand();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    if (item.isSimilar(Schematic.ARMADILHA)) {
	        if (p.isSneaking()) { // Verifica se o jogador está pressionando Shift
	            // Adicione aqui o código para remover a estrutura do arquivo de configuração
	            MessageUtils.send(p, m1.getStringList("Schematic.IsiraRemove"));
	            isNamingStructuredelete = true;
	            isNamingStructure = false;
	            start = null;
	            end = null;
	        } else {
	            Block clickedBlock = e.getClickedBlock(); // Obtém o bloco clicado
	            if (clickedBlock != null) { // Verifica se o bloco clicado não é nulo
	                if (e.getAction().name().contains("RIGHT")) {
	                    start = clickedBlock.getLocation(); // Salva a localização do bloco clicado
	                    structureCreator = p;
	                    MessageUtils.send(p, m1.getStringList("Schematic.Ponto2"));
	                    isNamingStructuredelete = false;
	                } else if (e.getAction().name().contains("LEFT")) {
	                    end = clickedBlock.getLocation(); // Salva a localização do bloco clicado
	                    structureCreator = p;
	                    MessageUtils.send(p, m1.getStringList("Schematic.Ponto1"));
	                    isNamingStructuredelete = false;
	                }
	            }

	            // Verifica se os pontos inicial e final foram definidos
	            if (start != null && end != null) {
	                MessageUtils.send(p, m1.getStringList("Schematic.Isira"));
	                isNamingStructure = true;
	            }
	        }
	    }
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
	    Player p = e.getPlayer();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    if (isNamingStructuredelete) {
	        String structureName = e.getMessage();
	        if (structureName.equalsIgnoreCase("cancele")) {
	            isNamingStructure = false;
	            start = null;
	            end = null;
	            structureCreator = null;
	            isNamingStructuredelete = false;
	            MessageUtils.send(p, m1.getStringList("Schematic.cancelado"));
	            e.setCancelled(true);
	        } else if (!SchematicAPI.structureDontExist(structureName)) {
	            	
	                List<String> messages = m1.getStringList("Schematic.Nao-Existe");
	                for (int i = 0; i < messages.size(); i++) {
	                    String message = messages.get(i);
	                    message = message.replace("{estrutura}", structureName);
	                    messages.set(i, message);
	                }
	                MessageUtils.send(p, messages);
	                e.setCancelled(true);
	            
	            } else {
	                List<String> messages = m1.getStringList("Schematic.Removido");
	                for (int i = 0; i < messages.size(); i++) {
	                    String message = messages.get(i);
	                    message = message.replace("{estrutura}", structureName);
	                    messages.set(i, message);
	                }
	                MessageUtils.send(p, messages);
	                SchematicAPI.removeStructure(structureName);
	                isNamingStructure = false;

	                // Não envia a mensagem no chat
	                e.setCancelled(true);

	                // Redefine as variáveis após salvar a estrutura
	                start = null;
	                end = null;
	                structureCreator = null;
	                isNamingStructuredelete = false;
	            }
	    }
	
	    if (isNamingStructure && p.equals(structureCreator)) {
	        String structureName = e.getMessage();

	        // Se o jogador digitar 'cancele', cancele o evento e redefina tudo
	        if (structureName.equalsIgnoreCase("cancele")) {
	            isNamingStructure = false;
	            start = null;
	            end = null;
	            structureCreator = null;
	            MessageUtils.send(p, m1.getStringList("Schematic.cancelado"));
	            e.setCancelled(true);
	        } else if (SchematicAPI.structureExists(structureName)) { // Verifica se a estrutura já existe
            	List<String> messages = m1.getStringList("Schematic.JaExiste");
            	for (int i = 0; i < messages.size(); i++) {
            	    String message = messages.get(i);
            	    message = message.replace("{estrutura}", structureName);
            	    messages.set(i, message);
            	}
    	        MessageUtils.send(p, messages);
	            e.setCancelled(true); // Cancela a mensagem do jogador
	        } else {
	            SchematicAPI.saveBlocks(p, start, end, structureName);
	            List<String> messages = m1.getStringList("Schematic.Salvo");
            	for (int i = 0; i < messages.size(); i++) {
            	    String message = messages.get(i);
            	    message = message.replace("{estrutura}", structureName);
            	    messages.set(i, message);
            	}
    	        MessageUtils.send(p, messages);
	            isNamingStructure = false;

	            // Não envia a mensagem no chat
	            e.setCancelled(true);

	            // Redefine as variáveis após salvar a estrutura
	            start = null;
	            end = null;
	            structureCreator = null;
	        }
	    }
	}


}
