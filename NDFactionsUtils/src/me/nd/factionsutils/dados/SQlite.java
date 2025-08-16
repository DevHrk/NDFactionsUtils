package me.nd.factionsutils.dados;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQlite {
    private static Connection connection;

    public SQlite() {
        this.openConnection();
    }
    
    public void openConnection() {
        File file = new File("plugins/NDFactionUtils/DataBase/database.db");
        String url = "jdbc:sqlite:" + file;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] §fConexão com §6SQLite §faberta com sucesso");
            this.createTables();
        }
        catch (ClassNotFoundException | SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] §cHouve um erro ao tentar fazer conexão com §6SQLite");
        }
    }

    public static void closeConnection() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
            Bukkit.getConsoleSender().sendMessage("§cConexão com SQLite fechada com sucesso");
        }
        catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§cOcorreu um erro ao tentar fechar a conexão com o SQLite, erro:");
            e.printStackTrace();
        }
    }

    public static boolean executeQuery(String query, Object[] objects) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.execute();
            preparedStatement.close();
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void executeUpdate(String query, Object ... params) {
        try (PreparedStatement ps = connection.prepareStatement(query);){
            if (params != null && params.length > 0) {
                for (int index = 0; index < params.length; ++index) {
                    ps.setObject(index + 1, params[index]);
                }
            }
            ps.executeUpdate();
        }
        catch (SQLException var16) {
            var16.printStackTrace();
        }
    }

    public void createTables() {
        try {
            // Tabela nditens
            try (PreparedStatement ps1 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS nditens (" +
                    "schematicname TEXT, " +
                    "location TEXT, " +
                    "gerador TEXT, " +
                    "schematicblocks TEXT)")) {
                ps1.execute();
            }

            // Tabela player_vaults
            try (PreparedStatement ps2 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_vaults (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "unlocked_vaults INTEGER DEFAULT 1)")) {
                ps2.execute();
            }

            // Tabela vault_contents
            try (PreparedStatement ps3 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS vault_contents (" +
                    "uuid TEXT, " +
                    "vault_number INTEGER, " +
                    "location TEXT, " +
                    "slot INTEGER, " +
                    "item TEXT, " +
                    "PRIMARY KEY (uuid, vault_number, location, slot))")) {
                ps3.execute();
            }

            // Tabela invites
            try (PreparedStatement ps4 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS invites (" +
                    "inviter_uuid TEXT, " +
                    "invitee_uuid TEXT, " +
                    "vault_number INTEGER, " +
                    "accepted BOOLEAN DEFAULT 0, " +
                    "PRIMARY KEY (inviter_uuid, invitee_uuid, vault_number))")) {
                ps4.execute();
            }

            // Tabela vault_area
            try (PreparedStatement ps5 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS vault_area (" +
                    "id INTEGER PRIMARY KEY, " +
                    "pos1 TEXT, " +
                    "pos2 TEXT)")) {
                ps5.execute();
            }

            // Tabela vault_chests
            try (PreparedStatement ps6 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS vault_chests (" +
                    "location TEXT, " +
                    "vault_number INTEGER NOT NULL, " +
                    "is_double BOOLEAN DEFAULT 0," +
                    "PRIMARY KEY (location))")) {
                ps6.execute();
            }

            // Tabela para o spawn de saída
            try (PreparedStatement ps7 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS vault_exit_spawn (" +
                    "id INTEGER PRIMARY KEY, " +
                    "location TEXT)")) {
                ps7.execute();
            }

            // Tabela para o spawn de entrada
            try (PreparedStatement ps8 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS vault_entry_spawn (" +
                    "id INTEGER PRIMARY KEY, " +
                    "location TEXT)")) {
                ps8.execute();
            }

            // Tabela player_cash
            try (PreparedStatement ps9 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_cash (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "cash INTEGER DEFAULT 0)")) {
                ps9.execute();
            }

            // Tabela vault_icons (nova)
            try (PreparedStatement ps10 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS vault_icons (" +
                    "uuid TEXT, " +
                    "vault_number INTEGER, " +
                    "icon TEXT, " +
                    "PRIMARY KEY (uuid, vault_number))")) {
                ps10.execute();
            }
            
            // Tabela redstone_clocks (nova)
            try (PreparedStatement ps11 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS redstone_clocks (" +
                    "location TEXT PRIMARY KEY, " +
                    "delay REAL," +
                    "active BOOLEAN)")) {
                ps11.execute();
            }
            
            try (PreparedStatement ps12 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ender_chests (" +
                    "uuid TEXT, " +
                    "chest_number INTEGER, " +
                    "slot INTEGER, " +
                    "item TEXT, " +
                    "custom_name TEXT DEFAULT 'Baú #{chest_number}', " +
                    "size INTEGER DEFAULT 27, " +
                    "icon TEXT DEFAULT 'CHEST', " +
                    "PRIMARY KEY (uuid, chest_number, slot))")) {
                ps12.execute();
            }

            try (PreparedStatement ps13 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ender_chest_access (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "unlocked_chests INTEGER DEFAULT 1)")) {
                ps13.execute();
            }
            
            try (PreparedStatement ps14 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ender_chest_metadata (" +
                    "uuid TEXT, " +
                    "chest_number INTEGER, " +
                    "custom_name TEXT DEFAULT 'Baú #{chest_number}', " +
                    "size INTEGER DEFAULT 27, " +
                    "icon TEXT DEFAULT 'CHEST', " +
                    "PRIMARY KEY (uuid, chest_number))")) {
                ps14.execute();
            }
            
            try (PreparedStatement ps15 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS beacons (" +
                    "location TEXT PRIMARY KEY, " + // Unique identifier for beacon location (serialized as world,x,y,z)
                    "is_disabled BOOLEAN DEFAULT 0, " + // Whether the beacon is disabled due to explosion
                    "disabled_until BIGINT DEFAULT 0, " + // Timestamp until which the beacon is disabled
                    "effects TEXT DEFAULT '', " + // Comma-separated string of applied effects (e.g., "speed:1,strength:2")
                    "active_effect TEXT DEFAULT '', " + // Currently active effect (e.g., "speed:1")
                    "radius INTEGER DEFAULT 200, " + // Beacon radius in blocks (default from Settings.Raio)
                    "radius_upgrades INTEGER DEFAULT 0, " + // Number of radius upgrades applied (max 5)
                    "active_effects TEXT DEFAULT '', " + // Comma-separated string of currently active effects (e.g., "speed:1,haste:2")
                    "max_effects INTEGER DEFAULT 1)" // Maximum number of effects (default 1, max 3)
            )) {
                ps15.execute();
            }
            try (PreparedStatement ps16 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS aura_nodes (" +
                    "id TEXT PRIMARY KEY, " +
                    "location TEXT NOT NULL, " +
                    "faction_id TEXT NOT NULL, " +
                    "effects TEXT DEFAULT '', " +
                    "energy_cost INTEGER DEFAULT 0, " +
                    "durability INTEGER DEFAULT 100, " +
                    "active BOOLEAN DEFAULT 1, " +
                    "linked_nodes TEXT DEFAULT ''" +
                    ")")) {
                ps16.execute();
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§c[NDFactionUtils] Erro ao criar tabelas no SQLite:");
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() {
        return connection;
    }
}
