package me.nd.factionsutils.listeners.itens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.SuperSocoItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class SuperSocoListener implements Listener {
    public static List<Object> SuperSoco = new ArrayList<>();
     FileConfiguration m = Main.get().getConfig(); 
     @EventHandler
     void onClick5(PlayerInteractEvent e) {
         SConfig m1 = Main.get().getConfig("Mensagens");
         Player p = e.getPlayer();
         ItemStack itemInHand = p.getInventory().getItemInHand();

         if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

         if (itemInHand.isSimilar(SuperSocoItem.ARMADILHA)) {
             if (!Utils.isZonaProtegida(p.getLocation())) {
                 p.sendMessage(m1.getString("Mensagens.ZonaProtegida").replace("&", "§"));
                 e.setCancelled(true);
             } else {
                 SuperSoco.add(p.getName());
                 MessageUtils.send(p, m1.getStringList("SuperSoco.Ativado"));
                 API.removeItem(p);
             }
         }
     }
     @EventHandler
     void Bater(EntityDamageByEntityEvent e) {
         SConfig m1 = Main.get().getConfig("Mensagens");
         if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
             Player t = (Player) e.getDamager();
             if (SuperSoco.contains(t.getName())) {
                 double multiplier = 1 + m.getInt("Super_Soco.Porcentagem") / 100.0;
                 e.setDamage(e.getDamage() * multiplier);
                 MessageUtils.send(t, m1.getStringList("SuperSoco.Usado"));
                 SuperSoco.remove(t.getName());
             }
         }
     }
}