package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public final class RegionBan {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private boolean autoUpdate = true;

	private final long id;
	private long regionId;
	private UUID playerId;
	private String reason;
	private long bannedAt;

	public RegionBan(long regionId, OfflinePlayer player, String reason) {
		this(regionId, player.getUniqueId(), reason, System.currentTimeMillis());
	}

	public RegionBan(long regionId, UUID playerId, String reason, long bannedAt) {
		this.id = Homestead.SNOWFLAKE.nextId();
		this.regionId = regionId;
		this.playerId = playerId;
		this.reason = reason;
		this.bannedAt = bannedAt;
	}

	public RegionBan(long id, long regionId, UUID playerId, String reason, long bannedAt) {
		this.id = id;
		this.regionId = regionId;
		this.playerId = playerId;
		this.reason = reason;
		this.bannedAt = bannedAt;
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

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
		update();
	}

	public long getBannedAt() {
		return bannedAt;
	}

	public void setBannedAt(long bannedAt) {
		this.bannedAt = bannedAt;
		update();
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.regionBanCache.putOrUpdate(this);
	}
}