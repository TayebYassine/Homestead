package me.tayebyassine.homestead.flags;

import org.bukkit.World;
import me.tayebyassine.homestead.ProtectionMode;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.FlagsFile;

public final class WorldRules {
	private WorldRules() {
	}

	public static boolean isEnabled() {
		return Resources.<FlagsFile>get(ResourceType.Flags).getBoolean("world-rules.enabled");
	}

	private static long getPlayerFlags(String worldName) {
		return Resources.<FlagsFile>get(ResourceType.Flags).getLong("world-rules.worlds." + worldName + ".player_flags", -1L);
	}

	private static long getWorldFlags(String worldName) {
		return Resources.<FlagsFile>get(ResourceType.Flags).getLong("world-rules.worlds." + worldName + ".world_flags", -1L);
	}

	public static boolean isPlayerFlagAllowed(World world, long flag) {
		if (world == null) return true;

		return isPlayerFlagAllowed(world.getName(), flag);
	}

	public static boolean isPlayerFlagAllowed(String worldName, long flag) {
		if (ProtectionMode.isEnabled()) return false;

		if (!isEnabled()) return true;

		long flags = getPlayerFlags(worldName);

		if (flags == -1) return true;

		return FlagCalculator.isFlagSet(flags, flag);
	}

	public static boolean isWorldFlagAllowed(World world, long flag) {
		if (world == null) return true;

		return isWorldFlagAllowed(world.getName(), flag);
	}

	public static boolean isWorldFlagAllowed(String worldName, long flag) {
		if (ProtectionMode.isEnabled()) return false;

		if (!isEnabled()) return true;

		long flags = getWorldFlags(worldName);

		if (flags == -1) return true;

		return FlagCalculator.isFlagSet(flags, flag);
	}
}
