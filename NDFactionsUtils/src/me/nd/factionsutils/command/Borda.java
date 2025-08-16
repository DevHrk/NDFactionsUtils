package me.nd.factionsutils.command;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;

public class Borda extends Commands {

    public Borda() {
        super("bd");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        // Verifica se o sender é o console
        if (sender instanceof org.bukkit.entity.Player) {
            sender.sendMessage("§cEste comando só pode ser usado pelo console!");
            return;
        }

        // Verifica se há argumentos suficientes
        if (args.length < 2) {
            sender.sendMessage("§cUso: /bd <numero> <mundo> ou /bd dm <numero> <mundo>");
            return;
        }

        try {
            // Determina se é para diminuir
            boolean isDecrease = args[0].equalsIgnoreCase("dm");
            int argIndex = isDecrease ? 1 : 0;
            
            double sizeChange = Double.parseDouble(args[argIndex]);
            
            // Verifica se o número é positivo
            if (sizeChange <= 0) {
                sender.sendMessage("§cO número deve ser maior que 0!");
                return;
            }

            // Obtém o nome do mundo
            String worldName = args[argIndex + 1];
            World world = Bukkit.getWorld(worldName);
            
            // Verifica se o mundo existe
            if (world == null) {
                sender.sendMessage("§cMundo '" + worldName + "' não encontrado!");
                return;
            }

            WorldBorder border = world.getWorldBorder();
            double currentSize = border.getSize();
            
            if (isDecrease) {
                // Diminui a borda
                double newSize = Math.max(1.0, currentSize - sizeChange);
                border.setSize(newSize);
                Bukkit.broadcastMessage("§eBorda do §5The EnD§e diminuída em §f" + sizeChange + "§e blocos!");
            } else {
                // Aumenta a borda, respeitando o limite de 397
                double newSize = Math.min(397.0, currentSize + sizeChange);
                double actualChange = newSize - currentSize; // Calcula a mudança real
                if (actualChange > 0) { // Verifica se houve aumento
                    border.setSize(newSize);
                    Bukkit.broadcastMessage("§eBorda do §5The EnD§e aumentada em §f" + actualChange + "§e blocos!");
                } else {
                    sender.sendMessage("§cA borda já está no limite máximo de 397 blocos!");
                }
            }
            
        } catch (NumberFormatException e) {
            sender.sendMessage("§cPor favor, insira um número válido!");
        }
    }
}