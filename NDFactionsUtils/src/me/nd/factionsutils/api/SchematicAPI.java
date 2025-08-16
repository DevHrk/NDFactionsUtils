package me.nd.factionsutils.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factionsutils.Main;

public class SchematicAPI {
	
	public static boolean structureExists(String structureName) {
	    boolean exists = false;
	    try {
	    	File file = new File("plugins/NDFactionsUtils/DataBase/database.db");
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+file);
	        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM nditens WHERE schematicname = ?");
	        statement.setString(1, structureName);
	        ResultSet resultSet = statement.executeQuery();
	        exists = resultSet.next();
	        resultSet.close();
	        statement.close();
	        connection.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return exists;
	}
	
	public static boolean structureDontExist(String structureName) {
	    boolean exists = false;
	    try {
	    	File file = new File("plugins/NDFactionsUtils/DataBase/database.db");
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ file);
	        String query = "SELECT 1 FROM nditens WHERE schematicname = ?";
	        PreparedStatement preparedStatement = connection.prepareStatement(query);
	        preparedStatement.setString(1, structureName);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        exists = resultSet.next();
	        resultSet.close();
	        preparedStatement.close();
	        connection.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return exists;
	}

	
	    // location = Localização em que a estrutura será gerada
	    // structureName = nome da estrutura
	    // player = pega o player que está utilizando o item ou executando o comando
	    // para usar em outra classe use assim:
	    // SchematicAPI.loadBlocks(p, p.getLocation(), nome da estrutura);

	public static void loadBlocks(final Player player, final Location location, String structureName) { 
	if (structureName == null) { 
		return; 
		}
	String blocksString = null;
	try {
	    File file = new File("plugins/NDFactionsUtils/DataBase/database.db");
	    Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ file);
	    PreparedStatement statement = connection.prepareStatement("SELECT schematicblocks FROM nditens WHERE schematicname = ?");
	    statement.setString(1, structureName);
	    ResultSet resultSet = statement.executeQuery();
	    if (resultSet.next()) {
	        blocksString = resultSet.getString("schematicblocks");
	    }
	    resultSet.close();
	    statement.close();
	    connection.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	if (blocksString == null) {
	    return;
	}

	// Decompress the blocks data
	byte[] decodedBytes = Base64.getDecoder().decode(blocksString);
	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
	GZIPInputStream gzipInputStream;
	try {
	gzipInputStream = new GZIPInputStream(byteArrayInputStream);
	Scanner scanner = new Scanner(gzipInputStream, "UTF-8");
	blocksString = scanner.useDelimiter("\\A").next();
	scanner.close();
	} catch (IOException e) {
		e.printStackTrace();
	}

	final List<String> blocks = Arrays.asList(blocksString.substring(1, blocksString.length() - 1).split(", "));
	final World world = player.getWorld();

	new BukkitRunnable() {
	    private int index = 0;

	    @Override
	    public void run() {
	        IntStream.range(0, Main.get().getConfig().getInt("Schematic.Blocos")).forEach(i -> {
	            if (index >= blocks.size()) {
	                this.cancel();
	                return;
	            }

	            String blockString = blocks.get(index);

	            String[] parts = blockString.split("; ");
	            double x = Double.parseDouble(parts[0].replace("\"", ""));
	            double y = Double.parseDouble(parts[1].replace("\"", ""));
	            double z = Double.parseDouble(parts[2].replace("\"", ""));
	            Material type = Material.getMaterial(parts[3]);
	            byte data = Byte.parseByte(parts[4].replace("\"", ""));

	            Location blockLocation = location.clone().add(x, y, z);
	            Block block = world.getBlockAt(blockLocation);

	            if (block.getType() != Material.MOB_SPAWNER) {
	                block.setType(type);
	                block.setData(data);
	            }

	            index++;
	        });
	    }
	}.runTaskTimer(Main.get(), 0L, Main.get().getConfig().getInt("Schematic.Velocidade"));
	}
	public static void saveBlocks(Player player, Location start, Location end, String structureName) {
	    List<String> blocks = new ArrayList<>();
	    World world = player.getWorld();

	    IntStream.rangeClosed(Math.min(start.getBlockX(), end.getBlockX()), Math.max(start.getBlockX(), end.getBlockX())).forEach(x ->
	        IntStream.rangeClosed(Math.min(start.getBlockY(), end.getBlockY()), Math.max(start.getBlockY(), end.getBlockY())).forEach(y ->
	            IntStream.rangeClosed(Math.min(start.getBlockZ(), end.getBlockZ()), Math.max(start.getBlockZ(), end.getBlockZ())).forEach(z -> {
	                Block block = world.getBlockAt(x, y, z);
	                    blocks.add("\"" + (x - player.getLocation().getBlockX()) + ".0; " + (y - player.getLocation().getBlockY()) + ".0; " + (z - player.getLocation().getBlockZ()) + ".0; " + block.getType().name() + "; " + block.getData() + "\"");
	            })));

	    try {
	        File file = new File("plugins/NDFactionsUtils/DataBase/database.db");
	        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
	        PreparedStatement statement = connection.prepareStatement("INSERT INTO nditens (schematicname, schematicblocks) VALUES (?, ?)");
	        statement.setString(1, structureName);
	        
	        // Compress the blocks data
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
	        gzipOutputStream.write(String.join(", ", blocks).getBytes(StandardCharsets.UTF_8));
	        gzipOutputStream.close();
	        String compressedBlocks = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

	        statement.setString(2, compressedBlocks);
	        statement.executeUpdate();
	        statement.close();
	        connection.close();
	    } catch (SQLException | IOException e) {
	        e.printStackTrace();
	    }
	}

	    public static void removeStructure(String structureName) {
	        try {
	        	File file = new File("plugins/NDFactionsUtils/DataBase/database.db");
	            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
	            PreparedStatement statement = connection.prepareStatement("DELETE FROM nditens WHERE schematicname = ?");
	            statement.setString(1, structureName);
	            int rowsAffected = statement.executeUpdate();
	            if (rowsAffected == 0) {
	                System.out.println("A estrutura " + structureName + " não existe no banco de dados.");
	            }
	            statement.close();
	            connection.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
}
