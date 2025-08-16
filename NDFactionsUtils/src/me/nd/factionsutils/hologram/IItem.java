package me.nd.factionsutils.hologram;

import org.bukkit.inventory.ItemStack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public interface IItem {

	public void setPassengerOf(Entity entity);

	public void setItemStack(ItemStack item);

	public void setLocation(double x, double y, double z);

	public boolean isDead();

	public void killEntity();

	public Item getEntity();

	public HologramLine getLine();

	public void update();

	public void hideToPlayer(Player player);

	public void showToPlayer(Player player);

	public void showToAll();
}
