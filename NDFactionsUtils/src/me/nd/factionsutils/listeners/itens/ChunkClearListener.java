package me.nd.factionsutils.listeners.itens;

import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import org.bukkit.inventory.*;
import org.bukkit.scheduler.*;
import org.bukkit.entity.*;

import me.nd.factions.enums.Cargo;
import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.holoboard.Holoboard;
import me.nd.factionsutils.itens.ClearChunkItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class ChunkClearListener implements Listener
{
	private static final FileConfiguration m = Main.get().getConfig(); 
	
	public void LimparChunk(Block b, int numBlocksToRemove) {
	    Chunk chunk = b.getLocation().getChunk();
	    World world = chunk.getWorld();
	    double worldBorder = world.getWorldBorder().getSize() / 2;
	    List<String> materialsToPreserveList = m.getStringList("Limpador.Blocos");
	    Set<Material> materialsToPreserve = materialsToPreserveList.stream().map(Material::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
	    List<Block> blocksToRemove = new ArrayList<>();
	    for (int x = 0; x < 16; x++) {
	        for (int y = 0; y < 256; y++) {
	            for (int z = 0; z < 16; z++) {
	                Block block = chunk.getBlock(x, y, z);
	                Location loc = block.getLocation();
	                if (Math.abs(loc.getBlockX()) <= worldBorder && Math.abs(loc.getBlockZ()) <= worldBorder) {
	                    if (!materialsToPreserve.contains(block.getType())) {
	                        blocksToRemove.add(block);
	                    }
	                }
	            }
	        }
	    }
	    new BukkitRunnable() {
	        int index = 0;
	        @Override
	        public void run() {
	            IntStream.range(0, numBlocksToRemove).forEach(i -> {
	                if (index >= blocksToRemove.size()) {
	                    this.cancel();
	                    return;
	                }
	                Block blockToRemove = blocksToRemove.get(index);
	                if (Math.abs(blockToRemove.getLocation().getBlockX()) <= worldBorder && Math.abs(blockToRemove.getLocation().getBlockZ()) <= worldBorder) {
	                    if (!materialsToPreserve.contains(blockToRemove.getType()) && blockToRemove.getType() != Material.AIR) {
	                        blockToRemove.setType(Material.AIR);
	                    }
	                }
	                index++;
	            });
	        }
	    }.runTaskTimer(Main.get(), 0L, m.getInt("Limpador.Velocidade"));
	}
    
	@EventHandler
	void onPlace(BlockPlaceEvent e) {
	    Player p = e.getPlayer();
	    ItemStack item = p.getItemInHand();
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    NDPlayer ps = DataManager.players.get(p.getName());
	    // Verificar se o jogador está no seu próprio território
        Terra terra = new Terra(e.getBlock().getWorld(), e.getBlock().getLocation().getChunk().getX(), e.getBlock().getLocation().getChunk().getZ());
        
	    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.isSimilar(ClearChunkItem.CLEARCHUNK)) {
	        return;
	    }

        if (ps == null) {
            p.sendMessage("§cErro: Jogador não encontrado no sistema.");
            e.setCancelled(true);
            return;
        }

	    if (ps.getFaction() != null && ps.getFaction().ownsTerritory(terra) && (ps.getCargo() == Cargo.Lider || ps.getCargo() == Cargo.Capitão)) {
	        Location location = e.getBlock().getLocation().clone().add(0.5, 0, 0.5);
	        Holoboard.add(location, p.getName() + "limpador", "limpador", p.getName());
	        setMeta(e.getBlock(), true);
	        API.removeItem(p);

	        new BukkitRunnable(){
	            int tempo = m.getInt("Limpador.tempo"); 
	            public void run() {
	                --this.tempo;

	                if (tempo <= m1.getInt("LimpadorChunck.TempoMensagem") && tempo >= 1) {
	                    List<String> messages = m1.getStringList("LimpadorChunck.Tempo").stream().map(message -> message.replace("{tempo}", String.valueOf(tempo))).collect(Collectors.toList());
	                    MessageUtils.send(p, messages);
	                    p.playSound(p.getLocation(), tempo <= m.getInt("Limpador.TempoSom") ? Sound.valueOf(m.getString("Limpador.Som")) : Sound.valueOf(m.getString("Limpador.SomTerminando")), 1.0f, 1.0f);
	                } else if (tempo == 0) {
	                	if(e.getBlock().hasMetadata("Limpador")) {
	                    LimparChunk(e.getBlock(), m.getInt("Limpador.BlocosV"));
	                    MessageUtils.send(p, m1.getStringList("LimpadorChunck.sucesso"));
	                    p.playSound(p.getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 1.0f);
	                    Holoboard.remove(Holoboard.getById(p.getName() + "limpador"));
	                    API.removeCustomArmorStand(e.getBlock().getLocation());
	                    removeMeta(e.getBlock());
	                    e.getBlock().setType(Material.AIR);
	                    return;
	                	}
	                }
	            }
	        }.runTaskTimer((Plugin)Main.get(), 0L, 20L);
	    } else {
	        e.setCancelled(true);
	        List<String> messageList = (ps.getFaction() != null && ps.getFaction().ownsTerritory(terra))
	                ? m1.getStringList("LimpadorChunck.sem-perm").stream()
	                    .map(msg -> msg.replace("&", "§"))
	                    .collect(Collectors.toList())
	                : m1.getStringList("LimpadorChunck.territorio").stream()
	                    .map(msg -> msg.replace("&", "§"))
	                    .collect(Collectors.toList());

	            // Enviar as mensagens ao jogador
	            MessageUtils.send(p, messageList);
	    }
	}
	
    @EventHandler
    void onExplosion(EntityExplodeEvent event) {
       for (Block block : event.blockList()) {
           if (block.hasMetadata("Limpador")) {
               Location originalLocation = block.getLocation();
               Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
               Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
               if (boardToRemove != null) {
                   Holoboard.remove(boardToRemove);
                   block.setType(Material.AIR);
                   removeMeta(block);
                   API.removeCustomArmorStand(block.getLocation());
               }
           }
       }
   }
    
	@EventHandler
	void onBreak(BlockBreakEvent e) {
	    Player p = e.getPlayer();
	    Block block = e.getBlock();
	    String playerName = p.getName();
	    if (block.hasMetadata("Limpador")) {
	        // Verifique se o jogador tem um "antitrap" colocado
	        if (m.getBoolean("Anti-Trap.Limite") && Holoboard.getById(playerName + "antitrap") != null && Holoboard.getByType("antitrap") != null) {
	        	    removeLimpador(block, playerName);
	                e.setCancelled(true);
	        } else {
	            // Se o jogador não tem um "antitrap" colocado, ele pode quebrar qualquer um
	        	removeLimpador(block, playerName);
	        }
	    }
	}
	
	void removeLimpador(Block block, String playerName) {
	    Location originalLocation = block.getLocation();
	    Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
	    Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
	    Holoboard.remove(boardToRemove);
	    block.setType(Material.AIR);
	    API.removeCustomArmorStand(originalLocation);
	    removeMeta(block);
	}

	  public void setMeta(Block b,boolean place) {
		  b.setMetadata("Limpador", new FixedMetadataValue(Main.get() , place));
	  }
	  
	  public void removeMeta(Block b) {
		  b.removeMetadata("Limpador", Main.get());
	  }
	  
	  public boolean placed(Block b) {
		  List<MetadataValue> meta = b.getMetadata("Limpador");
		  for (MetadataValue value : meta) {
			  return value.asBoolean();
		  }
		  return false;
	  }
	  
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e) {
	    if (!(e.getRightClicked() instanceof ArmorStand)) {
	        return;
	    }

	    ArmorStand armorStand = (ArmorStand) e.getRightClicked();
	    if (armorStand.isSmall()) {
	        e.setCancelled(true);
	    }
	}
}
