package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.List;
import java.util.Map;

public class PlayerRewards {
	public static int getChunksByEachMember(OfflinePlayer player) {
		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			return 0;
		}

		int chunksPerMember = Homestead.config.get("rewards.for-each-member.chunks");

		return region.getMembers().size() * chunksPerMember;
	}

	public static int getChunksByPlayTime(OfflinePlayer player) {
		if (!player.hasPlayedBefore() && !player.isOnline()) return 0;

		long playerMinutes = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20L * 60L);

		List<Map<?, ?>> rewards = Homestead.config.getConfig().getMapList("rewards.by-playtime");

		int maxChunks = 0;

		for (Map<?, ?> entry : rewards) {
			int min = getInt(entry, "minutes");
			int hrs = getInt(entry, "hours");
			int days = getInt(entry, "days");

			int chunks = getInt(entry, "chunks");

			long requiredMinutes = min + hrs * 60L + days * 24L * 60L;

			if (playerMinutes >= requiredMinutes && chunks > maxChunks) {
				maxChunks = chunks;
			}
		}

		return maxChunks;
	}

	private static int getInt(Map<?, ?> map, String key) {
		Object val = map.get(key);
		if (val instanceof Number) return ((Number) val).intValue();
		return 0;
	}
}
