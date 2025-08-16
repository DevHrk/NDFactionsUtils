package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class Broadcast extends Commands {

    public Broadcast() {
        super("bc");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        // Check if the sender is the console
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser executado pelo console!");
            return;
        }

        // Check if the sender has the required permission
        if (!sender.hasPermission("factionsutils.broadcast")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return;
        }

        // Check if any arguments (message) were provided
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Uso: /bc <mensagem>");
            return;
        }

        // Combine the arguments into a single message
        String message = String.join(" ", args);
        // Translate color codes (e.g., &a to green)
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Broadcast the message to all online players
        Bukkit.broadcastMessage(message);

        // Optional: Confirm to the console that the message was sent
        sender.sendMessage(ChatColor.GREEN + "Mensagem enviada: " + message);
    }
}