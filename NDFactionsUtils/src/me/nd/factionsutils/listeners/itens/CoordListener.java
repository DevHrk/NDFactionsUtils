package me.nd.factionsutils.listeners.itens;

import me.nd.factions.objetos.NDFaction;
import me.nd.factions.objetos.NDPlayer;
import me.nd.factions.objetos.Terra;
import me.nd.factionsutils.Main;
import me.nd.factionsutils.messages.MessageUtils;
import me.nd.factionsutils.plugin.SConfig;
import me.nd.factionsutils.utils.ItemBuilder;
import me.nd.factions.mysql.DataManager;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CoordListener implements Listener {
	
    private final Date now = new Date();
    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM");
    private final Random random = new Random();
    private final FileConfiguration m = Main.get().getConfig();

    @EventHandler
    void onPlayerDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        SConfig m1 = Main.get().getConfig("Mensagens");
        NDPlayer ndPlayer = DataManager.players.get(player.getName());
        if (ndPlayer == null) {
            return; // Jogador não registrado, ignora
        }

        NDFaction faction = ndPlayer.getFaction();
        if (faction == null) {
            return; // Jogador sem facção, ignora
        }

        Player killer = player.getKiller();
        List<Terra> allChunks = new ArrayList<>();
        allChunks.addAll(faction.getTerras());
        allChunks.addAll(faction.getTemporarios());

        if (allChunks.isEmpty()) {
            return; // Facção sem territórios, ignora
        }

        List<String> baseLoreList = m.getStringList("Coordenadas.Lore");
        if (percentChance(m.getInt("Coordenadas.Porcentagem"))) {
            // Seleciona um chunk aleatório
            Terra randomTerra = allChunks.get(random.nextInt(allChunks.size()));

            // Cria o lore personalizado
            ArrayList<String> lore = new ArrayList<>();
            for (String baseLore : baseLoreList) {
                String formattedLore = baseLore
                    .replace("&", "§")
                    .replace("{fac}", faction.getNome())
                    .replace("{data}", format.format(now))
                    .replace("{x}", String.valueOf(randomTerra.getX() * 16)) // Converte para coordenada do bloco
                    .replace("{z}", String.valueOf(randomTerra.getZ() * 16)); // Converte para coordenada do bloco
                lore.add(formattedLore);
            }

            // Cria e adiciona o item aos drops
            Material material = Material.getMaterial(m.getString("Coordenadas.Material", "PAPER"));
            if (material == null) {
                material = Material.PAPER; // Fallback para PAPER se o material for inválido
            }
            String itemName = m.getString("Coordenadas.Nome", "§eCoordenadas").replace("&", "§");
            e.getDrops().add(new ItemBuilder(material)
                .name(itemName)
                .lore(lore)
                .build());

            // Envia mensagem ao assassino, se for um jogador
            if (killer instanceof Player) {
                List<String> messages = m1.getStringList("Coordenadas.Matou").stream()
                    .map(msg -> msg.replace("&", "§"))
                    .collect(Collectors.toList());
                MessageUtils.send(killer, messages);
            }
        }
    }

    private boolean percentChance(double percent) {
        if (percent < 0.0 || percent > 100.0) {
            throw new IllegalArgumentException("Percentagem deve estar entre 0 e 100");
        }
        return random.nextDouble() * 100.0 <= percent;
    }
}