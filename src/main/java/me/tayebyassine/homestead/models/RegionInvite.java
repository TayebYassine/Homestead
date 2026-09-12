package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents an invitation to join a region.
 *
 * <p>Invites track the region, the invited player, and the timestamp
 * when the invite was issued.</p>
 */
public final class RegionInvite {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private long regionId;
    private UUID playerId;
    private long invitedAt;

    /**
     * Create an invite from a Bukkit player.
     *
     * @param regionId the region ID
     * @param player   the invited player
     */
    public RegionInvite(long regionId, OfflinePlayer player) {
        this(regionId, player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Create an invite with a generated snowflake ID.
     *
     * @param regionId  the region ID
     * @param playerId  the invited player UUID
     * @param invitedAt the invite timestamp
     */
    public RegionInvite(long regionId, UUID playerId, long invitedAt) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.playerId = playerId;
        this.invitedAt = invitedAt;
    }

    /**
     * Create an invitation from pre-existing data (deserialization).
     *
     * @param id        the snowflake ID
     * @param regionId  the region ID
     * @param playerId  the invited player UUID
     * @param invitedAt the invite timestamp
     */
    public RegionInvite(long id, long regionId, UUID playerId, long invitedAt) {
        this.id = id;
        this.regionId = regionId;
        this.playerId = playerId;
        this.invitedAt = invitedAt;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the invite ID
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
     * Get the invited player's UUID.
     *
     * @return the player UUID
     */
    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    /**
     * Set the invited player's UUID.
     *
     * @param playerId the new player UUID
     */
    public void setPlayerId(@NotNull UUID playerId) {
        this.playerId = playerId;
        update();
    }

    /**
     * Get the invited player as an offline player.
     *
     * @return the player, or {@code null} if not found
     */
    public @Nullable OfflinePlayer getPlayer() {
        if (INSTANCE == null) return null;

        return INSTANCE.getOfflinePlayerSync(playerId);
    }

    /**
     * Set the invited player.
     *
     * @param player the new player
     */
    public void setPlayer(@NotNull OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        update();
    }

    /**
     * Get the invited player's name safely. Returns {@code "?"} if not
     * found.
     *
     * @return the player's name
     */
    public @NotNull String getPlayerName() {
        OfflinePlayer player = getPlayer();

        return player == null || player.getName() == null ? "?" : player.getName();
    }

    /**
     * Get the invite timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getInvitedAt() {
        return invitedAt;
    }

    /**
     * Set the invite timestamp.
     *
     * @param invitedAt the new timestamp
     */
    public void setInvitedAt(long invitedAt) {
        this.invitedAt = invitedAt;
        update();
    }

    private void update() {
        Homestead.INVITE_CACHE.putOrUpdate(this);
    }
}
