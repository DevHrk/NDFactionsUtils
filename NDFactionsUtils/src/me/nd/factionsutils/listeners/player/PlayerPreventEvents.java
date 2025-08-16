package me.nd.factionsutils.listeners.player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import me.nd.factions.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.command.CriadorItem;
import me.nd.factionsutils.listeners.itens.*;
import me.nd.factionsutils.manager.*;
import me.nd.factionsutils.manager.especial.Especial;

public class PlayerPreventEvents implements Listener {
	
	private static final List<Especial> items = EspecialManager.getItems();
	private static final SelectedItems selectedItems = new SelectedItems(items);
	
	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String playerName = p.getName();
        
        InventoryManager.saveInventory(p, selectedItems);
        List<Collection<? extends Object>> listeners = Arrays.asList(
            AntiTrapListener.playersInArea,
            PulsoListener.pulsomagnetico,
            InversorListener.Inversor,
            CaldeiraoListener.CaldeiraoArea,
            IceWarListener.congelado,
            CaldeiraoListener.forca,
            CataclistListener.concluido,
            CataclistListener.naoconcluido,
            GosmaListener.frozen,
            InversorListener.inversortemporal,
            API.playing,
            GodChestPlateListener.godchest,
            SlimeBlockJumpListener.dano,
            SuperSocoListener.SuperSoco,
            RelogioDoTempoListener.relogiodotempo
        );

        @SuppressWarnings("serial")
		Map<Player, Location> locationListeners = new HashMap<Player, Location>() {{
            putAll(MaquinaDoTempoListener.maquinadotempoLocations);
            putAll(API.saves);
            putAll(RelogioDoTempoListener.relogiodotempoLocations);
        }};

        for (Collection<? extends Object> listener : listeners) {
            if (listener.contains(playerName)) {
                listener.remove(playerName);
            }
        }

        if (locationListeners.containsKey(p)) {
            locationListeners.remove(p);
        }

        if (OlhoDeDeusListener.olhos.contains(playerName) && p.getGameMode().equals(GameMode.SPECTATOR)) {
            p.setGameMode(GameMode.SURVIVAL);
            OlhoDeDeusListener.olhos.remove(playerName);
        }
        
        if (TntRadListener.canhaoh.containsKey(p.getName())) {
        	TntRadListener.canhaoh.remove(p.getName());
	        p.removePotionEffect(PotionEffectType.SLOW);
	        p.removePotionEffect(PotionEffectType.CONFUSION);
	      } 
    }
	private final Map<UUID, Long> lastSave = new HashMap<>();

	@EventHandler
	private void onInventoryChange(InventoryClickEvent event) {
	    if (!(event.getWhoClicked() instanceof Player)) return;

	    Player player = (Player) event.getWhoClicked();

	    // Aguarda 2 segundos desde o último save para evitar excesso
	    long now = System.currentTimeMillis();
	    long last = lastSave.getOrDefault(player.getUniqueId(), 0L);
	    if (now - last < 2000) return;

	    lastSave.put(player.getUniqueId(), now);

	    Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
	        InventoryManager.saveInventory(player, selectedItems);
	    });
	}

    
    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
       Player p = (Player)e.getEntity();
       String playerName = p.getName();
       
       InventoryManager.saveInventory(p, selectedItems);
       List<Collection<? extends Object>> listeners = Arrays.asList(
           AntiTrapListener.playersInArea,
           PulsoListener.pulsomagnetico,
           SlimeBlockJumpListener.dano,
           CaldeiraoListener.CaldeiraoArea,
           InversorListener.Inversor,
           IceWarListener.congelado,
           CaldeiraoListener.forca,
           CataclistListener.concluido,
           CataclistListener.naoconcluido,
           GosmaListener.frozen,
           API.playing,
           GodChestPlateListener.godchest,
           SuperSocoListener.SuperSoco,
           InversorListener.inversortemporal,
           RelogioDoTempoListener.relogiodotempo
       );

       @SuppressWarnings("serial")
	   Map<Player, Location> locationListeners = new HashMap<Player, Location>() {{
           putAll(MaquinaDoTempoListener.maquinadotempoLocations);
           putAll(RelogioDoTempoListener.relogiodotempoLocations);
           putAll(API.saves);
       }};

       for (Collection<? extends Object> listener : listeners) {
           if (listener.contains(playerName)) {
               listener.remove(playerName);
           }
       }

       if (locationListeners.containsKey(p)) {
           locationListeners.remove(p);
       }

       if (OlhoDeDeusListener.olhos.contains(playerName) && p.getGameMode().equals(GameMode.SPECTATOR)) {
           p.setGameMode(GameMode.SURVIVAL);
           OlhoDeDeusListener.olhos.remove(playerName);
       }
       
       if (TntRadListener.canhaoh.containsKey(p.getName())) {
    	   TntRadListener.canhaoh.remove(p.getName());
	        p.removePotionEffect(PotionEffectType.SLOW);
	        p.removePotionEffect(PotionEffectType.CONFUSION);
	      } 
   }
    
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
       Player p = e.getPlayer();
       String playerName = p.getName();
       
       CriadorItem.loadItems();
       InventoryManager.loadInventory(p, selectedItems); //Da load nos itens ao reiniciar o servidor
       List<Collection<? extends Object>> listeners = Arrays.asList(
           AntiTrapListener.playersInArea,
           PulsoListener.pulsomagnetico,
           SlimeBlockJumpListener.dano,
           CaldeiraoListener.CaldeiraoArea,
           InversorListener.Inversor,
           IceWarListener.congelado,
           CaldeiraoListener.forca,
           CataclistListener.concluido,
           CataclistListener.naoconcluido,
           GosmaListener.frozen,
           SuperSocoListener.SuperSoco,
           API.playing,
           GodChestPlateListener.godchest,
           InversorListener.inversortemporal,
           RelogioDoTempoListener.relogiodotempo
       );

       @SuppressWarnings("serial")
	   Map<Player, Location> locationListeners = new HashMap<Player, Location>() {{
           putAll(MaquinaDoTempoListener.maquinadotempoLocations);
           putAll(RelogioDoTempoListener.relogiodotempoLocations);
           putAll(API.saves);
       }};

       for (Collection<? extends Object> listener : listeners) {
           if (listener.contains(playerName)) {
               listener.remove(playerName);
           }
       }

       if (locationListeners.containsKey(p)) {
           locationListeners.remove(p);
       }
      
       if (OlhoDeDeusListener.olhos.contains(playerName) && p.getGameMode().equals(GameMode.SPECTATOR)) {
           p.setGameMode(GameMode.SURVIVAL);
           OlhoDeDeusListener.olhos.remove(playerName);
       }
       if (TntRadListener.canhaoh.containsKey(p.getName())) {
    	   TntRadListener.canhaoh.remove(p.getName());
	        p.removePotionEffect(PotionEffectType.SLOW);
	        p.removePotionEffect(PotionEffectType.CONFUSION);
	      } 
   }

}
