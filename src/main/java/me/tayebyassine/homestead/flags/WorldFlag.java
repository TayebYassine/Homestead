package me.tayebyassine.homestead.flags;

import java.util.*;

/**
 * Enum representing world flags that control world-level mechanics within a region.
 * <p>
 * World flags govern environmental and entity behavior such as mob spawning, fire spread,
 * liquid flow, explosions, and other world mechanics. These flags are applied at the
 * region level and affect all players and entities within the region boundaries.
 * Each flag is represented as a unique bit in a 64-bit integer.
 * </p>
 *
 * @see FlagCalculator
 * @see PlayerFlag
 * @see ControlFlag
 */
public enum WorldFlag {
	/**
	 * Allows passive entities (animals, villagers) to spawn naturally in the region.
	 */
	PASSIVE_ENTITY_SPAWN(1L, "passive-entity-spawn"),

	/**
	 * Allows hostile entities (mobs, monsters) to spawn naturally in the region.
	 */
	HOSTILE_ENTITY_SPAWN(1L << 1, "hostile-entity-spawn"),

	/**
	 * Allows entities to grief blocks (e.g., creeper explosions, enderman picking up blocks).
	 */
	ENTITY_GRIEFING(1L << 2, "entity-grief"),

	/**
	 * Allows entities to take damage from environmental sources in the region.
	 */
	ENTITY_DAMAGE(1L << 3, "entity-damage"),

	/**
	 * Allows leaves to decay naturally when their logs are removed.
	 */
	LEAVES_DECAY(1L << 4, "leaves-decay"),

	/**
	 * Allows fire to spread to adjacent flammable blocks.
	 */
	FIRE_SPREAD(1L << 5, "fire-spread"),

	/**
	 * Allows liquids (water, lava) to flow naturally.
	 */
	LIQUID_FLOW(1L << 6, "liquid-flow"),

	/**
	 * Allows explosions to damage blocks in the region.
	 */
	EXPLOSION_DAMAGE(1L << 7, "explosion-damage"),

	/**
	 * Allows the Wither boss to damage blocks in the region.
	 */
	WITHER_DAMAGE(1L << 8, "wither-damage"),

	/**
	 * Allows pistons to function in wilderness areas.
	 */
	WILDERNESS_PISTONS(1L << 9, "wilderness-pistons"),

	/**
	 * Allows dispensers to function in wilderness areas.
	 */
	WILDERNESS_DISPENSERS(1L << 10, "wilderness-dispensers"),

	/**
	 * Allows minecarts to function in wilderness areas.
	 */
	WILDERNESS_MINECARTS(1L << 11, "wilderness-minecarts"),

	/**
	 * Allows plants (crops, saplings) to grow naturally.
	 */
	PLANT_GROWTH(1L << 12, "plant-growth"),

	/**
	 * Allows grass and mycelium to spread to dirt blocks.
	 */
	GRASS_GROWTH(1L << 13, "grass-growth"),

	/**
	 * Allows sculk blocks to spread in the region.
	 */
	SCULK_SPREAD(1L << 14, "sculk-spread"),

	/**
	 * Makes players in the region glow (visible through walls).
	 */
	PLAYER_GLOWING(1L << 15, "player-glowing"),

	/**
	 * Allows snow layers to melt near light sources.
	 */
	SNOW_MELTING(1L << 16, "snow-melting"),

	/**
	 * Allows ice to melt near light sources.
	 */
	ICE_MELTING(1L << 17, "ice-melting"),

	/**
	 * Allows snowmen to leave snow trails as they move.
	 */
	SNOWMAN_TRAILS(1L << 18, "snowman-trails"),

	/**
	 * Allows wind charge burst effects in the region.
	 */
	WINDCHARGE_BURST(1L << 19, "windcharge-burst"),

	/**
	 * Allows copper golems to interact.
	 * @deprecated Use {@link #ENTITY_GRIEFING} instead.
	 */
	@Deprecated
	COPPER_GOLEMS_INTERACTION(1L << 20, "copper-golems-interaction"),

	/**
	 * Allows war mechanics (region vs region combat) in the region.
	 */
	WARS(1L << 21, "wars"),

	/**
	 * Allows projectiles (arrows, fireballs, etc.) to damage blocks or entities in the region.
	 */
	PROJECTILES(1L << 22, "projectiles"),

	/**
	 * Allows snow weather in the region.
	 */
	WEATHER_SNOW(1L << 23, "weather-snow");

	private final long bitmask;
	private final String name;

	WorldFlag(long bitmask, String name) {
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

	private static final Map<String, WorldFlag> BY_NAME = new LinkedHashMap<>();

	static {
		for (WorldFlag flag : values()) {
			BY_NAME.put(flag.name.toLowerCase(), flag);
		}
	}

	/**
	 * Returns a sorted list of all world flag config names.
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
		WorldFlag flag = BY_NAME.get(name.toLowerCase());
		return flag != null ? flag.bitmask : 0;
	}

	/**
	 * Returns the config name of a flag given its bitmask value.
	 *
	 * @param flag the bitmask value
	 * @return the config name, or null if not found
	 */
	public static String from(long flag) {
		for (WorldFlag f : values()) {
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
		for (WorldFlag f : values()) {
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
		for (WorldFlag f : values()) {
			if (!FlagCalculator.isFlagSet(flags, f.bitmask)) {
				disabled.add(f.name);
			}
		}
		return disabled;
	}
}