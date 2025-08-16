package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nd.factionsutils.dados.SQlite;
import me.nd.factionutils.cash.CashManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Cash extends Commands {
    private static final Connection connection = SQlite.getConnection();

    public Cash() {
        super("cash");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            // /cash - Mostra o próprio saldo
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cEste comando é apenas para jogadores!");
                return;
            }
            Player player = (Player) sender;
            long cash = CashManager.getCash(player);
            sender.sendMessage("§eSaldo: §6" + cash);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "enviar":
                handleSendCash(sender, args);
                break;
            case "top":
                handleTopCash(sender);
                break;
            case "ajuda":
                handleHelp(sender);
                break;
            case "add":
                handleAddCash(sender, args);
                break;
            case "set":
                handleSetCash(sender, args);
                break;
            case "remove":
                handleRemoveCash(sender, args);
                break;
            case "reset":
                handleResetCash(sender, args);
                break;
            case "check":
                handleCheck(sender);
                break;
            default:
                // /cash <jogador> - Mostra o saldo de outro jogador
                String targetName = args[0];
                String targetUUID = getUUIDFromName(targetName);
                if (targetUUID == null) {
                    sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
                    return;
                }
                long cash = CashManager.getCash(targetUUID);
                sender.sendMessage("§aSaldo de cash de " + targetName + ": §6" + cash);
        }
    }

    private void handleSendCash(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.pay")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando é apenas para jogadores!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /cash enviar <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        if (targetUUID.equals(((Player) sender).getUniqueId().toString())) {
            sender.sendMessage("§cVocê não pode enviar cash para si mesmo!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cA quantia deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        Player player = (Player) sender;
        long playerCash = CashManager.getCash(player);
        if (playerCash < amount) {
            sender.sendMessage("§cVocê não tem cash suficiente! Seu saldo: §6" + playerCash);
            return;
        }
        if (CashManager.removeCash(player, amount) && CashManager.addCash(targetUUID, amount)) {
            sender.sendMessage("§aVocê enviou §6" + amount + " cash §apara " + targetName + "!");
            Player target = Bukkit.getPlayer(UUID.fromString(targetUUID));
            if (target != null && target.isOnline()) {
                target.sendMessage("§aVocê recebeu §6" + amount + " cash §ade " + player.getName() + "!");
            }
        } else {
            sender.sendMessage("§cErro ao processar a transação!");
        }
    }

    private void handleTopCash(CommandSender sender) {
        if (!sender.hasPermission("nd.command.top")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        sender.sendMessage("§8=== §eTop 10 Jogadores com Mais Cash §8===");
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid, cash FROM player_cash ORDER BY cash DESC LIMIT 10")) {
            ResultSet rs = ps.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                long cash = rs.getLong("cash");
                String name = getNameFromUUID(uuid);
                sender.sendMessage("§f" + rank + "º §f" + (name != null ? name : uuid) + ": §e" + cash + " cash");
                rank++;
            }
        } catch (SQLException e) {
            sender.sendMessage("§cErro ao consultar o ranking de cash!");
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao consultar top cash:");
            e.printStackTrace();
        }
    }

    private void handleHelp(CommandSender sender) {
        if (!sender.hasPermission("nd.command.help")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        sender.sendMessage("§a=== Comandos do Sistema de Cash ===");
        sender.sendMessage("§6/cash §f- Veja seu saldo de cash.");
        sender.sendMessage("§6/cash <jogador> §f- Veja o saldo de outro jogador.");
        if (sender.hasPermission("nd.command.pay")) {
            sender.sendMessage("§6/cash enviar <jogador> <quantia> §f- Envie cash para outro jogador.");
        }
        if (sender.hasPermission("nd.command.top")) {
            sender.sendMessage("§6/cash top §f- Veja os 10 jogadores com mais cash.");
        }
        if (sender.hasPermission("nd.command.add")) {
            sender.sendMessage("§6/cash add <jogador> <quantia> §f- Adicione cash a um jogador.");
        }
        if (sender.hasPermission("nd.command.set")) {
            sender.sendMessage("§6/cash set <jogador> <quantia> §f- Defina o saldo de um jogador.");
        }
        if (sender.hasPermission("nd.command.remove")) {
            sender.sendMessage("§6/cash remove <jogador> <quantia> §f- Remova cash de um jogador.");
        }
        if (sender.hasPermission("nd.command.reset")) {
            sender.sendMessage("§6/cash reset <jogador> §f- Zere o saldo de um jogador.");
        }
        if (sender.hasPermission("nd.command.check")) {
            sender.sendMessage("§6/cash check §f- Veja informações sobre o sistema de cheques.");
        }
    }

    private void handleAddCash(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.add")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /cash add <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cA quantia deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        if (CashManager.addCash(targetUUID, amount)) {
            sender.sendMessage("§aAdicionado §6" + amount + " cash §apara " + targetName + "!");
        } else {
            sender.sendMessage("§cErro ao adicionar cash!");
        }
    }

    private void handleSetCash(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.set")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /cash set <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount < 0) {
                sender.sendMessage("§cA quantia não pode ser negativa!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        if (CashManager.setCash(targetUUID, amount)) {
            sender.sendMessage("§aSaldo de " + targetName + " definido para §6" + amount + " cash!");
        } else {
            sender.sendMessage("§cErro ao definir o cash!");
        }
    }

    private void handleRemoveCash(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.remove")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("§cUso: /cash remove <jogador> <quantia>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage("§cA quantia deve ser um número positivo!");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantia deve ser um número válido!");
            return;
        }
        if (CashManager.removeCash(targetUUID, amount)) {
            sender.sendMessage("§aRemovido §6" + amount + " cash §ade " + targetName + "!");
        } else {
            sender.sendMessage("§cErro ao remover cash!");
        }
    }

    private void handleResetCash(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nd.command.reset")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        if (args.length != 2) {
            sender.sendMessage("§cUso: /cash reset <jogador>");
            return;
        }
        String targetName = args[1];
        String targetUUID = getUUIDFromName(targetName);
        if (targetUUID == null) {
            sender.sendMessage("§cJogador '" + targetName + "' não encontrado!");
            return;
        }
        if (CashManager.setCash(targetUUID, 0)) {
            sender.sendMessage("§aSaldo de " + targetName + " zerado!");
        } else {
            sender.sendMessage("§cErro ao zerar o saldo!");
        }
    }

    private void handleCheck(CommandSender sender) {
        if (!sender.hasPermission("nd.command.check")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return;
        }
        sender.sendMessage("§a=== Sistema de Cheques ===");
        sender.sendMessage("§fO sistema de cheques ainda não está implementado.");
        sender.sendMessage("§fUse '/cheque' para criar e gerenciar cheques (em desenvolvimento).");
        sender.sendMessage("§fEntre em contato com a administração para mais informações.");
    }

    // Método auxiliar para obter UUID a partir do nome
    private String getUUIDFromName(String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target != null && target.isOnline()) {
            return target.getUniqueId().toString();
        }
        // Consulta offline (assumindo que o servidor armazena UUIDs em algum lugar)
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid FROM player_cash WHERE uuid IN (SELECT uuid FROM player_vaults)")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String playerName = getNameFromUUID(uuid);
                if (playerName != null && playerName.equalsIgnoreCase(name)) {
                    return uuid;
                }
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao consultar UUID para " + name + ":");
            e.printStackTrace();
        }
        return null;
    }

    // Método auxiliar para obter nome a partir do UUID
    private String getNameFromUUID(String uuid) {
        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
        if (player != null && player.isOnline()) {
            return player.getName();
        }
        // Tenta obter o nome offline (pode ser implementado com um sistema de cache ou API externa)
        return null; // Retorna null se não encontrar o nome
    }
}