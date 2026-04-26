package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionRating;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.RegionRate;

import java.util.List;

/**
 * A utility class that manages {@link RegionRate}.
 */
public final class RateManager {
	private RateManager() {}

	/**
	 * Add a rate score from a player to a region.
	 * @param region The region
	 * @param player The player
	 * @param score The score; randing from 0 to 5
	 */
	public static void rateRegion(Region region, OfflinePlayer player, int score) {
		rateRegion(region.getUniqueId(), player, score);
	}

	/**
	 * Add a rate score from a player to a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @param score The score; randing from 0 to 5
	 */
	public static void rateRegion(long regionId, OfflinePlayer player, int score) {
		RegionRate rate = Homestead.regionRateCache.getAll().stream()
				.filter(r -> r.getRegionId() == regionId && r.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(new RegionRate(regionId, player, score));

		rate.setRate(score);

		Homestead.regionRateCache.putOrUpdate(rate);
	}

	/**
	 * Get the average rating of all scores submitted by players.
	 * @param region The region
	 * @return Average rating
	 */
	public static double getAverageRating(Region region) {
		return getAverageRating(region.getUniqueId());
	}

	/**
	 * Get the average rating of all scores submitted by players.
	 * @param regionId The region ID
	 * @return Average rating
	 */
	public static double getAverageRating(long regionId) {
		List<RegionRate> rates = Homestead.regionRateCache.getAll().stream()
				.filter(r -> r.getRegionId() == regionId)
				.toList();

		if (rates.isEmpty()) return 0.0;

		return rates.stream().mapToInt(RegionRate::getRate).average().orElse(0.0);
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
		Homestead.regionRateCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.findFirst()
				.ifPresent(b -> Homestead.regionRateCache.remove(b.getUniqueId()));
	}

	public static boolean hasRatedRegion(OfflinePlayer player, Region region) {
		return hasRatedRegion(player, region.getUniqueId());
	}

	public static boolean hasRatedRegion(OfflinePlayer player, long regionId) {
		return Homestead.regionRateCache.getAll().stream()
				.anyMatch(b -> b.getRegionId() == regionId && b.getPlayerId().equals(player.getUniqueId()));
	}

	public static RegionRate getPlayerRate(OfflinePlayer player, Region region) {
		return getPlayerRate(player, region.getUniqueId());
	}

	public static RegionRate getPlayerRate(OfflinePlayer player, long regionId) {
		return Homestead.regionRateCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId && b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(null);
	}
}