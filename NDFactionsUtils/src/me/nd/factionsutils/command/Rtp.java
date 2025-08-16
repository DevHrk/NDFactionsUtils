package me.nd.factionsutils.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class Rtp extends Commands {

    private static final Random RANDOM = new Random();
    private static final int MIN_DISTANCE = 800;
    private static final int MAX_DISTANCE = 1200;
    private static final int MAX_ATTEMPTS = 10;

    public Rtp() {
        super("rtp");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        Location currentLocation = player.getLocation();

        // Attempt to find a safe location
        Location safeLocation = findSafeLocation(world, currentLocation);

        if (safeLocation == null) {
            player.sendMessage("§cNão foi possível encontrar um local seguro para teleporte após " + MAX_ATTEMPTS + " tentativas!");
            return;
        }

        // Teleport the player
        player.teleport(safeLocation);
        player.sendMessage("§aTeleportado a " + 
            String.format("%.1f", currentLocation.distance(safeLocation)) + " blocos de distância!");
    }

    private Location findSafeLocation(World world, Location center) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            Location candidate = generateRandomLocation(world, center);
            if (isSafeLocation(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private Location generateRandomLocation(World world, Location center) {
        double angle = RANDOM.nextDouble() * 2 * Math.PI; // Random angle in radians
        double distance = MIN_DISTANCE + (RANDOM.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE)); // Random distance between 500 and 800
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;

        // Find the highest block at the X, Z coordinates
        int y = world.getHighestBlockYAt((int) x, (int) z);
        return new Location(world, x, y + 1, z); // Place player one block above the highest block
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // Check the block the player will stand on
        Location feet = new Location(world, x, y, z);
        Location below = new Location(world, x, y - 1, z);
        Location above = new Location(world, x, y + 1, z);

        Material feetBlock = feet.getBlock().getType();
        Material belowBlock = below.getBlock().getType();
        Material aboveBlock = above.getBlock().getType();

        // Safe location criteria:
        // 1. Block below feet is solid (not air, water, lava, etc.)
        // 2. Block at feet and above are air (player can stand there)
        // 3. Not in liquid (water or lava)
        return belowBlock.isSolid() &&
               !belowBlock.equals(Material.LAVA) &&
               !belowBlock.equals(Material.WATER) &&
               feetBlock.equals(Material.AIR) &&
               aboveBlock.equals(Material.AIR);
    }
}