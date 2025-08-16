package me.nd.factionsutils.listeners.itens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.factions.MassiveFactions;
import me.nd.factionsutils.factions.SpawnFireWork;
import me.nd.factionsutils.itens.PoderMax;
import me.nd.factionsutils.plugin.SConfig;

public class PoderMaxListene implements Listener  {
	@EventHandler
	void aoClicar(PlayerInteractEvent e) {
	    SConfig m1 = Main.get().getConfig("Mensagens");
	    Player p = e.getPlayer();
	    ItemStack itemInHand = p.getInventory().getItemInHand();

	    if (itemInHand == null) return;

	    boolean isRightClick = e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK;
	    boolean isPoderMaximo = itemInHand.isSimilar(PoderMax.PODER_MAXIMO);

	    if (isRightClick && isPoderMaximo) {
	        if (!Utils.isZonaProtegida(p.getLocation())) {
	            p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
	            e.setCancelled(true);
	        } else if (MassiveFactions.upMaxPower(p)) {
	            SpawnFireWork.big(p);
	            API.removeItem(p);
	            e.setCancelled(true);
	        }
	    }
	}
}
