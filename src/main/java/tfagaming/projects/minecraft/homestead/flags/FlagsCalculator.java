package tfagaming.projects.minecraft.homestead.flags;

public final class FlagsCalculator {
	private FlagsCalculator() {
	}

	/**
	 * Add Bitwise flags with other flags.<br>
	 * <b>Warning</b>: Do not mix {@link PlayerFlags}, {@link WorldFlags}, and {@link RegionControlFlags}.
	 * @param flags The flags
	 */
	public static long addFlag(long... flags) {
		long combined = 0;

		for (long flag : flags) {
			combined |= flag;
		}

		return combined;
	}

	/**
	 * Remove a Bitwise flag from a list of flags.<br>
	 * <b>Warning</b>: Do not mix {@link PlayerFlags}, {@link WorldFlags}, and {@link RegionControlFlags}.
	 * @param flags The flags
	 * @param flag The flag to remove
	 */
	public static long removeFlag(long flags, long flag) {
		return flags & ~flag;
	}

	/**
	 * Return {@code true} when a bitwise flag exist in a list of flags, otherwise {@code false}.<br>
	 * <b>Warning</b>: Do not mix {@link PlayerFlags}, {@link WorldFlags}, and {@link RegionControlFlags}.
	 * @param flags The flags
	 * @param flag The flag to check
	 */
	public static boolean isFlagSet(long flags, long flag) {
		return (flags & flag) != 0;
	}
}
