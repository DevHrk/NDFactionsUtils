package me.nd.factionsutils.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import me.nd.factionsutils.Main;

public class ZeusListener implements Listener {

    private BukkitRunnable waterDamageTask = null;

    public ZeusListener() {
        // Inicia a tarefa de dano na água quando o listener é registrado
        startWaterDamageTask();
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        // Verifica se o evento ativo é o impulso na água
        if (!Main.get().getZeusEvent().equals("water_boost")) {
            return;
        }

        Player player = event.getPlayer();
        // Verifica se o jogador está agachando (shift) e em água
        if (event.isSneaking() && (player.getLocation().getBlock().getType() == Material.WATER || 
                                   player.getLocation().getBlock().getType() == Material.STATIONARY_WATER)) {
            // Aplica impulso para frente
            Vector direction = player.getLocation().getDirection().normalize().multiply(1.5);
            direction.setY(0.5); // Leve impulso vertical
            player.setVelocity(direction);
        }
    }

    @EventHandler
    public void onBlockFromTo(EntityChangeBlockEvent e) {
        // Verifica se o evento ativo é o de areia sem física
        if (Main.get().getZeusEvent().equals("no_gravity_sand")) {
            // Cancela a física de areia e cascalho
            if (e.getEntityType() == EntityType.FALLING_BLOCK && e.getTo() == Material.AIR) {
			if (e.getBlock().getType() == Material.SAND || e.getBlock().getType() == Material.GRAVEL) {
				e.setCancelled(true);
				e.getBlock().getState().update(false, false);
			}
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Verifica se o evento ativo é o de água/lava e se a entidade é um jogador
        if (Main.get().getZeusEvent().equals("water_lava_swap") && event.getEntity() instanceof Player) {
            // Cancela danos de lava, fogo ou queima
            if (event.getCause() == EntityDamageEvent.DamageCause.LAVA || 
                event.getCause() == EntityDamageEvent.DamageCause.FIRE || 
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
            }
        }
    }

    private void startWaterDamageTask() {
        waterDamageTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Verifica se o evento ativo é water_lava_swap
                if (!Main.get().getZeusEvent().equals("water_lava_swap")) {
                    return;
                }

                // Itera sobre todos os jogadores online
                for (Player player : Main.get().getServer().getOnlinePlayers()) {
                    // Verifica se o jogador está em um bloco de água
                    Material blockType = player.getLocation().getBlock().getType();
                    if (blockType == Material.WATER || blockType == Material.STATIONARY_WATER) {
                        // Aplica 1 coração de dano (2.0) por tick
                        player.damage(2.0);
                    }
                }
            }
        };
        // Executa a cada 20 ticks (1 segundo)
        waterDamageTask.runTaskTimer(Main.get(), 0L, 20L);
    }

    public void stopWaterDamageTask() {
        if (waterDamageTask != null) {
            waterDamageTask.cancel();
            waterDamageTask = null;
        }
    }
}