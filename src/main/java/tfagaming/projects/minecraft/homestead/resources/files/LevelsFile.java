package tfagaming.projects.minecraft.homestead.resources.files;

import tfagaming.projects.minecraft.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;

public class LevelsFile extends ResourceFile {

	public LevelsFile(File file) throws FileNotFoundException {
		super(file);
	}

	public boolean isEnabled() {
		return getBoolean("levels.enabled");
	}

	public int getKillXpMin(String entityType) {
		return getIntegerList("levels.on-kill-entity." + entityType)
				.stream().findFirst().orElse(0);
	}

	public int getKillXpMax(String entityType) {
		java.util.List<Integer> range = getIntegerList("levels.on-kill-entity." + entityType);
		return range.size() >= 2 ? range.get(1) : range.stream().findFirst().orElse(0);
	}

	public int getRewardChunks(int level) {
		return getInt("levels.rewards." + level + ".chunks", 0);
	}

	public int getRewardMembers(int level) {
		return getInt("levels.rewards." + level + ".members", 0);
	}

	public int getRewardSubAreas(int level) {
		return getInt("levels.rewards." + level + ".subareas", 0);
	}

	public int getRewardUpkeepReduction(int level) {
		return getInt("levels.rewards." + level + ".upkeep-reduction", 0);
	}
}