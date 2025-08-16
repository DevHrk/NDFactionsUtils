package me.nd.factionsutils;

import org.bukkit.plugin.java.*;

import com.creeperevents.oggehej.obsidianbreaker.StorageHandler;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.nd.factionsutils.command.ArmorsBonus;
import me.nd.factionsutils.command.Commands;
import me.nd.factionsutils.command.CriadorItem;
import me.nd.factionsutils.dados.DateManager;
import me.nd.factionsutils.dados.GeradorDAO;
import me.nd.factionsutils.dados.SQlite;
import me.nd.factionsutils.factions.RecipeItem;
import me.nd.factionsutils.gen.GeradorAPI;
import me.nd.factionsutils.holoboard.Holoboard;
import me.nd.factionsutils.hologram.*;
import me.nd.factionsutils.listeners.Kill;
import me.nd.factionsutils.listeners.Listenners;
import me.nd.factionsutils.listeners.itens.GeradorListener2;
import me.nd.factionsutils.listeners.tnt.TntListenner;
import me.nd.factionsutils.manager.EspecialManager;
import me.nd.factionsutils.manager.InventoryManager;
import me.nd.factionsutils.manager.SelectedItems;
import me.nd.factionsutils.menus.MenuController;
import me.nd.factionsutils.plugin.SConfig;
import me.nd.factionsutils.plugin.SWriter;
import me.nd.factionsutils.reflection.Reflection;
import me.nd.factionsutils.utils.FileUtils;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Main extends JavaPlugin
{
	
	public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§b[NDFactionUtils] Plugin iniciado");
        saveDefaultConfig();
        Holoboard.clear();
    	setupSQL();
        GeradorDAO.loadaAllGeradores();
        Bukkit.getScheduler().runTaskLater(this, Holoboard::setupLeaderboards, 5);
    	Commands.setupCommands();
    	Listenners.setupListeners();
		utf();
		fileUtils = new FileUtils(this);
		setupEconomy();
        new EspecialManager();
        new RecipeItem();
        new Kill(this);
        registerObjects();
        LOGGER_ACCESSOR.set(this, new SLogger(this));
		HologramLibrary.setupHolograms(this);
		NMS.setupNMS();
		// Bukkit.getScheduler().runTaskLater(Main.get(), Menu::setupMenu, 10L);
		new GeradorListener2().runTaskTimer(this, 20L, getConfig().getInt("Gerador.settings.DelayGerador") * 60 * 20L);
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> { CriadorItem.loadItems(); }, 5);
        Kit();
	    new me.nd.factionsutils.listeners.tnt.Runnable().runTaskTimer((Plugin)this, 0L, 1L);
	    this.getServer().getPluginManager().registerEvents((Listener)new TntListenner(), (Plugin)this);
	}
	
    public void onDisable() {
    	Bukkit.getConsoleSender().sendMessage("§e[NDFactionUtils] Plugin Desligado");
    	
    	SQlite.closeConnection();
    	
    	Holoboard.clear(); //remove qualquer holograma que esteja pelo mapa
    	CriadorItem.saveItems();
    	// Salva os itens do plugin de forma síncrona
        for (Player player : Bukkit.getOnlinePlayers()) {
        InventoryManager.saveInventory(player, new SelectedItems(EspecialManager.getItems()));
        }
    }
     
    public void Kit() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File kitsFolder = new File(getDataFolder(), "Kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdir();
        }
        
    }
    
    private void setupSQL() {
        DateManager.createFolder("database");
        sqlite = new SQlite();
        gson = new GsonBuilder().setPrettyPrinting().create();
        geradorAPI = new GeradorAPI();
        
    }
    
    public void utf() {
        try {
            File file = new File(getDataFolder() + File.separator, "config.yml");
            String allText = Resources.toString(file.toURI().toURL(), Charset.forName("UTF-8"));
            getConfig().loadFromString(allText);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return economy != null;
    }
    
    private void registerObjects() {
        reflection = new Reflection(this);
    }
    
	public static Main get() {
        return (Main)JavaPlugin.getPlugin((Class)Main.class);
    }
    
    public Gson getGson() {
      return this.gson;
    }
    
    public File getMenusDirectory() {
        return menusDirectory;
    }

    public MenuController getController() {
        return controller;
    }
	 
    public SQlite getSqlite() {
      return this.sqlite;
    }
    
    public GeradorAPI getGeradorAPI() {
      return this.geradorAPI;
    }
    
    public Reflection getReflection() {
      return this.reflection;
    }
    
	public FileUtils getFileUtils() {
		return this.fileUtils;
	}
    
    public SWriter getWriter(File file) {
        return getWriter(file, "");
    }
      
    public SWriter getWriter(File file, String header) {
        return new SWriter((SLogger) getLogger(), file, header);
    }
    
    public SConfig getConfig(String name) {
        return getConfig("", name);
    }
    
    public SConfig getConfig(String path, String name) {
        return SConfig.getConfig(this, "plugins/" + getName() + "/" + path, name);
    }
    
    public Economy getEconomy() {
        return economy;
    }
    

    public double getBuyDiscount(Player player) {
        return ArmorsBonus.getBuyDiscount(player);
    }

    public double getSellBonus(Player player) {
        return ArmorsBonus.getSellBonus(player);
    }
    
    public String getZeusEvent() {
        return zeusEvent;
    }

    public void setZeusEvent(String event) {
        this.zeusEvent = event;
    }

    public int getDefenseReduction() {
        return defenseReduction;
    }

    public void setDefenseReduction(int reduction) {
        this.defenseReduction = reduction;
    }

    public StorageHandler getBreakerStorage() {
        return breakerStorage;
    }
    
    private StorageHandler breakerStorage;
    private int defenseReduction = 0;
    private String zeusEvent = "none";
	public static Economy economy;
    public static File menusDirectory;
    public static MenuController controller;
    private Gson gson;
    private SQlite sqlite;
    private GeradorAPI geradorAPI;
    private Reflection reflection;
    private FileUtils fileUtils;
    private static final FieldAccessor<PluginLogger> LOGGER_ACCESSOR = Accessors.getField(JavaPlugin.class, "logger", PluginLogger.class);
}
