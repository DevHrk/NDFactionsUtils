package me.nd.factionsutils.hologram;

import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HologramLine {

	private Location location;
	private IArmorStand armor;
	private ISlime slime;
	private IItem item;
	private TouchHandler touch;
	private PickupHandler pickup;
	private String line;
	private Hologram hologram;
	private int id;

	public HologramLine(Hologram hologram, Location location, String line) {
	    if (hologram == null) {
	        throw new IllegalArgumentException("Hologram cannot be null");
	    }
	    if (location == null) {
	        throw new IllegalArgumentException("Location cannot be null");
	    }
	    if (line == null) {
	        throw new IllegalArgumentException("Line cannot be null");
	    }
	    this.line = StringUtils.formatColors(line);
	    this.location = location;
	    this.hologram = hologram;
	}

	public void spawn() {
	    if (this.hologram == null) {
	        Bukkit.getLogger().warning("Hologram is null in HologramLine.spawn()");
	        return;
	    }
	    if (this.location == null) {
	        Bukkit.getLogger().warning("Location is null in HologramLine.spawn()");
	        return;
	    }

	    if (this.armor == null) {
	        this.armor = NMS.createArmorStand(location, line, this);
	        if (this.armor == null) {
	            return;
	        }
	    }

	    // Atualiza o ArmorStand existente
	    this.armor.setName(line);

	    // Verifica se há players com addViewer
	    boolean hasViewers = !this.hologram.getViewers().isEmpty();
	    Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());

	    if (hasViewers) {
	        for (String viewer : this.hologram.getViewers()) {
	            Player player = Bukkit.getPlayer(viewer);
	            if (player != null && onlinePlayers.contains(player)) {
	                spawnForPlayer(player);
	                onlinePlayers.remove(player); // Remova o jogador da lista aqui
	            }
	        }
	        for (Player player : onlinePlayers) {
	            hideToPlayer(player);
	        }
	    } else {
	        for (Player player : onlinePlayers) {
	            spawnForPlayer(player);
	        }
	    }
	}

	public void despawn() {
		if (this.armor != null) {
			this.armor.killEntity();
			this.armor = null;
		}
		if (this.slime != null) {
			this.slime.killEntity();
			this.slime = null;
		}
		if (this.item != null) {
			this.item.killEntity();
			this.item = null;
		}
	}

	public void setTouchable(TouchHandler touch) {
		if (touch == null) {
			if (this.slime != null) {
				this.slime.killEntity();
				this.slime = null;
			}
			this.touch = null;
			return;
		}

		if (this.armor != null) {
			this.slime = this.slime == null ? NMS.createSlime(location, this) : this.slime;

			if (this.slime != null) {
				this.slime.setPassengerOf(this.armor.getEntity());
			}

			this.touch = touch;
		}
	}

	public void setItem(ItemStack item, PickupHandler pickup) {
		if (pickup == null) {
			if (this.item != null) {
				this.item.killEntity();
				this.item = null;
			}
			this.pickup = null;
			return;
		}

		if (this.armor != null) {
			this.item = this.item == null ? NMS.createItem(location, item, this) : this.item;

			if (this.item != null) {
				this.item.setPassengerOf(this.armor.getEntity());
			}

			this.pickup = pickup;
		}
	}

	public void setLocation(Location location) {
		if (this.armor != null) {
			this.armor.setLocation(location.getX(), location.getY(), location.getZ());
			if (this.slime != null) {
				this.slime.setPassengerOf(this.armor.getEntity());
			}
		}
	}

	public void setLine(String line) {
		if (this.armor == null) {
			if (this.hologram.isSpawned()) {
				this.spawn();
			}
			return;
		}

		if (this.line.equals(StringUtils.formatColors(line))) {
			this.armor.setName(this.line + "§r");
			this.line = this.line + "§r";
			return;
		}

		this.line = StringUtils.formatColors(line);
		this.armor.setName(this.line);
		this.spawn();
	}

	public Location getLocation() {
		return this.location;
	}

	public IArmorStand getArmor() {
		return this.armor;
	}

	public ISlime getSlime() {
		return this.slime;
	}

	public TouchHandler getTouchHandler() {
		return this.touch;
	}

	public PickupHandler getPickupHandler() {
		return this.pickup;
	}

	public String getLine() {
		return this.line;
	}

	public Hologram getHologram() {
		return this.hologram;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void update() {
		if (this.armor != null) {
			this.armor.update();
		}
		if (this.slime != null) {
			this.slime.update();
		}
		if (this.item != null) {
			this.item.update();
		}
	}

	public boolean isVisibleTo(Player player) {
		return this.hologram.canSee(player) && this.armor != null;
	}

	public void despawnForPlayer(Player player) {
		if (canShowToPlayer(player)) {
			if (this.armor != null) {
				this.armor.hideToPlayer(player);
			}
			if (this.slime != null) {
				this.slime.hideToPlayer(player);
			}
			if (this.item != null) {
				this.item.hideToPlayer(player);
			}
		}
	}

	public void respawnForPlayer(Player player) {
		if (canShowToPlayer(player)) {
			if (this.armor != null) {
				this.armor.showToPlayer(player);
			}
			if (this.slime != null) {
				this.slime.showToPlayer(player);
			}
			if (this.item != null) {
				this.item.showToPlayer(player);
			}
		}
	}

	public void respawn() {
		if (this.armor == null) {
			this.armor = NMS.createArmorStand(location, this.line, this);
		} else {
			this.armor.setName(this.line);
		}

		// Mostra o ArmorStand apenas para jogadores que podem ver e estão no mesmo
		// mundo
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (canShowToPlayer(player)) {
				this.armor.showToPlayer(player);
			}
		}

		// Se houver um slime, respawn também
		if (this.slime != null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (canShowToPlayer(player)) {
					this.slime.showToPlayer(player);
				}
			}
		}

		// Se houver um item, respawn também
		if (this.item != null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (canShowToPlayer(player)) {
					this.item.showToPlayer(player);
				}
			}
		}
	}

	public void spawnForPlayer(Player player) {
		// Verifica se o jogador pode ver o holograma
		if (canShowToPlayer(player)) {
			// Mostra o ArmorStand para o jogador
			if (this.armor != null) {
				this.armor.showToPlayer(player);
			}
			// Mostra o Slime para o jogador, se existir
			if (this.slime != null) {
				this.slime.showToPlayer(player);
			}
			// Mostra o Item para o jogador, se existir
			if (this.item != null) {
				this.item.showToPlayer(player);
			}
		}
	}

	public void hideToPlayer(Player player) {
		// Verifica se o jogador pode ver o holograma
		if (canShowToPlayer(player)) {
			// Esconde o ArmorStand do jogador
			if (this.armor != null) {
				this.armor.hideToPlayer(player);
			}
			// Esconde o Slime do jogador, se existir
			if (this.slime != null) {
				this.slime.hideToPlayer(player);
			}
			// Esconde o Item do jogador, se existir
			if (this.item != null) {
				this.item.hideToPlayer(player);
			}
		}
	}

	private boolean canShowToPlayer(Player player) {
		return this.hologram.canSee(player) && player.getWorld().equals(this.location.getWorld());
	}
}