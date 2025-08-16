package me.nd.factionsutils.dados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.nd.factionsutils.itens.RelogioRedstone;

public class RedStoneDAO {
	
	public static Connection connection = SQlite.getConnection();
    public static void saveRedstoneClock(Location loc, double delay, boolean active) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO redstone_clocks (location, delay, active) VALUES (?, ?, ?)")) {
            ps.setString(1, locationToString(loc));
            ps.setDouble(2, delay);
            ps.setBoolean(3, active);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao salvar relógio de redstone:");
            e.printStackTrace();
        }
    }

    public static double loadRedstoneClockDelay(Location loc) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT delay FROM redstone_clocks WHERE location = ?")) {
            ps.setString(1, locationToString(loc));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("delay");
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao carregar delay do relógio de redstone:");
            e.printStackTrace();
        }
        return RelogioRedstone.DEFAULT_DELAY;
    }

    public static void removeRedstoneClock(Location loc) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM redstone_clocks WHERE location = ?")) {
            ps.setString(1, locationToString(loc));
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao remover relógio de redstone:");
            e.printStackTrace();
        }
    }

    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public static Location stringToLocation(String locString) {
        String[] parts = locString.split(",");
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }
	
}
