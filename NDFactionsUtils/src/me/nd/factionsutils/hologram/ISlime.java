package me.nd.factionsutils.hologram;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

public interface ISlime {

	public void setPassengerOf(Entity entity);

	public void setLocation(double x, double y, double z);

	public boolean isDead();

	public void killEntity();

	public Slime getEntity();

	public HologramLine getLine();

	public void update();

	public void hideToPlayer(Player player);

	public void showToPlayer(Player player);

	public void showToAll();
}
