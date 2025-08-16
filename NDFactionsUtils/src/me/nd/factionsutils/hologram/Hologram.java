package me.nd.factionsutils.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Hologram {

	private String attached;

	private boolean spawned;
	private Location location;
	private ConcurrentHashMap<Integer, HologramLine> lines = new ConcurrentHashMap<>();
	private CopyOnWriteArraySet<String> viewers = new CopyOnWriteArraySet<>();

	private int entityId;
	private String customName;
	private Entity entity;

	public Hologram(Location location, String... lines) {
		this.location = location;

		int current = 0;
		for (String line : lines) {
			this.lines.put(++current, new HologramLine(this, location.clone().add(0, 0.33 * current, 0), line));
		}
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public void setAttached(String player) {
		this.attached = player;
	}

	public Hologram spawn() {
		if (spawned) {
			return this;
		}

		this.lines.values().forEach(HologramLine::spawn);
		this.spawned = true;
		return this;
	}

	public Hologram despawn() {
		if (!spawned) {
			return this;
		}

		this.lines.values().forEach(HologramLine::despawn);
		this.spawned = false;
		return this;
	}

	public Hologram withLine(String line) {
		int l = 1;
		while (this.lines.containsKey(l)) {
			l++;
		}

		this.lines.put(l, new HologramLine(this, this.location.clone().add(0, 0.33 * l, 0), line));
		if (spawned) {
			this.lines.get(l).spawn();
		}

		return this;
	}

	public Hologram updateLine(int id, String line) {
		if (!this.lines.containsKey(id)) {
			return this;
		}

		HologramLine hl = this.lines.get(id);
		hl.setLine(line);
		return this;
	}

	public boolean canSee(Player player) {
		return this.attached == null || this.attached.equals(player.getName())
				|| this.viewers.contains(player.getName());
	}

	public boolean isSpawned() {
		return this.spawned;
	}

	public Location getLocation() {
		return this.location;
	}

	public HologramLine getLine(int id) {
		return this.lines.get(id);
	}

	public Collection<HologramLine> getLines() {
		return this.lines.values();
	}

	public Hologram addViewer(Player player) {
		this.viewers.add(player.getName());
		return this;
	}

	public Hologram removeViewer(Player player) {
		this.viewers.remove(player.getName());
		for (HologramLine line : this.lines.values()) {
			if (line.getArmor() != null) {
				line.getArmor().hideToPlayer(player);
			}
		}
		return this;
	}

	public Set<String> getViewers() {
		return this.viewers;
	}

	public Hologram setText(String text) {
		updateLine(1, text);
		return this;
	}

	public Hologram update() {
		for (HologramLine line : lines.values()) {
			line.update();
		}
		return this;
	}

	public boolean isVisibleTo(Player player) {
		return canSee(player) && isSpawned();
	}

	public int getLineCount() {
		return lines.size();
	}

	public HologramLine getHighestLine() {
		return lines.values().stream().max((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).orElse(null);
	}

	public HologramLine getLowestLine() {
		return lines.values().stream().min((o1, o2) -> Integer.compare(o1.getId(), o2.getId())).orElse(null);
	}

	public Hologram respawn() {
		if (spawned) {
			despawn();
		}
		spawn();
		return this;
	}

	public void despawnForPlayer(Player player) {
		// Verifica se o jogador está no mesmo mundo que o holograma
		if (canSee(player) && player.getWorld().equals(this.location.getWorld())) {
			for (HologramLine line : lines.values()) {
				line.despawnForPlayer(player);
			}
		}
	}

	public void respawnForPlayer(Player player) {
		// Verifica se o jogador está no mesmo mundo que o holograma
		if (canSee(player) && player.getWorld().equals(this.location.getWorld())) {
			for (HologramLine line : lines.values()) {
				line.respawnForPlayer(player);
			}
		}
	}

	public int size() {
		return lines.size();
	}

	public Hologram addLine(String line) {
		int l = 1;
		while (this.lines.containsKey(l)) {
			l++;
		}

		this.lines.put(l, new HologramLine(this, this.location.clone().add(0, 0.33 * l, 0), line));
		if (spawned) {
			this.lines.get(l).spawn();
		}

		return this;
	}

	public Hologram removeLine(int id) {
		// Verifica se a linha existe
		if (!this.lines.containsKey(id)) {
			return this;
		}

		// Remove a linha do mapa
		HologramLine removedLine = this.lines.remove(id);

		// Despawna a linha, se o holograma estiver spawnado
		if (this.spawned) {
			removedLine.despawn();
		}

		// Reorganiza as posições das linhas restantes
		for (int i = id + 1; i <= this.lines.size() + 1; i++) {
			HologramLine line = this.lines.remove(i);
			if (line != null) {
				// Atualiza o ID da linha e sua posição
				line.setId(i - 1);
				line.setLocation(this.location.clone().add(0, 0.33 * (i - 1), 0));
				this.lines.put(i - 1, line);

				// Atualiza a linha no mundo, se o holograma estiver spawnado
				if (this.spawned) {
					line.respawn();
				}
			}
		}

		return this;
	}
    public Hologram setLines(String... lines) {
        // Despawn and clear existing lines
        if (spawned) {
            this.lines.values().forEach(HologramLine::despawn);
        }
        this.lines.clear();

        // Add new lines
        int current = 0;
        for (String line : lines) {
            this.lines.put(++current, new HologramLine(this, this.location.clone().add(0, 0.33 * current, 0), line));
        }

        // Spawn new lines if hologram is spawned
        if (spawned) {
            this.lines.values().forEach(HologramLine::spawn);
        }

        return this;
    }

    public Hologram setLocation(Location location) {
        this.location = location.clone();

        // Update each line's position
        int index = 1;
        for (HologramLine line : this.lines.values()) {
            line.setLocation(this.location.clone().add(0, 0.33 * index, 0));
            if (spawned) {
                line.respawn(); // Respawn to update position
            }
            index++;
        }

        return this;
    }
}