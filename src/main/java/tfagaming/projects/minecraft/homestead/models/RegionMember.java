package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;

import java.util.UUID;

public final class RegionMember {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private boolean autoUpdate = true;

	private final long id;
	private UUID playerId;
	private final LinkageType linkageType;
	private long regionId = -1L;
	private long subAreaId = -1L;
	private long playerFlags = 0L;
	private long controlFlags = 0L;
	private long joinedAt;
	private long taxesAt;

	public RegionMember(OfflinePlayer player, LinkageType type, long linkageId) {
		this(player.getUniqueId(), type, linkageId);
	}

	public RegionMember(UUID playerId, LinkageType type, long linkageId) {
		this.id = Homestead.getSnowflake().nextId();
		this.playerId = playerId;
		this.linkageType = type;

		switch (linkageType) {
			case REGION -> this.regionId = linkageId;
			case SUBAREA -> this.subAreaId = linkageId;
		}

		this.joinedAt = System.currentTimeMillis();
	}

	public RegionMember(long id, UUID playerId, LinkageType type, long linkageId, long playerFlags, long controlFlags, long taxesAt, long joinedAt) {
		this.id = id;
		this.playerId = playerId;
		this.linkageType = type;

		switch (linkageType) {
			case REGION -> this.regionId = linkageId;
			case SUBAREA -> this.subAreaId = linkageId;
		}

		this.playerFlags = playerFlags;
		this.controlFlags = controlFlags;
		this.taxesAt = taxesAt;
		this.joinedAt = joinedAt;
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

	public UUID getPlayerId() {
		return playerId;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
		update();
	}

	public OfflinePlayer getPlayer() {
		if (INSTANCE == null) return null;

		return INSTANCE.getOfflinePlayerSync(playerId);
	}

	/**
	 * Returns the player's name safely. If the player was not found by their ID, it will
	 * return {@code "?"} instead.
	 * @return The player's name if found, {@code "?"} otherwise.
	 */
	public String getPlayerName() {
		OfflinePlayer player = getPlayer();

		return player == null ? "?" : player.getName();
	}

	public void setPlayer(OfflinePlayer player) {
		this.playerId = player.getUniqueId();
		update();
	}

	public LinkageType getLinkageType() {
		return linkageType;
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

	/**
	 * Returns the region name safely by directly fetching with region ID from cache.
	 * @return The region name if found, {@code "?"} otherwise.
	 */
	public String getRegionName() {
		Region region = getRegion();

		return region == null ? "?" : region.getName();
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
		update();
	}

	public long getSubAreaId() {
		return subAreaId;
	}

	/**
	 * Returns the sub-area by directly fetching with sub-area ID from cache.
	 * @return The sub-area if found, {@code null} otherwise.
	 */
	public @Nullable SubArea getSubArea() {
		return SubAreaManager.findSubArea(subAreaId);
	}

	/**
	 * Returns the sub-area name safely by directly fetching with sub-area ID from cache.
	 * @return The sub-area name if found, {@code "?"} otherwise.
	 */
	public String getSubAreaName() {
		SubArea subArea = getSubArea();

		return subArea == null ? "?" : subArea.getName();
	}

	public void setSubAreaId(long subAreaId) {
		this.subAreaId = subAreaId;
		update();
	}

	public long getPlayerFlags() {
		return playerFlags;
	}

	public void setPlayerFlags(long playerFlags) {
		this.playerFlags = playerFlags;
		update();
	}

	public long getControlFlags() {
		return controlFlags;
	}

	public void setControlFlags(long controlFlags) {
		this.controlFlags = controlFlags;
		update();
	}

	public long getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(long joinedAt) {
		this.joinedAt = joinedAt;
		update();
	}

	public long getTaxesAt() {
		return taxesAt;
	}

	public void setTaxesAt(long taxesAt) {
		this.taxesAt = taxesAt;
		update();
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.MEMBER_CACHE.putOrUpdate(this);
	}

	public enum LinkageType {
		REGION(1),
		SUBAREA(1 << 1);

		private final int value;

		LinkageType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
