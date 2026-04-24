package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public final class RegionRate {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private boolean autoUpdate = true;

	private final long id;
	private long regionId;
	private UUID playerId;
	private int rate;
	private long ratedAt;

	public RegionRate(long regionId, OfflinePlayer player, int rate) {
		this(regionId, player.getUniqueId(), rate, System.currentTimeMillis());
	}

	public RegionRate(long regionId, UUID playerId, int rate, long ratedAt) {
		this.id = Homestead.SNOWFLAKE.nextId();
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

	public void setRegionId(long regionId) {
		this.regionId = regionId;
		update();
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

	public void setPlayer(OfflinePlayer player) {
		this.playerId = player.getUniqueId();
		update();
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
		if (!autoUpdate) return;

		Homestead.regionRateCache.putOrUpdate(this);
	}
}