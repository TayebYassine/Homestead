package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionRate;

import java.util.List;

public final class RateManager {
	private RateManager() {}

	public static void rateRegion(Region region, OfflinePlayer player, int score) {
		rateRegion(region.getUniqueId(), player, score);
	}

	public static void rateRegion(long regionId, OfflinePlayer player, int score) {
		RegionRate rate = Homestead.regionRateCache.getAll().stream()
				.filter(r -> r.getRegionId() == regionId && r.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(new RegionRate(regionId, player, score));

		rate.setRate(score);

		Homestead.regionRateCache.putOrUpdate(rate);
	}

	public static double getAverageRating(Region region) {
		return getAverageRating(region.getUniqueId());
	}

	public static double getAverageRating(long regionId) {
		List<RegionRate> rates = Homestead.regionRateCache.getAll().stream()
				.filter(r -> r.getRegionId() == regionId)
				.toList();

		if (rates.isEmpty()) return 0.0;

		return rates.stream().mapToInt(RegionRate::getRate).average().orElse(0.0);
	}
}