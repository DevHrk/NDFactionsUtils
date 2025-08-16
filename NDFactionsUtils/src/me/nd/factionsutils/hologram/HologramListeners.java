package me.nd.factionsutils.hologram;

import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.*;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramListeners implements Listener {

	private final Plugin plugin;
	private final Map<Player, Long> anticlickSpam = new ConcurrentHashMap<>();
	private final Map<ChunkCoord, List<Hologram>> toRespawn = new ConcurrentHashMap<>();

	public HologramListeners() {
		this.plugin = HologramLibrary.getPlugin();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemSpawn(ItemSpawnEvent event) {
		if (event.getEntity() instanceof IItem) {
			event.setCancelled(false); // NMS item holograma, não cancelar
		}
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		for (Hologram hologram : HologramLibrary.listHolograms()) {
			if (hologram.isSpawned() && hologram.getLocation().getWorld().equals(event.getFrom())) {
				hologram.despawnForPlayer(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		if (plugin.equals(event.getPlugin())) {
			HologramLibrary.unregisterAll();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		anticlickSpam.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() != EntityType.SLIME || !(event.getRightClicked() instanceof ISlime)) return;

		Player player = event.getPlayer();
		if (player.getGameMode().toString().contains("SPECTATOR")) return;

		long now = System.nanoTime();
		Long lastClick = anticlickSpam.put(player, now);
		if (lastClick != null && now - lastClick < 1_000_000_000L) return;

		ISlime slime = (ISlime) event.getRightClicked();
		HologramLine line = slime.getLine();
		if (line != null && line.getTouchHandler() != null) {
			line.getTouchHandler().onTouch(player);
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		toRespawn.entrySet().removeIf(entry -> {
			ChunkCoord coord = entry.getKey();
			if (!coord.world.equals(event.getWorld().getName())) return false;

			if (event.getWorld().isChunkLoaded(coord.x, coord.z)) {
				entry.getValue().forEach(Hologram::respawn);
				return true;
			}
			return false;
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldUnload(WorldUnloadEvent event) {
		List<Hologram> hologramsToRespawn = new ArrayList<>();
		for (Hologram hologram : HologramLibrary.listHolograms()) {
			if (hologram != null && hologram.isSpawned() && hologram.getLocation().getWorld().equals(event.getWorld())) {
				if (event.isCancelled()) {
					respawnAllFromCoord(getChunkCoord(hologram.getLocation().getChunk()));
				} else {
					hologram.despawn();
					hologramsToRespawn.add(hologram);
				}
			}
		}
		for (Hologram hologram : hologramsToRespawn) {
			storeForRespawn(hologram);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		for (Hologram hologram : HologramLibrary.listHolograms()) {
			if (hologram == null || !hologram.isSpawned()) continue;
			if (!hologram.getLocation().getChunk().equals(chunk)) continue;

			for (Player player : chunk.getWorld().getPlayers()) {
				if (player.getWorld().equals(hologram.getLocation().getWorld()) &&
						player.getLocation().getChunk().equals(chunk)) {
					hologram.despawnForPlayer(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		for (Hologram hologram : HologramLibrary.listHolograms()) {
			if (hologram == null || !hologram.isSpawned()) continue;
			if (!hologram.getLocation().getChunk().equals(chunk)) continue;

			for (Player player : chunk.getWorld().getPlayers()) {
				if (player.getWorld().equals(hologram.getLocation().getWorld()) &&
						player.getLocation().getChunk().equals(chunk)) {
					hologram.respawnForPlayer(player);
				}
			}
		}
	}

	private void respawnAllFromCoord(ChunkCoord coord) {
		List<Hologram> holograms = toRespawn.remove(coord);
		if (holograms != null) {
			holograms.forEach(Hologram::respawn);
		}
	}

	private void storeForRespawn(Hologram hologram) {
		ChunkCoord coord = getChunkCoord(hologram.getLocation().getChunk());
		toRespawn.computeIfAbsent(coord, k -> new ArrayList<>()).add(hologram);
	}

	private ChunkCoord getChunkCoord(Chunk chunk) {
		return new ChunkCoord(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	private static class ChunkCoord {
		private final String world;
		private final int x, z;

		public ChunkCoord(String world, int x, int z) {
			this.world = world;
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ChunkCoord that = (ChunkCoord) o;
			return x == that.x && z == that.z && world.equals(that.world);
		}

		@Override
		public int hashCode() {
			return Objects.hash(world, x, z);
		}
	}
}
