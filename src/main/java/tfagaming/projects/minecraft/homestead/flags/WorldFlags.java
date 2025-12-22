package tfagaming.projects.minecraft.homestead.flags;

import java.util.*;

public final class WorldFlags {

	public static final long PASSIVE_ENTITIES_SPAWN = 1L << 0;
	public static final long HOSTILE_ENTITIES_SPAWN = 1L << 1;
	public static final long ENTITIES_GRIEF = 1L << 2;
	public static final long ENTITIES_DAMAGE_ENTITIES = 1L << 3;
	public static final long LEAVES_DECAY = 1L << 4;
	public static final long FIRE_SPREAD = 1L << 5;
	public static final long LIQUID_FLOW = 1L << 6;
	public static final long EXPLOSIONS_DAMAGE = 1L << 7;
	public static final long WITHER_DAMAGE = 1L << 8;
	public static final long WILDERNESS_PISTONS = 1L << 9;
	public static final long WILDERNESS_DISPENSERS = 1L << 10;
	public static final long WILDERNESS_MINECARTS = 1L << 11;
	public static final long PLANT_GROWTH = 1L << 12;
	public static final long GRASS_GROWTH = 1L << 13;
	public static final long SCULK_SPREAD = 1L << 14;
	public static final long PLAYER_GLOWING = 1L << 15;
	public static final long SNOW_MELTING = 1L << 16;
	public static final long ICE_MELTING = 1L << 17;
	public static final long SNOWMAN_TRAILS = 1L << 18;
	public static final long WINDCHARGE_BURST = 1L << 19;
	public static final long WILDERNESS_COPPER_GOLEMS = 1L << 20;
	public static final long WARS = 1L << 21;

	public static final Map<String, Long> MAPPED_FLAGS = new LinkedHashMap<>();

	static {
		MAPPED_FLAGS.put("passive-entities-spawn", PASSIVE_ENTITIES_SPAWN);
		MAPPED_FLAGS.put("hostile-entities-spawn", HOSTILE_ENTITIES_SPAWN);
		MAPPED_FLAGS.put("entities-grief", ENTITIES_GRIEF);
		MAPPED_FLAGS.put("entities-damage-entities", ENTITIES_DAMAGE_ENTITIES);
		MAPPED_FLAGS.put("leaves-decay", LEAVES_DECAY);
		MAPPED_FLAGS.put("fire-spread", FIRE_SPREAD);
		MAPPED_FLAGS.put("liquid-flow", LIQUID_FLOW);
		MAPPED_FLAGS.put("explosions-damage", EXPLOSIONS_DAMAGE);
		MAPPED_FLAGS.put("wither-damage", WITHER_DAMAGE);
		MAPPED_FLAGS.put("wilderness-pistons", WILDERNESS_PISTONS);
		MAPPED_FLAGS.put("wilderness-dispensers", WILDERNESS_DISPENSERS);
		MAPPED_FLAGS.put("wilderness-minecarts", WILDERNESS_MINECARTS);
		MAPPED_FLAGS.put("plant-growth", PLANT_GROWTH);
		MAPPED_FLAGS.put("grass-growth", GRASS_GROWTH);
		MAPPED_FLAGS.put("sculk-spread", SCULK_SPREAD);
		MAPPED_FLAGS.put("player-glowing", PLAYER_GLOWING);
		MAPPED_FLAGS.put("snow-melting", SNOW_MELTING);
		MAPPED_FLAGS.put("ice-melting", ICE_MELTING);
		MAPPED_FLAGS.put("snowman-trails", SNOWMAN_TRAILS);
		MAPPED_FLAGS.put("windcharge-burst", WINDCHARGE_BURST);
		MAPPED_FLAGS.put("wilderness-copper-golems", WILDERNESS_COPPER_GOLEMS);
		MAPPED_FLAGS.put("wars", WARS);
	}

	public static List<String> getFlags() {
		List<String> flags = new ArrayList<>(MAPPED_FLAGS.keySet());

		Collections.sort(flags);

		return flags;
	}

	public static long valueOf(String name) {
		Long val = MAPPED_FLAGS.get(name.toLowerCase(Locale.ROOT));
		return val != null ? val : 0;
	}

	public static String from(long flag) {
		for (Map.Entry<String, Long> e : MAPPED_FLAGS.entrySet()) {
			if (e.getValue() == flag) return e.getKey();
		}
		return "unknown-flag";
	}

	public static List<String> getSet(long flags) {
		List<String> enabled = new ArrayList<>();
		for (Map.Entry<String, Long> e : MAPPED_FLAGS.entrySet()) {
			if (FlagsCalculator.isFlagSet(flags, e.getValue())) enabled.add(e.getKey());
		}
		return enabled;
	}

	public static List<String> getUnset(long flags) {
		List<String> disabled = new ArrayList<>();
		for (Map.Entry<String, Long> e : MAPPED_FLAGS.entrySet()) {
			if (!FlagsCalculator.isFlagSet(flags, e.getValue())) disabled.add(e.getKey());
		}
		return disabled;
	}
}