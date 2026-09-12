package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.serialize.SeBlock;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.models.serialize.SeRent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A rectangular sub-area within a parent region.
 *
 * <p>Sub-areas allow regions to be partitioned into smaller parcels that
 * can have independent flags and optionally be rented out. Each sub-area
 * is defined by two corner blocks ({@link #point1} and {@link #point2})
 * within a single world.</p>
 */
public final class SubArea {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private final long createdAt;
    private long regionId;
    private String name;
    private UUID worldId;
    private SeBlock point1;
    private SeBlock point2;
    private long playerFlags = 0L;
    private SeRent rent;

    /**
     * Create a new sub-area with a generated snowflake ID.
     *
     * @param regionId    the parent region ID
     * @param name        the sub-area name
     * @param world       the world
     * @param point1      the first corner block
     * @param point2      the second corner block
     * @param playerFlags the initial player flags
     */
    public SubArea(long regionId, String name, World world, Block point1, Block point2, long playerFlags) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.name = name;
        this.worldId = world.getUID();
        this.point1 = new SeBlock(point1);
        this.point2 = new SeBlock(point2);
        this.playerFlags = playerFlags;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Create a sub-area from Bukkit blocks with deserialised rent data.
     *
     * @param id          the snowflake ID
     * @param regionId    the parent region ID
     * @param name        the sub-area name
     * @param worldId     the world UUID
     * @param point1      the first corner block
     * @param point2      the second corner block
     * @param playerFlags the player flags
     * @param rent        the rent data
     * @param createdAt   the creation timestamp
     */
    public SubArea(long id, long regionId, String name, UUID worldId, Block point1, Block point2, long playerFlags, SeRent rent, long createdAt) {
        this(id, regionId, name, worldId, new SeBlock(point1), new SeBlock(point2), playerFlags, rent, createdAt);
    }

    /**
     * Create a sub-area from pre-existing data (deserialization).
     *
     * @param id          the snowflake ID
     * @param regionId    the parent region ID
     * @param name        the sub-area name
     * @param worldId     the world UUID
     * @param point1      the first corner
     * @param point2      the second corner
     * @param playerFlags the player flags
     * @param rent        the rent data
     * @param createdAt   the creation timestamp
     */
    public SubArea(long id, long regionId, String name, UUID worldId, SeBlock point1, SeBlock point2, long playerFlags, SeRent rent, long createdAt) {
        this.id = id;
        this.regionId = regionId;
        this.name = name;
        this.worldId = worldId;
        this.point1 = point1;
        this.point2 = point2;
        this.playerFlags = playerFlags;
        this.rent = rent;
        this.createdAt = createdAt;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the sub-area ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the creation timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the parent region ID.
     *
     * @return the region ID
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Set the parent region ID.
     *
     * @param regionId the new region ID
     */
    public void setRegionId(long regionId) {
        this.regionId = regionId;
        update();
    }

    /**
     * Get the parent region from the cache.
     *
     * @return the region, or {@code null} if not found
     */
    public @Nullable Region getRegion() {
        return RegionManager.findRegion(regionId);
    }

    /**
     * Get the parent region's name safely. Returns {@code "?"} if not
     * found.
     *
     * @return the region name
     */
    public @NotNull String getRegionName() {
        Region region = getRegion();

        return region == null ? "?" : region.getName();
    }

    /**
     * Get the sub-area name.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Set the sub-area name.
     *
     * @param name the new name
     */
    public void setName(@NotNull String name) {
        this.name = name;
        update();
    }

    /**
     * Get the world UUID.
     *
     * @return the world UUID
     */
    public @NotNull UUID getWorldId() {
        return worldId;
    }

    /**
     * Set the world UUID.
     *
     * @param worldId the new world UUID
     */
    public void setWorldId(@NotNull UUID worldId) {
        this.worldId = worldId;
        update();
    }

    /**
     * Get the Bukkit world.
     *
     * @return the world, or {@code null} if not loaded
     */
    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldId);
    }

    /**
     * Get the first corner block.
     *
     * @return the first corner
     */
    public @NotNull SeBlock getPoint1() {
        return point1;
    }

    /**
     * Set the first corner from a serialisable block.
     *
     * @param point1 the new corner
     */
    public void setPoint1(@NotNull SeBlock point1) {
        this.point1 = point1;
        update();
    }

    /**
     * Set the first corner from a Bukkit block.
     *
     * @param block the new corner
     */
    public void setPoint1(@NotNull Block block) {
        this.point1 = new SeBlock(block);
        update();
    }

    /**
     * Get the second corner block.
     *
     * @return the second corner
     */
    public @NotNull SeBlock getPoint2() {
        return point2;
    }

    /**
     * Set the second corner from a serialisable block.
     *
     * @param point2 the new corner
     */
    public void setPoint2(@NotNull SeBlock point2) {
        this.point2 = point2;
        update();
    }

    /**
     * Set the second corner from a Bukkit block.
     *
     * @param block the new corner
     */
    public void setPoint2(@NotNull Block block) {
        this.point2 = new SeBlock(block);
        update();
    }

    /**
     * Get the minimum x coordinate.
     *
     * @return the minimum x
     */
    public int getMinX() {
        return Math.min(point1.getX(), point2.getX());
    }

    /**
     * Get the maximum x coordinate.
     *
     * @return the maximum x
     */
    public int getMaxX() {
        return Math.max(point1.getX(), point2.getX());
    }

    /**
     * Get the minimum y coordinate.
     *
     * @return the minimum y
     */
    public int getMinY() {
        return Math.min(point1.getY(), point2.getY());
    }

    /**
     * Get the maximum y coordinate.
     *
     * @return the maximum y
     */
    public int getMaxY() {
        return Math.max(point1.getY(), point2.getY());
    }

    /**
     * Get the minimum z coordinate.
     *
     * @return the minimum z
     */
    public int getMinZ() {
        return Math.min(point1.getZ(), point2.getZ());
    }

    /**
     * Get the maximum z coordinate.
     *
     * @return the maximum z
     */
    public int getMaxZ() {
        return Math.max(point1.getZ(), point2.getZ());
    }

    /**
     * Get the total block volume.
     *
     * @return width &times; height &times; depth
     */
    public int getVolume() {
        int width = getMaxX() - getMinX() + 1;
        int height = getMaxY() - getMinY() + 1;
        int depth = getMaxZ() - getMinZ() + 1;
        return width * height * depth;
    }

    /**
     * Check whether a serialisable block is inside this sub-area.
     *
     * @param block the block to test
     * @return {@code true} if the block is inside
     */
    public boolean isBlockInside(SeBlock block) {
        if (block == null) return false;

        return isBlockInside(block.toBukkit());
    }

    /**
     * Check whether a Bukkit block is inside this sub-area.
     *
     * @param block the block to test
     * @return {@code true} if the block is inside
     */
    public boolean isBlockInside(Block block) {
        if (block == null) return false;

        return block.getX() >= getMinX() && block.getX() <= getMaxX()
                && block.getY() >= getMinY() && block.getY() <= getMaxY()
                && block.getZ() >= getMinZ() && block.getZ() <= getMaxZ();
    }

    /**
     * Check whether a serializable location is inside this sub-area.
     *
     * @param location the location to test
     * @return {@code true} if the location is inside
     */
    public boolean isLocationInside(SeLocation location) {
        if (location == null || location.getWorld() == null) return false;

        return isLocationInside(location.toBukkit());
    }

    /**
     * Check whether a Bukkit location is inside this sub-area.
     *
     * @param location the location to test
     * @return {@code true} if the location is inside
     */
    public boolean isLocationInside(org.bukkit.Location location) {
        if (location == null || location.getWorld() == null) return false;
        World subAreaWorld = getWorld();
        if (subAreaWorld == null || !location.getWorld().equals(subAreaWorld)) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= getMinX() && x <= getMaxX()
                && y >= getMinY() && y <= getMaxY()
                && z >= getMinZ() && z <= getMaxZ();
    }

    /**
     * Check whether this sub-area intersects another.
     *
     * @param other the other sub-area
     * @return {@code true} if they overlap
     */
    public boolean isIntersecting(SubArea other) {
        if (other == null) return false;
        if (!this.worldId.equals(other.worldId)) return false;

        return (this.getMinX() <= other.getMaxX() && this.getMaxX() >= other.getMinX())
                && (this.getMinY() <= other.getMaxY() && this.getMaxY() >= other.getMinY())
                && (this.getMinZ() <= other.getMaxZ() && this.getMaxZ() >= other.getMinZ());
    }

    /**
     * Get the player flags bitmask.
     *
     * @return the player flags
     */
    public long getPlayerFlags() {
        return playerFlags;
    }

    /**
     * Set the player flags bitmask.
     *
     * @param playerFlags the new flags
     */
    public void setPlayerFlags(long playerFlags) {
        this.playerFlags = playerFlags;
        update();
    }

    /**
     * Get the rent configuration. Always returns a non-null instance.
     *
     * @return the rent
     */
    public @NotNull SeRent getRent() {
        if (rent == null) {
            rent = new SeRent();
        }
        return rent;
    }

    /**
     * Set the rent configuration.
     *
     * @param rent the new rent, or {@code null} to reset to defaults
     */
    public void setRent(@Nullable SeRent rent) {
        this.rent = rent != null ? rent : new SeRent();
        update();
    }
    
    private void update() {
        Homestead.SUBAREA_CACHE.putOrUpdate(this);
    }
}
