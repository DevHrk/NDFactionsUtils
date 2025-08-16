package me.nd.factionsutils.listeners.itens;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.nd.factions.mysql.DataManager;
import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.utils.Utils;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.api.API;
import me.nd.factionsutils.itens.AspectoItem;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;

public class AspectoListener implements Listener {
    private final FileConfiguration config = Main.get().getConfig();

    @EventHandler
    public void onClick2(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        SConfig messageConfig = Main.get().getConfig("Mensagens");

        // Verifica se é o item Aspecto Noturno
        if (!isAspectoNoturnoItem(player)) {
            return;
        }

        // Verifica se está em uma zona protegida
        if (!Utils.isZonaProtegida(player.getLocation())) {
            player.sendMessage(messageConfig.getString("Mensagens.ZonaProtegida").replace("&", "§"));
            e.setCancelled(true);
            return;
        }

        // Verifica se é clique direito
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);

            int radius = config.getInt("Aspecto_Noturno.Raio");
            List<Player> nearbyPlayers = getNearbyPlayers(player, radius);
            boolean playerFound = false;

            NDPlayer playerMPlayer = DataManager.players.get(player.getName());
            NDFaction playerFaction = playerMPlayer != null ? playerMPlayer.getFaction() : null;

            for (Player target : nearbyPlayers) {
                // Exclui o próprio jogador
                if (target.equals(player)) {
                    continue;
                }

                NDPlayer targetMPlayer = DataManager.players.get(target.getName());
                NDFaction targetFaction = targetMPlayer != null ? targetMPlayer.getFaction() : null;

                // Aplica efeitos se o alvo não for da mesma facção
                if (targetFaction == null || (playerFaction != null && !targetFaction.equals(playerFaction))) {
                    dispatchEffects(target);
                    sendMessage(target, "AspectoNoturno.Player-Acertado");
                    playerFound = true;
                }
            }

            if (playerFound) {
                removeItem(player);
                sendMessage(player, "AspectoNoturno.Funcionou");
            } else {
                sendMessage(player, "AspectoNoturno.Nao-Ha");
            }
        }
    }

    private boolean isAspectoNoturnoItem(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        return item != null && item.isSimilar(AspectoItem.ASPECTONOTURNO);
    }

    private List<Player> getNearbyPlayers(Player player, int radius) {
        return player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(player.getLocation()) <= radius)
                .collect(Collectors.toList());
    }

    private void dispatchEffects(Player target) {
        API.dispatchCommands(target, config.getStringList("Aspecto_Noturno.Efeitos"));
    }

    private void removeItem(Player player) {
        API.removeItem(player);
    }

    private void sendMessage(Player player, String messageKey) {
        SConfig messageConfig = Main.get().getConfig("Mensagens");
        MessageUtils.send(player, messageConfig.getString(messageKey).replace("&", "§"), 
                         messageConfig.getStringList(messageKey));
    }
}