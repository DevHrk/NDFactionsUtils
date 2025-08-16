package me.nd.factionsutils.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class BloquearPigZombie implements Listener{
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Check if the spawned entity is a Pig Zombie
        if (event.getEntityType() == EntityType.PIG_ZOMBIE) {
            // Check if the spawn reason is due to a Nether portal
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) {
                // Cancel the spawn event to prevent Pig Zombie from spawning
                event.setCancelled(true);
            }
        }
    }
}
