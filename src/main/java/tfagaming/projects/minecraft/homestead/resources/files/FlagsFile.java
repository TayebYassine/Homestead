package tfagaming.projects.minecraft.homestead.resources.files;

import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class FlagsFile extends ResourceFile {

	public FlagsFile(File file) throws FileNotFoundException {
		super(file);
	}

	public boolean isFlagDisabled(String flag) {
		return getStringList("disabled-flags").contains(flag);
	}

	public boolean doSpawnersIgnoreSpawnFlags() {
		return getBoolean("flags-configuration.exclude-spawners", true);
	}

	public boolean allowFlagsOnPlayerTrust() {
		return getBoolean("flags-configuration.allow-all-flags-for-trusted-players", true);
	}

	public long getDefaultPlayerFlags() {
		List<String> keys = getKeysUnderPath("default-players-flags");
		long flags = 0L;

		for (String key : keys) {
			if (getBoolean("default-players-flags." + key)) {
				try {
					flags = FlagsCalculator.addFlag(flags, PlayerFlags.valueOf(key));
				} catch (IllegalArgumentException ignored) {
					// Skip silently
				}
			}
		}
		return flags;
	}

	public long getDefaultWorldFlags() {
		List<String> keys = getKeysUnderPath("default-world-flags");
		long flags = 0L;

		for (String key : keys) {
			if (getBoolean("default-world-flags." + key)) {
				try {
					flags = FlagsCalculator.addFlag(flags, WorldFlags.valueOf(key));
				} catch (IllegalArgumentException ignored) {
					// Skip silently
				}
			}
		}
		return flags;
	}
}