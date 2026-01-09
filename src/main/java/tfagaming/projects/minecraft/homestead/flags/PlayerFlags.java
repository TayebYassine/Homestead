package tfagaming.projects.minecraft.homestead.flags;

import java.util.*;

public final class PlayerFlags {
	public static final long BREAK_BLOCKS = 1L;
	public static final long PLACE_BLOCKS = 1L << 1;
	public static final long CONTAINERS = 1L << 2;
	public static final long DOORS = 1L << 3;
	public static final long TRAP_DOORS = 1L << 4;
	public static final long FENCE_GATES = 1L << 5;
	public static final long USE_ANVIL = 1L << 6;
	public static final long REDSTONE = 1L << 7;
	public static final long LEVERS = 1L << 8;
	public static final long BUTTONS = 1L << 9;
	public static final long PRESSURE_PLATES = 1L << 10;
	public static final long USE_BELLS = 1L << 11;
	public static final long TRIGGER_TRIPWIRE = 1L << 12;
	public static final long FROST_WALKER = 1L << 13;
	public static final long HARVEST_CROPS = 1L << 14;
	public static final long BLOCK_TRAMPLING = 1L << 15;
	public static final long GENERAL_INTERACTION = 1L << 16;
	public static final long ARMOR_STANDS = 1L << 17;
	public static final long INTERACT_ENTITIES = 1L << 18;
	public static final long ITEM_FRAME_INTERACTION = 1L << 19;
	public static final long DAMAGE_PASSIVE_ENTITIES = 1L << 20;
	public static final long DAMAGE_HOSTILE_ENTITIES = 1L << 21;
	public static final long TRADE_VILLAGERS = 1L << 22;
	public static final long IGNITE = 1L << 23;
	public static final long VEHICLES = 1L << 24;
	public static final long TELEPORT_SPAWN = 1L << 25;
	public static final long PASSTHROUGH = 1L << 26;
	public static final long PVP = 1L << 27;
	public static final long TAKE_FALL_DAMAGE = 1L << 28;
	public static final long TELEPORT = 1L << 29;
	public static final long THROW_POTIONS = 1L << 30;
	public static final long PICKUP_ITEMS = 1L << 31;
	public static final long SLEEP = 1L << 32;
	public static final long TRIGGER_RAID = 1L << 33;
	public static final long ELYTRA = 1L << 34;
	public static final long SPAWN_ENTITIES = 1L << 35;

	public static final Map<String, Long> MAPPED_FLAGS = new HashMap<>();

	static {
		MAPPED_FLAGS.put("pvp", PVP);
		MAPPED_FLAGS.put("passthrough", PASSTHROUGH);
		MAPPED_FLAGS.put("break-blocks", BREAK_BLOCKS);
		MAPPED_FLAGS.put("place-blocks", PLACE_BLOCKS);
		MAPPED_FLAGS.put("containers", CONTAINERS);
		MAPPED_FLAGS.put("doors", DOORS);
		MAPPED_FLAGS.put("trap-doors", TRAP_DOORS);
		MAPPED_FLAGS.put("fence-gates", FENCE_GATES);
		MAPPED_FLAGS.put("use-anvil", USE_ANVIL);
		MAPPED_FLAGS.put("redstone", REDSTONE);
		MAPPED_FLAGS.put("levers", LEVERS);
		MAPPED_FLAGS.put("buttons", BUTTONS);
		MAPPED_FLAGS.put("pressure-plates", PRESSURE_PLATES);
		MAPPED_FLAGS.put("use-bells", USE_BELLS);
		MAPPED_FLAGS.put("trigger-tripwire", TRIGGER_TRIPWIRE);
		MAPPED_FLAGS.put("frost-walker", FROST_WALKER);
		MAPPED_FLAGS.put("harvest-crops", HARVEST_CROPS);
		MAPPED_FLAGS.put("block-trampling", BLOCK_TRAMPLING);
		MAPPED_FLAGS.put("general-interaction", GENERAL_INTERACTION);
		MAPPED_FLAGS.put("armor-stands", ARMOR_STANDS);
		MAPPED_FLAGS.put("interact-entities", INTERACT_ENTITIES);
		MAPPED_FLAGS.put("item-frame-interaction", ITEM_FRAME_INTERACTION);
		MAPPED_FLAGS.put("damage-passive-entities", DAMAGE_PASSIVE_ENTITIES);
		MAPPED_FLAGS.put("damage-hostile-entities", DAMAGE_HOSTILE_ENTITIES);
		MAPPED_FLAGS.put("trade-villagers", TRADE_VILLAGERS);
		MAPPED_FLAGS.put("ignite", IGNITE);
		MAPPED_FLAGS.put("vehicles", VEHICLES);
		MAPPED_FLAGS.put("teleport-spawn", TELEPORT_SPAWN);
		MAPPED_FLAGS.put("take-fall-damage", TAKE_FALL_DAMAGE);
		MAPPED_FLAGS.put("teleport", TELEPORT);
		MAPPED_FLAGS.put("throw-potions", THROW_POTIONS);
		MAPPED_FLAGS.put("pickup-items", PICKUP_ITEMS);
		MAPPED_FLAGS.put("sleep", SLEEP);
		MAPPED_FLAGS.put("trigger-raid", TRIGGER_RAID);
		MAPPED_FLAGS.put("elytra", ELYTRA);
		MAPPED_FLAGS.put("spawn-entities", SPAWN_ENTITIES);
	}

	public static List<String> getFlags() {
		List<String> flags = new ArrayList<>(MAPPED_FLAGS.keySet());

		Collections.sort(flags);

		return flags;
	}

	public static long valueOf(String name) {
		Long val = MAPPED_FLAGS.get(name.toLowerCase());
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
			if (FlagsCalculator.isFlagSet(flags, e.getValue())) {
				enabled.add(e.getKey());
			}
		}
		return enabled;
	}

	public static List<String> getUnset(long flags) {
		List<String> disabled = new ArrayList<>();
		for (Map.Entry<String, Long> e : MAPPED_FLAGS.entrySet()) {
			if (!FlagsCalculator.isFlagSet(flags, e.getValue())) {
				disabled.add(e.getKey());
			}
		}
		return disabled;
	}
}