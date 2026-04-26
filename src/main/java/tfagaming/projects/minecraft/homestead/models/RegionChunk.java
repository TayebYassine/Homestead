package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import java.util.UUID;

public final class RegionChunk {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private boolean autoUpdate = true;

	private final long id;
	private long regionId;
	private UUID worldId;
	private int x;
	private int z;
	private long claimedAt;
	private boolean forceLoaded;

	public RegionChunk(long regionId, Chunk chunk) {
		this(regionId, chunk.getWorld().getUID(), chunk.getX(), chunk.getZ(), System.currentTimeMillis(), false);
	}

	public RegionChunk(long regionId, World world, int x, int z) {
		this(regionId, world.getUID(), x, z, System.currentTimeMillis(), false);
	}

	public RegionChunk(long regionId, UUID worldId, int x, int z, long claimedAt, boolean forceLoaded) {
		this.id = Homestead.SNOWFLAKE.nextId();
		this.regionId = regionId;
		this.worldId = worldId;
		this.x = x;
		this.z = z;
		this.claimedAt = claimedAt;
		this.forceLoaded = forceLoaded;
	}

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
	 * Toggle Auto-Update for caching. If {@code true}, any call for setters will automatically
	 * update the cache. Otherwise, only the instance of the class will be updated.<br>
	 * @param autoUpdate Auto-Update toggle
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public long getUniqueId() {
		return id;
	}

	public long getRegionId() {
		return regionId;
	}

	/**
	 * Returns the region by directly fetching with region ID from cache.
	 * @return The region if found, {@code null} otherwise.
	 */
	public @Nullable Region getRegion() {
		return RegionManager.findRegion(regionId);
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
		update();
	}

	public UUID getWorldId() {
		return worldId;
	}

	public void setWorldId(UUID worldId) {
		this.worldId = worldId;
		update();
	}

	public World getWorld() {
		return Bukkit.getWorld(worldId);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
		update();
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
		update();
	}

	public long getClaimedAt() {
		return claimedAt;
	}

	public void setClaimedAt(long claimedAt) {
		this.claimedAt = claimedAt;
		update();
	}

	public boolean isForceLoaded() {
		return forceLoaded;
	}

	public void setForceLoaded(boolean forceLoaded) {
		this.forceLoaded = forceLoaded;
		update();
	}

	public Chunk toBukkit() {
		World world = getWorld();
		if (world == null) return null;
		return world.getChunkAt(x, z);
	}

	public Location toBukkitLocation() {
		World world = getWorld();
		if (world == null) return null;
		Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
		location.setY(world.getHighestBlockYAt(location) + 2);
		return location;
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
		if (!autoUpdate) return;

		Homestead.regionChunkCache.putOrUpdate(this);
	}
}