package me.tayebyassine.homestead.models.serialize;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Serializable representation of a location with full precision.
 *
 * <p>Stores the world UUID, double-precision coordinates, and yaw/pitch
 * so the location can be persisted as a comma-separated string and later
 * deserialized back into a Bukkit {@link Location}.
 * </p>
 */
public final class SeLocation {

    private UUID worldId;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    /**
     * Create a serializable location from a Bukkit location.
     *
     * @param location the location to copy from
     */
    public SeLocation(Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Create a serializable location from a world and coordinates.
     *
     * @param world the world
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @param yaw   the yaw rotation
     * @param pitch the pitch rotation
     */
    public SeLocation(World world, double x, double y, double z, float yaw, float pitch) {
        this(world.getUID(), x, y, z, yaw, pitch);
    }

    /**
     * Create a serializable location from a world UUID and coordinates.
     *
     * @param worldId the world UUID
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param z       the z coordinate
     * @param yaw     the yaw rotation
     * @param pitch   the pitch rotation
     */
    public SeLocation(UUID worldId, double x, double y, double z, float yaw, float pitch) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Deserialize a location from its comma-separated string form.
     *
     * @param serialized the string in the format
     *                   {@code worldId,x,y,z,yaw,pitch}
     * @return the deserialized location, or {@code null} if the world no
     * longer exists
     * @throws IllegalArgumentException if the format is invalid
     */
    public static SeLocation deserialize(@NotNull String serialized) {
        String[] split = serialized.split(",");

        try {
            UUID worldId = UUID.fromString(split[0]);

            if (Bukkit.getWorld(worldId) == null) return null;

            double x = Double.parseDouble(split[1]);
            double y = Double.parseDouble(split[2]);
            double z = Double.parseDouble(split[3]);

            float yaw = Float.parseFloat(split[4]);
            float pitch = Float.parseFloat(split[5]);

            return new SeLocation(worldId, x, y, z, yaw, pitch);
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
        return Bukkit.getWorld(this.worldId);
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
    public double getX() {
        return x;
    }

    /**
     * Set the x coordinate.
     *
     * @param x the new x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Get the y coordinate.
     *
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Set the y coordinate.
     *
     * @param y the new y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Get the z coordinate.
     *
     * @return the z coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Set the z coordinate.
     *
     * @param z the new z coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Get the yaw rotation.
     *
     * @return the yaw in degrees
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Set the yaw rotation.
     *
     * @param yaw the new yaw in degrees
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * Get the pitch rotation.
     *
     * @return the pitch in degrees
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Set the pitch rotation.
     *
     * @param pitch the new pitch in degrees
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Serialise the location to a comma-separated string.
     *
     * @return the encoded string in the format
     * {@code worldId,x,y,z,yaw,pitch}
     */
    public String serialize() {
        return String.format("%s,%s,%s,%s,%s,%s",
                worldId.toString(),
                x,
                y,
                z,
                yaw,
                pitch
        );
    }

    /**
     * Convert this serializable location to a Bukkit location.
     *
     * @return the Bukkit location, or {@code null} if the world no
     * longer exists
     */
    public Location toBukkit() {
        World world = getWorld();

        if (world == null) {
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SeLocation other)) return false;
        return this.x == other.x && this.y == other.y && this.z == other.z &&
                this.worldId.equals(other.worldId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldId, x, y, z, yaw, pitch);
    }
}
