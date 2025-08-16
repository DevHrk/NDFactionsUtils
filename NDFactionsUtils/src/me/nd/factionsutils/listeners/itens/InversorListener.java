package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.*;
import me.nd.factionsutils.holoboard.Holoboard;
import me.nd.factionsutils.itens.InversorItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class InversorListener implements Listener { 
	
	static FileConfiguration c = Main.get().getConfig();  
	public static ArrayList<String> inversortemporal = new ArrayList<String>(); 
	private HashMap<Player, Item> grenades = new HashMap<>();
	private HashMap<Player, Block> blocks = new HashMap<>();
	public static HashSet<String> Inversor = new HashSet<>();

	@EventHandler 
	void jogar(PlayerInteractEvent e) {
	    Player p = e.getPlayer(); 	
	    World world = p.getWorld(); 
	    SConfig m = Main.get().getConfig("Mensagens");
	    ItemStack regen = new ItemStack(Material.getMaterial(c.getInt("InversorTemporal.Id"))); 
	    
	    if (p.getInventory().getItemInHand().isSimilar(InversorItem.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
	        p.sendMessage(m.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	        e.setCancelled(true);
	        return;
	    }
	    
	    if (p.getItemInHand().isSimilar(InversorItem.ARMADILHA)) {
	        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR) {
	            Item grenade = world.dropItem(p.getEyeLocation(),regen); 
	            grenades.put(p, grenade);
	            Block b = grenade.getLocation().getBlock();
	            blocks.put(p, b);
	            API.removeItem(p);
	            regen.setDurability((short) c.getInt("InversorTemporal.Data"));
	            grenade.setVelocity(p.getEyeLocation().getDirection()); 
	            grenade.setCustomName(c.getString("InversorTemporal.NomeJ").replace("&", "§"));
	            grenade.setCustomNameVisible(true);

	            // Declare a final Block variable outside the runTaskLater method
	            final Block[] block = new Block[1];

	            Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
	                String blocos = c.getString("InversorTemporal.Material");
	                String b1 = blocos.split(":")[0];
	                String b2 = blocos.split(":")[1];

	                grenade.getLocation().clone().add(0.5, 0, 0.5);
	                Holoboard.add(grenade.getLocation().clone(), p.getName()+"inversor", "inversor", p.getName());

	                block[0] = grenade.getLocation().getBlock();
	                new BukkitRunnable() {
	                    int counter = 0;
	                    public void run() {
	                        if (counter >= c.getInt("InversorTemporal.Sphere.Tempo")) {
	                            this.cancel();
	                        } else {
	                            API.sphere(grenade.getLocation(), c.getInt("InversorTemporal.Sphere.Raio"), Effect.HAPPY_VILLAGER);
	                            counter++;
	                        }
	                    }
	                }.runTaskTimer(Main.get(), 0L, 20L); // 20 ticks = 1 segundo
	                // Obtenha o bloco que a granada gera
	                block[0].setTypeIdAndData(Integer.valueOf(b1).intValue(), Byte.valueOf(b2).byteValue(), false);
	                
	                // Agora você pode usar essa variável de bloco para chamar setMeta
	                setMeta(block[0], true);
	            }, c.getInt("InversorTemporal.TempoExplodir") * 20);

	            Bukkit.getScheduler().runTaskLater(Main.get(), () -> grenade.remove(), c.getInt("InversorTemporal.TempoExcluir"));
	            
	            Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
	                grenade.remove();
	                Holoboard.remove(Holoboard.getById(p.getName()+"inversor"));
	                inversortemporal.remove(p.getName());
	                block[0].setType(Material.AIR);
	                removeMeta(block[0]);
	            }, c.getInt("InversorTemporal.Tempo") * 20);
	        }
	    }
	}
	
	@EventHandler
	void onPlayerMove(PlayerMoveEvent e) {
	    // Ignora se não mudou de bloco
	    if (e.getFrom().getBlockX() == e.getTo().getBlockX()
	     && e.getFrom().getBlockY() == e.getTo().getBlockY()
	     && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

	    Player p = e.getPlayer();
	    Location center = p.getLocation();
	    SConfig m1 = Main.get().getConfig("Mensagens");

	    // CONFIG CACHEADA (pode mover para o construtor do listener)
	    final int raio = c.getInt("InversorTemporal.Raio");
	    final int altura = c.getInt("InversorTemporal.Altura");
	    final Material tipo = Material.getMaterial(c.getString("InversorTemporal.MaterialV"));

	    if (tipo == null) return; // evita null pointer

	    // Percorre cubo de raio limitado
	    World world = center.getWorld();
	    int cx = center.getBlockX();
	    int cy = center.getBlockY();
	    int cz = center.getBlockZ();

	    for (int x = cx - raio; x <= cx + raio; x++) {
	        for (int y = cy - altura; y <= cy + altura; y++) {
	            for (int z = cz - raio; z <= cz + raio; z++) {
	                Block block = world.getBlockAt(x, y, z);

	                if (block.getType() == tipo && placed(block)) {
	                    // Aqui, getMetadata nunca retorna null
	                    if (block.hasMetadata("InversorTemporal")) {
	                        // Executa comandos e adiciona
	                        API.dispatchCommands(p, c.getStringList("InversorTemporal.potion"));
	                        if (Inversor.add(p.getName())) {
	                            MessageUtils.send(p, m1.getStringList("Inversor.Entrar"));
	                        }
	                        return;
	                    }
	                }
	            }
	        }
	    }

	    // Se não está mais dentro do campo do Inversor
	    if (Inversor.remove(p.getName())) {
	        MessageUtils.send(p, m1.getStringList("Inversor.Saiu"));
	    }
	}

	
	void removeInversor(Block block, String playerName) {
	    Location originalLocation = block.getLocation();
	    Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
	    Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
	    Holoboard.remove(boardToRemove);
	    block.setType(Material.AIR);
	    removeMeta(block);
	}
    @EventHandler
    void onExplosion(EntityExplodeEvent event) {
       for (Block block : event.blockList()) {
           if (block.hasMetadata("InversorTemporal")) {
               Location originalLocation = block.getLocation();
               Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
               Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
               if (boardToRemove != null) {
                   Holoboard.remove(boardToRemove);
                   block.setType(Material.AIR);
                   removeMeta(block);
               }
           }
       }
   }
	
	@EventHandler
	void onBreak(BlockBreakEvent e) {
	    Player p = e.getPlayer();
	    Block block = e.getBlock();
	    String playerName = p.getName();
	    if (block.hasMetadata("InversorTemporal")) {
	        // Verifique se o jogador tem um "antitrap" colocado
	        if (true && Holoboard.getById(playerName + "inversor") != null && Holoboard.getByType("inversor") != null) {
	        	 removeInversor(block, playerName);
	                e.setCancelled(true);
	        } else {
	            // Se o jogador não tem um "antitrap" colocado, ele pode quebrar qualquer um
	        	removeInversor(block, playerName);
	        }
	    }
	}
		  public void setMeta(Block b,boolean place) {
			  b.setMetadata("InversorTemporal", new FixedMetadataValue(Main.get() , place));
		  }
		  
		  public void removeMeta(Block b) {
			  b.removeMetadata("InversorTemporal", Main.get());
		  }
		  
		  public boolean placed(Block b) {
			  List<MetadataValue> meta = b.getMetadata("InversorTemporal");
			  for (MetadataValue value : meta) {
				  return value.asBoolean();
			  }
			  return false;
		  }
}
