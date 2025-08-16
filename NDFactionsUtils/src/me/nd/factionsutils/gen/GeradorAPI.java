package me.nd.factionsutils.gen;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import me.nd.factionsutils.Main;
import me.nd.factionsutils.dados.GeradorDAO;
import me.nd.factionsutils.dados.SQlite;
import me.nd.factionsutils.hologram.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GeradorAPI extends SQlite {

    public String getValue(String table, String column, Object value, int columnIndex) {
        String valueFinally = null;
        try {
            PreparedStatement preparedStatement = GeradorAPI.getConnection().prepareStatement("SELECT * FROM " + table + " WHERE " + column + "='" + value + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                valueFinally = resultSet.getString(columnIndex);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return valueFinally;
    }

    public void createItem(Location loc, ItemStack item) {
        GeradorDAO.getallGeradores().add(new Gerador(loc, item));
        try {
            PreparedStatement preparedStatement = GeradorAPI.getConnection().prepareStatement("INSERT INTO nditens (location, gerador)VALUES(?,?)");
            preparedStatement.setString(1, StringUtils.serializeLocation(loc));
            preparedStatement.setString(2, Main.get().getGson().toJson((Object)GeradorDAO.getGerador(StringUtils.serializeLocation(loc))));
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException var3) {
            var3.printStackTrace();
        }
    }

    public void deleteItem(Location loc) {
        try {
            PreparedStatement preparedStatement = GeradorAPI.getConnection().prepareStatement("DELETE FROM nditens WHERE location='" + StringUtils.serializeLocation(loc) + "';");
            GeradorDAO.getallGeradores().remove(GeradorDAO.getGerador(StringUtils.serializeLocation(loc)));
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException var3) {
            var3.printStackTrace();
        }
    }

    public Gerador getGerador(Location loc) {
        return GeradorDAO.getGerador(this.getValue("nditens", "location", StringUtils.serializeLocation(loc), 1));
    }

    public Gerador transform(ResultSet resultSet) throws SQLException {
        String geradores = resultSet.getString("gerador");
        return (Gerador)Main.get().getGson().fromJson(geradores, Gerador.class);
    }

	public List<Gerador> findAll() {
        ArrayList users = new ArrayList();

        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM `nditens`");
            Throwable var3 = null;

            try {
                ResultSet resultSet = ps.executeQuery();

                while (resultSet.next()) {
                    Gerador gerador = this.transform(resultSet);
                    if (gerador != null) {
                        users.add(gerador);
                    }
                }
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (ps != null) {
                    if (var3 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        ps.close();
                    }
                }

            }
        } catch (SQLException var16) {
            var16.printStackTrace();
        }

        return users;
    }
}
