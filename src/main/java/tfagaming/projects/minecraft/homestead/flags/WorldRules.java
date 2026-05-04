package tfagaming.projects.minecraft.homestead.flags;

import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;

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
		if (!isEnabled()) return true;

		long flags = getPlayerFlags(worldName);

		if (flags == -1) return true;

		return FlagsCalculator.isFlagSet(flags, flag);
	}

	public static boolean isWorldFlagAllowed(World world, long flag) {
		if (world == null) return true;

		return isWorldFlagAllowed(world.getName(), flag);
	}

	public static boolean isWorldFlagAllowed(String worldName, long flag) {
		if (!isEnabled()) return true;

		long flags = getWorldFlags(worldName);

		if (flags == -1) return true;

		return FlagsCalculator.isFlagSet(flags, flag);
	}
}
