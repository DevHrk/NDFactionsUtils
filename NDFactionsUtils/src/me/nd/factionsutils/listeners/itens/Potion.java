package me.nd.factionsutils.listeners.itens;

import me.nd.factionsutils.command.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Potion extends Commands {

    public Potion() {
        super("potion");
    }
    
    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("nd.potion.give")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUso correto: /potion give <jogador> <speed|strength|combo>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        String type = args[1].toLowerCase();
        ItemStack potion;
        PotionMeta meta;

        switch (type) {
            case "speed":
                potion = new ItemStack(Material.POTION, 1, (short) 8230); // Velocidade II
                meta = (PotionMeta) potion.getItemMeta();
                meta.setDisplayName("§b§lPoção de Velocidade II");
                meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 300, 1), true);
                meta.setLore(java.util.Arrays.asList(
                    "§7Efeito: §bVelocidade II",
                    "§7Duração: §f5 minutos"
                ));
                break;

            case "strength":
                potion = new ItemStack(Material.POTION, 1, (short) 8193); // Força II
                meta = (PotionMeta) potion.getItemMeta();
                meta.setDisplayName("§c§lPoção de Força II");
                meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 300, 1), true);
                meta.setLore(java.util.Arrays.asList(
                    "§7Efeito: §cForça II",
                    "§7Duração: §f5 minutos"
                ));
                break;

            case "combo":
                potion = new ItemStack(Material.POTION, 1, (short) 8268); // Personalizado
                meta = (PotionMeta) potion.getItemMeta();
                meta.setDisplayName("§d§lPoção de Velocidade II + Força II");
                meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 300, 1), true);
                meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 300, 1), true);
                meta.setLore(java.util.Arrays.asList(
                    "§7Efeitos:",
                    "§b· Velocidade II",
                    "§c· Força II",
                    "§7Duração: §f5 minutos"
                ));
                break;

            default:
                sender.sendMessage("§cTipo de poção inválido. Use: speed, strength ou combo.");
                return;
        }

        potion.setItemMeta(meta);
        target.getInventory().addItem(potion);
        sender.sendMessage("§aPoção enviada para " + target.getName() + ".");
    }



}
