package me.nd.factionsutils.api;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import java.util.HashMap;
import java.util.Map;

public class ActionbarAPI
{
	private static Map<Player, BukkitTask> PENDING_MESSAGES = new HashMap<>();

	public static void sendActionBarMessage(Player player, String message) {
	    sendRawActionBarMessage(player, "{\"text\": \"" + message + "\"}");
	}

	public static void sendRawActionBarMessage(Player player, String rawMessage) {
	    IChatBaseComponent chatBaseComponent = IChatBaseComponent.ChatSerializer.a(rawMessage);
	    PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(chatBaseComponent, (byte)2);
	    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutChat);
	}

	public static void sendActionBarMessage(Player player, String message, int duration, Plugin plugin) {
	    cancelPendingMessages(player);
	    BukkitTask messageTask = new BukkitRunnable() {
	        private int count = 0;

	        public void run() {
	            if (this.count >= duration - 3) {
	                this.cancel();
	            }
	            sendActionBarMessage(player, message);
	            ++this.count;
	        }
	    }.runTaskTimer(plugin, 0L, 20L);
	    PENDING_MESSAGES.put(player, messageTask);
	}

	private static void cancelPendingMessages(Player player) {
	    if (PENDING_MESSAGES.containsKey(player)) {
	        PENDING_MESSAGES.get(player).cancel();
	    }
	}
}
