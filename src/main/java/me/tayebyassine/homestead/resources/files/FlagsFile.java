package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.resources.ResourceFile;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;

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
					flags = FlagCalculator.addFlag(flags, PlayerFlag.parse(key));
				} catch (IllegalArgumentException ignored) {
					// Skip silently
				}
			}
		}
		return flags;
	}

	public long getAllAllowedPlayerFlagsExcludeDisabledOnes() {
		List<String> keys = PlayerFlag.getFlags();
		long flags = 0L;

		for (String key : keys) {
			if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(key)) continue;

			flags = FlagCalculator.addFlag(flags, PlayerFlag.parse(key));
		}

		return flags;
	}

	public long getDefaultWorldFlags() {
		List<String> keys = getKeysUnderPath("default-world-flags");
		long flags = 0L;

		for (String key : keys) {
			if (getBoolean("default-world-flags." + key)) {
				try {
					flags = FlagCalculator.addFlag(flags, WorldFlag.parse(key));
				} catch (IllegalArgumentException ignored) {
					// Skip silently
				}
			}
		}
		return flags;
	}

	public long getAllAllowedWorldFlagsExcludeDisabledOnes() {
		List<String> keys = WorldFlag.getFlags();
		long flags = 0L;

		for (String key : keys) {
			if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(key)) continue;

			flags = FlagCalculator.addFlag(flags, WorldFlag.parse(key));
		}

		return flags;
	}
}


