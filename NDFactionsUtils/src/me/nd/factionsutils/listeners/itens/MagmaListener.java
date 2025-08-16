package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import net.minecraft.server.v1_8_R3.Entity;
import com.creeperevents.oggehej.obsidianbreaker.StorageHandler;
import com.creeperevents.oggehej.obsidianbreaker.UnknownBlockTypeException;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.factions.BreakerObsidian;
import me.nd.factionsutils.hologram.HologramLibrary;
import me.nd.factionsutils.hologram.Hologram;
import me.nd.factionsutils.itens.MagmaFlamejante;

public class MagmaListener implements Listener {
    private static final String MAGMA_NAME = "Magma";
    private static final int EXPLOSION_DAMAGE = 10;
    private static final double EXPLOSION_RADIUS = 3.0;
    private static final double EXPLOSION_DISTANCE = 2.0;
    private static final long EXPLOSION_DELAY = 100L; // 5 segundos (20 ticks/segundo * 5)
    private static final java.text.DecimalFormat DECIMAL_FORMAT = new java.text.DecimalFormat("#.#");
    private static final int MAGMA_SIZE = 2; // Tamanho pequeno do Magma Cube

    private final List<MagmaCube> spawnedMagmaCubes = new ArrayList<>();
    private final java.util.Map<MagmaCube, Hologram> magmaHolograms = new java.util.HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!item.isSimilar(MagmaFlamejante.BOLADEFOGO)) return;

        if (!Utils.isZonaProtegida(player.getLocation())) {
            player.sendMessage(Main.get().getConfig("Mensagens").getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        Location spawn = getCorrectLocation(e.getClickedBlock().getLocation(), e.getBlockFace());
        MagmaCube magma = (MagmaCube) spawn.getWorld().spawnEntity(spawn, EntityType.MAGMA_CUBE);
        magma.setCustomName(MAGMA_NAME);
        magma.setCustomNameVisible(true);
        magma.setSize(MAGMA_SIZE); // Definir tamanho padrão do Magma Cube
        API.removeItem(player);
        spawnedMagmaCubes.add(magma);
        disableAI(magma);

        // Criar holograma
        Hologram hologram = HologramLibrary.createHologram(
            spawn.clone().add(0.0, 0.3, 0.0), // Posição acima do Magma Cube
            "§7" + DECIMAL_FORMAT.format(EXPLOSION_DELAY / 20.0) + "s"
        );
        magmaHolograms.put(magma, hologram);

        scheduleExplosion(magma);
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof MagmaCube) {
            MagmaCube magma = (MagmaCube) event.getEntity();
            if (spawnedMagmaCubes.contains(magma)) {
                cleanupMagma(magma);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getEntityType() == EntityType.MAGMA_CUBE && MAGMA_NAME.equals(e.getEntity().getCustomName())) {
            e.setCancelled(true); // Cancela a explosão padrão para usar a personalizada
            createFakeExplosionMagmaFlamejante(e.getLocation());
            cleanupMagma((MagmaCube) e.getEntity());
        }
    }

    private Location getCorrectLocation(Location location, BlockFace face) {
        switch (face) {
            case UP: return location.add(0.5, 1.0, 0.5);
            case DOWN: return location.add(0.5, -1.0, 0.5);
            case EAST: return location.add(1.5, 0.0, 0.5);
            case WEST: return location.add(-0.5, 0.0, 0.5);
            case SOUTH: return location.add(0.5, 0.0, 1.5);
            case NORTH: return location.add(0.5, 0.0, -0.5);
            default: return location;
        }
    }

    private void disableAI(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof org.bukkit.entity.LivingEntity)) return;

        org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
        try {
            Entity nmsEntity = ((CraftLivingEntity) living).getHandle();
            nmsEntity.getDataWatcher().watch(15, (byte) 1); // Define noAI em 1.8.8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleExplosion(MagmaCube magma) {
        new BukkitRunnable() {
            double timeLeft = EXPLOSION_DELAY / 20.0; // Tempo em segundos

            @Override
            public void run() {
                if (!magma.isValid()) {
                    cleanupMagma(magma);
                    cancel();
                    return;
                }

                // Atualizar holograma com o tempo restante
                Hologram hologram = magmaHolograms.get(magma);
                if (hologram != null) {
                    hologram.setLines("§f" + DECIMAL_FORMAT.format(timeLeft) + "s");
                }

                timeLeft -= 0.05; // Decrementar 0.05 segundos (1 tick)

                if (timeLeft <= 0) {
                    // Forçar a explosão removendo o Magma Cube e criando a explosão personalizada
                    magma.remove();
                    createFakeExplosionMagmaFlamejante(magma.getLocation());
                    cleanupMagma(magma);
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 0L, 1L); // Executar a cada tick (0.05 segundos)
    }

    private void createFakeExplosionMagmaFlamejante(Location location) {
        StorageHandler breaker = BreakerObsidian.getStorage();
        for (double x = location.getX() - EXPLOSION_RADIUS; x <= location.getX() + EXPLOSION_RADIUS; x++) {
            for (double y = location.getY() - EXPLOSION_RADIUS; y <= location.getY() + EXPLOSION_RADIUS; y++) {
                for (double z = location.getZ() - EXPLOSION_RADIUS; z <= location.getZ() + EXPLOSION_RADIUS; z++) {
                    Block block = location.getWorld().getBlockAt((int) x, (int) y, (int) z);
                    if (y >= 1 && breaker.isValidBlock(block)) {
                        double distance = location.distance(block.getLocation().add(0.5, 0.5, 0.5));
                        int power = distance < EXPLOSION_DISTANCE ? EXPLOSION_DAMAGE : (int) (Math.random() * EXPLOSION_DAMAGE);
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
        // Tocar efeito sonoro de explosão
        location.getWorld().playEffect(location, Effect.EXPLOSION_HUGE, 1);
    }

    private void cleanupMagma(MagmaCube magma) {
        spawnedMagmaCubes.remove(magma);
        Hologram hologram = magmaHolograms.remove(magma);
        if (hologram != null) {
            HologramLibrary.removeHologram(hologram);
        }
    }
}