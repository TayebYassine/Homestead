package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks the level and experience of a region.
 *
 * <p>Regions accumulate experience through various actions and level up
 * according to a quadratic formula:
 * {@code BASE * level^2 + COEFFICIENT * level + CONSTANT}.</p>
 *
 * <p>When a level-up occurs the region owner receives a notification if
 * they are online.</p>
 */
public final class Level {

    /**
     * Quadratic coefficient for the XP formula.
     */
    public static final double BASE = 5;

    /**
     * Linear coefficient for the XP formula.
     */
    public static final double COEFFICIENT = 50;

    /**
     * Constant term for the XP formula.
     */
    public static final double CONSTANT = 100;

    private final long id;
    private final long createdAt;
    private long regionId = -1L;
    private int level = 0;
    private long experience = 0L;
    private long totalExperience = 0L;

    /**
     * Create a new level tracker with a generated snowflake ID.
     *
     * @param regionId the owning region ID
     */
    public Level(long regionId) {
        this.id = Homestead.getSnowflake().nextId();
        this.regionId = regionId;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Create a level tracker from pre-existing data (deserialization).
     *
     * @param id              the snowflake ID
     * @param regionId        the owning region ID
     * @param level           the current level
     * @param experience      the current XP within the level
     * @param totalExperience the lifetime XP earned
     * @param createdAt       the creation timestamp
     */
    public Level(long id, long regionId, int level, long experience, long totalExperience, long createdAt) {
        this.id = id;
        this.regionId = regionId;
        this.level = level;
        this.experience = experience;
        this.totalExperience = totalExperience;
        this.createdAt = createdAt;
    }

    /**
     * Calculate the XP required to reach a given level.
     *
     * @param level the target level
     * @return the XP needed, or {@code 0} for negative levels
     */
    public static long getXpForLevel(int level) {
        if (level < 0) return 0;
        double xp = BASE * Math.pow(level, 2) + (COEFFICIENT * level) + CONSTANT;
        return (long) Math.floor(xp);
    }

    /**
     * Get the unique snowflake ID.
     *
     * @return the level ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the creation timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the owning region ID.
     *
     * @return the region ID
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Set the owning region ID.
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
    public String getRegionName() {
        Region region = getRegion();

        return region == null ? "?" : region.getName();
    }

    /**
     * Get the current level.
     *
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set the level directly. Resets the current experience to zero.
     *
     * @param level the new level (clamped to &ge; 0)
     */
    public void setLevel(int level) {
        this.level = Math.max(0, level);
        this.experience = 0;
        update();
    }

    /**
     * Get the current experience within the level.
     *
     * @return the XP
     */
    public long getExperience() {
        return experience;
    }

    /**
     * Get the lifetime experience earned.
     *
     * @return the total XP
     */
    public long getTotalExperience() {
        return totalExperience;
    }

    /**
     * Get the total XP required to reach the current level from level 0.
     *
     * @return the cumulative XP
     */
    public long getTotalXpForCurrentLevel() {
        long total = 0;
        for (int i = 0; i < this.level; i++) {
            total += getXpForLevel(i);
        }
        return total;
    }

    /**
     * Get the XP required for the next level-up.
     *
     * @return the XP needed
     */
    public long getXpForNextLevel() {
        return getXpForLevel(this.level);
    }

    /**
     * Get the XP progress toward the next level.
     *
     * @return the current XP within the level
     */
    public long getXpProgress() {
        return this.experience;
    }

    /**
     * Get the XP remaining until the next level.
     *
     * @return the remaining XP
     */
    public long getXpRemaining() {
        long needed = getXpForNextLevel();
        return Math.max(0, needed - this.experience);
    }

    /**
     * Get the progress percentage toward the next level.
     *
     * @return the percentage (0.0 &ndash; 100.0)
     */
    public double getProgressPercentage() {
        long needed = getXpForNextLevel();
        if (needed == 0) return 100.0;
        return (double) this.experience / needed * 100.0;
    }

    /**
     * Add experience. Triggers level-up checks.
     *
     * @param amount the XP to add (ignored if non-positive)
     */
    public void addXp(long amount) {
        if (amount <= 0) return;

        this.experience += amount;
        this.totalExperience += amount;

        checkLevelUp();
        update();
    }

    /**
     * Set the experience directly. Triggers level-up checks.
     *
     * @param experience the new XP value
     */
    public void setXp(long experience) {
        this.experience = Math.max(0, experience);
        checkLevelUp();
        update();
    }

    /**
     * Remove experience. Never goes below zero.
     *
     * @param amount the XP to remove (ignored if non-positive)
     */
    public void removeXp(long amount) {
        if (amount <= 0) return;

        this.experience = Math.max(0, this.experience - amount);
        update();
    }

    /**
     * Reset the level to 0 and clear all experience.
     */
    public void reset() {
        this.level = 0;
        this.experience = 0;
        this.totalExperience = 0;
        update();
    }

    private void checkLevelUp() {
        long needed = getXpForNextLevel();

        while (this.experience >= needed && needed > 0) {
            this.experience -= needed;
            this.level++;
            onLevelUp();
            needed = getXpForNextLevel();
        }
    }

    private void onLevelUp() {
        Region region = Homestead.REGION_CACHE.get(this.regionId);

        if (region == null) return;

        if (region.getOwner() != null && region.getOwner().isOnline()) {
            Player owner = (Player) region.getOwner();

            Messages.send(owner, "common.level_up", this.level);
        }
    }

    private void update() {
        Homestead.LEVEL_CACHE.putOrUpdate(this);
    }
}
