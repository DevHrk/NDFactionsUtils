package me.nd.factionutils.cash;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.nd.factionsutils.dados.SQlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CashManager {
    private static final Connection connection = SQlite.getConnection();

    // Adiciona cash ao jogador
    public static boolean addCash(Player player, long amount) {
        return addCash(player.getUniqueId().toString(), amount);
    }

    public static boolean addCash(String uuid, long amount) {
        if (amount < 0) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Tentativa de adicionar valor negativo de cash: " + amount);
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_cash (uuid, cash) VALUES (?, COALESCE((SELECT cash FROM player_cash WHERE uuid = ?), 0) + ?)")) {
            ps.setString(1, uuid);
            ps.setString(2, uuid);
            ps.setLong(3, amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao adicionar cash para UUID " + uuid + ":");
            e.printStackTrace();
            return false;
        }
    }

    // Remove cash do jogador
    public static boolean removeCash(Player player, long amount) {
        return removeCash(player.getUniqueId().toString(), amount);
    }

    public static boolean removeCash(String uuid, long amount) {
        if (amount < 0) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Tentativa de remover valor negativo de cash: " + amount);
            return false;
        }
        // Obtém o saldo atual
        long currentCash = getCash(uuid);
        if (currentCash < amount) {
            // Não permite que o saldo fique negativo
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_cash (uuid, cash) VALUES (?, ?)")) {
            ps.setString(1, uuid);
            ps.setLong(2, currentCash - amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao remover cash para UUID " + uuid + ":");
            e.printStackTrace();
            return false;
        }
    }

    // Define o cash do jogador
    public static boolean setCash(Player player, long amount) {
        return setCash(player.getUniqueId().toString(), amount);
    }

    public static boolean setCash(String uuid, long amount) {
        if (amount < 0) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Tentativa de definir valor negativo de cash: " + amount);
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_cash (uuid, cash) VALUES (?, ?)")) {
            ps.setString(1, uuid);
            ps.setLong(2, amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao definir cash para UUID " + uuid + ":");
            e.printStackTrace();
            return false;
        }
    }

    // Obtém o cash do jogador
    public static long getCash(Player player) {
        return getCash(player.getUniqueId().toString());
    }

    public static long getCash(String uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT cash FROM player_cash WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("cash");
            }
            return 0L; // Retorna 0 se o jogador não tiver registro
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao consultar cash para UUID " + uuid + ":");
            e.printStackTrace();
            return 0L;
        }
    }
}