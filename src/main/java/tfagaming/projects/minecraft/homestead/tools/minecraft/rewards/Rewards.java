package tfagaming.projects.minecraft.homestead.tools.minecraft.rewards;

import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.List;
import java.util.Map;

public class Rewards {
	public static int getChunksByEachMember(Region region) {
		if (!Homestead.config.isRewardsEnabled()) {
			return 0;
		}

		int chunksPerMember = Homestead.config.get("rewards.for-each-member.chunks");
		return region.getMembers().size() * chunksPerMember;
	}

	public static int getSubAreasByEachMember(Region region) {
		if (!Homestead.config.isRewardsEnabled()) {
			return 0;
		}

		int subAreasPerMember = Homestead.config.get("rewards.for-each-member.subareas");
		return region.getMembers().size() * subAreasPerMember;
	}

	public static int getChunksByPlayTime(OfflinePlayer player) {
		if (!Homestead.config.isRewardsEnabled()) {
			return 0;
		}

		if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
			return 0;
		}

		long playerMinutes = getPlayerMinutes(player);
		List<Map<?, ?>> rewards = Homestead.config.getConfig().getMapList("rewards.by-playtime");

		return getHighestRewardValue(rewards, playerMinutes, "chunks");
	}

	public static int getSubAreasByPlayTime(OfflinePlayer player) {
		if (!Homestead.config.isRewardsEnabled()) {
			return 0;
		}

		if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
			return 0;
		}

		long playerMinutes = getPlayerMinutes(player);
		List<Map<?, ?>> rewards = Homestead.config.getConfig().getMapList("rewards.by-playtime");

		return getHighestRewardValue(rewards, playerMinutes, "subareas");
	}

	private static long getPlayerMinutes(OfflinePlayer player) {
		return player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20L * 60L);
	}

	private static int getHighestRewardValue(List<Map<?, ?>> rewards, long playerMinutes, String rewardKey) {
		int maxValue = 0;

		for (Map<?, ?> entry : rewards) {
			long requiredMinutes = calculateRequiredMinutes(entry);
			int rewardValue = getInt(entry, rewardKey);

			if (playerMinutes >= requiredMinutes && rewardValue > maxValue) {
				maxValue = rewardValue;
			}
		}

		return maxValue;
	}

	private static long calculateRequiredMinutes(Map<?, ?> entry) {
		int minutes = getInt(entry, "minutes");
		int hours = getInt(entry, "hours");
		int days = getInt(entry, "days");

		return minutes + (hours * 60L) + (days * 24L * 60L);
	}

	private static int getInt(Map<?, ?> map, String key) {
		Object val = map.get(key);
		if (val instanceof Number) {
			return ((Number) val).intValue();
		}
		return 0;
	}
}