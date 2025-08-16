package me.nd.factionsutils.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;
import me.nd.factionsutils.utils.Helper;

public class CriadorItem extends Commands {

    private static final Main main = Main.get();
    private static final FileConfiguration m = main.getConfig();
    public static final HashMap<ItemStack, List<String>> NEW_ITENS = new HashMap<>();
    public static final HashMap<String, ItemStack> NAME_ITENS = new HashMap<>();

    public CriadorItem() {
        super("giveitem");
    }

    public void perform(CommandSender s, String lb, String[] args) {
    	if (!(s instanceof Player)) return;

        Player p = (Player) s;
        SConfig m1 = main.getConfig("Mensagens");
        SConfig config = main.getConfig("CriadorItens");

        if (!p.hasPermission(m.getString("Permissões.givegerador")) || args.length < 3) {
            p.sendMessage(m1.getString("Criador-Itens.comando-errado").replace("&", "§").replace("{itens}", this.getCreatorList(config)));
            return;
        }

        String preAmount = args[2];
        if (!Helper.isInteger(preAmount)) {
            p.sendMessage(m1.getString("Mensagens.Quantidade").replace("&", "§"));
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        int quantidade = Integer.parseInt(preAmount);

        if (player == null || !player.isOnline()) {
            p.sendMessage(m1.getString("Mensagens.Jogador").replace("&", "§"));
            return;
        }

        for (String item : config.getSection("Criador-Itens").getKeys(false)) {
            if (!args[1].equalsIgnoreCase(config.getString("Criador-Itens." + item + ".item").replace("&", "§"))) continue;
            
            ItemStack gerador = createItem(config, item, quantidade);
            
            NEW_ITENS.put(gerador, config.getStringList("Criador-Itens." + item + ".Executa"));
            NAME_ITENS.put(item.toUpperCase(), gerador);
            p.getInventory().addItem(gerador);
            p.sendMessage(m1.getString("Criador-Itens.enviado").replace("&", "§").replace("{item}", item));
            return;
        }
    }
    private ItemStack createItem(SConfig config, String item, int quantidade) {
        ItemStack gerador = new ItemStack(config.getInt("Criador-Itens." + item + ".Id"), quantidade);
        ItemMeta geradormeta = gerador.getItemMeta();

        List<String> lore = config.getStringList("Criador-Itens." + item + ".Lore").stream().map(str -> str.replace("&", "§")).collect(Collectors.toList());
        geradormeta.setDisplayName(config.getString("Criador-Itens." + item + ".Nome").replace("&", "§"));
        geradormeta.setLore(lore);
        gerador.setItemMeta(geradormeta);
        gerador.setDurability((short) config.getInt("Criado-De-Itens." + item + ".Data"));

        if (config.getBoolean("Criador-Itens." + item + ".Glow")) {
            geradormeta.addEnchant(Enchantment.DURABILITY, 1, true);
            geradormeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (config.getBoolean("Criador-Itens." + item + ".Flags")) {
            for (ItemFlag flag : ItemFlag.values()) {
                geradormeta.addItemFlags(flag);
            }
        }

        return gerador;
    }
    
    private String getCreatorList(SConfig config) {
        StringBuilder builder = new StringBuilder();
        ConfigurationSection criadorItens = config.getSection("Criador-Itens");
        for (String item : criadorItens.getKeys(false)) {
            builder.append(config.getString("Criador-Itens." + item + ".item").replace("&", "§")).append(", ");
        }
        return builder.toString().substring(0, builder.length() - 2);
    }
    
    public static void saveItems() {
        File file = new File(main.getDataFolder(), "items.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (Map.Entry<ItemStack, List<String>> entry : NEW_ITENS.entrySet()) {
            String base64Item = itemToBase64(entry.getKey());
            config.set(base64Item, entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadItems() {
        File file = new File(main.getDataFolder(), "items.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            ItemStack item = itemFromBase64(key);
            List<String> commands = config.getStringList(key);
            NEW_ITENS.put(item, commands);
        }
    }

    private static String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Save every element in the list
            dataOutput.writeObject(item);

            // Serialize that array
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
