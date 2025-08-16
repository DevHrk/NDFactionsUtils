package me.nd.factionsutils.dados;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.nd.factionsutils.Main;

import java.io.File;

public class DateManager {
    private static final Main main = Main.get();

    public static void createFolder(String folder) {
        try {
            File paste = new File(main.getDataFolder() + File.separator + folder);

            if (!paste.exists())
                paste.mkdirs();

        } catch (Throwable e) {
            Bukkit.getConsoleSender().sendMessage("§cNão foi possivel criar a pasta §6" + folder + "§c.");
            e.printStackTrace();
        }

    }

    public static void createFile(File file) {
        try {
            file.createNewFile();

        } catch (Throwable e) {
            Bukkit.getConsoleSender().sendMessage("§cNão foi possivel criar o arquivo §6" + file.getName() + "§c.");
            e.printStackTrace();
        }

    }

    public static File getFolder(String folder) {
        File arquive = new File(main.getDataFolder() + File.separator + folder);

        return arquive;
    }

    public static File getFile(String file, String folder) {
        File arquive = new File(main.getDataFolder() + File.separator + folder, file + ".yml");

        return arquive;
    }

    public static File getFile(String file) {
        File arquive = new File(main.getDataFolder() + File.separator + file + ".yml");

        return arquive;
    }

    public static FileConfiguration getConfiguration(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void deleteFile(File file) {
        file.delete();
    }

    public static void createConfig(String file) {
        if (!(new File(main.getDataFolder(), String.valueOf(file) + ".yml")).exists())
            main.saveResource(String.valueOf(file) + ".yml", false);
    }

    public static FileConfiguration getConfig(String file) {
        File archive = new File(main.getDataFolder() + File.separator + file + ".yml");
        return YamlConfiguration.loadConfiguration(archive);
    }

    public static void createConfig(String file, String folder) {
        String name = main.getDescription().getName();

        if (!new File("plugins/" + name + "/" + folder + "/" + file + ".yml").exists())
            main.saveResource(folder + "/" + file + ".yml", false);
    }

    public static FileConfiguration getConfig(String file, String folder) {
        File archive = new File(main.getDataFolder() + File.separator + folder, file + ".yml");
        return YamlConfiguration.loadConfiguration(archive);
    }
}
