package tfagaming.projects.minecraft.homestead.resources.files;

import tfagaming.projects.minecraft.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class RegionsFile extends ResourceFile {

	public RegionsFile(File file) throws FileNotFoundException {
		super(file);
	}

	public boolean isInstantTrustSystemEnabled() {
		return getBoolean("special-feat.ignore-trust-acceptance-system");
	}

	public boolean teleportPlayersBackToTegionSpawnWhenEnteringEndExitPortal() {
		return getBoolean("special-feat.teleport-players-back-to-region-spawn-when-entering-end-exit-portal");
	}

	public boolean isWelcomeSignEnabled() {
		return getBoolean("welcome-signs.enabled");
	}

	public boolean isAdjacentChunksRuleEnabled() {
		return getBoolean("adjacent-chunks");
	}

	public boolean isRewardsEnabled() {
		return getBoolean("rewards.enabled");
	}

	public int getRewardChunksPerMember() {
		return getInt("rewards.for-each-member.chunks", 0);
	}

	public int getRewardSubAreasPerMember() {
		return getInt("rewards.for-each-member.subareas", 0);
	}

	public boolean isRegionStorageEnabled() {
		return getBoolean("storage.enabled");
	}

	public int getRegionStorageSize() {
		int size = getInt("storage.size");

		if (!List.of(9, 18, 27, 36, 45, 54).contains(size)) {
			size = 27;
		}

		return size;
	}
}