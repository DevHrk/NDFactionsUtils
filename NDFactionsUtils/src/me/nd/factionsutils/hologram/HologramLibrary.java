package me.nd.factionsutils.hologram;

import com.google.common.collect.ImmutableList;

import me.nd.factionsutils.Main;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class HologramLibrary {

	public static final Logger LOGGER = Main.get().getLogger();

	private static Plugin plugin;
	private static final Set<Hologram> holograms = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public static Hologram createHologram(Location location, List<String> lines) {
		return createHologram(location, lines.toArray(new String[0]));
	}

	public static Hologram createHologram(Location location, String... lines) {
		return createHologram(location, true, lines);
	}

	public static Hologram createHologram(Location location, boolean spawn, String... lines) {
		Hologram hologram = new Hologram(location, lines);
		if (spawn) {
			hologram.spawn();
		}
		holograms.add(hologram);
		return hologram;
	}

	public static void removeHologram(Hologram hologram) {
		if (holograms.remove(hologram)) {
			hologram.despawn();
		}
	}

	public static void unregisterAll() {
		holograms.forEach(Hologram::despawn);
		holograms.clear();
		plugin = null;
	}

	public static Entity getHologramEntity(int entityId) {
		return holograms.stream().filter(Hologram::isSpawned)
				.flatMap(hologram -> hologram.getLines().stream()
						.filter(line -> line.getArmor() != null && line.getArmor().getId() == entityId)
						.map(line -> line.getArmor().getEntity()))
				.findFirst().orElse(null);
	}

	public static Hologram getHologram(Entity entity) {
		return NMS.getHologram(entity);
	}

	public static boolean isHologramEntity(Entity entity) {
		return NMS.isHologramEntity(entity);
	}

	public static Collection<Hologram> listHolograms() {
		return ImmutableList.copyOf(holograms);
	}

	public static void setupHolograms(Main pl) {
		if (plugin == null) {
			plugin = pl;
			Bukkit.getPluginManager().registerEvents(new HologramListeners(), plugin);
		}
	}

	public static Plugin getPlugin() {
		return plugin;
	}
}