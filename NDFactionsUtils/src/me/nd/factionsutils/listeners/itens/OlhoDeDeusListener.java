package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.OlhoDeDeusItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class OlhoDeDeusListener implements Listener {
	
	FileConfiguration m = Main.get().getConfig(); 
	public static ArrayList<String> olhos = new ArrayList<>();
	  public Location getLoc(String loc) {
		SConfig m = Main.get().getConfig("database","SetSpawn");
	    World w2 = Bukkit.getWorld(m.getString(String.valueOf(loc) + ".world"));
	    double x2 = m.getDouble(String.valueOf(loc) + ".x");
	    double y2 = m.getDouble(String.valueOf(loc) + ".y");
	    double z2 = m.getDouble(String.valueOf(loc) + ".z");
	    float yaw2 = (float)m.getDouble(String.valueOf(loc) + ".yaw");
	    float pitch2 = (float)m.getDouble(String.valueOf(loc) + ".pitch");
	    Location saidaloc = new Location(w2, x2, y2, z2, yaw2, pitch2);
	    return saidaloc;
	  }
	  
	  
	@EventHandler
     void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		SConfig m1 = Main.get().getConfig("Mensagens");
		ItemStack item =  p.getItemInHand();
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
		 if (item.isSimilar(OlhoDeDeusItem.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
				p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
				e.setCancelled(true);
				return;
			   }
		    if (item == null || 
		  	      !e.getAction().name().toLowerCase().contains("right") || 
		  	      !item.isSimilar(OlhoDeDeusItem.ARMADILHA)) {
		  	      return; 
		  	}
		    if (olhos.contains(p.getName()) && p.getGameMode() == GameMode.SPECTATOR) {
			      e.setCancelled(true);
			      return;
			    }
		 
		Location loc = p.getLocation();
		olhos.add(p.getName());
		p.setGameMode(GameMode.SPECTATOR);
		API.removeItem(p.getPlayer());
		MessageUtils.send(p, m1.getStringList("OlhoDeDeus.Tittle"));
		new BukkitRunnable(){
		    int tempo = m.getInt("Olho-De-Deus.Tempo");
		    public void run() {
		        tempo--;
		        if (tempo <= m1.getInt("OlhoDeDeus.TempoMensagem") && tempo >= 1) {
		        	List<String> messages = m1.getStringList("OlhoDeDeus.Tempo").stream().map(message -> message.replace("{tempo}", String.valueOf(tempo))).collect(Collectors.toList());
		        	MessageUtils.send(p, messages);
		        	p.playSound(p.getLocation(), tempo <= m.getInt("Olho-De-Deus.TempoSom") ? Sound.valueOf(m.getString("Olho-De-Deus.Som")) : Sound.valueOf(m.getString("Olho-De-Deus.SomTerminando")), 1.0f, 1.0f);
		        } else if (tempo == 0) {
		            p.setGameMode(GameMode.SURVIVAL);
		            olhos.remove(p.getName());
		            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
		            MessageUtils.send(p, m1.getStringList("OlhoDeDeus.Acabou"));
		            p.teleport(new Location(Bukkit.getServer().getWorld(m.getString("Olho-De-Deus.Back.Mundo")), m.getInt("Olho-De-Deus.Back.X"), m.getInt("Olho-De-Deus.Back.Y"), m.getInt("Olho-De-Deus.Back.Z")));
		            p.teleport(m.getBoolean("Olho-De-Deus.VoltarAoSpawn") ? getLoc("Spawn") : loc);
		        }
		    }
		}.runTaskTimer(Main.get(), 0L, 20L);
       }
	}
		
	@EventHandler
	void onInteractEntityPlayer(PlayerInteractEntityEvent e) {
	    Player p = e.getPlayer();
	    if (p.getGameMode() == GameMode.SPECTATOR && olhos.contains(p.getName())) {
	        e.setCancelled(true); 
	    }
	}

	@EventHandler
	void onInteractEntity(PlayerInteractAtEntityEvent e) {
	    Player p = e.getPlayer();
	    if (p.getGameMode() == GameMode.SPECTATOR && olhos.contains(p.getName())) {
	        e.setCancelled(true); 
	    }
	}
	  
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
        	Player player = (Player) event.getEntity();
            if (olhos.contains(player.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (olhos.contains(player.getName())) {
                event.setCancelled(true);
            }
        }
    }
	  
	  @EventHandler
	  void onTeleport(PlayerTeleportEvent e) {
	      Player p = e.getPlayer();
	      if (olhos.contains(p.getName())) {
	          PlayerTeleportEvent.TeleportCause cause = e.getCause();
	          if (cause == TeleportCause.ENDER_PEARL ||  cause == TeleportCause.SPECTATE || cause == TeleportCause.UNKNOWN || cause == TeleportCause.COMMAND || cause == TeleportCause.END_PORTAL || cause == TeleportCause.NETHER_PORTAL) {
	              e.setCancelled(true);
	          }
	      }
	  }
	    
	    @EventHandler
	     void armorstand(PlayerArmorStandManipulateEvent e) {
	      Player p = e.getPlayer();
	      if (olhos.contains(p.getName())) {
	        e.setCancelled(true);
	      }
	    }
	    
	    @EventHandler
	     void droparitem(PlayerDropItemEvent e) {
	      Player p = e.getPlayer();
	      if (olhos.contains(p.getName())) {
	        e.setCancelled(true);
	      }
	    }
	    
	    @EventHandler
	     void pegaritem(PlayerPickupItemEvent e) {
	      Player p = e.getPlayer();
	      if (olhos.contains(p.getName())) {
	        e.setCancelled(true); 
	    }
       }
	    
	  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	  void aoUsar(PlayerCommandPreprocessEvent e) {
		    SConfig m1 = Main.get().getConfig("Mensagens");
		    Player p = e.getPlayer();
		    if (olhos.contains(p.getName())) {
		        m.getStringList("Olho-De-Deus.Comandos").stream().filter(msg -> e.getMessage().startsWith(msg)).findFirst().ifPresent(msg -> {
		        e.setCancelled(true);
		        MessageUtils.send(p, m1.getStringList("OlhoDeDeus.Comando"));
		       });
		    }
		}
	  
    } 	

