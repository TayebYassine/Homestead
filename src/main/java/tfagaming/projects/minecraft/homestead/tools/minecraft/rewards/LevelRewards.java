package tfagaming.projects.minecraft.homestead.tools.minecraft.rewards;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.*;

public class LevelRewards {
	public static int getChunksByLevel(OfflinePlayer player) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			return 0;
		}

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxChunks = 0;

		Map<String, Object> rewards = getLevelRewardsSection();
		if (rewards == null) return 0;

		for (String key : rewards.keySet()) {
			int requiredLevel = parseInt(key);
			if (requiredLevel < 0) continue;

			Map<?, ?> reward = (Map<?, ?>) rewards.get(key);
			if (currentLevel >= requiredLevel) {
				int chunks = getInt(reward, "chunks");
				if (chunks > maxChunks) {
					maxChunks = chunks;
				}
			}
		}

		return maxChunks;
	}

	public static int getMembersByLevel(OfflinePlayer player) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			return 0;
		}

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxMembers = 0;

		Map<String, Object> rewards = getLevelRewardsSection();
		if (rewards == null) return 0;

		for (String key : rewards.keySet()) {
			int requiredLevel = parseInt(key);
			if (requiredLevel < 0) continue;

			Map<?, ?> reward = (Map<?, ?>) rewards.get(key);
			if (currentLevel >= requiredLevel) {
				int members = getInt(reward, "members");
				if (members > maxMembers) {
					maxMembers = members;
				}
			}
		}

		return maxMembers;
	}

	public static int getSubAreasByLevel(OfflinePlayer player) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			return 0;
		}

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxSubAreas = 0;

		Map<String, Object> rewards = getLevelRewardsSection();
		if (rewards == null) return 0;

		for (String key : rewards.keySet()) {
			int requiredLevel = parseInt(key);
			if (requiredLevel < 0) continue;

			Map<?, ?> reward = (Map<?, ?>) rewards.get(key);
			if (currentLevel >= requiredLevel) {
				int subAreas = getInt(reward, "subareas");
				if (subAreas > maxSubAreas) {
					maxSubAreas = subAreas;
				}
			}
		}

		return maxSubAreas;
	}

	public static int getUpkeepReductionByLevel(OfflinePlayer player) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			return 0;
		}

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxReduction = 0;

		Map<String, Object> rewards = getLevelRewardsSection();
		if (rewards == null) return 0;

		for (String key : rewards.keySet()) {
			int requiredLevel = parseInt(key);
			if (requiredLevel < 0) continue;

			Map<?, ?> reward = (Map<?, ?>) rewards.get(key);
			if (currentLevel >= requiredLevel) {
				int reduction = getInt(reward, "upkeep-reduction");
				if (reduction > maxReduction) {
					maxReduction = reduction;
				}
			}
		}

		return Math.min(maxReduction, 100);
	}

	public static Map<String, Integer> getRewardsAtLevel(int targetLevel) {
		Map<String, Integer> result = new HashMap<>();
		result.put("chunks", 0);
		result.put("members", 0);
		result.put("subareas", 0);
		result.put("upkeep-reduction", 0);

		Map<String, Object> rewards = getLevelRewardsSection();
		if (rewards == null) return result;

		for (String key : rewards.keySet()) {
			if (parseInt(key) == targetLevel) {
				Map<?, ?> reward = (Map<?, ?>) rewards.get(key);
				result.put("chunks", getInt(reward, "chunks"));
				result.put("members", getInt(reward, "members"));
				result.put("subareas", getInt(reward, "subareas"));
				result.put("upkeep-reduction", getInt(reward, "upkeep-reduction"));
				break;
			}
		}

		return result;
	}

	private static Map<String, Object> getLevelRewardsSection() {
		if (!Homestead.config.isLevelsEnabled()) {
			return null;
		}

		ConfigurationSection section = Homestead.config.getConfig().getConfigurationSection("levels.rewards");
		if (section == null) return null;

		return section.getValues(false);
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private static int getInt(Map<?, ?> map, String key) {
		Object val = map.get(key);
		if (val instanceof Number) return ((Number) val).intValue();
		return 0;
	}
}