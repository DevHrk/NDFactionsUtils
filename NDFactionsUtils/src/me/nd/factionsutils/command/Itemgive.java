package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.manager.EspecialManager;
import me.nd.factionsutils.manager.especial.Especial;
import me.nd.factionsutils.plugin.SConfig;
import me.nd.factionsutils.utils.Helper;

public class Itemgive extends Commands {

	public Itemgive() {
        super("item");
    }
	static FileConfiguration m = Main.get().getConfig(); 
	public void perform(CommandSender s, String lb, String[] args) {
		SConfig m1 = Main.get().getConfig("Mensagens");
        if (!s.hasPermission(m.getString("Permissões.giveitem"))) {
            s.sendMessage(m1.getString("Mensagens.SemPerm").replace("&", "§"));
            return;
        } switch (args.length) {
            case 3: {
                String targetName = args[0];
                String itemNameOrId = args[1];
                String preAmount = args[2];
                if (!Helper.isInteger(preAmount)) {
                    s.sendMessage(m1.getString("Mensagens.Quantidade").replace("&", "§"));
                    return;
                }
                Integer amount = Integer.parseInt(preAmount);
                Especial item = null;
                if (Helper.isInteger(itemNameOrId)) {
                    Integer id = Integer.parseInt(itemNameOrId);
                    item = EspecialManager.getItem(id);
                } else {
                    item = EspecialManager.getItem(itemNameOrId);
                }
                if (item == null) {
                    s.sendMessage(m1.getString("Mensagens.NaoExiste").replace("&", "§"));
                    return;
                }
                ItemStack item1 = item.getItem();
                Player player = Bukkit.getPlayerExact((String)targetName);
                if (player == null) {
                    s.sendMessage(m1.getString("Mensagens.Jogador").replace("&", "§"));
                    return;
                }
                item1.setAmount(amount.intValue());
                ItemStack itemstack = item1;
                player.getInventory().addItem(new ItemStack[]{itemstack});
                s.sendMessage(m1.getString("Mensagens.Recebeu").replace("&", "§").replace("{player}", player.getName()).replace("{quantia}", String.valueOf(amount)).replace("{item}", item.getName()));
                return;
            }
        }
        s.sendMessage(m1.getString("Comando-Errado.Item").replace("&", "§").replace("{itens}",this.getItemList()));
        return;
    }

    @SuppressWarnings("unused")
	private String getItemList() {
        StringBuilder builder = new StringBuilder();
        EspecialManager.getItems().forEach(item -> { StringBuilder stringBuilder2 = builder.append(String.valueOf(item.getName()) + ", "); });
        return builder.toString().substring(0, builder.length() - 2);
    }
}
