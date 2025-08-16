package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.creeperevents.oggehej.obsidianbreaker.StorageHandler;
import com.creeperevents.oggehej.obsidianbreaker.UnknownBlockTypeException;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.factions.BreakerObsidian;
import me.nd.factionsutils.itens.BlazeExplosivo;

public class BlazeListener implements Listener {
    private static final String FIREBALL_NAME = "Fire";
    private static final double FIREBALL_SPEED = 0.7;
    private static final double TARGET_SEARCH_RADIUS = 16.0;
    private static final float FIREBALL_YIELD = 3f;

    private final List<Blaze> spawnedBlazes = new ArrayList<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!item.isSimilar(BlazeExplosivo.BOLADEFOGO)) return;

        if (!Utils.isZonaProtegida(player.getLocation())) {
            player.sendMessage(Main.get().getConfig("Mensagens").getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        Location spawn = getCorrectLocation(e.getClickedBlock().getLocation(), e.getBlockFace());
        Blaze blaze = (Blaze) spawn.getWorld().spawnEntity(spawn, EntityType.BLAZE);
        API.removeItem(player);
        spawnedBlazes.add(blaze);
        startFireballTask(blaze, player);
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Blaze) {
            spawnedBlazes.remove((Blaze) event.getEntity());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Blaze && spawnedBlazes.contains(event.getEntity())) {
            // Allow damage to spawned blazes so they can be killed
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getEntityType() == EntityType.FIREBALL && FIREBALL_NAME.equals(e.getEntity().getCustomName())) {
            FileConfiguration config = Main.get().getConfig();
            createFakeExplosionBlazeFireball(e.getLocation(), config.getInt("BlazeExplosivo.DanoDaExplosaoPorBloco"));
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

    private void startFireballTask(Blaze blaze, Player owner) {
        FileConfiguration config = Main.get().getConfig();
        double fireballInterval = config.getDouble("BlazeExplosivo.IntervaloTiros", 2.0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!blaze.isValid()) {
                    spawnedBlazes.remove(blaze);
                    cancel();
                    return;
                }
                throwFireball(blaze, owner);
            }
        }.runTaskTimer(Main.get(), 0L, (long) (fireballInterval * 20));
    }

    private void throwFireball(Blaze blaze, Player owner) {
        Location eyeLocation = blaze.getEyeLocation();
        Player target = findTarget(blaze, owner);

        // Atualizar rotação do blaze para mirar no alvo, se encontrado
        if (target != null) {
            Vector directionToTarget = target.getEyeLocation().toVector().subtract(eyeLocation.toVector()).normalize();
            blaze.teleport(blaze.getLocation().setDirection(directionToTarget));
        }

        // Lançar fireball na direção do olhar do blaze
        Vector direction = eyeLocation.getDirection();
        Fireball fireball = (Fireball) blaze.getWorld().spawnEntity(eyeLocation, EntityType.FIREBALL);
        fireball.setYield(FIREBALL_YIELD);
        fireball.setShooter(blaze);
        fireball.setVelocity(direction.normalize().multiply(FIREBALL_SPEED));
        fireball.setIsIncendiary(false);
        fireball.setCustomName(FIREBALL_NAME);
        fireball.setCustomNameVisible(true);
        eyeLocation.getWorld().playEffect(eyeLocation, Effect.GHAST_SHOOT, 1);
    }

    private Player findTarget(Blaze blaze, Player owner) {
        // Priorizar o jogador que invocou o blaze
        if (owner != null && owner.isOnline() && owner.getWorld() == blaze.getWorld()) {
            return owner;
        }

        // Procurar o jogador mais próximo dentro do raio
        Player closestPlayer = null;
        double closestDistance = TARGET_SEARCH_RADIUS * TARGET_SEARCH_RADIUS;

        for (Entity entity : blaze.getNearbyEntities(TARGET_SEARCH_RADIUS, TARGET_SEARCH_RADIUS, TARGET_SEARCH_RADIUS)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.isOnline()) {
                    double distance = entity.getLocation().distanceSquared(blaze.getLocation());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestPlayer = player;
                    }
                }
            }
        }
        return closestPlayer;
    }

    private void createFakeExplosionBlazeFireball(Location location, int maxPower) {
        StorageHandler breaker = BreakerObsidian.getStorage();
        FileConfiguration config = Main.get().getConfig();
        double radius = config.getDouble("BlazeExplosivo.AreaExplosao", 3.0);
        double maxDistance = config.getDouble("BlazeExplosivo.Distancia", 2.0);

        for (double x = location.getX() - radius; x <= location.getX() + radius; x++) {
            for (double y = location.getY() - radius; y <= location.getY() + radius; y++) {
                for (double z = location.getZ() - radius; z <= location.getZ() + radius; z++) {
                    Block block = location.getWorld().getBlockAt((int) x, (int) y, (int) z);
                    if (y >= 1 && breaker.isValidBlock(block)) {
                        double distance = location.distance(block.getLocation().add(0.5, 0.5, 0.5));
                        int power = distance < maxDistance ? maxPower : (int) (Math.random() * maxPower);
                        try {
                            if (breaker.addDamage(block, power)) {
                                block.breakNaturally();
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