package me.tayebyassine.homestead.flags;

import java.util.*;

/**
 * Enum representing control flags for region management permissions.
 * <p>
 * Control flags govern administrative actions that members can perform within a region,
 * such as managing other players, modifying region settings, and handling finances.
 * Each flag is represented as a unique bit in a 64-bit integer.
 * </p>
 *
 * @see FlagCalculator
 * @see PlayerFlag
 * @see WorldFlag
 */
public enum ControlFlag {
	/**
	 * Allows trusting players to the region.
	 */
	TRUST_PLAYERS(1L, "trust-players"),

	/**
	 * Allows untrusting players in the region.
	 */
	UNTRUST_PLAYERS(1L << 1, "untrust-players"),

	/**
	 * Allows banning players from the region.
	 */
	BAN_PLAYERS(1L << 2, "ban-players"),

	/**
	 * Allows unbanning previously banned players.
	 */
	UNBAN_PLAYERS(1L << 3, "unban-players"),

	/**
	 * Allows claiming new chunks for the region.
	 */
	CLAIM_CHUNKS(1L << 4, "claim-chunks"),

	/**
	 * Allows unclaiming chunks from the region.
	 */
	UNCLAIM_CHUNKS(1L << 5, "unclaim-chunks"),

	/**
	 * Allows depositing money into the region bank.
	 */
	DEPOSIT_MONEY(1L << 6, "deposit-money"),

	/**
	 * Allows withdrawing money from the region bank.
	 */
	WITHDRAW_MONEY(1L << 7, "withdraw-money"),

	/**
	 * Allows setting global flags that affect all regions.
	 */
	SET_GLOBAL_FLAGS(1L << 8, "set-global-flags"),

	/**
	 * Allows setting world-specific flags for the region.
	 */
	SET_WORLD_FLAGS(1L << 9, "set-world-flags"),

	/**
	 * Allows setting member-specific flags (player flags) for region members.
	 */
	SET_MEMBER_FLAGS(1L << 10, "set-member-flags"),

	/**
	 * Allows setting the region's spawn point.
	 */
	SET_SPAWN(1L << 11, "set-spawn"),

	/**
	 * Allows creating and managing subareas within the region.
	 */
	MANAGE_SUBAREAS(1L << 12, "manage-subareas"),

	/**
	 * Allows renaming the region.
	 */
	RENAME_REGION(1L << 13, "rename-region"),

	/**
	 * Allows setting the region's description.
	 */
	SET_DESCRIPTION(1L << 14, "set-description"),

	/**
	 * Allows managing region logs (viewing, clearing).
	 */
	MANAGE_LOGS(1L << 15, "manage-logs"),

	/**
	 * Allows kicking players from the region.
	 */
	KICK_PLAYERS(1L << 16, "kick-players"),

	/**
	 * Allows setting the region's weather and time.
	 */
	SET_WEATHER_AND_TIME(1L << 17, "set-weather-and-time");

	private final long bitmask;
	private final String name;

	ControlFlag(long bitmask, String name) {
		this.bitmask = bitmask;
		this.name = name;
	}

	/**
	 * Returns the bitmask value of this flag.
	 *
	 * @return the bitmask as a long value
	 */
	public long getBitmask() {
		return bitmask;
	}

	/**
	 * Returns the configuration name of this flag (lowercase with hyphens).
	 *
	 * @return the config name
	 */
	public String getName() {
		return name;
	}

	private static final Map<String, ControlFlag> BY_NAME = new LinkedHashMap<>();

	static {
		for (ControlFlag flag : values()) {
			BY_NAME.put(flag.name.toLowerCase(), flag);
		}
	}

	/**
	 * Returns a sorted list of all control flag config names.
	 *
	 * @return list of flag names
	 */
	public static List<String> getFlags() {
		List<String> flags = new ArrayList<>(BY_NAME.keySet());
		Collections.sort(flags);
		return flags;
	}

	/**
	 * Parses a flag name and returns its bitmask value.
	 *
	 * @param name the config name of the flag (case-insensitive)
	 * @return the bitmask value, or 0 if not found
	 */
	public static long parse(String name) {
		ControlFlag flag = BY_NAME.get(name.toLowerCase());
		return flag != null ? flag.bitmask : 0;
	}

	/**
	 * Returns the config name of a flag given its bitmask value.
	 *
	 * @param flag the bitmask value
	 * @return the config name, or null if not found
	 */
	public static String from(long flag) {
		for (ControlFlag f : values()) {
			if (f.bitmask == flag) return f.name;
		}
		return null;
	}

	/**
	 * Returns a list of flag names that are set (enabled) in the given bitmask.
	 *
	 * @param flags the combined bitmask of flags
	 * @return list of enabled flag names
	 */
	public static List<String> getSet(long flags) {
		List<String> enabled = new ArrayList<>();
		for (ControlFlag f : values()) {
			if (FlagCalculator.isFlagSet(flags, f.bitmask)) {
				enabled.add(f.name);
			}
		}
		return enabled;
	}

	/**
	 * Returns a list of flag names that are not set (disabled) in the given bitmask.
	 *
	 * @param flags the combined bitmask of flags
	 * @return list of disabled flag names
	 */
	public static List<String> getUnset(long flags) {
		List<String> disabled = new ArrayList<>();
		for (ControlFlag f : values()) {
			if (!FlagCalculator.isFlagSet(flags, f.bitmask)) {
				disabled.add(f.name);
			}
		}
		return disabled;
	}
}