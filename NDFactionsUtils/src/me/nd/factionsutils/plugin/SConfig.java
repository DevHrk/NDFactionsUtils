package me.nd.factionsutils.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.nd.factionsutils.Main;

public class SConfig {

	private final Main plugin;
	private final File file;
	private YamlConfiguration config;
	private static final Map<String, SConfig> cache = new HashMap<>();

	public SConfig(Main plugin, String path, String name) {
		this.plugin = plugin;
		this.file = new File(path, name + ".yml");
		this.file.getParentFile().mkdirs();

		if (!file.exists()) {
			createDefaultConfig(name);
		}

		loadConfig();
	}

	private void createDefaultConfig(String name) {
		try (InputStream in = plugin.getResource(name + ".yml")) {
			if (in != null) {
				plugin.getFileUtils().copyFile(in, file);
			} else {
				file.createNewFile();
			}
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Error creating file \"" + file.getName() + "\": ", ex);
		}
	}

	private void loadConfig() {
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			config = YamlConfiguration.loadConfiguration(reader);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Error loading config \"" + file.getName() + "\": ", ex);
		}
	}

	public boolean createSection(String path) {
		config.createSection(path);
		return save();
	}

	public boolean set(String path, Object obj) {
		config.set(path, obj);
		return save();
	}

	public boolean contains(String path) {
		return config.contains(path);
	}

	public Object get(String path) {
		return config.get(path);
	}

	public int getInt(String path) {
		return config.getInt(path);
	}

	public int getInt(String path, int def) {
		return config.getInt(path, def);
	}

	public double getDouble(String path) {
		return config.getDouble(path);
	}

	public double getDouble(String path, double def) {
		return config.getDouble(path, def);
	}

	public String getString(String path) {
		return config.getString(path);
	}

	public boolean getBoolean(String path) {
		return config.getBoolean(path);
	}

	public boolean getBoolean(String path, boolean def) {
		return config.getBoolean(path, def);
	}

	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}

	public List<Integer> getIntegerList(String path) {
		return config.getIntegerList(path);
	}

	public Set<String> getKeys(boolean flag) {
		return config.getKeys(flag);
	}

	public ConfigurationSection getSection(String path) {
		return config.getConfigurationSection(path);
	}

	public void reload() {
		loadConfig();
	}

	public boolean save() {
		try {
			config.save(file);
			return true;
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Error saving config \"" + file.getName() + "\": ", ex);
			return false;
		}
	}

	public File getFile() {
		return file;
	}

	public YamlConfiguration getRawConfig() {
		return config;
	}

	public static SConfig getConfig(Main plugin, String path, String name) {
		return cache.computeIfAbsent(path + "/" + name, key -> new SConfig(plugin, path, name));
	}

	public boolean isList(String path) {
		return config.isList(path);
	}

	public boolean isSet(String path) {
		return config.isSet(path);
	}

	public String getString(String path, String def) {
	    return config.getString(path, def);
	}
}