package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import java.util.UUID;

public final class RegionChunk {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private final long id;
	private long regionId;
	private final UUID worldId;
	private final int x;
	private final int z;
	private long claimedAt;
	private boolean forceLoaded;

	public RegionChunk(long regionId, Chunk chunk) {
		this(regionId, chunk.getWorld().getUID(), chunk.getX(), chunk.getZ(), System.currentTimeMillis(), false);
	}

	public RegionChunk(long regionId, World world, int x, int z) {
		this(regionId, world.getUID(), x, z, System.currentTimeMillis(), false);
	}

	public RegionChunk(long regionId, UUID worldId, int x, int z, long claimedAt, boolean forceLoaded) {
		this.id = Homestead.getSnowflake().nextId();
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

	public long getUniqueId() {
		return id;
	}

	public long getRegionId() {
		return regionId;
	}

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
	 * Returns the region by directly fetching with region ID from cache.
	 * @return The region if found, {@code null} otherwise.
	 */
	public @Nullable Region getRegion() {
		return RegionManager.findRegion(regionId);
	}

	/**
	 * Returns the region name safely by directly fetching with region ID from cache.
	 * @return The region name if found, {@code "?"} otherwise.
	 */
	public @NotNull String getRegionName() {
		Region region = getRegion();

		return region == null ? "?" : region.getName();
	}

	public @NotNull UUID getWorldId() {
		return worldId;
	}

	public @Nullable World getWorld() {
		return Bukkit.getWorld(worldId);
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
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

	public @Nullable Chunk toBukkit() {
		World world = getWorld();
		if (world == null) return null;
		return world.getChunkAt(x, z);
	}

	public @Nullable Location toBukkitLocation() {
		World world = getWorld();

		if (world == null) return null;

		Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);

		location.setY(world.getHighestBlockYAt(location) + 2);

		return location;
	}

	/**
	 * Exactly the same as {@link RegionChunk#toBukkitLocation()}, without the highest
	 * block Y check.
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