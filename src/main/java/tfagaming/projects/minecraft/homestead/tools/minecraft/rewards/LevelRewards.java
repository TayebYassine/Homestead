package tfagaming.projects.minecraft.homestead.tools.minecraft.rewards;

import org.bukkit.configuration.ConfigurationSection;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.*;

public class LevelRewards {
	public static int getChunksByLevel(Region region) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Level lvl = LevelsManager.getLevelByRegion(region.getUniqueId());
		int current = lvl == null ? 0 : lvl.getLevel();

		ConfigurationSection rewards = Homestead.config.getConfig().getConfigurationSection("levels.rewards");
		if (rewards == null) return 0;

		int maxChunks = 0;
		for (String key : rewards.getKeys(false)) {
			int requiredLevel = Integer.parseInt(key);
			if (current < requiredLevel) continue;

			ConfigurationSection reward = rewards.getConfigurationSection(key);
			if (reward == null) continue;

			maxChunks = Math.max(maxChunks, reward.getInt("chunks", 0));
		}

		return maxChunks;
	}

	public static int getMembersByLevel(Region region) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxMembers = 0;

		ConfigurationSection rewards = Homestead.config.getConfig().getConfigurationSection("levels.rewards");
		if (rewards == null) return 0;

		for (String key : rewards.getKeys(false)) {
			int requiredLevel = Integer.parseInt(key);
			if (currentLevel < requiredLevel) continue;

			ConfigurationSection reward = rewards.getConfigurationSection(key);
			if (reward == null) continue;

			maxMembers = Math.max(maxMembers, reward.getInt("members", 0));
		}

		return maxMembers;
	}

	public static int getSubAreasByLevel(Region region) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxSubAreas = 0;

		ConfigurationSection rewards = Homestead.config.getConfig().getConfigurationSection("levels.rewards");
		if (rewards == null) return 0;

		for (String key : rewards.getKeys(false)) {
			int requiredLevel = Integer.parseInt(key);
			if (currentLevel < requiredLevel) continue;

			ConfigurationSection reward = rewards.getConfigurationSection(key);
			if (reward == null) continue;

			maxSubAreas = Math.max(maxSubAreas, reward.getInt("subareas", 0));
		}

		return maxSubAreas;
	}

	public static int getUpkeepReductionByLevel(Region region) {
		if (!Homestead.config.isLevelsEnabled()) return 0;

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) return 0;

		int currentLevel = level.getLevel();
		int maxReduction = 0;

		ConfigurationSection rewards = Homestead.config.getConfig().getConfigurationSection("levels.rewards");
		if (rewards == null) return 0;

		for (String key : rewards.getKeys(false)) {
			int requiredLevel = Integer.parseInt(key);
			if (currentLevel < requiredLevel) continue;

			ConfigurationSection reward = rewards.getConfigurationSection(key);
			if (reward == null) continue;

			maxReduction = Math.max(maxReduction, reward.getInt("upkeep-reduction", 0));
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