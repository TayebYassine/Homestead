package tfagaming.projects.minecraft.homestead.managers;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class LevelManager {
	private static final Random random = new Random();

	private LevelManager() {
	}

	/**
	 * Create a new level entry for a region.
	 * @param regionId The region ID
	 */
	public static Level createLevel(long regionId) {
		if (getLevelByRegion(regionId) != null) {
			return null;
		}

		Level level = new Level(Homestead.SNOWFLAKE.nextId(), regionId, 0, 0, 0, System.currentTimeMillis());
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
	 */
	public static Level findLevel(long id) {
		return Homestead.levelsCache.get(id);
	}

	/**
	 * Get or create level for a region.
	 * @param regionId The region ID
	 */
	public static Level getOrCreateLevel(long regionId) {
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
	public static void deleteLevel(long id) {
		Homestead.levelsCache.remove(id);
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
		Homestead.levelsCache.putOrUpdate(level);
	}

	/**
	 * Add random XP between min and max (inclusive).
	 * @param regionId The region ID
	 * @param min Minimum XP (can be double, will be floored)
	 * @param max Maximum XP (can be double, will be floored)
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
			Homestead.levelsCache.putOrUpdate(level);
		}
	}

	/**
	 * Set exact XP amount (triggers level fetch).
	 * @param regionId The region ID
	 * @param experience XP amount
	 */
	public static void setXp(long regionId, long experience) {
		Level level = getOrCreateLevel(regionId);
		level.setXp(experience);
		Homestead.levelsCache.putOrUpdate(level);
	}

	/**
	 * Set exact level (resets progress to 0).
	 * @param regionId The region ID
	 * @param level Level to set
	 */
	public static void setLevel(long regionId, int level) {
		Level lvl = getOrCreateLevel(regionId);
		lvl.setLevel(level);
		Homestead.levelsCache.putOrUpdate(lvl);
	}

	/**
	 * Reset level to 0.
	 * @param regionId The region ID
	 */
	public static void resetLevel(long regionId) {
		Level level = getLevelByRegion(regionId);
		if (level != null) {
			level.reset();
			Homestead.levelsCache.putOrUpdate(level);
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

	public static void cleanStartup() {
		Logger.debug("Cleaning up levels data...");

		List<Level> levelsToDelete = new ArrayList<>();
		int updated = 0;

		for (Level level : Homestead.levelsCache.getAll()) {
			if (RegionManager.findRegion(level.getRegionId()) == null) {
				levelsToDelete.add(level);
			}
		}

		for (Level level : levelsToDelete) {
			LevelManager.deleteLevel(level.getUniqueId());
			updated++;
		}

		if (updated == 0) {
			Logger.debug("No data corruption was found!");
		} else {
			Logger.debug(updated + " updates have been applied to levels data.");
		}
	}
}