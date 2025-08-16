package me.nd.factionsutils.utils.misc;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WorldCuboid implements Cloneable, Iterable<Block> {

    public static final int CHUNK_SHIFTS = 4;

    private String worldName;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    // No-args constructor
    public WorldCuboid() {
        this.worldName = "world";
    }

    // Constructor with parameters
    public WorldCuboid(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.worldName = worldName;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public WorldCuboid(Location min, Location max) {
        this(min.getWorld().getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    public WorldCuboid(String worldName, Vector min, Vector max) {
        this(worldName, min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    public WorldCuboid(Location min, Location max, World world) {
        this(world.getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    // Getters
    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    // Setters
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldCuboid that = (WorldCuboid) o;
        return minX == that.minX &&
               minY == that.minY &&
               minZ == that.minZ &&
               maxX == that.maxX &&
               maxY == that.maxY &&
               maxZ == that.maxZ &&
               (worldName == null ? that.worldName == null : worldName.equals(that.worldName));
    }

    @Override
    public int hashCode() {
        int result = worldName != null ? worldName.hashCode() : 0;
        result = 31 * result + minX;
        result = 31 * result + minY;
        result = 31 * result + minZ;
        result = 31 * result + maxX;
        result = 31 * result + maxY;
        result = 31 * result + maxZ;
        return result;
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getMinLocation() {
        return new Location(getBukkitWorld(), minX, minY, minZ);
    }

    public Location getMaxLocation() {
        return new Location(getBukkitWorld(), maxX, maxY, maxZ);
    }

    public Vector getMinVector() {
        return new Vector(minX, minY, minZ);
    }

    public Vector getMaxVector() {
        return new Vector(maxX, maxY, maxZ);
    }

    public void getLocations(Consumer<Location> callback) {
        getBlocks((Block block) -> {
            callback.accept(block.getLocation());
        });
    }

    public void getBlocks(Consumer<Block> callback) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    callback.accept(getBukkitWorld().getBlockAt(x, y, z));
                }
            }
        }
    }

    public void getSolidBlocks(final Consumer<Block> callback) {
        getBlocks((Block block) -> {
            if (block != null && block.getType() != Material.AIR) {
                callback.accept(block);
            }
        });
    }

    public void getWalls(final Consumer<Block> callback) {
        World world = getBukkitWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                callback.accept(world.getBlockAt(x, y, minZ));

                if (minZ != maxZ) {
                    callback.accept(world.getBlockAt(x, y, maxZ));
                }
            }
        }

        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                callback.accept(world.getBlockAt(minX, y, z));

                if (minX != maxX) {
                    callback.accept(world.getBlockAt(maxX, y, z));
                }
            }
        }
    }

    public void getHollow(final Consumer<Block> callback) {
        getWalls((Block block) -> callback.accept(block));

        World world = getBukkitWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                callback.accept(world.getBlockAt(x, minY, z));

                if (maxY != minY) {
                    callback.accept(world.getBlockAt(x, maxY, z));
                }
            }
        }
    }

    public void getRoof(final Consumer<Block> callBack) {
        getBlocks(block -> {
            if (block.getLocation().getBlockY() == maxY) {
                callBack.accept(block);
            }
        });
    }

    public void getFloor(final Consumer<Block> callBack) {
        getBlocks(block -> {
            if (block.getLocation().getBlockY() == minY) {
                callBack.accept(block);
            }
        });
    }

    public void getBorder(final Consumer<Block> callBack) {
        getBlocks(block -> {
            if (block.getLocation().getBlockY() == maxY) {
                callBack.accept(block);
            }
            if (block.getLocation().getBlockY() == minY) {
                callBack.accept(block);
            }
        });
        getWalls(callBack);
    }

    public void getChunks(Consumer<Vector2D> callback) {
        getChunks((Integer x, Integer z) -> callback.accept(new Vector2D(x, z)));
    }

    public void getChunks(BiConsumer<Integer, Integer> callback) {
        for (int x = minX >> CHUNK_SHIFTS; x <= maxX >> CHUNK_SHIFTS; ++x) {
            for (int z = minZ >> CHUNK_SHIFTS; z <= maxZ >> CHUNK_SHIFTS; ++z) {
                callback.accept(x, z);
            }
        }
    }

    public List<Entity> getEntities() {
        return getEntities(entity -> true);
    }

    public List<Entity> getEntities(Predicate<? super Entity> predicate) {
        AxisAlignedBB bb = new AxisAlignedBB(minY, minY, minZ, maxX, maxY, maxZ);

        return ((CraftWorld) getBukkitWorld()).getHandle().a((net.minecraft.server.v1_8_R3.Entity) null, bb, (entity) -> {
            if (predicate == null) {
                return true;
            }

            return entity != null && predicate.test(entity.getBukkitEntity());
        }).stream()
                .map(net.minecraft.server.v1_8_R3.Entity::getBukkitEntity)
                .collect(Collectors.toList());
    }

    public void destroy(boolean removeEntities) {
        getSolidBlocks(block -> block.setType(Material.AIR));

        if (removeEntities) {
            getBukkitWorld().getEntities().stream()
                    .filter(entity -> this.contains(entity.getLocation(), true))
                    .filter(entity -> !(entity instanceof Player))
                    .forEach(entity -> entity.remove());
        }
    }

    public Location getCenter() {
        double x = (maxX - minX) / 2.0;
        double y = (maxY - minY) / 2.0;
        double z = (maxZ - minZ) / 2.0;

        return new Location(getBukkitWorld(), minX + x, minY + y, minZ + z);
    }

    public int getWidth() {
        return maxX - minX + 1;
    }

    public int getLength() {
        return maxZ - minZ + 1;
    }

    public int getHeight() {
        return maxY - minY + 1;
    }

    public int getSize() {
        return getWidth() * getLength() * getHeight();
    }

    public boolean contains(int x, int y, int z) {
        return (x >= minX && x <= maxX) && (y >= minY && y <= maxY) && (z >= minZ && z <= maxZ);
    }

    public boolean contains(Location location, boolean sameWorld) {
        if (location == null) {
            return false;
        }

        if (sameWorld && !location.getWorld().getName().equals(worldName)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return contains(x, y, z);
    }

    public WorldCuboid expand(int x, int y, int z) {
        return new WorldCuboid(worldName, minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
    }

    public WorldCuboid contract(int x, int y, int z) {
        return expand(-x, -y, -z);
    }

    @Override
    public WorldCuboid clone() {
        return new WorldCuboid(worldName, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public Iterator<Block> iterator() {
        return new Iterator<Block>() {

            private int nextX = minX;
            private int nextY = minY;
            private int nextZ = minZ;

            @Override
            public boolean hasNext() {
                return nextX != Integer.MIN_VALUE;
            }

            @Override
            public Block next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                Block block = getBukkitWorld().getBlockAt(nextX, nextY, nextZ);
                if (++nextX > maxX) {
                    nextX = minX;
                    if (++nextY > maxY) {
                        nextY = minY;
                        if (++nextZ > maxZ) {
                            nextX = Integer.MIN_VALUE;
                        }
                    }
                }

                return block;
            }
        };
    }
}
