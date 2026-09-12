package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player's membership within a region or sub-area.
 *
 * <p>A member is linked to either a {@link LinkageType#REGION} or a
 * {@link LinkageType#SUBAREA} and carries its own player and control
 * flag bitmasks, join timestamp, and tax tracking timestamp.</p>
 */
public final class RegionMember {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private final LinkageType linkageType;
    private UUID playerId;
    private long regionId = -1L;
    private long subAreaId = -1L;
    private long playerFlags = 0L;
    private long controlFlags = 0L;
    private long joinedAt;
    private long taxesAt;

    /**
     * Create a member from a Bukkit player.
     *
     * @param player    the player
     * @param type      the linkage type
     * @param linkageId the region or sub-area ID
     */
    public RegionMember(OfflinePlayer player, LinkageType type, long linkageId) {
        this(player.getUniqueId(), type, linkageId);
    }

    /**
     * Create a member with a generated snowflake ID.
     *
     * @param playerId  the player UUID
     * @param type      the linkage type
     * @param linkageId the region or sub-area ID
     */
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

    /**
     * Create a member from pre-existing data (deserialization).
     *
     * @param id           the snowflake ID
     * @param playerId     the player UUID
     * @param type         the linkage type
     * @param linkageId    the region or sub-area ID
     * @param playerFlags  the player flags
     * @param controlFlags the control flags
     * @param taxesAt      the tax tracking timestamp
     * @param joinedAt     the join timestamp
     */
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
     * Get the unique snowflake ID.
     *
     * @return the member ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the member's UUID.
     *
     * @return the player UUID
     */
    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    /**
     * Set the member's UUID.
     *
     * @param playerId the new player UUID
     */
    public void setPlayerId(@NotNull UUID playerId) {
        this.playerId = playerId;
        update();
    }

    /**
     * Get the member as an offline player.
     *
     * @return the player, or {@code null} if not found
     */
    public @Nullable OfflinePlayer getPlayer() {
        if (INSTANCE == null) return null;

        return INSTANCE.getOfflinePlayerSync(playerId);
    }

    /**
     * Set the member player.
     *
     * @param player the new player
     */
    public void setPlayer(@NotNull OfflinePlayer player) {
        this.playerId = player.getUniqueId();
        update();
    }

    /**
     * Get the member's name safely. Returns {@code "?"} if not found.
     *
     * @return the player's name
     */
    public @NotNull String getPlayerName() {
        OfflinePlayer player = getPlayer();

        return player == null || player.getName() == null ? "?" : player.getName();
    }

    /**
     * Get the linkage type (region or sub-area).
     *
     * @return the linkage type
     */
    public @NotNull LinkageType getLinkageType() {
        return linkageType;
    }

    /**
     * Get the region ID. Defaults to {@code -1} if linked to a sub-area.
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
     * Get the sub-area ID. Defaults to {@code -1} if linked to a region.
     *
     * @return the sub-area ID
     */
    public long getSubAreaId() {
        return subAreaId;
    }

    /**
     * Set the sub-area ID.
     *
     * @param subAreaId the new sub-area ID
     */
    public void setSubAreaId(long subAreaId) {
        this.subAreaId = subAreaId;
        update();
    }

    /**
     * Get the sub-area from the cache.
     *
     * @return the sub-area, or {@code null} if not found
     */
    public @Nullable SubArea getSubArea() {
        return SubAreaManager.findSubArea(subAreaId);
    }

    /**
     * Get the sub-area's name safely. Returns {@code "?"} if not found.
     *
     * @return the sub-area name
     */
    public @NotNull String getSubAreaName() {
        SubArea subArea = getSubArea();

        return subArea == null ? "?" : subArea.getName();
    }

    /**
     * Get the player flags bitmask.
     *
     * @return the player flags
     */
    public long getPlayerFlags() {
        return playerFlags;
    }

    /**
     * Set the player flags bitmask.
     *
     * @param playerFlags the new flags
     */
    public void setPlayerFlags(long playerFlags) {
        this.playerFlags = playerFlags;
        update();
    }

    /**
     * Get the control flags bitmask.
     *
     * @return the control flags
     */
    public long getControlFlags() {
        return controlFlags;
    }

    /**
     * Set the control flags bitmask.
     *
     * @param controlFlags the new flags
     */
    public void setControlFlags(long controlFlags) {
        this.controlFlags = controlFlags;
        update();
    }

    /**
     * Get the join timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getJoinedAt() {
        return joinedAt;
    }

    /**
     * Set the join timestamp.
     *
     * @param joinedAt the new timestamp
     */
    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
        update();
    }

    /**
     * Get the tax tracking timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getTaxesAt() {
        return taxesAt;
    }

    /**
     * Set the tax tracking timestamp.
     *
     * @param taxesAt the new timestamp
     */
    public void setTaxesAt(long taxesAt) {
        this.taxesAt = taxesAt;
        update();
    }

    private void update() {
        Homestead.MEMBER_CACHE.putOrUpdate(this);
    }

    /**
     * Identifies whether a member is linked to a region or a sub-area.
     */
    public enum LinkageType {

        /**
         * Member is linked to a region.
         */
        REGION(1),

        /**
         * Member is linked to a sub-area.
         */
        SUBAREA(1 << 1);

        private final int value;

        LinkageType(int value) {
            this.value = value;
        }

        /**
         * Get the numeric value of this linkage type.
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }
}
