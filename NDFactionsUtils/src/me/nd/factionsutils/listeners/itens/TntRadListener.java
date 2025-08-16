package me.nd.factionsutils.listeners.itens;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.TntRadioativaItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class TntRadListener implements Listener {
	 
    public static HashMap<String, Integer> canhaoh = new HashMap<>();
	    @EventHandler(priority = EventPriority.HIGHEST)
	     void onInteractItem(PlayerInteractEvent e) {
	    	SConfig m1 = Main.get().getConfig("Mensagens");
	     if (e.getAction() == Action.RIGHT_CLICK_AIR) {
	    Player p = e.getPlayer();
	        
	       if (e.getItem() == null || e.getItem().getType() == Material.AIR) {
	          return;
	        }
	        
	      if (!e.getItem().hasItemMeta()) {
	          return;
	        }
	        
	       if (p.getItemInHand().isSimilar(TntRadioativaItem.ARMADILHA)) {
	       if (p.getLocation().getY() >= 256.0D) {
	    	   MessageUtils.send(p, m1.getStringList("TnTRadioativa.Altura"));
	            return;
	          } 
	       if (!Utils.isZonaProtegida(p.getLocation())) {
	        	p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	            return;
	          } 
	        
	        if (p.getItemInHand().isSimilar(TntRadioativaItem.ARMADILHA)) {
	         p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2147483647, 100), true);
	           p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 2147483647, 10), true);
	       if (!canhaoh.containsKey(p.getName())) {
	           canhaoh.put(p.getName(), Integer.valueOf(1));
	            } else {
	           canhaoh.put(p.getName(), Integer.valueOf(((Integer)canhaoh.get(p.getName())).intValue() + 1));
	            } 
	            
	          API.removeItem(p);
	      p.sendMessage("§aCanhao ativado!");
	            if (((Integer)canhaoh.get(p.getName())).intValue() <= 1) {
	            	List<String> messages = m1.getStringList("TnTRadioativa.Restante1").stream().map(message -> message.replace("{tiros}", String.valueOf(canhaoh.get(p.getName())))).collect(Collectors.toList());
	    	        MessageUtils.send(p, messages);
	              
	              return;
	            } 
	            List<String> messages = m1.getStringList("TnTRadioativa.Restante").stream().map(message -> message.replace("{tiros}", String.valueOf(canhaoh.get(p.getName())))).collect(Collectors.toList());
		        MessageUtils.send(p, messages);
	          } 
	        } 
	      } 
	    }
	    
	    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	     void onPlaceTnt2(BlockPlaceEvent e) {
	    	SConfig m1 = Main.get().getConfig("Mensagens");
        Player p = e.getPlayer();
	      
	    if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
	        return;
	      }
	      
    if (p.getItemInHand().getType() != Material.SKULL_ITEM) {
	        return;
	      }
	      
	  if (!p.getItemInHand().hasItemMeta()) {
	        return;
	      }
	      
	  if (p.getItemInHand().isSimilar(TntRadioativaItem.ARMADILHA)) {
	  e.setCancelled(true);
	  MessageUtils.send(p, m1.getStringList("TnTRadioativa.NaoPode"));
	      } 
	    }
	  
	    
	    @EventHandler(priority = EventPriority.HIGHEST)
	     void onHitAir(PlayerAnimationEvent e) {
	    	SConfig m1 = Main.get().getConfig("Mensagens");
	    	Player p = e.getPlayer();
	      if (e.getAnimationType() == PlayerAnimationType.ARM_SWING && 
	        canhaoh.containsKey(p.getName())) {
	        launchTnt(p);
	        if (((Integer)canhaoh.get(p.getName())).intValue() <= 1) {
	          canhaoh.remove(p.getName());
	          p.removePotionEffect(PotionEffectType.SLOW);
	          p.removePotionEffect(PotionEffectType.CONFUSION);
	        } else {
	          canhaoh.put(p.getName(), Integer.valueOf(((Integer)canhaoh.get(p.getName())).intValue() - 1));
	        } 
	        MessageUtils.send(p, m1.getStringList("TnTRadioativa.Ao-Atirar"));
	        if (!canhaoh.containsKey(p.getName())) {
	        	MessageUtils.send(p, m1.getStringList("TnTRadioativa.Acabou"));
	          return;
	        } 
	        if (((Integer)canhaoh.get(p.getName())).intValue() >= 2) {
	        	List<String> messages = m1.getStringList("TnTRadioativa.Restante").stream().map(message -> message.replace("{tiros}", String.valueOf(canhaoh.get(p.getName())))).collect(Collectors.toList());
	        	MessageUtils.send(p, messages);
	          return;
	        } 
	        List<String> messages = m1.getStringList("TnTRadioativa.Restante1").stream().map(message -> message.replace("{tiros}", String.valueOf(canhaoh.get(p.getName())))).collect(Collectors.toList());
	        MessageUtils.send(p, messages);
	      } 
	    }
	    
	    private void launchTnt(Player p) {
	      FileConfiguration m = Main.get().getConfig(); 
	      Location location = p.getEyeLocation();
	      Vector v = location.getDirection().multiply(m.getInt("TnTRadioativa.Forca"));
	      TNTPrimed tnt = (TNTPrimed)p.getWorld().spawn(location, TNTPrimed.class);
	      tnt.setVelocity(v);
	      tnt.setMetadata("TnTRadioativa", (MetadataValue)new FixedMetadataValue((Plugin)Main.get(), Boolean.valueOf(true)));
	    }
	}