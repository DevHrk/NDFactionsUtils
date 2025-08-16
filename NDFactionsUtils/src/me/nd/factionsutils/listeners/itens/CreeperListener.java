package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.creeperevents.oggehej.obsidianbreaker.StorageHandler;
import com.creeperevents.oggehej.obsidianbreaker.UnknownBlockTypeException;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.factions.BreakerObsidian;
import me.nd.factionsutils.itens.BolaDeFogo;
import me.nd.factionsutils.itens.SuperCreeper;
import me.nd.factionsutils.plugin.SConfig;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;

public class CreeperListener implements Listener {
    List<Creeper> immobileCreepers = new ArrayList<>();

    @EventHandler
    public void aoClicar(PlayerInteractEvent e) {
        SConfig m1 = Main.get().getConfig("Mensagens");
        Player p = e.getPlayer();
        if (e.getItem() == null) return;
        ItemStack item = e.getItem();

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.getInventory().getItemInHand().isSimilar(BolaDeFogo.BOLADEFOGO) && !Utils.isZonaProtegida(p.getLocation())) {
                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                e.setCancelled(true);
                return;
            }
            if (item.isSimilar(BolaDeFogo.BOLADEFOGO)) {
                throwFireball(e.getPlayer());
                API.removeItem(e.getPlayer());
                e.setCancelled(true);
                return;
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.getInventory().getItemInHand().isSimilar(SuperCreeper.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                e.setCancelled(true);
                return;
            }
            if (item.isSimilar(SuperCreeper.ARMADILHA)) {
                Location spawn = getCorrectLocation(e.getClickedBlock().getLocation(), e.getBlockFace());
                Creeper cc = (Creeper) spawn.getWorld().spawnEntity(spawn, EntityType.CREEPER);
                cc.setPowered(true);
                API.removeItem(e.getPlayer());
                immobileCreepers.add(cc);
                e.setCancelled(true);
                return;
            }

            if (p.getInventory().getItemInHand().isSimilar(me.nd.factionsutils.itens.Creeper.ARMADILHA) && !Utils.isZonaProtegida(p.getLocation())) {
                p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                e.setCancelled(true);
                return;
            }
            if (item.isSimilar(me.nd.factionsutils.itens.Creeper.ARMADILHA)) {
                Location spawn = getCorrectLocation(e.getClickedBlock().getLocation(), e.getBlockFace());
                Creeper cc = (Creeper) spawn.getWorld().spawnEntity(spawn, EntityType.CREEPER);
                API.removeItem(e.getPlayer());
                immobileCreepers.add(cc);
                disableAI(cc);
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Creeper) {
            immobileCreepers.remove((Creeper) event.getEntity());
        }
    }

    private void disableAI(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity)) return;

        org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
        try {
            Entity nmsEntity = ((CraftLivingEntity) living).getHandle();
            nmsEntity.getDataWatcher().watch(15, (byte) 1); // Mark as no AI (bit 0x01 = noAI in 1.8.8)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Location getCorrectLocation(Location l, BlockFace face) {
        switch (face) {
            case UP: return l.add(0.5, 1.0, 0.5);
            case DOWN: return l.add(0.5, -1.0, 0.5);
            case EAST: return l.add(1.5, 0.0, 0.5);
            case WEST: return l.add(-0.5, 0.0, 0.5);
            case SOUTH: return l.add(0.5, 0.0, 1.5);
            case NORTH: return l.add(0.5, 0.0, -0.5);
            default: return l;
        }
    }

    private void throwFireball(Player p) {
        Vector vec = p.getEyeLocation().getDirection().multiply(0.5);
        Location location = p.getLocation().add(0.0, 1.0, 0.0);
        Fireball fireball = (Fireball) p.getWorld().spawnEntity(location, EntityType.FIREBALL);
        fireball.setYield(4f);
        fireball.setShooter(p);
        fireball.setVelocity(vec);
        fireball.setIsIncendiary(false);
        fireball.setCustomNameVisible(true);
        location.getWorld().playEffect(location, Effect.GHAST_SHOOT, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void aoExplodir(EntityExplodeEvent e) {
        FileConfiguration m = Main.get().getConfig();
        if (e.getEntityType() == EntityType.CREEPER) {
            if (((Creeper) e.getEntity()).isPowered()) {
                createFakeExplosionSuperCreeper(e.getLocation(), m.getInt("Super_Creeper.DanoDaExplosaoPorBloco"));
            } else {
                createFakeExplosionCreeper(e.getLocation(), m.getInt("Creeper.DanoDaExplosaoPorBloco"));
            }
        }
        if (e.getEntityType() == EntityType.FIREBALL) {
            if (e.getEntity().isCustomNameVisible()) {
                createFakeExplosionBolaDeFogo(e.getLocation(), m.getInt("Bola_de_Fogo.DanoDaExplosaoPorBloco"));
            }
        }
    }

    private void createFakeExplosionBolaDeFogo(Location l, int maxPower) {
        StorageHandler breaker = BreakerObsidian.getStorage();
        FileConfiguration m = Main.get().getConfig();
        double radius = m.getDouble("Bola_de_Fogo.AreaExplosao");
        for (double x = l.getX() - radius; x <= l.getX() + radius; x++) {
            for (double y = l.getY() - radius; y <= l.getY() + radius; y++) {
                for (double z = l.getZ() - radius; z <= l.getZ() + radius; z++) {
                    Block b = new Location(l.getWorld(), x, y, z).getBlock();
                    if (y >= 1) {
                        if (breaker.isValidBlock(b)) {
                            double distance = l.distance(new Location(l.getWorld(), x, y, z));
                            int power;
                            if (distance < m.getDouble("Bola_de_Fogo.Distancia")) {
                                power = maxPower;
                            } else {
                                power = (int) (Math.random() * maxPower);
                            }
                            try {
                                if (breaker.addDamage(b, power)) {
                                    b.breakNaturally();
                                }
                            } catch (UnknownBlockTypeException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void createFakeExplosionCreeper(Location l, int maxPower) {
        StorageHandler breaker = BreakerObsidian.getStorage();
        FileConfiguration m = Main.get().getConfig();
        double radius = m.getDouble("Creeper.AreaExplosao");
        for (double x = l.getX() - radius; x <= l.getX() + radius; x++) {
            for (double y = l.getY() - radius; y <= l.getY() + radius; y++) {
                for (double z = l.getZ() - radius; z <= l.getZ() + radius; z++) {
                    Block b = new Location(l.getWorld(), x, y, z).getBlock();
                    if (y >= 1) {
                        if (breaker.isValidBlock(b)) {
                            double distance = l.distance(new Location(l.getWorld(), x, y, z));
                            int power;
                            if (distance < m.getDouble("Creeper.Distancia")) {
                                power = maxPower;
                            } else {
                                power = (int) (Math.random() * maxPower);
                            }
                            try {
                                if (breaker.addDamage(b, power)) {
                                    b.breakNaturally();
                                }
                            } catch (UnknownBlockTypeException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void createFakeExplosionSuperCreeper(Location l, int maxPower) {
        StorageHandler breaker = BreakerObsidian.getStorage();
        FileConfiguration m = Main.get().getConfig();
        double radius = m.getDouble("Super_Creeper.AreaExplosao");
        for (double x = l.getX() - radius; x <= l.getX() + radius; x++) {
            for (double y = l.getY() - radius; y <= l.getY() + radius; y++) {
                for (double z = l.getZ() - radius; z <= l.getZ() + radius; z++) {
                    Block b = new Location(l.getWorld(), x, y, z).getBlock();
                    if (y >= 1) {
                        if (breaker.isValidBlock(b)) {
                            double distance = l.distance(new Location(l.getWorld(), x, y, z));
                            int power;
                            if (distance < m.getDouble("Super_Creeper.Distancia")) {
                                power = maxPower;
                            } else {
                                power = (int) (Math.random() * maxPower);
                            }
                            try {
                                if (breaker.addDamage(b, power)) {
                                    b.breakNaturally();
                                }
                            } catch (UnknownBlockTypeException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}