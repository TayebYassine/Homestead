package tfagaming.projects.minecraft.homestead.tools.minecraft.limits;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.Rewards;

public class Limits {

	public static int getPlayerLimit(OfflinePlayer player, LimitType limit) {
		return switch (limit) {
			case REGIONS, MAX_SUBAREA_VOLUME, COMMANDS_COOLDOWN -> getBaseLimitValue(player, limit);
			default -> 0;
		};
	}

	public static int getRegionLimit(Region region, LimitType limit) {
		if (region == null) {
			return 0;
		}

		OfflinePlayer owner = region.getOwner();
		if (owner == null) {
			return 0;
		}

		return switch (limit) {
			case CHUNKS_PER_REGION -> getBaseLimitValue(owner, limit)
					+ Rewards.getChunksByEachMember(region)
					+ Rewards.getChunksByPlayTime(owner)
					+ LevelRewards.getChunksByLevel(region);
			case MEMBERS_PER_REGION -> getBaseLimitValue(owner, limit)
					+ LevelRewards.getMembersByLevel(region);
			case SUBAREAS_PER_REGION -> getBaseLimitValue(owner, limit)
					+ Rewards.getSubAreasByEachMember(region)
					+ Rewards.getSubAreasByPlayTime(owner)
					+ LevelRewards.getSubAreasByLevel(region);
			case MAX_SUBAREA_VOLUME, COMMANDS_COOLDOWN -> getBaseLimitValue(owner, limit);
			default -> 0;
		};
	}

	public static boolean hasReachedLimit(OfflinePlayer player, Region region, LimitType limit) {
		return switch (limit) {
			case REGIONS -> hasReachedRegionsLimit(player);
			case CHUNKS_PER_REGION -> hasReachedChunksLimit(region);
			case MEMBERS_PER_REGION -> hasReachedMembersLimit(region);
			case SUBAREAS_PER_REGION -> hasReachedSubAreasLimit(region);
			default -> false;
		};
	}

	private static int getBaseLimitValue(OfflinePlayer player, LimitType limit) {
		String limitKey = getLimitConfigKey(limit);

		Object playerOverride = Homestead.config.get(
				"player-limits." + player.getName() + "." + limitKey
		);
		if (playerOverride != null) {
			return (int) playerOverride;
		}

		LimitMethod method = getLimitsMethod();

		switch (method) {
			case STATIC:
				String opKey = PlayerUtils.isOperator(player) ? "op" : "non-op";
				Object staticValue = Homestead.config.get(
						"limits.static." + opKey + "." + limitKey
				);
				return staticValue == null ? 0 : (int) staticValue;

			case GROUPS:
				String group = PlayerUtils.getPlayerGroup(player);
				if (group == null) {
					group = "default";
				}
				Object groupValue = Homestead.config.get(
						"limits.groups." + group + "." + limitKey
				);
				return groupValue == null ? 0 : (int) groupValue;

			default:
				return 0;
		}
	}

	private static boolean hasReachedRegionsLimit(OfflinePlayer player) {
		if (player == null) {
			return false;
		}

		int current = RegionsManager.getRegionsOwnedByPlayer(player).size();
		int max = getPlayerLimit(player, LimitType.REGIONS);
		return current >= max;
	}

	private static boolean hasReachedChunksLimit(Region region) {
		if (region == null) {
			return false;
		}

		int current = region.getChunks().size();
		int max = getRegionLimit(region, LimitType.CHUNKS_PER_REGION);
		return current >= max;
	}

	private static boolean hasReachedMembersLimit(Region region) {
		if (region == null) {
			return false;
		}

		int current = region.getMembers().size();
		int max = getRegionLimit(region, LimitType.MEMBERS_PER_REGION);
		return current >= max;
	}

	private static boolean hasReachedSubAreasLimit(Region region) {
		if (region == null) {
			return false;
		}

		int current = SubAreasManager.getSubAreasOfRegion(region.getUniqueId()).size();
		int max = getRegionLimit(region, LimitType.SUBAREAS_PER_REGION);
		return current >= max;
	}

	private static String getLimitConfigKey(LimitType limit) {
		return switch (limit) {
			case REGIONS -> "regions";
			case CHUNKS_PER_REGION -> "chunks-per-region";
			case MEMBERS_PER_REGION -> "members-per-region";
			case SUBAREAS_PER_REGION -> "subareas-per-region";
			case MAX_SUBAREA_VOLUME -> "max-subarea-volume";
			case COMMANDS_COOLDOWN -> "commands-cooldown";
		};
	}

	public static LimitMethod getLimitsMethod() {
		String method = Homestead.config.get("limits.method");
		return switch (method) {
			case "static" -> LimitMethod.STATIC;
			case "groups" -> LimitMethod.GROUPS;
			default -> LimitMethod.STATIC;
		};
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