package me.nd.factionsutils.menus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.menus.item.MItem;
import me.nd.factionsutils.menus.listener.MenuListener;

public class Menu {
    private final String name;
    private final String inventoryTitle;
    private final Inventory inventory;
    private final int size;
    private final boolean cancelClick;
    private final Map<Integer, MItem> items;
    private final String command;
    private final String permission;
    private final Main plugin;

    public Menu(String name, String inventoryTitle, int size, boolean cancelClick, String command, String permission) {
        this.name = name;
        this.inventoryTitle = ChatColor.translateAlternateColorCodes('&', inventoryTitle);
        this.size = size;
        this.cancelClick = cancelClick;
        this.command = command;
        this.permission = permission;
        this.items = new HashMap<>();
        this.plugin = Main.get();
        this.inventory = Bukkit.createInventory(null, size, this.inventoryTitle);
        this.registerCommand();
    }

    public void setItem(MItem item, int slot) {
        this.inventory.setItem(slot, item.getItem());
        this.items.put(slot, item);
    }

    public void openInventory(Player player) {
        player.openInventory(this.inventory);
    }

    public MItem getItemByStack(ItemStack itemStack) {
        for (Map.Entry<Integer, MItem> entry : this.items.entrySet()) {
            if (!entry.getValue().getItem().equals(itemStack)) continue;
            return entry.getValue();
        }
        return null;
    }

    private void registerCommand() {
        ((CraftServer)Bukkit.getServer()).getCommandMap().register(this.command, new Command(this.command) {
            @Override
            public boolean execute(CommandSender sender, String lb, String[] args) {
                if (!(sender instanceof Player)) {
                    return false;
                }
                Player player = (Player)sender;
                if (!Menu.this.permission.equalsIgnoreCase("") && !player.hasPermission(Menu.this.permission)) {
                    return false;
                }
                Menu.this.openInventory(player);
                return false;
            }
        });
    }

    public String getName() {
        return this.name;
    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public int getSize() {
        return this.size;
    }

    public boolean isCancelClick() {
        return this.cancelClick;
    }

    public Map<Integer, MItem> getItems() {
        return this.items;
    }

    public String getCommand() {
        return this.command;
    }

    public String getPermission() {
        return this.permission;
    }

    public Main getPlugin() {
        return this.plugin;
    }

    public static void setupMenu() {
        if (!Main.get().setupEconomy()) {
            Bukkit.getPluginManager().disablePlugin(Main.get());
            return;
        }
        if (!Main.get().getDataFolder().exists())
            Main.get().getDataFolder().mkdir();
        Main.menusDirectory = new File("plugins" + File.separator + "NDFactionUtils" + File.separator + "Menus");
        if (!Main.menusDirectory.exists())
            Main.menusDirectory.mkdir();
        Main.controller = new MenuController(Main.get());
        Main.controller.loadMenus();
        Bukkit.getPluginManager().registerEvents(new MenuListener(), Main.get());
    }

    public MItem getItem(int slot) {
        return this.items.get(slot);
    }
}