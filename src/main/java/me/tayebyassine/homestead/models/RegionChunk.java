package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a single chunk claimed by a region.
 *
 * <p>Region chunks are indexed by both region ID and world position for
 * fast spatial lookups. Each chunk can optionally be force-loaded.</p>
 */
public final class RegionChunk {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private final UUID worldId;
    private final int x;
    private final int z;
    private long regionId;
    private long claimedAt;
    private boolean forceLoaded;

    /**
     * Create a chunk claim from a Bukkit chunk.
     *
     * @param regionId the owning region ID
     * @param chunk    the chunk to claim
     */
    public RegionChunk(long regionId, Chunk chunk) {
        this(regionId, chunk.getWorld().getUID(), chunk.getX(), chunk.getZ(), System.currentTimeMillis(), false);
    }

    /**
     * Create a chunk claim from a world and chunk coordinates.
     *
     * @param regionId the owning region ID
     * @param world    the world
     * @param x        the chunk x coordinate
     * @param z        the chunk z coordinate
     */
    public RegionChunk(long regionId, World world, int x, int z) {
        this(regionId, world.getUID(), x, z, System.currentTimeMillis(), false);
    }

    /**
     * Create a chunk claim with a generated snowflake ID.
     *
     * @param regionId    the owning region ID
     * @param worldId     the world UUID
     * @param x           the chunk x coordinate
     * @param z           the chunk z coordinate
     * @param claimedAt   the claim timestamp
     * @param forceLoaded whether the chunk is force-loaded
     */
    public RegionChunk(long regionId, UUID worldId, int x, int z, long claimedAt, boolean forceLoaded) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.worldId = worldId;
        this.x = x;
        this.z = z;
        this.claimedAt = claimedAt;
        this.forceLoaded = forceLoaded;
    }

    /**
     * Create a chunk claim from pre-existing data (deserialization).
     *
     * @param id          the snowflake ID
     * @param regionId    the owning region ID
     * @param worldId     the world UUID
     * @param x           the chunk x coordinate
     * @param z           the chunk z coordinate
     * @param claimedAt   the claim timestamp
     * @param forceLoaded whether the chunk is force-loaded
     */
    public RegionChunk(long id, long regionId, UUID worldId, int x, int z, long claimedAt, boolean forceLoaded) {
        this.id = id;
        this.regionId = regionId;
        this.worldId = worldId;
        this.x = x;
        this.z = z;
        this.claimedAt = claimedAt;
        this.forceLoaded = forceLoaded;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the chunk ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the owning region ID.
     *
     * @return the region ID
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Set the owning region ID. Automatically updates the region and
     * position indexed caches.
     *
     * @param regionId the new region ID
     */
    public void setRegionId(long regionId) {
        long oldRegionId = this.regionId;

        if (oldRegionId == regionId) {
            return;
        }

        Homestead.REGION_INDEXED_CHUNK_CACHE.removeFromRegion(this, oldRegionId);

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
     * Get the world UUID.
     *
     * @return the world UUID
     */
    public @NotNull UUID getWorldId() {
        return worldId;
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
     * Get the chunk x coordinate.
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Get the chunk z coordinate.
     *
     * @return the z coordinate
     */
    public int getZ() {
        return z;
    }

    /**
     * Get the claim timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getClaimedAt() {
        return claimedAt;
    }

    /**
     * Set the claim timestamp.
     *
     * @param claimedAt the new timestamp
     */
    public void setClaimedAt(long claimedAt) {
        this.claimedAt = claimedAt;
        update();
    }

    /**
     * Whether the chunk is force-loaded.
     *
     * @return {@code true} if force-loaded
     */
    public boolean isForceLoaded() {
        return forceLoaded;
    }

    /**
     * Set whether the chunk is force-loaded.
     *
     * @param forceLoaded {@code true} to force-load
     */
    public void setForceLoaded(boolean forceLoaded) {
        this.forceLoaded = forceLoaded;
        update();
    }

    /**
     * Convert this chunk claim to a Bukkit chunk.
     *
     * @return the chunk, or {@code null} if the world is not loaded
     */
    public @Nullable Chunk toBukkit() {
        World world = getWorld();
        if (world == null) return null;
        return world.getChunkAt(x, z);
    }

    /**
     * Convert to a Bukkit location at the center of the chunk surface.
     *
     * @return the location, or {@code null} if the world is not loaded
     */
    public @Nullable Location toBukkitLocation() {
        World world = getWorld();

        if (world == null) return null;

        Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);

        location.setY(world.getHighestBlockYAt(location) + 2);

        return location;
    }

    /**
     * Convert to a Bukkit location without the highest-block-Y
     * adjustment. Used for display/map purposes.
     *
     * @return the location, or {@code null} if the world is not loaded
     */
    public @Nullable Location toBukkitDisplayLocation() {
        World world = getWorld();

        if (world == null) return null;

        return new Location(world, x * 16 + 8, 64, z * 16 + 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RegionChunk other)) return false;
        return this.x == other.x && this.z == other.z && this.worldId.equals(other.worldId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldId, x, z);
    }

    private void update() {
        Homestead.REGION_INDEXED_CHUNK_CACHE.add(this);
        Homestead.POSITION_INDEXED_CHUNK_CACHE.add(this);

        Homestead.CHUNK_CACHE.putOrUpdate(this);
    }
}
