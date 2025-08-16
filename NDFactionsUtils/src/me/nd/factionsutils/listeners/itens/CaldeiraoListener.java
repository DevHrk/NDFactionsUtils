package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.holoboard.Holoboard;
import me.nd.factionsutils.itens.CaldeiraoItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class CaldeiraoListener implements Listener {
	
	FileConfiguration m = Main.get().getConfig(); 
	public static List<Object> forca = new ArrayList<>();
	public static HashSet<String> CaldeiraoArea = new HashSet<>();
	
	@EventHandler
	void onPlace(BlockPlaceEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Block b = e.getBlock();
	    Location l = b.getLocation().clone();
	    Player p = e.getPlayer();
	    ItemStack item = p.getItemInHand();
	    boolean isAntiTrapLimit = m.getBoolean("Anti-Trap.Limite");
	    
	    if (item == null || !item.isSimilar(CaldeiraoItem.CALDEIRAO)) {
	        return;
	    }

	    if (!Utils.isZonaProtegida(b.getLocation()) || (isAntiTrapLimit && Holoboard.getById(p.getName() + "caldeirao") != null && Holoboard.getByType("caldeirao") != null)) {
	        String messageKey = !Utils.isZonaProtegida(b.getLocation()) ? "Mensagens.ZonaProtegida" : "Mensagens.Limite";
	        p.sendMessage(m1.getString(messageKey).replace("&", "§"));
	        e.setCancelled(true);
	        return;
	    }

	    setMeta(b, true);
	    API.removeItem(p);
	    forca.add(p.getName());

	    if (m.getBoolean("Caldeirao.Limite")) {
	        l.setX(l.getBlock().getLocation().getX() + 0.5);
	        l.setZ(l.getBlock().getLocation().getZ() + 0.5);
	        Holoboard.add(l, p.getName() + "caldeirao", "caldeirao", p.getName());
	    }

	    Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
	        Holoboard board = Holoboard.getById(p.getName() + "caldeirao");
	        if (b != null && b.hasMetadata("caldeirao")) {
	            if (b.getType() != Material.AIR) {
	                b.setType(Material.AIR);
	            }
	            forca.remove(p.getName());
	            removeMeta(b);
	            if (isAntiTrapLimit) {
	                Holoboard.remove(board);
	            }
	            if (m.getBoolean("Caldeirao.força-ao-deletar") && p.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
	                p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
	            }
	        } else if (board != null) {
	        	forca.remove(p.getName());
	            // Se o bloco não existir mais, remova apenas o holograma
	            return;
	        }
	    }, 20L * m.getInt("Caldeirao.tempo"));
	}

	@EventHandler
	void onPlayerMove(PlayerMoveEvent e) {
	    if (e.getFrom().getBlockX() == e.getTo().getBlockX()
	     && e.getFrom().getBlockY() == e.getTo().getBlockY()
	     && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

	    Player p = e.getPlayer();
	    NDPlayer mp = DataManager.players.get(p.getName());
	    if (mp == null) return;

	    NDFaction facp = mp.getFaction();
	    Location l = e.getTo().getBlock().getLocation();
	    int radius = m.getInt("Caldeirao.raio");
	    int radiusSquared = radius * radius;
	    World world = p.getWorld();
	    SConfig m1 = Main.get().getConfig("Mensagens");

	    boolean isPlayerInArea = false;

	    // Cache de jogadores próximos
	    List<Player> playersNearby = world.getEntitiesByClass(Player.class)
	        .stream()
	        .filter(entity -> entity instanceof Player)
	        .map(entity -> (Player) entity)
	        .collect(Collectors.toList());

	    int startX = l.getBlockX() - radius;
	    int endX = l.getBlockX() + radius;
	    int startZ = l.getBlockZ() - radius;
	    int endZ = l.getBlockZ() + radius;
	    int y = l.getBlockY();

	    for (int x = startX; x <= endX; x++) {
	        for (int z = startZ; z <= endZ; z++) {
	            Block b = world.getBlockAt(x, y, z);
	            if (b.getType() != Material.CAULDRON || !placed(b) || !b.hasMetadata("caldeirao")) continue;

	            Location cauldronLoc = b.getLocation();

	            for (Player t : playersNearby) {
	                if (t.getWorld() != world) continue;

	                if (t.getLocation().distanceSquared(cauldronLoc) > radiusSquared) continue;

	                NDPlayer mt = DataManager.players.get(t.getName());
	                if (mt == null) continue;

	                NDFaction fact = mt.getFaction();

	                if (forca.contains(t.getName()) || (facp != null && fact != null && fact.equals(facp))) {
	                    if (CaldeiraoArea.add(t.getName())) {
	                        MessageUtils.send(t, m1.getStringList("Caldeirao.Entrar"));
	                    }

	                    API.dispatchCommands(t, m.getStringList("Caldeirao.Efeito"));

	                    if (t.getName().equals(p.getName())) {
	                        isPlayerInArea = true;
	                    }
	                }
	            }
	        }
	    }

	    // Remove se não estiver mais
	    if (!isPlayerInArea && CaldeiraoArea.remove(p.getName())) {
	        MessageUtils.send(p, m1.getStringList("Caldeirao.Saiu"));
	    }
	}

	    
	    @EventHandler
	     void onExplosion(EntityExplodeEvent event) {
	        for (Block block : event.blockList()) {
	            if (block.hasMetadata("caldeirao")) {
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
    	    if (block.hasMetadata("caldeirao")) {
    	        // Verifique se o jogador tem um "antitrap" colocado
    	        if (m.getBoolean("Caldeirao.Limite") && Holoboard.getById(playerName + "caldeirao") != null && Holoboard.getByType("caldeirao") != null) {
    	        	removeCaldeirao(block, playerName);
    	        	if (forca.contains(p.getName())) {
    	        	forca.remove(p.getName());
    	        	}
    	                e.setCancelled(true);
    	        } else {
    	            // Se o jogador não tem um "antitrap" colocado, ele pode quebrar qualquer um
    	        	removeCaldeirao(block, playerName);
    	        }
    	    }
    	}
    	
    	void removeCaldeirao(Block block, String playerName) {
    	    Location originalLocation = block.getLocation();
    	    Location offsetLocation = new Location(originalLocation.getWorld(), originalLocation.getX() + 0.5, originalLocation.getY(), originalLocation.getZ() + 0.5);
    	    Holoboard boardToRemove = Holoboard.getByLocation(offsetLocation);
    	    Holoboard.remove(boardToRemove);
    	    block.setType(Material.AIR);
    	    removeMeta(block);
    	}
	    
	    public void removeMeta(Block b) {
			  b.removeMetadata("caldeirao", Main.get());
		  }
	    
		  public void setMeta(Block b,boolean place) {
			  b.setMetadata("caldeirao", new FixedMetadataValue(Main.get() , place));
		  }
		  public boolean placed(Block b) {
			  List<MetadataValue> meta = b.getMetadata("caldeirao");
			  for (MetadataValue value : meta) {
				  return value.asBoolean();
			  }
			  return false;
		  }

}