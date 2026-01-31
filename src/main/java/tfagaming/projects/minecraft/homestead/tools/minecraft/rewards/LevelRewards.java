package tfagaming.projects.minecraft.homestead.tools.minecraft.rewards;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelRewards {
	public static int getChunksByLevel(Region region) {
		return getHighestLevelReward(region, "chunks");
	}

	public static int getMembersByLevel(Region region) {
		return getHighestLevelReward(region, "members");
	}

	public static int getSubAreasByLevel(Region region) {
		return getHighestLevelReward(region, "subareas");
	}

	public static int getUpkeepReductionByLevel(Region region) {
		int reduction = getHighestLevelReward(region, "upkeep-reduction");
		return Math.min(reduction, 100);
	}

	public static Map<String, Integer> getRewardsAtLevel(int targetLevel) {
		Map<String, Integer> result = new HashMap<>();
		result.put("chunks", 0);
		result.put("members", 0);
		result.put("subareas", 0);
		result.put("upkeep-reduction", 0);

		if (!Homestead.config.isLevelsEnabled()) {
			return result;
		}

		ConfigurationSection rewards = Homestead.config.getConfig()
				.getConfigurationSection("levels.rewards");

		if (rewards == null) {
			return result;
		}

		String levelKey = String.valueOf(targetLevel);
		ConfigurationSection levelReward = rewards.getConfigurationSection(levelKey);

		if (levelReward != null) {
			result.put("chunks", levelReward.getInt("chunks", 0));
			result.put("members", levelReward.getInt("members", 0));
			result.put("subareas", levelReward.getInt("subareas", 0));
			result.put("upkeep-reduction", levelReward.getInt("upkeep-reduction", 0));
		}

		return result;
	}

	private static int getHighestLevelReward(Region region, String rewardKey) {
		if (!Homestead.config.isLevelsEnabled()) {
			return 0;
		}

		Level level = LevelsManager.getLevelByRegion(region.getUniqueId());
		if (level == null) {
			return 0;
		}

		int currentLevel = level.getLevel();
		ConfigurationSection rewards = Homestead.config.getConfig()
				.getConfigurationSection("levels.rewards");

		if (rewards == null) {
			return 0;
		}

		int maxReward = 0;

		for (String levelKey : rewards.getKeys(false)) {
			int requiredLevel = parseInt(levelKey);

			if (currentLevel < requiredLevel) {
				continue;
			}

			ConfigurationSection reward = rewards.getConfigurationSection(levelKey);
			if (reward == null) {
				continue;
			}

			int rewardValue = reward.getInt(rewardKey, 0);
			maxReward = Math.max(maxReward, rewardValue);
		}

		return maxReward;
	}

	public static boolean hasEntityKillReward(EntityType entityType) {
		if (!Homestead.config.isLevelsEnabled()) {
			return false;
		}

		ConfigurationSection killRewards = Homestead.config.getConfig()
				.getConfigurationSection("levels.on-kill-entity");

		if (killRewards == null) {
			return false;
		}

		String entityName = entityType.name();

		if (!killRewards.contains(entityName)) {
			return false;
		}

		List<?> values = killRewards.getList(entityName);
		return values != null && values.size() == 2;
	}

	public static int[] getEntityKillReward(EntityType entityType) {
		if (!hasEntityKillReward(entityType)) {
			return null;
		}

		ConfigurationSection killRewards = Homestead.config.getConfig()
				.getConfigurationSection("levels.on-kill-entity");

		if (killRewards == null) {
			return null;
		}

		String entityName = entityType.name();
		List<?> values = killRewards.getList(entityName);

		if (values == null || values.size() != 2) {
			return null;
		}

		try {
			int min = ((Number) values.get(0)).intValue();
			int max = ((Number) values.get(1)).intValue();
			return new int[]{min, max};
		} catch (Exception e) {
			return null;
		}
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}