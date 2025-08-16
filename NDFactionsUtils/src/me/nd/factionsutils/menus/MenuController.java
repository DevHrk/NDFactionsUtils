package me.nd.factionsutils.menus;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.command.Kit;

public class MenuController {
    private final Set<Menu> menus;
    private final Main plugin;
    private final MenuAdapter adapter = new MenuAdapter(new Kit());

    public MenuController(Main plugin) {
        this.plugin = plugin;
        this.menus = new HashSet<>();
    }

    public Set<Menu> getMenus() {
        return this.menus;
    }

    public void insert(Menu occurrence) {
        this.menus.add(occurrence);
    }

    public Menu search(String name) {
        return this.menus.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Menu searchByTitle(String title) {
        return this.menus.stream().filter(kit -> kit.getInventoryTitle().equalsIgnoreCase(title)).findFirst().orElse(null);
    }

    public void loadMenus() {
        File[] files = plugin.getMenusDirectory().listFiles();
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Nenhum arquivo de menu encontrado na pasta Menus!");
            return;
        }

        for (File file : files) {
            if (file != null && file.getName().endsWith(".yml")) {
                plugin.getLogger().info("Carregando menu: " + file.getName());
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    Menu menu = this.adapter.read(config, null);
                    if (menu != null) {
                        insert(menu);
                        plugin.getLogger().info("Menu carregado com sucesso: " + menu.getName());
                    } else {
                        plugin.getLogger().warning("Menu inválido ou null no arquivo: " + file.getName());
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Erro ao carregar menu de " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}