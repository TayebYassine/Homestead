package tfagaming.projects.minecraft.homestead.managers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link Level} progression for regions.
 */
public final class LevelManager {
	private static final Random random = new Random();

	private LevelManager() {
	}

	/**
	 * Create a new level entry for a region.
	 * @param regionId The region ID
	 * @return The created Level, or {@code null} if one already exists.
	 */
	public static Level createLevel(long regionId) {
		if (getLevelByRegion(regionId) != null) {
			return null;
		}

		Level level = new Level(Homestead.getSnowflake().nextId(), regionId, 0, 0, 0, System.currentTimeMillis());
		Homestead.LEVEL_CACHE.putOrUpdate(level);
		return level;
	}

	/**
	 * Returns an immutable view of every loaded level.
	 * @return List of all levels.
	 */
	public static List<Level> getAll() {
		return Homestead.LEVEL_CACHE.getAll();
	}

	/**
	 * Returns all region IDs that have a level entry.
	 * @return List of region IDs.
	 */
	public static List<Long> getAllRegions() {
		return getAll().stream()
				.map(Level::getRegionId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Get level by region ID.
	 * @param regionId The region ID
	 * @return The Level, or {@code null} if not found.
	 */
	public static Level getLevelByRegion(long regionId) {
		for (Level level : getAll()) {
			if (level.getRegionId() == regionId) {
				return level;
			}
		}
		return null;
	}

	/**
	 * Retrieves the level with the exact ID, or null if none exists.
	 * @param id The level ID
	 * @return The Level, or {@code null}.
	 */
	public static Level findLevel(long id) {
		return Homestead.LEVEL_CACHE.get(id);
	}

	/**
	 * Get or create level for a region.
	 * @param regionId The region ID
	 * @return The existing or newly created Level.
	 */
	public static Level getOrCreateLevel(long regionId) {
		Level level = getLevelByRegion(regionId);
		if (level == null) {
			level = createLevel(regionId);
		}
		return level;
	}

	/**
	 * Returns the number of levels in the server.
	 * @return The level count.
	 */
	public static int getLevelCount() {
		return Homestead.LEVEL_CACHE.getAll().size();
	}

	/**
	 * Permanently deletes the specified level.
	 * @param id The level ID
	 */
	public static void deleteLevel(long id) {
		Homestead.LEVEL_CACHE.remove(id);
	}

	/**
	 * Delete level by region ID.
	 * @param regionId The region ID
	 */
	public static void deleteLevelByRegion(long regionId) {
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
	public static void addXp(long regionId, long amount) {
		Level level = getOrCreateLevel(regionId);
		level.addXp(amount);
		Homestead.LEVEL_CACHE.putOrUpdate(level);
	}

	/**
	 * Add XP as a percentage of the XP required for the next level.
	 * @param regionId The region ID
	 * @param percentage Percentage of next level XP (0.0–100.0)
	 */
	public static void addXpPercentage(long regionId, double percentage) {
		Level level = getOrCreateLevel(regionId);
		long nextLevelXp = level.getXpForNextLevel();
		long amount = (long) Math.floor(nextLevelXp * (percentage / 100.0));
		if (amount > 0) {
			level.addXp(amount);
			Homestead.LEVEL_CACHE.putOrUpdate(level);
		}
	}

	/**
	 * Multiplies the current XP progress by a factor (useful for boosters).
	 * @param regionId The region ID
	 * @param factor The multiplier (e.g., 2.0 for double)
	 */
	public static void multiplyXp(long regionId, double factor) {
		Level level = getLevelByRegion(regionId);
		if (level == null) return;

		long newXp = (long) Math.floor(level.getExperience() * factor);
		level.setXp(newXp);
		Homestead.LEVEL_CACHE.putOrUpdate(level);
	}

	/**
	 * Add random XP between min and max (inclusive).
	 * @param regionId The region ID
	 * @param min Minimum XP (can be double, will be floored)
	 * @param max Maximum XP (can be double, will be floored)
	 * @return The actual amount of XP added.
	 */
	public static long addRandomXp(long regionId, double min, double max) {
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
	 * @return The actual amount of XP added.
	 */
	public static long addRandomXp(long regionId, long min, long max) {
		return addRandomXp(regionId, (double) min, (double) max);
	}

	/**
	 * Remove XP from a region (won't decrease level, only progress).
	 * @param regionId The region ID
	 * @param amount Amount of XP to remove
	 */
	public static void removeXp(long regionId, long amount) {
		Level level = getLevelByRegion(regionId);
		if (level != null) {
			level.removeXp(amount);
			Homestead.LEVEL_CACHE.putOrUpdate(level);
		}
	}

	/**
	 * Set exact XP amount (triggers level re-calculation).
	 * @param regionId The region ID
	 * @param experience XP amount
	 */
	public static void setXp(long regionId, long experience) {
		Level level = getOrCreateLevel(regionId);
		level.setXp(experience);
		Homestead.LEVEL_CACHE.putOrUpdate(level);
	}

	/**
	 * Set exact level (resets progress to 0).
	 * @param regionId The region ID
	 * @param level Level to set
	 */
	public static void setLevel(long regionId, int level) {
		Level lvl = getOrCreateLevel(regionId);
		lvl.setLevel(level);
		Homestead.LEVEL_CACHE.putOrUpdate(lvl);
	}

	/**
	 * Grants multiple levels at once.
	 * @param regionId The region ID
	 * @param levels Number of levels to add
	 */
	public static void grantLevels(long regionId, int levels) {
		if (levels <= 0) return;
		Level level = getOrCreateLevel(regionId);
		level.setLevel(level.getLevel() + levels);
		Homestead.LEVEL_CACHE.putOrUpdate(level);
	}

	/**
	 * Removes levels from a region without going below 0.
	 * @param regionId The region ID
	 * @param levels Number of levels to remove
	 */
	public static void delevel(long regionId, int levels) {
		if (levels <= 0) return;
		Level level = getLevelByRegion(regionId);
		if (level == null) return;

		level.setLevel(Math.max(0, level.getLevel() - levels));
		Homestead.LEVEL_CACHE.putOrUpdate(level);
	}

	/**
	 * Reset level to 0.
	 * @param regionId The region ID
	 */
	public static void resetLevel(long regionId) {
		Level level = getLevelByRegion(regionId);
		if (level != null) {
			level.reset();
			Homestead.LEVEL_CACHE.putOrUpdate(level);
		}
	}

	/**
	 * Resets every level in the cache. Use with caution.
	 * @return The number of levels reset.
	 */
	public static int resetAllLevels() {
		int count = 0;
		for (Level level : getAll()) {
			level.reset();
			Homestead.LEVEL_CACHE.putOrUpdate(level);
			count++;
		}
		return count;
	}

	/**
	 * Returns the total accumulated XP (including spent on levels) for a region.
	 * @param regionId The region ID
	 * @return Total XP, or {@code 0} if no level exists.
	 */
	public static long getTotalXpOfRegion(long regionId) {
		Level level = getLevelByRegion(regionId);
		return level != null ? level.getTotalExperience() : 0L;
	}

	/**
	 * Returns the current progress percentage toward the next level.
	 * @param regionId The region ID
	 * @return Progress from 0.0 to 100.0, or {@code 0.0} if no level exists.
	 */
	public static double getLevelProgressPercentage(long regionId) {
		Level level = getLevelByRegion(regionId);
		return level != null ? level.getProgressPercentage() : 0.0;
	}

	/**
	 * Returns the remaining XP needed to reach the next level.
	 * @param regionId The region ID
	 * @return XP remaining, or {@code 0} if no level exists.
	 */
	public static long getXpUntilNextLevel(long regionId) {
		Level level = getLevelByRegion(regionId);
		return level != null ? level.getXpRemaining() : 0L;
	}

	/**
	 * Returns the XP required to reach a specific level from level 0.
	 * @param targetLevel The target level
	 * @return Total XP required.
	 */
	public static long getTotalXpForLevel(int targetLevel) {
		long total = 0;
		for (int i = 0; i < targetLevel; i++) {
			total += Level.getXpForLevel(i);
		}
		return total;
	}

	/**
	 * Calculates the level difference between two regions.
	 * @param regionIdA First region ID
	 * @param regionIdB Second region ID
	 * @return Positive if A > B, negative if A < B, 0 if equal or missing.
	 */
	public static int getLevelDifference(long regionIdA, long regionIdB) {
		Level a = getLevelByRegion(regionIdA);
		Level b = getLevelByRegion(regionIdB);
		if (a == null || b == null) return 0;
		return a.getLevel() - b.getLevel();
	}

	/**
	 * Returns the server-wide average region level.
	 * @return Average level, or {@code 0.0} if no levels exist.
	 */
	public static double getAverageLevel() {
		List<Level> all = getAll();
		if (all.isEmpty()) return 0.0;

		long sum = 0;
		for (Level level : all) {
			sum += level.getLevel();
		}
		return (double) sum / all.size();
	}

	/**
	 * Returns all region IDs at exactly the specified level.
	 * @param level The level to search for
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsAtLevel(int level) {
		return getAll().stream()
				.filter(l -> l.getLevel() == level)
				.map(Level::getRegionId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns all region IDs above the specified level threshold.
	 * @param minLevel The minimum level (exclusive)
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsAboveLevel(int minLevel) {
		return getAll().stream()
				.filter(l -> l.getLevel() > minLevel)
				.map(Level::getRegionId)
				.collect(Collectors.toList());
	}

	/**
	 * Finds the region with the highest level. In case of ties, the one with most XP wins.
	 * @return The highest level, or {@code null} if no levels exist.
	 */
	public static Level getHighestLevel() {
		return getAll().stream()
				.max(Comparator.comparingInt(Level::getLevel)
						.thenComparingLong(Level::getExperience))
				.orElse(null);
	}

	/**
	 * Finds the region ID with the highest level.
	 * @return The region ID, or {@code -1} if no levels exist.
	 */
	public static long getHighestLevelRegion() {
		Level highest = getHighestLevel();
		return highest != null ? highest.getRegionId() : -1L;
	}

	/**
	 * Returns a histogram of level distribution.
	 * @return Map of level -> count.
	 */
	public static Map<Integer, Integer> getLevelDistribution() {
		Map<Integer, Integer> distribution = new HashMap<>();
		for (Level level : getAll()) {
			distribution.merge(level.getLevel(), 1, Integer::sum);
		}
		return distribution;
	}

	/**
	 * Checks if a region has reached or exceeded a maximum level cap.
	 * @param regionId The region ID
	 * @param maxLevel The maximum allowed level
	 * @return {@code true} if at or above cap.
	 */
	public static boolean isMaxLevel(long regionId, int maxLevel) {
		Level level = getLevelByRegion(regionId);
		return level != null && level.getLevel() >= maxLevel;
	}

	/**
	 * Returns how many milliseconds ago the level entry was created.
	 * @param regionId The region ID
	 * @return Age in milliseconds, or {@code -1} if not found.
	 */
	public static long getLevelAge(long regionId) {
		Level level = getLevelByRegion(regionId);
		return level != null ? System.currentTimeMillis() - level.getCreatedAt() : -1L;
	}

	/**
	 * Get top levels sorted by level (desc), then by XP (desc).
	 * @param limit Maximum results
	 * @return Sorted list of top levels.
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
	 * @return 1-based rank, or {@code -1} if not found.
	 */
	public static int getRank(long regionId) {
		Level target = getLevelByRegion(regionId);
		if (target == null) return -1;

		int rank = 1;
		for (Level level : getAll()) {
			if (level.getUniqueId() == target.getUniqueId()) continue;

			if (level.getLevel() > target.getLevel()) {
				rank++;
			} else if (level.getLevel() == target.getLevel() &&
					level.getExperience() > target.getExperience()) {
				rank++;
			}
		}
		return rank;
	}

	/**
	 * Removes all level entries with invalid references:<br>
	 * - Regions that no longer exist
	 * @return Number of corrupted levels removed.
	 */
	public static int cleanupInvalidLevels() {
		List<Long> toRemove = new ArrayList<>();

		for (Level level : Homestead.LEVEL_CACHE.getAll()) {
			boolean invalidRegion = level.getRegion() == null;

			if (invalidRegion) {
				toRemove.add(level.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			Homestead.LEVEL_CACHE.remove(id);
		}
		return toRemove.size();
	}
}