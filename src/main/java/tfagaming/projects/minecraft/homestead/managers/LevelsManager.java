package tfagaming.projects.minecraft.homestead.managers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class LevelsManager {
    private static final Random random = new Random();

    private LevelsManager() {
    }

    /**
     * Create a new level entry for a region.
     * @param regionId The region ID
     */
    public static Level createLevel(UUID regionId) {
        if (getLevelByRegion(regionId) != null) {
            return null;
        }

        Level level = new Level(regionId);

        Homestead.levelsCache.putOrUpdate(level);

        return level;
    }

    /**
     * Returns an immutable view of every loaded levels.
     */
    public static List<Level> getAll() {
        return Homestead.levelsCache.getAll();
    }

    /**
     * Get level by region ID.
     * @param regionId The region ID
     */
    public static Level getLevelByRegion(UUID regionId) {
        for (Level level : getAll()) {
            if (level.getRegionId().equals(regionId)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Retrieves the level with the exact UUID, or null if none exists.
     * @param id The level ID
     */
    public static Level findLevel(UUID id) {
        for (Level level : getAll()) {
            if (level.getUniqueId().equals(id)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Get or create level for a region.
     * @param regionId The region ID
     */
    public static Level getOrCreateLevel(UUID regionId) {
        Level level = getLevelByRegion(regionId);
        if (level == null) {
            level = createLevel(regionId);
        }
        return level;
    }

    /**
     * Permanently deletes the specified level.
     * @param id The level ID
     */
    public static void deleteLevel(UUID id) {
        Level level = findLevel(id);
        if (level == null) {
            return;
        }
        Homestead.levelsCache.remove(id);
    }

    /**
     * Delete level by region ID.
     * @param regionId The region ID
     */
    public static void deleteLevelByRegion(UUID regionId) {
        Level level = getLevelByRegion(regionId);
        if (level != null) {
            deleteLevel(level.getUniqueId());
        }
    }

    /**
     * Add fixed XP to a region.
     * @param regionId The region ID
     * @param amount Amount of XP to add
     */
    public static void addXp(UUID regionId, long amount) {
        Level level = getOrCreateLevel(regionId);
        level.addXp(amount);
    }

    /**
     * Add random XP between min and max (inclusive).
     * @param regionId The region ID
     * @param min Minimum XP (can be double, will be floored)
     * @param max Maximum XP (can be double, will be floored)
     */
    public static long addRandomXp(UUID regionId, double min, double max) {
        long minLong = (long) Math.floor(min);
        long maxLong = (long) Math.floor(max);

        if (minLong > maxLong) {
            long temp = minLong;
            minLong = maxLong;
            maxLong = temp;
        }

        long range = maxLong - minLong + 1;
        long amount = minLong + (long) (random.nextDouble() * range);

        addXp(regionId, amount);

        return amount;
    }

    /**
     * Add random XP with integer bounds.
     * @param regionId The region ID
     * @param min Minimum XP
     * @param max Maximum XP
     */
    public static long addRandomXp(UUID regionId, long min, long max) {
        return addRandomXp(regionId, (double) min, (double) max);
    }

    /**
     * Remove XP from a region (won't decrease level, only progress).
     * @param regionId The region ID
     * @param amount Amount of XP to remove
     */
    public static void removeXp(UUID regionId, long amount) {
        Level level = getLevelByRegion(regionId);
        if (level != null) {
            level.removeXp(amount);
        }
    }

    /**
     * Set exact XP amount (triggers level check).
     * @param regionId The region ID
     * @param experience XP amount
     */
    public static void setXp(UUID regionId, long experience) {
        Level level = getOrCreateLevel(regionId);
        level.setXp(experience);
    }

    /**
     * Set exact level (resets progress to 0).
     * @param regionId The region ID
     * @param level Level to set
     */
    public static void setLevel(UUID regionId, int level) {
        Level lvl = getOrCreateLevel(regionId);
        lvl.setLevel(level);
    }

    /**
     * Reset level to 0.
     * @param regionId The region ID
     */
    public static void resetLevel(UUID regionId) {
        Level level = getLevelByRegion(regionId);
        if (level != null) {
            level.reset();
        }
    }

    /**
     * Get top levels sorted by level (desc), then by XP (desc).
     * @param limit Maximum results
     */
    public static List<Level> getTopLevels(int limit) {
        List<Level> sorted = new ArrayList<>(getAll());
        sorted.sort((a, b) -> {
            if (a.getLevel() != b.getLevel()) {
                return Integer.compare(b.getLevel(), a.getLevel());
            }
            return Long.compare(b.getExperience(), a.getExperience());
        });
        return sorted.size() > limit ? sorted.subList(0, limit) : sorted;
    }

    /**
     * Get region's rank on leaderboard.
     * @param regionId The region ID
     */
    public static int getRank(UUID regionId) {
        Level target = getLevelByRegion(regionId);
        if (target == null) return -1;

        int rank = 1;
        for (Level level : getAll()) {
            if (level.getUniqueId().equals(target.getUniqueId())) continue;

            if (level.getLevel() > target.getLevel()) {
                rank++;
            } else if (level.getLevel() == target.getLevel() &&
                    level.getExperience() > target.getExperience()) {
                rank++;
            }
        }
        return rank;
    }

    public static void cleanStartup() {
        Logger.warning("Cleaning up levels data...");

        int updated = 0;

        for (Level level : Homestead.levelsCache.getAll()) {
            if (RegionsManager.findRegion(level.getRegionId()) == null) {
                LevelsManager.deleteLevel(level.getUniqueId());
                updated++;
            }
        }

        if (updated == 0) {
            Logger.info("No data corruption was found!");
        } else {
            Logger.info(updated + " updates have been applied to levels data.");
        }
    }
}