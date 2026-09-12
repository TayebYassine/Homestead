package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a ban issued against a player for a specific region.
 *
 * <p>Bans are capped at 256 characters for the reason string. The ban
 * record persists until explicitly removed by the region owner.</p>
 */
public final class RegionBan {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private long regionId;
    private UUID playerId;
    private String reason;
    private long bannedAt;

    /**
     * Create a ban from a Bukkit player.
     *
     * @param regionId the region ID
     * @param player   the player to ban
     * @param reason   the ban reason
     */
    public RegionBan(long regionId, OfflinePlayer player, String reason) {
        this(regionId, player.getUniqueId(), reason, System.currentTimeMillis());
    }

    /**
     * Create a ban with a generated snowflake ID.
     *
     * @param regionId the region ID
     * @param playerId the player UUID
     * @param reason   the ban reason (truncated to 256 chars)
     * @param bannedAt the ban timestamp
     */
    public RegionBan(long regionId, UUID playerId, String reason, long bannedAt) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.playerId = playerId;
        this.reason = reason.length() > 256 ? reason.substring(0, 256) : reason;
        this.bannedAt = bannedAt;
    }

    /**
     * Create a ban from pre-existing data (deserialization).
     *
     * @param id       the snowflake ID
     * @param regionId the region ID
     * @param playerId the player UUID
     * @param reason   the ban reason
     * @param bannedAt the ban timestamp
     */
    public RegionBan(long id, long regionId, UUID playerId, String reason, long bannedAt) {
        this.id = id;
        this.regionId = regionId;
        this.playerId = playerId;
        this.reason = reason.length() > 256 ? reason.substring(0, 256) : reason;
        this.bannedAt = bannedAt;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the ban ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the region ID.
     *
     * @return the region ID
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Set the region ID.
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
     * Get the banned player's UUID.
     *
     * @return the player UUID
     */
    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    /**
     * Set the banned player's UUID.
     *
     * @param playerId the new player UUID
     */
    public void setPlayerId(@NotNull UUID playerId) {
        this.playerId = playerId;
        update();
    }

    /**
     * Get the banned player as an offline player.
     *
     * @return the player, or {@code null} if not found
     */
    public @Nullable OfflinePlayer getPlayer() {
        if (INSTANCE == null) return null;

        return INSTANCE.getOfflinePlayerSync(playerId);
    }

    /**
     * Set the banned player.
     *
     * @param player the new player
     */
    public void setPlayer(@NotNull OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        update();
    }

    /**
     * Get the banned player's name safely. Returns {@code "?"} if not
     * found.
     *
     * @return the player's name
     */
    public @NotNull String getPlayerName() {
        OfflinePlayer player = getPlayer();

        return player == null || player.getName() == null ? "?" : player.getName();
    }

    /**
     * Get the ban reason.
     *
     * @return the reason, or {@code null} if not provided
     */
    public @Nullable String getReason() {
        return reason;
    }

    /**
     * Set the ban reason. Truncated to 256 characters.
     *
     * @param reason the new reason, or {@code null} to clear
     */
    public void setReason(@Nullable String reason) {
        if (reason != null) {
            this.reason = reason.length() > 256 ? reason.substring(0, 256) : reason;
        } else {
            this.reason = null;
        }

        update();
    }

    /**
     * Get the ban timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getBannedAt() {
        return bannedAt;
    }

    /**
     * Set the ban timestamp.
     *
     * @param bannedAt the new timestamp
     */
    public void setBannedAt(long bannedAt) {
        this.bannedAt = bannedAt;
        update();
    }

    private void update() {
        Homestead.BAN_CACHE.putOrUpdate(this);
    }
}
