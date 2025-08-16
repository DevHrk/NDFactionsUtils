package me.nd.factionsutils.listeners.itens;

import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.addons.SobAtaque;
import me.nd.factions.api.ActionBar;
import me.nd.factions.api.Config;
import me.nd.factions.enums.Cargo;
import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.Terra;
import me.nd.factions.utils.ColorData;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.AttackItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class RemoverAttackListener implements Listener {

    public static HashMap<String, Long> cooldowns = new HashMap<>();
    public static ArrayList<String> removerattack = new ArrayList<>();

    @EventHandler
    void aoClicar(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();
        ItemStack item = p.getInventory().getItemInHand();
        
        if (item == null || !item.isSimilar(AttackItem.REMOVEDORATTACK)) {
            return;
        }

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        e.setCancelled(true); // Prevent default block placement

        NDPlayer p2 = DataManager.players.get(p.getName());
        if (p2 == null) {
            p.sendMessage("§cErro: Jogador não encontrado no banco de dados!");
            return;
        }

        SConfig m1 = Main.get().getConfig("Mensagens");
        FileConfiguration m = Main.get().getConfig();
        Terra terra = new Terra(p.getWorld(), p.getLocation().getChunk().getX(), p.getLocation().getChunk().getZ());
        NDFaction facAt = terra.getFaction();

        // Check if player has a faction
        if (!p2.hasFaction()) {
            MessageUtils.send(p, m1.getStringList("AttackRemove.sem-faction"));
            return;
        }

        NDFaction playerFaction = p2.getFaction();

        // Check if the chunk belongs to player's faction
        if (facAt == null || !facAt.equals(playerFaction)) {
            MessageUtils.send(p, m1.getStringList("AttackRemove.nao-proprio-territorio"));
            return;
        }

        // Check if the chunk is in a protected zone
        if (Utils.isZonaProtegida(p.getLocation())) {
            MessageUtils.send(p, m1.getStringList("Mensagens.ZonaProtegida"));
            return;
        }

        // Check if the chunk is in a free zone
        if (Utils.isZonaLivre(p.getLocation())) {
            MessageUtils.send(p, m1.getStringList("Mensagens.TerritorioLivre"));
            return;
        }

        // Check if faction is under attack
        if (!SobAtaque.cooldown.contains(playerFaction)) {
            MessageUtils.send(p, m1.getStringList("AttackRemove.combate"));
            return;
        }

        // Check player role
        if (p2.getCargo() != Cargo.Lider && p2.getCargo() != Cargo.Capitão) {
            MessageUtils.send(p, m1.getStringList("AttackRemove.sem-perm"));
            return;
        }

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(p.getName())) {
            long cooldownTime = cooldowns.get(p.getName());
            long cooldownDuration = m.getInt("Remover-Attack.cooldown", 60) * 60000L;
            long timeLeft = cooldownTime + cooldownDuration - currentTime;
            
            if (timeLeft > 0) {
                String tempo = API.formatTime(timeLeft);
                MessageUtils.send(p, m1.getString("AttackRemove.cooldown").replace("{tempo}", tempo));
                return;
            }
            cooldowns.remove(p.getName());
        }

        // Remove attack status
        SobAtaque.cooldown.remove(playerFaction);
        SobAtaque.attackStartTimes.remove(playerFaction);
        SobAtaque.lastExplosionTimes.remove(playerFaction);
        SobAtaque.fastUpdatePlayers.removeAll(playerFaction.getAllOnline());

        // Consume item
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            p.getInventory().setItemInHand(null);
        }

        // Add player to control list
        removerattack.add(p.getName());

        // Send success message
        MessageUtils.send(p, m1.getStringList("AttackRemove.ItemUtilizado"));

        // Notify nearby players
        int radius = m.getInt("Remover-Attack.RaioMensagem", 50);
        for (Entity entity : p.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                NDPlayer targetND = DataManager.players.get(target.getName());
                if (targetND != null && (!targetND.hasFaction() || !targetND.getFaction().equals(playerFaction))) {
                    MessageUtils.send(target, m1.getStringList("AttackRemove.Utilizado"));
                }
            }
        }

        // Add player to cooldown
        cooldowns.put(p.getName(), currentTime);

        // Schedule attack reactivation
        int reactivationTime = m.getInt("Remover-Attack.tempo", 300);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!removerattack.contains(p.getName())) {
                    return;
                }

                removerattack.remove(p.getName());
                
                // Restart attack status
                long now = System.currentTimeMillis();
                SobAtaque.cooldown.add(playerFaction);
                SobAtaque.attackStartTimes.put(playerFaction, now);
                SobAtaque.lastExplosionTimes.put(playerFaction, now);
                SobAtaque.fastUpdatePlayers.addAll(playerFaction.getAllOnline());

                // Start action bar updates
                ColorData data = new ColorData(
                    Config.get("SobAtaque.Mensagem").toString().replace("&", "§"),
                    "§4",
                    "§c"
                );

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!SobAtaque.cooldown.contains(playerFaction)) {
                            cancel();
                            return;
                        }

                        long start = SobAtaque.attackStartTimes.getOrDefault(playerFaction, now);
                        long last = SobAtaque.lastExplosionTimes.getOrDefault(playerFaction, now);
                        long timeNow = System.currentTimeMillis();

                        if (timeNow - start >= SobAtaque.getMaxAttackDuration(playerFaction)) {
                            SobAtaque.encerrarAtaque(playerFaction, SobAtaque.getAttackingFaction(playerFaction), true);
                            cancel();
                            return;
                        }

                        if (timeNow - last >= SobAtaque.RESET_TIMER_ON_EXPLOSION) {
                            SobAtaque.encerrarAtaque(playerFaction, SobAtaque.getAttackingFaction(playerFaction), false);
                            cancel();
                            return;
                        }

                        data.next();
                        String msg = data.getMessage();
                        for (Player onlinePlayer : playerFaction.getAllOnline()) {
                            ActionBar.sendActionBarMessage(onlinePlayer, msg);
                        }
                    }
                }.runTaskTimerAsynchronously(Main.getPlugin(Main.class), 0L, 5L);
            }
        }.runTaskLater(Main.getPlugin(Main.class), 20L * reactivationTime);
    }
}