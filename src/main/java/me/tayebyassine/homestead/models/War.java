package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a war between multiple regions.
 *
 * <p>A war tracks participating regions by ID, a shared prize pool, and
 * a start timestamp. When only one region remains, {@link #getWinner()}
 * returns it as the victor.
 * </p>
 */
public final class War {

    private final long id;
    private final List<Long> regionIds;
    private String name;
    private String displayName;
    private String description;
    private double prize;
    private long startedAt;
    private WagerType wagerType;
    private int killsToWin;
    private int attackerKills;
    private int defenderKills;
    private long timeout;

    /**
     * Create a new war with a generated snowflake ID.
     *
     * @param name the war name
     */
    public War(String name) {
        this.id = Homestead.getSnowflake().nextId();
        this.name = name;
        this.displayName = name;
        this.description = "";
        this.regionIds = new ArrayList<>();
        this.prize = 0.0;
        this.startedAt = System.currentTimeMillis();
        this.wagerType = WagerType.MONEY;
        this.killsToWin = 0;
        this.attackerKills = 0;
        this.defenderKills = 0;
        this.timeout = 0;
    }

    /**
     * Create a new war with initial participating regions.
     *
     * @param name      the war name
     * @param regionIds the participating region IDs
     */
    public War(String name, List<Long> regionIds) {
        this(name);
        this.regionIds.addAll(regionIds);
    }

    /**
     * Create a war from pre-existing data (deserialisation).
     *
     * @param id            the snowflake ID
     * @param name          the war name
     * @param displayName   the display name
     * @param description   the description
     * @param regionIds     the participating region IDs
     * @param prize         the prize pool
     * @param startedAt     the start timestamp
     * @param wagerType     the wager type
     * @param killsToWin    kills needed to win (ownership wars)
     * @param attackerKills attacker's current kill count
     * @param defenderKills defender's current kill count
     * @param timeout       timeout timestamp (0 = no timeout)
     */
    public War(long id, String name, String displayName, String description, List<Long> regionIds,
               double prize, long startedAt, WagerType wagerType, int killsToWin,
               int attackerKills, int defenderKills, long timeout) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.regionIds = new ArrayList<>(regionIds);
        this.prize = prize;
        this.startedAt = startedAt;
        this.wagerType = wagerType != null ? wagerType : WagerType.MONEY;
        this.killsToWin = killsToWin;
        this.attackerKills = attackerKills;
        this.defenderKills = defenderKills;
        this.timeout = timeout;
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the war ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the war name.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Set the war name.
     *
     * @param name the new name
     */
    public void setName(@NotNull String name) {
        this.name = name;
        update();
    }

    /**
     * Get the display name. Falls back to the name if not set.
     *
     * @return the display name
     */
    public @NotNull String getDisplayName() {
        return displayName == null ? name : displayName;
    }

    /**
     * Set the display name.
     *
     * @param displayName the new display name, or {@code null} to clear
     */
    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
        update();
    }

    /**
     * Get the description.
     *
     * @return the description, or {@code null} if not set
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * Set the description.
     *
     * @param description the new description, or {@code null} to clear
     */
    public void setDescription(@Nullable String description) {
        this.description = description;
        update();
    }

    /**
     * Get a copy of the participating region IDs.
     *
     * @return a list of region IDs
     */
    public List<Long> getRegionIds() {
        return new ArrayList<>(regionIds);
    }

    /**
     * Replace all participating region IDs.
     *
     * @param regionIds the new list of region IDs
     */
    public void setRegionIds(List<Long> regionIds) {
        this.regionIds.clear();
        this.regionIds.addAll(regionIds);
        update();
    }

    /**
     * Get all participating regions from the cache.
     *
     * @return a list of resolved regions (missing IDs are silently
     * omitted)
     */
    public List<Region> getRegions() {
        List<Region> regions = new ArrayList<>();

        for (long id : getRegionIds()) {
            Region region = RegionManager.findRegion(id);

            if (region != null) regions.add(region);
        }

        return regions;
    }

    /**
     * Add a region to the war. Ignored if already present.
     *
     * @param regionId the region ID to add
     */
    public void addRegionId(long regionId) {
        if (!this.regionIds.contains(regionId)) {
            this.regionIds.add(regionId);
            update();
        }
    }

    /**
     * Remove a region from the war.
     *
     * @param regionId the region ID to remove
     */
    public void removeRegionId(long regionId) {
        if (this.regionIds.remove(regionId)) {
            update();
        }
    }

    /**
     * Get the prize pool.
     *
     * @return the prize amount
     */
    public double getPrize() {
        return prize;
    }

    /**
     * Set the prize pool.
     *
     * @param prize the new prize amount
     */
    public void setPrize(double prize) {
        this.prize = prize;
        update();
    }

    /**
     * Get the start timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getStartedAt() {
        return startedAt;
    }

    /**
     * Set the start timestamp.
     *
     * @param startedAt the new timestamp
     */
    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
        update();
    }

    /**
     * Get the wager type.
     *
     * @return the wager type
     */
    public @NotNull WagerType getWagerType() {
        return wagerType;
    }

    /**
     * Set the wager type.
     *
     * @param wagerType the new wager type
     */
    public void setWagerType(@NotNull WagerType wagerType) {
        this.wagerType = wagerType;
        update();
    }

    /**
     * Get the number of kills needed to win an ownership war.
     *
     * @return kills to win
     */
    public int getKillsToWin() {
        return killsToWin;
    }

    /**
     * Set the number of kills needed to win.
     *
     * @param killsToWin the kills threshold
     */
    public void setKillsToWin(int killsToWin) {
        this.killsToWin = killsToWin;
        update();
    }

    /**
     * Get the attacker's current kill count.
     *
     * @return attacker kills
     */
    public int getAttackerKills() {
        return attackerKills;
    }

    /**
     * Set the attacker's kill count.
     *
     * @param attackerKills the kill count
     */
    public void setAttackerKills(int attackerKills) {
        this.attackerKills = attackerKills;
        update();
    }

    /**
     * Get the defender's current kill count.
     *
     * @return defender kills
     */
    public int getDefenderKills() {
        return defenderKills;
    }

    /**
     * Set the defender's kill count.
     *
     * @param defenderKills the kill count
     */
    public void setDefenderKills(int defenderKills) {
        this.defenderKills = defenderKills;
        update();
    }

    /**
     * Get the timeout timestamp for ownership wars.
     *
     * @return timeout epoch-millis, or 0 if no timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout timestamp.
     *
     * @param timeout the timeout epoch-millis, or 0 to clear
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
        update();
    }

    /**
     * Check if this war has timed out.
     *
     * @return {@code true} if timeout is set and has passed
     */
    public boolean hasTimedOut() {
        return timeout > 0 && System.currentTimeMillis() >= timeout;
    }

    /**
     * Get the winning region. A region wins when it is the last one
     * remaining.
     *
     * @return the winning region, or {@code null} if the war is still in
     * progress
     */
    public @Nullable Region getWinner() {
        if (regionIds.size() == 1) {
            return Homestead.REGION_CACHE.get(regionIds.getFirst());
        }
        return null;
    }

    private void update() {
        Homestead.WAR_CACHE.putOrUpdate(this);
    }

    /**
     * The type of wager for a war.
     */
    public enum WagerType {
        MONEY, OWNERSHIP
    }
}
