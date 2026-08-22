package me.tayebyassine.homestead.flags;

import java.util.*;

/**
 * Utility class for bitwise flag operations.
 * <p>
 * Provides methods for combining, removing, and checking bitwise flags.
 * Includes type-safe overloads for {@link PlayerFlag}, {@link WorldFlag}, and {@link ControlFlag}
 * enums to prevent mixing different flag types.
 * </p>
 *
 * @see PlayerFlag
 * @see WorldFlag
 * @see ControlFlag
 */
public final class FlagCalculator {
	private FlagCalculator() {
		throw new AssertionError("Uninstantiable class");
	}

	/**
	 * Combines multiple bitwise flags into a single value.
	 * <p>
	 * Do not mix {@link PlayerFlag}, {@link WorldFlag}, and {@link ControlFlag}
	 * in the same operation as they use overlapping bit positions.
	 * </p>
	 *
	 * @param flags the flags to combine
	 * @return the combined bitmask
	 */
	public static long addFlag(long... flags) {
		long combined = 0;
		for (long flag : flags) {
			combined |= flag;
		}
		return combined;
	}

	/**
	 * Removes a flag from a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the flag to remove
	 * @return the bitmask with the flag removed
	 */
	public static long removeFlag(long flags, long flag) {
		return flags & ~flag;
	}

	/**
	 * Checks if a specific flag is set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the flag to check
	 * @return true if the flag is set, false otherwise
	 */
	public static boolean isFlagSet(long flags, long flag) {
		return (flags & flag) != 0;
	}

	/**
	 * Checks if all specified flags are set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param requiredFlags the flags that must all be present
	 * @return true if all required flags are set
	 */
	public static boolean areAllFlagsSet(long flags, long... requiredFlags) {
		for (long flag : requiredFlags) {
			if (!isFlagSet(flags, flag)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if any of the specified flags are set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flagsToCheck the flags to check for
	 * @return true if at least one flag is set
	 */
	public static boolean isAnyFlagSet(long flags, long... flagsToCheck) {
		for (long flag : flagsToCheck) {
			if (isFlagSet(flags, flag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Counts how many of the specified flags are set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flagsToCheck the flags to count
	 * @return the number of flags that are set
	 */
	public static int countFlagsSet(long flags, long... flagsToCheck) {
		int count = 0;
		for (long flag : flagsToCheck) {
			if (isFlagSet(flags, flag)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Toggles a flag in a combined bitmask (adds if not present, removes if present).
	 *
	 * @param flags the combined bitmask
	 * @param flag  the flag to toggle
	 * @return the new bitmask with the flag toggled
	 */
	public static long toggleFlag(long flags, long flag) {
		return flags ^ flag;
	}

	/**
	 * Returns a new bitmask with the specified flags removed.
	 *
	 * @param flags the combined bitmask
	 * @param flagsToRemove the flags to remove
	 * @return the bitmask with flags removed
	 */
	public static long removeFlags(long flags, long... flagsToRemove) {
		long mask = 0;
		for (long flag : flagsToRemove) {
			mask |= flag;
		}
		return flags & ~mask;
	}

	/**
	 * Combines multiple player flags into a single bitmask.
	 *
	 * @param flags the player flags to combine
	 * @return the combined bitmask
	 */
	public static long addFlag(PlayerFlag... flags) {
		long combined = 0;
		for (PlayerFlag flag : flags) {
			combined |= flag.getBitmask();
		}
		return combined;
	}

	/**
	 * Removes a player flag from a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the player flag to remove
	 * @return the bitmask with the flag removed
	 */
	public static long removeFlag(long flags, PlayerFlag flag) {
		return flags & ~flag.getBitmask();
	}

	/**
	 * Checks if a player flag is set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the player flag to check
	 * @return true if the flag is set
	 */
	public static boolean isFlagSet(long flags, PlayerFlag flag) {
		return (flags & flag.getBitmask()) != 0;
	}

	/**
	 * Checks if all specified player flags are set.
	 *
	 * @param flags the combined bitmask
	 * @param requiredFlags the player flags that must all be present
	 * @return true if all are set
	 */
	public static boolean areAllFlagsSet(long flags, PlayerFlag... requiredFlags) {
		for (PlayerFlag flag : requiredFlags) {
			if (!isFlagSet(flags, flag)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if any of the specified player flags are set.
	 *
	 * @param flags the combined bitmask
	 * @param flagsToCheck the player flags to check
	 * @return true if at least one is set
	 */
	public static boolean isAnyFlagSet(long flags, PlayerFlag... flagsToCheck) {
		for (PlayerFlag flag : flagsToCheck) {
			if (isFlagSet(flags, flag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Toggles a player flag in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the player flag to toggle
	 * @return the new bitmask
	 */
	public static long toggleFlag(long flags, PlayerFlag flag) {
		return flags ^ flag.getBitmask();
	}

	/**
	 * Combines multiple world flags into a single bitmask.
	 *
	 * @param flags the world flags to combine
	 * @return the combined bitmask
	 */
	public static long addFlag(WorldFlag... flags) {
		long combined = 0;
		for (WorldFlag flag : flags) {
			combined |= flag.getBitmask();
		}
		return combined;
	}

	/**
	 * Removes a world flag from a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the world flag to remove
	 * @return the bitmask with the flag removed
	 */
	public static long removeFlag(long flags, WorldFlag flag) {
		return flags & ~flag.getBitmask();
	}

	/**
	 * Checks if a world flag is set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the world flag to check
	 * @return true if the flag is set
	 */
	public static boolean isFlagSet(long flags, WorldFlag flag) {
		return (flags & flag.getBitmask()) != 0;
	}

	/**
	 * Checks if all specified world flags are set.
	 *
	 * @param flags the combined bitmask
	 * @param requiredFlags the world flags that must all be present
	 * @return true if all are set
	 */
	public static boolean areAllFlagsSet(long flags, WorldFlag... requiredFlags) {
		for (WorldFlag flag : requiredFlags) {
			if (!isFlagSet(flags, flag)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if any of the specified world flags are set.
	 *
	 * @param flags the combined bitmask
	 * @param flagsToCheck the world flags to check
	 * @return true if at least one is set
	 */
	public static boolean isAnyFlagSet(long flags, WorldFlag... flagsToCheck) {
		for (WorldFlag flag : flagsToCheck) {
			if (isFlagSet(flags, flag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Toggles a world flag in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the world flag to toggle
	 * @return the new bitmask
	 */
	public static long toggleFlag(long flags, WorldFlag flag) {
		return flags ^ flag.getBitmask();
	}

	/**
	 * Combines multiple control flags into a single bitmask.
	 *
	 * @param flags the control flags to combine
	 * @return the combined bitmask
	 */
	public static long addFlag(ControlFlag... flags) {
		long combined = 0;
		for (ControlFlag flag : flags) {
			combined |= flag.getBitmask();
		}
		return combined;
	}

	/**
	 * Removes a control flag from a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the control flag to remove
	 * @return the bitmask with the flag removed
	 */
	public static long removeFlag(long flags, ControlFlag flag) {
		return flags & ~flag.getBitmask();
	}

	/**
	 * Checks if a control flag is set in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the control flag to check
	 * @return true if the flag is set
	 */
	public static boolean isFlagSet(long flags, ControlFlag flag) {
		return (flags & flag.getBitmask()) != 0;
	}

	/**
	 * Checks if all specified control flags are set.
	 *
	 * @param flags the combined bitmask
	 * @param requiredFlags the control flags that must all be present
	 * @return true if all are set
	 */
	public static boolean areAllFlagsSet(long flags, ControlFlag... requiredFlags) {
		for (ControlFlag flag : requiredFlags) {
			if (!isFlagSet(flags, flag)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if any of the specified control flags are set.
	 *
	 * @param flags the combined bitmask
	 * @param flagsToCheck the control flags to check
	 * @return true if at least one is set
	 */
	public static boolean isAnyFlagSet(long flags, ControlFlag... flagsToCheck) {
		for (ControlFlag flag : flagsToCheck) {
			if (isFlagSet(flags, flag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Toggles a control flag in a combined bitmask.
	 *
	 * @param flags the combined bitmask
	 * @param flag  the control flag to toggle
	 * @return the new bitmask
	 */
	public static long toggleFlag(long flags, ControlFlag flag) {
		return flags ^ flag.getBitmask();
	}

	/**
	 * Converts an array of PlayerFlags to a combined bitmask.
	 *
	 * @param flags the player flags
	 * @return the combined bitmask
	 */
	public static long toBitmask(PlayerFlag... flags) {
		return addFlag(flags);
	}

	/**
	 * Converts an array of WorldFlags to a combined bitmask.
	 *
	 * @param flags the world flags
	 * @return the combined bitmask
	 */
	public static long toBitmask(WorldFlag... flags) {
		return addFlag(flags);
	}

	/**
	 * Converts an array of ControlFlags to a combined bitmask.
	 *
	 * @param flags the control flags
	 * @return the combined bitmask
	 */
	public static long toBitmask(ControlFlag... flags) {
		return addFlag(flags);
	}

	/**
	 * Returns a list of PlayerFlag enums that are set in the given bitmask.
	 *
	 * @param flags the combined bitmask
	 * @return list of set PlayerFlag enums
	 */
	public static List<PlayerFlag> getSetPlayerFlags(long flags) {
		List<PlayerFlag> set = new ArrayList<>();
		for (PlayerFlag flag : PlayerFlag.values()) {
			if (isFlagSet(flags, flag)) {
				set.add(flag);
			}
		}
		return set;
	}

	/**
	 * Returns a list of WorldFlag enums that are set in the given bitmask.
	 *
	 * @param flags the combined bitmask
	 * @return list of set WorldFlag enums
	 */
	public static List<WorldFlag> getSetWorldFlags(long flags) {
		List<WorldFlag> set = new ArrayList<>();
		for (WorldFlag flag : WorldFlag.values()) {
			if (isFlagSet(flags, flag)) {
				set.add(flag);
			}
		}
		return set;
	}

	/**
	 * Returns a list of ControlFlag enums that are set in the given bitmask.
	 *
	 * @param flags the combined bitmask
	 * @return list of set ControlFlag enums
	 */
	public static List<ControlFlag> getSetControlFlags(long flags) {
		List<ControlFlag> set = new ArrayList<>();
		for (ControlFlag flag : ControlFlag.values()) {
			if (isFlagSet(flags, flag)) {
				set.add(flag);
			}
		}
		return set;
	}

	/**
	 * Returns a list of PlayerFlag enums that are NOT set in the given bitmask.
	 *
	 * @param flags the combined bitmask
	 * @return list of unset PlayerFlag enums
	 */
	public static List<PlayerFlag> getUnsetPlayerFlags(long flags) {
		List<PlayerFlag> unset = new ArrayList<>();
		for (PlayerFlag flag : PlayerFlag.values()) {
			if (!isFlagSet(flags, flag)) {
				unset.add(flag);
			}
		}
		return unset;
	}

	/**
	 * Returns a list of WorldFlag enums that are NOT set in the given bitmask.
	 *
	 * @param flags the combined bitmask
	 * @return list of unset WorldFlag enums
	 */
	public static List<WorldFlag> getUnsetWorldFlags(long flags) {
		List<WorldFlag> unset = new ArrayList<>();
		for (WorldFlag flag : WorldFlag.values()) {
			if (!isFlagSet(flags, flag)) {
				unset.add(flag);
			}
		}
		return unset;
	}

	/**
	 * Returns a list of ControlFlag enums that are NOT set in the given bitmask.
	 *
	 * @param flags the combined bitmask
	 * @return list of unset ControlFlag enums
	 */
	public static List<ControlFlag> getUnsetControlFlags(long flags) {
		List<ControlFlag> unset = new ArrayList<>();
		for (ControlFlag flag : ControlFlag.values()) {
			if (!isFlagSet(flags, flag)) {
				unset.add(flag);
			}
		}
		return unset;
	}
}