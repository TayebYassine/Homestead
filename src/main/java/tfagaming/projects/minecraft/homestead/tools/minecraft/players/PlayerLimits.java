package tfagaming.projects.minecraft.homestead.tools.minecraft.players;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.Objects;

public class PlayerLimits {
	public static String getString(LimitType limit) {
		switch (limit) {
			case REGIONS:
				return "regions";
			case CHUNKS_PER_REGION:
				return "chunks-per-region";
			case MEMBERS_PER_REGION:
				return "members-per-region";
			case SUBAREAS_PER_REGION:
				return "subareas-per-region";
			case MAX_SUBAREA_VOLUME:
				return "max-subarea-volume";
			case COMMANDS_COOLDOWN:
				return "commands-cooldown";
			default:
				return null;
		}
	}

	public static LimitMethod getLimitsMethod() {
		switch ((String) Homestead.config.get("limits.method")) {
			case "static":
				return LimitMethod.STATIC;
			case "groups":
				return LimitMethod.GROUPS;
		}

		return LimitMethod.STATIC;
	}

	private static Object getLimitValueByPlayername(OfflinePlayer player, LimitType limit) {
        return Homestead.config.get("player-limits." + player.getName() + "." + getString(limit));
	}

	public static int getDefaultLimitValue(OfflinePlayer player, LimitType limit) {
		Object playerLimitByName = getLimitValueByPlayername(player, limit);

		if (playerLimitByName != null) {
			return (int) playerLimitByName;
		}

		switch (getLimitsMethod()) {
			case STATIC:
				if (PlayerUtils.isOperator(player)) {
					Object value = Homestead.config.get("limits.static.op." + getString(limit));

					return value == null ? 0 : (int) value;
				} else {
					Object value = Homestead.config.get("limits.static.non-op." + getString(limit));

					return value == null ? 0 : (int) value;
				}
			case GROUPS:
				String group = PlayerUtils.getPlayerGroup(player);

				if (group == null) {
					group = "default";
				}

				Object value = Homestead.config.get("limits.groups." + group + "." + getString(limit));

				return value == null ? 0 : (int) value;
			default:
				return 0;
		}
	}

	public static int getLimitOfPlayer(OfflinePlayer player, LimitType limit) {
		if (Objects.requireNonNull(limit) == LimitType.CHUNKS_PER_REGION) {
			int defChunks = getDefaultLimitValue(player, limit);

			boolean rewardsEnabled = Homestead.config.get("rewards.enabled");

			if (rewardsEnabled) {
				return PlayerRewards.getChunksByEachMember(player) + PlayerRewards.getChunksByPlayTime(player) + defChunks;
			} else {
				return defChunks;
			}
		}

		return getDefaultLimitValue(player, limit);
	}

	public static boolean hasPlayerReachedLimit(OfflinePlayer player, LimitType limit) {
		switch (limit) {
			case REGIONS: {
				int current = RegionsManager.getRegionsOwnedByPlayer(player).size();
				int max = getLimitOfPlayer(player, limit);

				return current >= max;
			}
			case CHUNKS_PER_REGION: {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					return false;
				}

				int current = region.getChunks().size();
				int max = getLimitOfPlayer(player, limit);

				return current >= max;
			}
			case MEMBERS_PER_REGION: {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					return false;
				}

				int current = region.getMembers().size();
				int max = getLimitOfPlayer(player, limit);

				return current >= max;
			}
			case SUBAREAS_PER_REGION: {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					return false;
				}

				int current = region.getSubAreas().size();
				int max = getLimitOfPlayer(player, limit);

				return current >= max;
			}
			default:
				return true;
		}
	}

	public enum LimitType {
		REGIONS,
		CHUNKS_PER_REGION,
		MEMBERS_PER_REGION,
		SUBAREAS_PER_REGION,
		MAX_SUBAREA_VOLUME,
		COMMANDS_COOLDOWN
	}

	public enum LimitMethod {
		GROUPS,
		STATIC
	}
}
