package me.nd.factionsutils.listeners.tnt;

import com.google.common.collect.Lists;
import io.netty.util.internal.ThreadLocalRandom;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.itens.TnT;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TntListenner implements Listener {

    public int randomNumber = ThreadLocalRandom.current().nextInt(2, 5);

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void place(BlockPlaceEvent e) {
        ItemStack item = e.getPlayer().getItemInHand();
        if (item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().equals(Main.get().getConfig().getString("TnT.Nome").replace("&", "§"))) {
            Block b = e.getBlockPlaced();
            b.setMetadata("CanhaoTnT", new FixedMetadataValue(Main.get(), "CanhaoTnT"));
        }
    }

    private List<Location> random(Location a) {
        double n3;
        LinkedList<Location> list = Lists.newLinkedList();
        int field0 = this.randomNumber;
        double n2 = n3 = (double)(-field0);
        while (n3 <= (double)field0) {
            double n5;
            double n4 = n5 = (double)(-field0);
            while (n5 <= (double)field0) {
                double n7;
                double n6 = n7 = (double)(-field0);
                while (n7 <= (double)field0) {
                    Location add;
                    double n8 = n2;
                    double n9 = n8 * n8;
                    double n10 = n4;
                    double n11 = n9 + n10 * n10;
                    double n12 = n6;
                    double n13 = n11 + n12 * n12;
                    int n14 = field0;
                    if (n13 <= (double)(n14 * n14) && !(add = a.clone().add((double)((int)n2), (double)((int)n4), (double)((int)n6))).equals(a)) {
                        list.add(add);
                    }
                    n6 = n7 = n6 + 1.0;
                }
                n4 = n5 = n4 + 1.0;
            }
            n2 = n3 = n2 + 1.0;
        }
        return list;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void removeWaterFunction(EntityExplodeEvent a) {
        List<Location> loc;
        TNTPrimed tntPrimed;
        if (Main.get().getConfig().getBoolean("TnT.AguaExplodirTnT") && a.getEntityType() == EntityType.PRIMED_TNT && !(tntPrimed = (TNTPrimed)a.getEntity()).hasMetadata("CanhaoTnT") && (tntPrimed.getLocation().getBlock().getType() == Material.STATIONARY_WATER || tntPrimed.getLocation().getBlock().getType() == Material.WATER)) {
            loc = this.random(tntPrimed.getLocation());
            int i = 0;
            int n = 0;
            while (i < loc.size()) {
                List blockList = a.blockList();
                Location value = loc.get(n);
                blockList.add(value.getBlock());
                i = ++n;
            }
        }
        if (Main.get().getConfig().getBoolean("TnT.AguaExplodirCreeper") && a.getEntityType() == EntityType.CREEPER && (a.getEntity().getLocation().getBlock().getType() == Material.STATIONARY_WATER || a.getEntity().getLocation().getBlock().getType() == Material.WATER)) {
            loc = this.random(a.getEntity().getLocation());
            int j = 0;
            int n2 = 0;
            while (j < loc.size()) {
                List blockList2 = a.blockList();
                Location value2 = loc.get(n2);
                blockList2.add(value2.getBlock());
                j = ++n2;
            }
        }
    }

    @EventHandler
    public void activeTntDispenser(BlockDispenseEvent event) {
        if (event.getBlock().getType() != Material.DISPENSER ||
            event.getItem().getType() != Material.TNT ||
            !event.getItem().hasItemMeta() ||
            !event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.get().getConfig().getString("TnT.Nome").replace("&", "§"))) {
            return;
        }

        event.setCancelled(true);

        Dispenser dispenser = (Dispenser) event.getBlock().getState();
        org.bukkit.material.Dispenser dispenserData = (org.bukkit.material.Dispenser) dispenser.getData();
        BlockFace facing = dispenserData.getFacing();

        Location dispenserLoc = event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5);
        Location spawnLoc;

        switch (facing) {
            case UP:
                spawnLoc = dispenserLoc.clone().add(0, 1.0, 0);
                break;
            case DOWN:
                spawnLoc = dispenserLoc.clone().add(0, -1.0, 0);
                break;
            case NORTH:
                spawnLoc = dispenserLoc.clone().add(0, 0, -1.0);
                break;
            case SOUTH:
                spawnLoc = dispenserLoc.clone().add(0, 0, 1.0);
                break;
            case WEST:
                spawnLoc = dispenserLoc.clone().add(-1.0, 0, 0);
                break;
            case EAST:
                spawnLoc = dispenserLoc.clone().add(1.0, 0, 0);
                break;
            default:
                spawnLoc = dispenserLoc;
                break;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Dispenser dispenser = (Dispenser) event.getBlock().getState();
                dispenser.getInventory().removeItem(new ItemStack[]{TnT.ARMADILHA});
                TNTPrimed tnt = (TNTPrimed) spawnLoc.getWorld().spawn(spawnLoc, TNTPrimed.class);
                tnt.setMetadata("CanhaoTnT", new FixedMetadataValue(Main.get(), "CanhaoTnT"));
                tnt.setVelocity(new Vector(0.02, 0.15, 0.02));
                spawnLoc.getWorld().playSound(spawnLoc, Sound.FIRE_IGNITE, 1.0f, 1.0f);
            }
        }.runTaskLater(Main.get(), 1L);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void explodir(EntityExplodeEvent a) {
    	Location explosionLocation = a.getEntity().getLocation();
    	 double radius = 5.0; // Raio de efeito do knockback (ajuste conforme necessário)
        if (a.getEntityType() == EntityType.PRIMED_TNT && ((TNTPrimed)a.getEntity()).hasMetadata("CanhaoTnT")) {
            a.blockList().clear(); // Mantém a lógica original de não destruir blocos

            // Obter todas as entidades próximas no raio definido
            for (Entity entity : explosionLocation.getWorld().getNearbyEntities(explosionLocation, radius, radius, radius)) {
                // Aplica knockback apenas a entidades vivas que não sejam jogadores, a própria TNT, ou TNT com knockback
                if (entity instanceof LivingEntity && !(entity instanceof Player) && entity != a.getEntity()) {
                    // Se a entidade é TNT, verificar se é TNT sem knockback
                    if (entity.getType() == EntityType.PRIMED_TNT) {
                        if (!((TNTPrimed)entity).hasMetadata("CanhaoTnT")) {
                            // TNT com knockback pode afetar TNT sem knockback
                            entity.setVelocity(new Vector(0.02, 0.15, 0.02));
                        }
                        // TNT com knockback não afeta outra TNT com knockback
                    } else {
                        // Aplica knockback a outras entidades vivas (não TNT)
                        entity.setVelocity(new Vector(0.02, 0.15, 0.02));
                    }
                }
            }
        } else if (a.getEntityType() == EntityType.PRIMED_TNT && !((TNTPrimed)a.getEntity()).hasMetadata("CanhaoTnT")) {
            // TNT sem knockback não aplica knockback a nenhuma entidade
            for (Entity entity : explosionLocation.getWorld().getNearbyEntities(explosionLocation, radius, radius, radius)) {
                if (entity instanceof LivingEntity && !(entity instanceof Player) && entity != a.getEntity()) {
                    // Explicitamente não aplica knockback para nenhuma entidade
                    entity.setVelocity(new Vector(0, 0, 0));
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void Method5(BlockBreakEvent a) {
        if (a.getBlock().getState().hasMetadata("CanhaoTnT")) {
            a.getBlock().getState().removeMetadata("CanhaoTnT", Main.get());
            a.getPlayer().getInventory().addItem(new ItemStack[]{TnT.ARMADILHA});
            a.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void Method6(EventosInstance a) {
        Block block = a.getTNT().getWorld().getBlockAt(a.getX(), a.getY(), a.getZ());
        if (block.hasMetadata("CanhaoTnT")) {
            a.getTNT().setMetadata("CanhaoTnT", block.getMetadata("CanhaoTnT").get(0));
            a.getTNT().setFuseTicks(80);
        }
        new Run(this, block).runTaskLater(Main.get(), 60L);
    }
}