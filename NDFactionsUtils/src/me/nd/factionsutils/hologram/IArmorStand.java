package me.nd.factionsutils.hologram;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public interface IArmorStand {

	int getId();

	void setName(String name);

	void setLocation(double x, double y, double z);

	boolean isDead();

	void killEntity();

	void showToPlayer(Player player);

	public void hideToPlayer(Player player);

	ArmorStand getEntity();

	HologramLine getLine();

	void update();

	void showToAll();
}
