package me.nd.factionsutils.listeners.itens;

import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;

import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.*;
import org.bukkit.event.block.*;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.SlimeBlockJumpItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import java.util.*;
import org.bukkit.event.entity.*;

public class SlimeBlockJumpListener implements Listener
{
    public static Set<String> dano;
    FileConfiguration m = Main.get().getConfig(); 
    public SlimeBlockJumpListener() {
        dano = new HashSet<String>();
    }
	  public void setMeta(Block b,boolean place) {
		  b.setMetadata("SlimeJump", new FixedMetadataValue(Main.get() , place));
	  }
	  
	  public boolean placed(Block b) {
		  List<MetadataValue> meta = b.getMetadata("SlimeJump");
		  for (MetadataValue value : meta) {
			  return value.asBoolean();
		  }
		  return false;
	  }
	  public void removeMeta(Block b) {
		  b.removeMetadata("SlimeJump", Main.get());
	  }
	  
	  @EventHandler
	     void onPlace(BlockPlaceEvent e) {
	    	Block b = e.getBlock();
	    	SConfig m1 = Main.get().getConfig("Mensagens");
	         Player p = e.getPlayer();
	         ItemStack item = p.getItemInHand();
		        if (item == null) {
		            return;
		        }
		        if (p.getInventory().getItemInHand().isSimilar(SlimeBlockJumpItem.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
					p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
					e.setCancelled(true);
					return;
				}
		        if (p.getInventory().getItemInHand().isSimilar(SlimeBlockJumpItem.ARMADILHA)) {
		        	setMeta(b,true);
		        }
	  }
    
	@EventHandler
     void onJumpBlock(PlayerMoveEvent e) {
         Player p = e.getPlayer();
         Block block = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (placed(block)) {
        if (block.getTypeId() == m.getInt("Impulsor.Id")) {
        	if (block.getMetadata("SlimeJump") != null) {
            p.setVelocity(p.getLocation().getDirection().multiply(m.getInt("Impulsor.Velocidade")).setY(m.getInt("Impulsor.Altura")));
            p.playEffect(block.getLocation(), Effect.HAPPY_VILLAGER, 3);
            p.playSound(p.getLocation(), Sound.MAGMACUBE_JUMP, 1.0f, 1.0f);
            dano.add(p.getName());
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)Main.get(), (Runnable)new Runnable() {
                @Override
                public void run() {
                   dano.remove(p.getName());
                  }
                }, 120L);
             }
           }
        }
    }
    
	@EventHandler
     void onBlockBreak(BlockBreakEvent e) {
    	Block b = e.getBlock();
    	SConfig m1 = Main.get().getConfig("Mensagens");
    	Player p = e.getPlayer();
        	if (b.hasMetadata("SlimeJump")) {
        	e.setCancelled(true);
        	MessageUtils.send(p, m1.getStringList("Impulsor.explodir"));
        	  }
    }
    
    @EventHandler
    void onExplosion(EntityExplodeEvent event) {
       for (Block b : event.blockList()) {
    	   if (b.hasMetadata("SlimeJump")) {
			b.removeMetadata("SlimeJump", Main.get()); 
           	removeMeta(b);
           	Bukkit.getScheduler().runTaskLater(Main.get(), () -> b.getDrops().clear(), 4L);
            b.getWorld().dropItemNaturally(b.getLocation(), SlimeBlockJumpItem.ARMADILHA);
              }
       }
   }
    
    @EventHandler
     void onDamageFall(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
             Player p = (Player)e.getEntity();
            if (dano.contains(p.getName())) {
                dano.remove(p.getName());
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
