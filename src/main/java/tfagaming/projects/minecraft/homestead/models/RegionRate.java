package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import java.util.UUID;

public final class RegionRate {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private final long id;
	private long regionId;
	private UUID playerId;
	private int rate;
	private long ratedAt;

	public RegionRate(long regionId, OfflinePlayer player, int rate) {
		this(regionId, player.getUniqueId(), rate, System.currentTimeMillis());
	}

	public RegionRate(long regionId, UUID playerId, int rate, long ratedAt) {
		this.id = Homestead.getSnowflake().nextId();
		this.regionId = regionId;
		this.playerId = playerId;
		this.rate = rate;
		this.ratedAt = ratedAt;
	}

	public RegionRate(long id, long regionId, UUID playerId, int rate, long ratedAt) {
		this.id = id;
		this.regionId = regionId;
		this.playerId = playerId;
		this.rate = rate;
		this.ratedAt = ratedAt;
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

	public @NotNull UUID getPlayerId() {
		return playerId;
	}

	public void setPlayerId(@NotNull UUID playerId) {
		this.playerId = playerId;
		update();
	}

	public @Nullable OfflinePlayer getPlayer() {
		if (INSTANCE == null) return null;

		return INSTANCE.getOfflinePlayerSync(playerId);
	}

	public void setPlayer(@NotNull OfflinePlayer player) {
		this.playerId = player.getUniqueId();
		update();
	}

	/**
	 * Returns the player's name safely. If the player was not found by their ID, it will
	 * return {@code "?"} instead.
	 * @return The player's name if found, {@code "?"} otherwise.
	 */
	public @NotNull String getPlayerName() {
		OfflinePlayer player = getPlayer();

		return player == null || player.getName() == null ? "?" : player.getName();
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
		update();
	}

	public long getRatedAt() {
		return ratedAt;
	}

	public void setRatedAt(long ratedAt) {
		this.ratedAt = ratedAt;
		update();
	}

	private void update() {
		Homestead.RATE_CACHE.putOrUpdate(this);
	}
}