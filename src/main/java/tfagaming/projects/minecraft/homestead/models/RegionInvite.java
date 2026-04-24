package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public final class RegionInvite {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private boolean autoUpdate = true;

	private long id;
	private long regionId;
	private UUID playerId;
	private long invitedAt;

	public RegionInvite(long regionId, OfflinePlayer player) {
		this(regionId, player.getUniqueId(), System.currentTimeMillis());
	}

	public RegionInvite(long regionId, UUID playerId, long invitedAt) {
		this.regionId = regionId;
		this.playerId = playerId;
		this.invitedAt = invitedAt;
	}

	public RegionInvite(long id, long regionId, UUID playerId, long invitedAt) {
		this.id = id;
		this.regionId = regionId;
		this.playerId = playerId;
		this.invitedAt = invitedAt;
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

	public long getInvitedAt() {
		return invitedAt;
	}

	public void setInvitedAt(long invitedAt) {
		this.invitedAt = invitedAt;
		update();
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.regionInviteCache.putOrUpdate(this);
	}
}