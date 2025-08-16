package me.nd.factionsutils.listeners;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.command.Kit;

public class KitInventoryListener implements Listener {

    private final Kit kitPlugin;

    public KitInventoryListener(Kit kitPlugin) {
        this.kitPlugin = kitPlugin;
        // Registrar o listener no plugin
        Bukkit.getPluginManager().registerEvents(this, JavaPlugin.getPlugin(Main.class));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("Visualizar Kit: ")) {
            // Cancelar qualquer interação no inventário de visualização
            event.setCancelled(true);
        } else if (title.startsWith("Editar Kit: ")) {
            // Permitir interação no inventário de edição, mas não cancelar
            // As alterações serão salvas ao fechar o inventário
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.startsWith("Editar Kit: ")) {
            Player jogador = (Player) event.getPlayer();
            String nomeKit = kitPlugin.editandoKits.get(jogador.getUniqueId());
            if (nomeKit != null) {
                // Salvar o conteúdo do inventário no arquivo do kit
                File arquivoKit = new File(kitPlugin.pastaKits, nomeKit + ".yml");
                if (arquivoKit.exists()) {
                    try {
                        YamlConfiguration configKit = YamlConfiguration.loadConfiguration(arquivoKit);
                        configKit.set("itens", event.getInventory().getContents());
                        configKit.save(arquivoKit);
                        jogador.sendMessage("§aKit " + nomeKit + " atualizado com sucesso.");
                    } catch (IOException e) {
                        jogador.sendMessage("§cErro ao salvar o kit.");
                        e.printStackTrace();
                    }
                }
                // Remover o jogador da lista de edição
                kitPlugin.editandoKits.remove(jogador.getUniqueId());
            }
        }
    }
}
