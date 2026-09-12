package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a rating given by a player to a region.
 *
 * <p>Each player may rate a region once; subsequent ratings overwrite the
 * previous one.</p>
 */
public final class RegionRate {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private long regionId;
    private UUID playerId;
    private int rate;
    private long ratedAt;

    /**
     * Create a rating from a Bukkit player.
     *
     * @param regionId the region ID
     * @param player   the rating player
     * @param rate     the rating value
     */
    public RegionRate(long regionId, OfflinePlayer player, int rate) {
        this(regionId, player.getUniqueId(), rate, System.currentTimeMillis());
    }

    /**
     * Create a rating with a generated snowflake ID.
     *
     * @param regionId the region ID
     * @param playerId the player UUID
     * @param rate     the rating value
     * @param ratedAt  the rating timestamp
     */
    public RegionRate(long regionId, UUID playerId, int rate, long ratedAt) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.playerId = playerId;
        this.rate = rate;
        this.ratedAt = ratedAt;
    }

    /**
     * Create a rating from pre-existing data (deserialization).
     *
     * @param id       the snowflake ID
     * @param regionId the region ID
     * @param playerId the player UUID
     * @param rate     the rating value
     * @param ratedAt  the rating timestamp
     */
    public RegionRate(long id, long regionId, UUID playerId, int rate, long ratedAt) {
        this.id = id;
        this.regionId = regionId;
        this.playerId = playerId;
        this.rate = rate;
        this.ratedAt = ratedAt;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the rating ID
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
     * Get the rating player's UUID.
     *
     * @return the player UUID
     */
    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    /**
     * Set the rating player's UUID.
     *
     * @param playerId the new player UUID
     */
    public void setPlayerId(@NotNull UUID playerId) {
        this.playerId = playerId;
        update();
    }

    /**
     * Get the rating player as an offline player.
     *
     * @return the player, or {@code null} if not found
     */
    public @Nullable OfflinePlayer getPlayer() {
        if (INSTANCE == null) return null;

        return INSTANCE.getOfflinePlayerSync(playerId);
    }

    /**
     * Set the rating player.
     *
     * @param player the new player
     */
    public void setPlayer(@NotNull OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        update();
    }

    /**
     * Get the rating player's name safely. Returns {@code "?"} if not
     * found.
     *
     * @return the player's name
     */
    public @NotNull String getPlayerName() {
        OfflinePlayer player = getPlayer();

        return player == null || player.getName() == null ? "?" : player.getName();
    }

    /**
     * Get the rating value.
     *
     * @return the rating
     */
    public int getRate() {
        return rate;
    }

    /**
     * Set the rating value.
     *
     * @param rate the new rating
     */
    public void setRate(int rate) {
        this.rate = rate;
        update();
    }

    /**
     * Get the rating timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getRatedAt() {
        return ratedAt;
    }

    /**
     * Set the rating timestamp.
     *
     * @param ratedAt the new timestamp
     */
    public void setRatedAt(long ratedAt) {
        this.ratedAt = ratedAt;
        update();
    }

    private void update() {
        Homestead.RATE_CACHE.putOrUpdate(this);
    }
}
