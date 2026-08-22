package me.tayebyassine.homestead.flags;

import java.util.*;

/**
 * Enum representing player flags that control individual player permissions within a region.
 * <p>
 * Player flags govern what actions a specific player can perform in a region, such as
 * breaking/placing blocks, using containers, interacting with entities, PvP combat,
 * and various other gameplay mechanics. These flags are typically assigned per-member
 * or for everyone within a region. Each flag is represented as a unique bit in a 64-bit integer.
 * </p>
 *
 * @see FlagCalculator
 * @see WorldFlag
 * @see ControlFlag
 */
public enum PlayerFlag {
	/**
	 * Allows breaking blocks in the region.
	 */
	BREAK_BLOCKS(1L, "break-blocks"),

	/**
	 * Allows placing blocks in the region.
	 */
	PLACE_BLOCKS(1L << 1, "place-blocks"),

	/**
	 * Allows opening and accessing containers (chests, furnaces, barrels, etc.).
	 */
	CONTAINERS(1L << 2, "containers"),

	/**
	 * Allows opening and closing wooden doors.
	 */
	DOORS(1L << 3, "doors"),

	/**
	 * Allows opening and closing trapdoors.
	 */
	TRAP_DOORS(1L << 4, "trap-doors"),

	/**
	 * Allows opening and closing fence gates.
	 */
	FENCE_GATES(1L << 5, "fence-gates"),

	/**
	 * Allows using anvils for repairing and renaming items.
	 */
	USE_ANVIL(1L << 6, "use-anvil"),

	/**
	 * Allows interacting with redstone components, excluding buttons, levers, and pressure plates.
	 */
	REDSTONE(1L << 7, "redstone"),

	/**
	 * Allows using levers.
	 */
	LEVERS(1L << 8, "levers"),

	/**
	 * Allows pressing buttons.
	 */
	BUTTONS(1L << 9, "buttons"),

	/**
	 * Allows activating pressure plates.
	 */
	PRESSURE_PLATES(1L << 10, "pressure-plates"),

	/**
	 * Allows ringing bells.
	 */
	USE_BELLS(1L << 11, "use-bells"),

	/**
	 * Allows triggering tripwire hooks.
	 */
	TRIGGER_TRIPWIRE(1L << 12, "trigger-tripwire"),

	/**
	 * Allows Frost Walker enchantment to create ice on water.
	 */
	FROST_WALKER(1L << 13, "frost-walker"),

	/**
	 * Allows harvesting fully grown crops.
	 */
	HARVEST_CROPS(1L << 14, "harvest-crops"),

	/**
	 * Prevents trampling farmland when walking/jumping on it.
	 */
	BLOCK_TRAMPLING(1L << 15, "block-trampling"),

	/**
	 * Allows general interaction with blocks.
	 */
	GENERAL_INTERACTION(1L << 16, "general-interaction"),

	/**
	 * Allows interacting with armor stands.
	 */
	ARMOR_STANDS(1L << 17, "armor-stands"),

	/**
	 * Allows interacting with entities.
	 */
	INTERACT_ENTITIES(1L << 18, "interact-entities"),

	/**
	 * Allows interacting with item frames and glow item frames.
	 */
	ITEM_FRAME_INTERACTION(1L << 19, "item-frame-interaction"),

	/**
	 * Allows damaging passive entities (animals, villagers).
	 */
	DAMAGE_PASSIVE_ENTITIES(1L << 20, "damage-passive-entities"),

	/**
	 * Allows damaging hostile entities (mobs, monsters).
	 */
	DAMAGE_HOSTILE_ENTITIES(1L << 21, "damage-hostile-entities"),

	/**
	 * Allows trading with villagers.
	 */
	TRADE_VILLAGERS(1L << 22, "trade-villagers"),

	/**
	 * Allows igniting blocks.
	 */
	IGNITE(1L << 23, "ignite"),

	/**
	 * Allows entering and controlling vehicles (boats, minecarts, horses).
	 */
	VEHICLES(1L << 24, "vehicles"),

	/**
	 * Allows teleporting to the region's spawn point.
	 */
	TELEPORT_SPAWN(1L << 25, "teleport-spawn"),

	/**
	 * Allows passing through region borders without permission checks.
	 */
	PASSTHROUGH(1L << 26, "passthrough"),

	/**
	 * Allows PvP combat with other players in the region.
	 */
	PVP(1L << 27, "pvp"),

	/**
	 * Allows taking fall damage in the region.
	 */
	TAKE_FALL_DAMAGE(1L << 28, "take-fall-damage"),

	/**
	 * Allows teleporting in the region.
	 */
	TELEPORT(1L << 29, "teleport"),

	/**
	 * Allows throwing splash/lingering potions in the region.
	 */
	THROW_POTIONS(1L << 30, "throw-potions"),

	/**
	 * Allows picking up items from the ground in the region.
	 */
	PICKUP_ITEMS(1L << 31, "pickup-items"),

	/**
	 * Allows sleeping in beds in the region.
	 */
	SLEEP(1L << 32, "sleep"),

	/**
	 * Allows triggering raid events in the region.
	 */
	TRIGGER_RAID(1L << 33, "trigger-raid"),

	/**
	 * Allows using elytra for gliding in the region.
	 */
	ELYTRA(1L << 34, "elytra"),

	/**
	 * Allows spawning entities in the region.
	 */
	SPAWN_ENTITIES(1L << 35, "spawn-entities"),

	/**
	 * Allows punching sulfur cubes (custom gameplay mechanic).
	 */
	PUNCH_SULFUR_CUBES(1L << 36, "punch-sulfur-cubes");

	private final long bitmask;
	private final String name;

	PlayerFlag(long bitmask, String name) {
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

	private static final Map<String, PlayerFlag> BY_NAME = new HashMap<>();

	static {
		for (PlayerFlag flag : values()) {
			BY_NAME.put(flag.name.toLowerCase(), flag);
		}
	}

	/**
	 * Returns a sorted list of all player flag config names.
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
		PlayerFlag flag = BY_NAME.get(name.toLowerCase());
		return flag != null ? flag.bitmask : 0;
	}

	/**
	 * Returns the config name of a flag given its bitmask value.
	 *
	 * @param flag the bitmask value
	 * @return the config name, or null if not found
	 */
	public static String from(long flag) {
		for (PlayerFlag f : values()) {
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
		for (PlayerFlag f : values()) {
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
		for (PlayerFlag f : values()) {
			if (!FlagCalculator.isFlagSet(flags, f.bitmask)) {
				disabled.add(f.name);
			}
		}
		return disabled;
	}
}