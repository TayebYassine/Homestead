package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;

import java.util.UUID;

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

	public SubArea(long id, long regionId, String name, UUID worldId, Block point1, Block point2, long playerFlags, SeRent rent, long createdAt) {
		this(id, regionId, name, worldId, new SeBlock(point1), new SeBlock(point2), playerFlags, rent, createdAt);
	}

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

	public long getUniqueId() {
		return id;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
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

	public @NotNull String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
		update();
	}

	public @NotNull UUID getWorldId() {
		return worldId;
	}

	public void setWorldId(@NotNull UUID worldId) {
		this.worldId = worldId;
		update();
	}

	public @Nullable World getWorld() {
		return Bukkit.getWorld(worldId);
	}

	public @NotNull SeBlock getPoint1() {
		return point1;
	}

	public void setPoint1(@NotNull SeBlock point1) {
		this.point1 = point1;
		update();
	}

	public void setPoint1(@NotNull Block block) {
		this.point1 = new SeBlock(block);
		update();
	}

	public @NotNull SeBlock getPoint2() {
		return point2;
	}

	public void setPoint2(@NotNull SeBlock point2) {
		this.point2 = point2;
		update();
	}

	public void setPoint2(@NotNull Block block) {
		this.point2 = new SeBlock(block);
		update();
	}

	public long getPlayerFlags() {
		return playerFlags;
	}

	public void setPlayerFlags(long playerFlags) {
		this.playerFlags = playerFlags;
		update();
	}

	public @Nullable SeRent getRent() {
		return rent;
	}

	public void setRent(@Nullable SeRent rent) {
		this.rent = rent;
		update();
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public int getMinX() {
		return Math.min(point1.getX(), point2.getX());
	}

	public int getMaxX() {
		return Math.max(point1.getX(), point2.getX());
	}

	public int getMinY() {
		return Math.min(point1.getY(), point2.getY());
	}

	public int getMaxY() {
		return Math.max(point1.getY(), point2.getY());
	}

	public int getMinZ() {
		return Math.min(point1.getZ(), point2.getZ());
	}

	public int getMaxZ() {
		return Math.max(point1.getZ(), point2.getZ());
	}

	public int getVolume() {
		int width = getMaxX() - getMinX() + 1;
		int height = getMaxY() - getMinY() + 1;
		int depth = getMaxZ() - getMinZ() + 1;
		return width * height * depth;
	}

	public boolean isBlockInside(SeBlock block) {
		if (block == null) return false;

		return isBlockInside(block.toBukkit());
	}

	public boolean isBlockInside(Block block) {
		if (block == null) return false;

		return block.getX() >= getMinX() && block.getX() <= getMaxX()
				&& block.getY() >= getMinY() && block.getY() <= getMaxY()
				&& block.getZ() >= getMinZ() && block.getZ() <= getMaxZ();
	}

	public boolean isLocationInside(SeLocation location) {
		if (location == null || location.getWorld() == null) return false;

		return isLocationInside(location.toBukkit());
	}

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

	public boolean isIntersecting(SubArea other) {
		if (other == null) return false;
		if (!this.worldId.equals(other.worldId)) return false;

		return (this.getMinX() <= other.getMaxX() && this.getMaxX() >= other.getMinX())
				&& (this.getMinY() <= other.getMaxY() && this.getMaxY() >= other.getMinY())
				&& (this.getMinZ() <= other.getMaxZ() && this.getMaxZ() >= other.getMinZ());
	}

	private void update() {
		Homestead.SUBAREA_CACHE.putOrUpdate(this);
	}
}