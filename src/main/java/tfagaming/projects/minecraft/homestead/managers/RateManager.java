package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionRate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionRate}.
 */
public final class RateManager {
	private RateManager() {}

	/**
	 * Retrieves a specific rating by its unique ID.
	 * @param id The rating ID
	 * @return The {@link RegionRate}, or {@code null} if not found.
	 */
	public static RegionRate getRate(long id) {
		return Homestead.RATE_CACHE.get(id);
	}

	/**
	 * Returns all ratings for a specific region.
	 * @param region The region
	 * @return List of ratings.
	 */
	public static List<RegionRate> getRatesOfRegion(Region region) {
		return getRatesOfRegion(region.getUniqueId());
	}

	/**
	 * Returns all ratings for a specific region.
	 * @param regionId The region ID
	 * @return List of ratings.
	 */
	public static List<RegionRate> getRatesOfRegion(long regionId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getRegionId() == regionId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of ratings in the server.
	 * @return Rating count.
	 */
	public static int getRateCount() {
		return Homestead.RATE_CACHE.getAll().size();
	}

	/**
	 * Returns the number of ratings for a region.
	 * @param region The region
	 * @return Rating count.
	 */
	public static int getRateCount(Region region) {
		return getRateCount(region.getUniqueId());
	}

	/**
	 * Returns the number of ratings for a region.
	 * @param regionId The region ID
	 * @return Rating count.
	 */
	public static int getRateCount(long regionId) {
		return (int) Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getRegionId() == regionId)
				.count();
	}

	/**
	 * Returns the sum of all rating scores for a region.
	 * @param region The region
	 * @return Total score.
	 */
	public static int getTotalScore(Region region) {
		return getTotalScore(region.getUniqueId());
	}

	/**
	 * Returns the sum of all rating scores for a region.
	 * @param regionId The region ID
	 * @return Total score.
	 */
	public static int getTotalScore(long regionId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getRegionId() == regionId)
				.mapToInt(RegionRate::getRate)
				.sum();
	}

	/**
	 * Add a rate score from a player to a region.
	 * @param region The region
	 * @param player The player
	 * @param score The score; ranging from 0 to 5
	 */
	public static void rateRegion(Region region, OfflinePlayer player, int score) {
		rateRegion(region.getUniqueId(), player, score);
	}

	/**
	 * Add a rate score from a player to a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @param score The score; ranging from 0 to 5
	 */
	public static void rateRegion(long regionId, OfflinePlayer player, int score) {
		RegionRate rate = Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getRegionId() == regionId && r.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(new RegionRate(regionId, player, score));

		rate.setRate(score);

		Homestead.RATE_CACHE.putOrUpdate(rate);
	}

	/**
	 * Updates an existing rating or creates a new one.
	 * @param player The player
	 * @param region The region
	 * @param score The new score
	 * @return {@code true} if an existing rating was updated, {@code false} if a new one was created.
	 */
	public static boolean updateRating(OfflinePlayer player, Region region, int score) {
		return updateRating(player, region.getUniqueId(), score);
	}

	/**
	 * Updates an existing rating or creates a new one.
	 * @param player The player
	 * @param regionId The region ID
	 * @param score The new score
	 * @return {@code true} if an existing rating was updated, {@code false} if a new one was created.
	 */
	public static boolean updateRating(OfflinePlayer player, long regionId, int score) {
		boolean existed = hasRatedRegion(player, regionId);
		rateRegion(regionId, player, score);
		return existed;
	}

	/**
	 * Get the average rating of all scores submitted by players.
	 * @param region The region
	 * @return Average rating, or {@code 0.0} if no ratings.
	 */
	public static double getAverageRating(Region region) {
		return getAverageRating(region.getUniqueId());
	}

	/**
	 * Get the average rating of all scores submitted by players.
	 * @param regionId The region ID
	 * @return Average rating, or {@code 0.0} if no ratings.
	 */
	public static double getAverageRating(long regionId) {
		List<RegionRate> rates = getRatesOfRegion(regionId);

		if (rates.isEmpty()) return 0.0;

		return rates.stream().mapToInt(RegionRate::getRate).average().orElse(0.0);
	}

	/**
	 * Returns the average rating rounded to the nearest integer (useful for star displays).
	 * @param region The region
	 * @return Rounded rating, or {@code 0} if no ratings.
	 */
	public static int getAverageRatingRounded(Region region) {
		return getAverageRatingRounded(region.getUniqueId());
	}

	/**
	 * Returns the average rating rounded to the nearest integer (useful for star displays).
	 * @param regionId The region ID
	 * @return Rounded rating, or {@code 0} if no ratings.
	 */
	public static int getAverageRatingRounded(long regionId) {
		return (int) Math.round(getAverageRating(regionId));
	}

	/**
	 * Returns the mode (most common rating) for a region.
	 * @param region The region
	 * @return Mode score, or {@code 0} if no ratings.
	 */
	public static int getModeRating(Region region) {
		return getModeRating(region.getUniqueId());
	}

	/**
	 * Returns the mode (most common rating) for a region.
	 * @param regionId The region ID
	 * @return Mode score, or {@code 0} if no ratings.
	 */
	public static int getModeRating(long regionId) {
		Map<Integer, Integer> frequency = getRatingDistribution(regionId);

		int mode = 0;
		int maxCount = 0;
		for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				mode = entry.getKey();
			}
		}
		return mode;
	}

	/**
	 * Returns a distribution map of ratings (score -> count) for a region.
	 * @param region The region
	 * @return Map of score to count.
	 */
	public static Map<Integer, Integer> getRatingDistribution(Region region) {
		return getRatingDistribution(region.getUniqueId());
	}

	/**
	 * Returns a distribution map of ratings (score -> count) for a region.
	 * @param regionId The region ID
	 * @return Map of score to count.
	 */
	public static Map<Integer, Integer> getRatingDistribution(long regionId) {
		Map<Integer, Integer> distribution = new HashMap<>();
		for (RegionRate rate : getRatesOfRegion(regionId)) {
			distribution.merge(rate.getRate(), 1, Integer::sum);
		}
		return distribution;
	}

	/**
	 * Returns the percentage of ratings that match a specific score.
	 * @param region The region
	 * @param score The score to check
	 * @return Percentage from 0.0 to 100.0.
	 */
	public static double getRatingPercentage(Region region, int score) {
		return getRatingPercentage(region.getUniqueId(), score);
	}

	/**
	 * Returns the percentage of ratings that match a specific score.
	 * @param regionId The region ID
	 * @param score The score to check
	 * @return Percentage from 0.0 to 100.0.
	 */
	public static double getRatingPercentage(long regionId, int score) {
		int total = getRateCount(regionId);
		if (total == 0) return 0.0;
		int matching = getRatingDistribution(regionId).getOrDefault(score, 0);
		return (matching * 100.0) / total;
	}

	/**
	 * Returns the top-rated regions, sorted by average rating descending.
	 * @param limit Maximum number of results
	 * @return List of top-rated regions.
	 */
	public static List<Region> getTopRatedRegions(int limit) {
		return RegionManager.getAll().stream()
				.sorted(Comparator.<Region>comparingDouble(r -> getAverageRating(r.getUniqueId())).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the lowest-rated regions, sorted by average rating ascending.
	 * @param limit Maximum number of results
	 * @return List of lowest-rated regions.
	 */
	public static List<Region> getLowestRatedRegions(int limit) {
		return RegionManager.getAll().stream()
				.sorted(Comparator.comparingDouble(r -> getAverageRating(r.getUniqueId())))
				.limit(limit)
				.collect(Collectors.toList());
	}

	/**
	 * Returns all regions that have not received any ratings.
	 * @return List of unrated regions.
	 */
	public static List<Region> getUnratedRegions() {
		return RegionManager.getAll().stream()
				.filter(r -> getRateCount(r.getUniqueId()) == 0)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the average rating a specific player gives across all regions.
	 * @param player The player
	 * @return Average score given, or {@code 0.0} if no ratings.
	 */
	public static double getPlayerAverageRating(OfflinePlayer player) {
		return getPlayerAverageRating(player.getUniqueId());
	}

	/**
	 * Returns the average rating a specific player gives across all regions.
	 * @param playerId The player UUID
	 * @return Average score given, or {@code 0.0} if no ratings.
	 */
	public static double getPlayerAverageRating(UUID playerId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getPlayerId().equals(playerId))
				.mapToInt(RegionRate::getRate)
				.average()
				.orElse(0.0);
	}

	/**
	 * Returns all region IDs that a player has rated.
	 * @param player The player
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsRatedByPlayer(OfflinePlayer player) {
		return getRegionsRatedByPlayer(player.getUniqueId());
	}

	/**
	 * Returns all region IDs that a player has rated.
	 * @param playerId The player UUID
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsRatedByPlayer(UUID playerId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getPlayerId().equals(playerId))
				.map(RegionRate::getRegionId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Checks if a player has rated a region.
	 * @param player The player
	 * @param region The region
	 * @return {@code true} if the player has rated the region.
	 */
	public static boolean hasRatedRegion(OfflinePlayer player, Region region) {
		return hasRatedRegion(player, region.getUniqueId());
	}

	/**
	 * Checks if a player has rated a region.
	 * @param player The player
	 * @param regionId The region ID
	 * @return {@code true} if the player has rated the region.
	 */
	public static boolean hasRatedRegion(OfflinePlayer player, long regionId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.anyMatch(b -> b.getRegionId() == regionId && b.getPlayerId().equals(player.getUniqueId()));
	}

	/**
	 * Checks if a player has rated a region using UUID only.
	 * @param playerId The player UUID
	 * @param regionId The region ID
	 * @return {@code true} if the player has rated the region.
	 */
	public static boolean hasRatedRegion(UUID playerId, long regionId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.anyMatch(b -> b.getRegionId() == regionId && b.getPlayerId().equals(playerId));
	}

	/**
	 * Retrieves a player's specific rating for a region.
	 * @param player The player
	 * @param region The region
	 * @return The {@link RegionRate}, or {@code null} if not found.
	 */
	public static RegionRate getPlayerRate(OfflinePlayer player, Region region) {
		return getPlayerRate(player, region.getUniqueId());
	}

	/**
	 * Retrieves a player's specific rating for a region.
	 * @param player The player
	 * @param regionId The region ID
	 * @return The {@link RegionRate}, or {@code null} if not found.
	 */
	public static RegionRate getPlayerRate(OfflinePlayer player, long regionId) {
		return Homestead.RATE_CACHE.getAll().stream()
				.filter(b -> b.getRegionId() == regionId && b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Deletes a specific player's rating for a region.
	 * @param player The player
	 * @param region The region
	 * @return {@code true} if a rating was found and deleted.
	 */
	public static boolean deletePlayerRating(OfflinePlayer player, Region region) {
		return deletePlayerRating(player, region.getUniqueId());
	}

	/**
	 * Deletes a specific player's rating for a region.
	 * @param player The player
	 * @param regionId The region ID
	 * @return {@code true} if a rating was found and deleted.
	 */
	public static boolean deletePlayerRating(OfflinePlayer player, long regionId) {
		RegionRate rate = getPlayerRate(player, regionId);
		if (rate == null) return false;
		Homestead.RATE_CACHE.remove(rate.getUniqueId());
		return true;
	}

	/**
	 * Deletes all ratings submitted by a specific player.
	 * @param player The player
	 * @return The number of ratings deleted.
	 */
	public static int deleteAllRatingsByPlayer(OfflinePlayer player) {
		return deleteAllRatingsByPlayer(player.getUniqueId());
	}

	/**
	 * Deletes all ratings submitted by a specific player.
	 * @param playerId The player UUID
	 * @return The number of ratings deleted.
	 */
	public static int deleteAllRatingsByPlayer(UUID playerId) {
		List<Long> toRemove = Homestead.RATE_CACHE.getAll().stream()
				.filter(r -> r.getPlayerId().equals(playerId))
				.map(RegionRate::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			Homestead.RATE_CACHE.remove(id);
		}
		return toRemove.size();
	}

	/**
	 * Delete all ratings sent to this region.
	 * @param region The region
	 */
	public static void deleteAll(Region region) {
		deleteAll(region.getUniqueId());
	}

	/**
	 * Delete all ratings sent to this region.
	 * @param regionId The region ID
	 */
	public static void deleteAll(long regionId) {
		List<Long> toRemove = Homestead.RATE_CACHE.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.map(RegionRate::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			Homestead.RATE_CACHE.remove(id);
		}
	}

	/**
	 * Deletes every rating in the cache. Use with caution.
	 * @return The number of ratings deleted.
	 */
	public static int deleteAllRatings() {
		List<Long> ids = Homestead.RATE_CACHE.getAll().stream()
				.map(RegionRate::getUniqueId)
				.toList();

		for (Long id : ids) {
			Homestead.RATE_CACHE.remove(id);
		}
		return ids.size();
	}

	/**
	 * Removes all ratings with invalid references:<br>
	 * - Players whose UUID no longer maps to a known player<br>
	 * - Regions that no longer exist
	 * @return Number of corrupted ratings removed.
	 */
	public static int cleanupInvalidRatings() {
		List<Long> toRemove = new ArrayList<>();

		for (RegionRate rate : Homestead.RATE_CACHE.getAll()) {
			OfflinePlayer player = rate.getPlayer();

			boolean invalidRegion = rate.getRegion() == null;
			boolean invalidPlayer = player == null || player.getName() == null;

			if (invalidRegion || invalidPlayer) {
				toRemove.add(rate.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			Homestead.RATE_CACHE.remove(id);
		}
		return toRemove.size();
	}
}