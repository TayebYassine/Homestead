package tfagaming.projects.minecraft.homestead.structure;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public class Level {
    public UUID id;
    public UUID regionId;
    public int level;
    public long experience;
    public long totalExperience;
    public long createdAt;

    // MEE6 formula constants (configurable)
    public static double BASE = 5;
    public static double COEFFICIENT = 50;
    public static double CONSTANT = 100;

    public Level(UUID regionId) {
        this.id = UUID.randomUUID();
        this.regionId = regionId;
        this.level = 0; // MEE6 starts at 0
        this.experience = 0;
        this.totalExperience = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public Level(UUID id, UUID regionId, int level, long experience, long totalExperience, long createdAt) {
        this.id = id;
        this.regionId = regionId;
        this.level = level;
        this.experience = experience;
        this.totalExperience = totalExperience;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getUniqueId() {
        return id;
    }

    public UUID getRegionId() {
        return regionId;
    }

    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
        updateCache();
    }

    public int getLevel() {
        return level;
    }

    public long getExperience() {
        return experience;
    }

    public long getTotalExperience() {
        return totalExperience;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // MEE6 XP formula: ƒ(x) = base * (x²) + (coefficient * x) + constant
    public static long getXpForLevel(int level) {
        if (level < 0) return 0;
        double xp = BASE * Math.pow(level, 2) + (COEFFICIENT * level) + CONSTANT;
        return (long) Math.floor(xp);
    }

    // Total XP needed to reach current level from 0
    public long getTotalXpForCurrentLevel() {
        long total = 0;
        for (int i = 0; i < this.level; i++) {
            total += getXpForLevel(i);
        }
        return total;
    }

    // XP needed for next level
    public long getXpForNextLevel() {
        return getXpForLevel(this.level);
    }

    // XP progress in current level
    public long getXpProgress() {
        return this.experience;
    }

    // XP remaining to level up
    public long getXpRemaining() {
        long needed = getXpForNextLevel();
        return Math.max(0, needed - this.experience);
    }

    // Progress percentage (0.0 to 100.0)
    public double getProgressPercentage() {
        long needed = getXpForNextLevel();
        if (needed == 0) return 100.0;
        return (double) this.experience / needed * 100.0;
    }

    // Core XP addition
    public void addXp(long amount) {
        if (amount <= 0) return;

        this.experience += amount;
        this.totalExperience += amount;

        checkLevelUp();
        updateCache();
    }

    // Check and process level ups (handles multiple levels)
    private void checkLevelUp() {
        long needed = getXpForNextLevel();

        while (this.experience >= needed && needed > 0) {
            this.experience -= needed;
            this.level++;
            onLevelUp();
            needed = getXpForNextLevel();
        }
    }

    // Level up hook
    public void onLevelUp() {
        // Fire custom event, give rewards, etc.
    }

    // Setters for admin commands
    public void setLevel(int level) {
        this.level = Math.max(0, level);
        this.experience = 0;
        updateCache();
    }

    public void setXp(long experience) {
        this.experience = Math.max(0, experience);
        checkLevelUp();
        updateCache();
    }

    public void removeXp(long amount) {
        if (amount <= 0) return;

        this.experience = Math.max(0, this.experience - amount);
        // Note: totalExperience never decreases (lifetime stat)
        updateCache();
    }

    // Reset completely
    public void reset() {
        this.level = 0;
        this.experience = 0;
        this.totalExperience = 0;
        updateCache();
    }

    private void updateCache() {
        Homestead.levelsCache.putOrUpdate(this);
    }
}