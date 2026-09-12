package me.tayebyassine.homestead.models.serialize;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Serializable representation of a block position.
 *
 * <p>Stores the world UUID and block coordinates so the position can be
 * persisted as a comma-separated string and later deserialized back into
 * a Bukkit {@link Block}.
 * </p>
 */
public final class SeBlock {

    private UUID worldId;
    private int x;
    private int y;
    private int z;

    /**
     * Create a serializable block position from a Bukkit block.
     *
     * @param block the block to copy coordinates from
     */
    public SeBlock(Block block) {
        this(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    /**
     * Create a serializable block position from a world and coordinates.
     *
     * @param world the world
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     */
    public SeBlock(World world, int x, int y, int z) {
        this(world.getUID(), x, y, z);
    }

    /**
     * Create a serializable block position from a world UUID and
     * coordinates.
     *
     * @param worldId the world UUID
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param z       the z coordinate
     */
    public SeBlock(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Deserialize a block position from its comma-separated string form.
     *
     * @param serialized the string in the format {@code worldId,x,y,z}
     * @return the deserialized block position, or {@code null} if the
     * world no longer exists
     * @throws IllegalArgumentException if the format is invalid
     */
    public static SeBlock deserialize(@NotNull String serialized) {
        String[] split = serialized.split(",");

        try {
            UUID worldId = UUID.fromString(split[0]);

            if (Bukkit.getWorld(worldId) == null) return null;

            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);

            return new SeBlock(worldId, x, y, z);
        } catch (Exception e) {
            throw new IllegalArgumentException("Serialized string cannot be parsed, invalid format");
        }
    }

    /**
     * Get the world UUID.
     *
     * @return the world UUID
     */
    public UUID getWorldId() {
        return worldId;
    }

    /**
     * Set the world UUID.
     *
     * @param worldId the new world UUID
     */
    public void setWorldId(UUID worldId) {
        this.worldId = worldId;
    }

    /**
     * Get the Bukkit world.
     *
     * @return the world, or {@code null} if it no longer exists
     */
    public World getWorld() {
        return Bukkit.getWorld(worldId);
    }

    /**
     * Set the world from a Bukkit world reference.
     *
     * @param world the world
     */
    public void setWorld(World world) {
        this.worldId = world.getUID();
    }

    /**
     * Get the x coordinate.
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Set the x coordinate.
     *
     * @param x the new x coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the y coordinate.
     *
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Set the y coordinate.
     *
     * @param y the new y coordinate
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Get the z coordinate.
     *
     * @return the z coordinate
     */
    public int getZ() {
        return z;
    }

    /**
     * Set the z coordinate.
     *
     * @param z the new z coordinate
     */
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * Serialise the block position to a comma-separated string.
     *
     * @return the encoded string in the format {@code worldId,x,y,z}
     */
    public String serialize() {
        return String.format("%s,%s,%s,%s",
                worldId.toString(),
                x,
                y,
                z
        );
    }

    /**
     * Convert this position to a Bukkit block.
     *
     * @return the block at this position, or {@code null} if the world
     * no longer exists
     */
    public Block toBukkit() {
        World world = getWorld();

        if (world == null) {
            return null;
        }

        return world.getBlockAt(x, y, z);
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SeBlock other)) return false;
        return this.x == other.x && this.y == other.y && this.z == other.z &&
                this.worldId.equals(other.worldId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldId, x, y, z);
    }
}
