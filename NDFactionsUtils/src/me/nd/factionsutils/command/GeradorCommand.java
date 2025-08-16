package me.nd.factionsutils.command;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.plugin.SConfig;
import me.nd.factionsutils.utils.Helper;

public class GeradorCommand extends Commands {

	public GeradorCommand() {
        super("givegerador");
    }
	static FileConfiguration m = Main.get().getConfig(); 
	  public void perform(CommandSender s, String lb, String[] args) {
		  SConfig m1 = Main.get().getConfig("Mensagens");
		  String permission = m.getString("Permissões.givegerador");

		  if (!s.hasPermission(permission) || args.length < 3) {
		      s.sendMessage(m1.getString("GiveGeradores.comando-errado").replace("&", "§"));
		      s.sendMessage(m1.getString("GiveGeradores.tipo").replace("&", "§").replace("{itens}", this.getGeneratorList()));
		      return;
		  }

		  String preAmount = args[2];
		  if (!Helper.isInteger(preAmount)) {
		      s.sendMessage(m1.getString("Mensagens.Quantidade").replace("&", "§"));
		      return;
		  }

		  Player player = Bukkit.getPlayer(args[0]);
		  if (player == null || !player.isOnline()) {
		      s.sendMessage(m1.getString("Mensagens.Jogador").replace("&", "§"));
		      return;
		  }

		  int quantidade = Integer.parseInt(preAmount);
		  ConfigurationSection geradores = m.getConfigurationSection("Geradores");

		  for (String item : geradores.getKeys(false)) {
		      if (args[1].equalsIgnoreCase(m.getString("Geradores." + item + ".item").replace("&", "§"))) {
		          ItemStack gerador = new ItemStack(m.getInt("Geradores." + item + ".Id"), quantidade);
		          ItemMeta meta = gerador.getItemMeta();

		          List<String> lore = m.getStringList("Geradores." + item + ".Lore").stream()
		                  .map(l -> l.replace("&", "§"))
		                  .collect(Collectors.toList());

		          meta.setDisplayName(m.getString("Geradores." + item + ".Nome").replace("&", "§"));
		          meta.setLore(lore);
		          gerador.setItemMeta(meta);
		          gerador.setDurability((short) m.getInt("Geradores." + item + ".Data"));

		          if (m.getBoolean("Geradores." + item + ".Glow")) {
		              meta.addEnchant(Enchantment.DURABILITY, 1, true);
		              meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		          }

		          if (m.getBoolean("Geradores." + item + ".Flags")) {
		              for (ItemFlag flag : ItemFlag.values()) {
		                  meta.addItemFlags(flag);
		              }
		          }

		          player.getInventory().addItem(gerador);
		          s.sendMessage(m1.getString("GiveGeradores.enviado").replace("&", "§").replace("{item}", item));
		          return;
		      }
		  }
	  }
	  private String getGeneratorList() {
		    StringBuilder builder = new StringBuilder();
		    ConfigurationSection geradores = m.getConfigurationSection("Geradores");
		    for (String item : geradores.getKeys(false)) {
		        builder.append(m.getString("Geradores." + item + ".item").replace("&", "§")).append(", ");
		    }
		    return builder.toString().substring(0, builder.length() - 2);
		}
}
