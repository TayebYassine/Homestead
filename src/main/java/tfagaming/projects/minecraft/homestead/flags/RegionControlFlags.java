package tfagaming.projects.minecraft.homestead.flags;

import java.util.*;

public final class RegionControlFlags {

	public static final long TRUST_PLAYERS        = 1L << 0;
	public static final long UNTRUST_PLAYERS      = 1L << 1;
	public static final long BAN_PLAYERS          = 1L << 2;
	public static final long UNBAN_PLAYERS        = 1L << 3;
	public static final long CLAIM_CHUNKS         = 1L << 4;
	public static final long UNCLAIM_CHUNKS       = 1L << 5;
	public static final long DEPOSIT_MONEY        = 1L << 6;
	public static final long WITHDRAW_MONEY       = 1L << 7;
	public static final long SET_GLOBAL_FLAGS     = 1L << 8;
	public static final long SET_WORLD_FLAGS      = 1L << 9;
	public static final long SET_MEMBER_FLAGS     = 1L << 10;
	public static final long SET_SPAWN            = 1L << 11;
	public static final long MANAGE_SUBAREAS      = 1L << 12;
	public static final long RENAME_REGION        = 1L << 13;
	public static final long SET_DESCRIPTION      = 1L << 14;
	public static final long MANAGE_LOGS          = 1L << 15;
	public static final long KICK_PLAYERS         = 1L << 16;
	public static final long SET_WEATHER_AND_TIME = 1L << 17;

	public static final Map<String, Long> MAPPED_FLAGS = new LinkedHashMap<>();
	static {
		MAPPED_FLAGS.put("trust-players",        TRUST_PLAYERS);
		MAPPED_FLAGS.put("untrust-players",      UNTRUST_PLAYERS);
		MAPPED_FLAGS.put("ban-players",          BAN_PLAYERS);
		MAPPED_FLAGS.put("unban-players",        UNBAN_PLAYERS);
		MAPPED_FLAGS.put("claim-chunks",         CLAIM_CHUNKS);
		MAPPED_FLAGS.put("unclaim-chunks",       UNCLAIM_CHUNKS);
		MAPPED_FLAGS.put("deposit-money",        DEPOSIT_MONEY);
		MAPPED_FLAGS.put("withdraw-money",       WITHDRAW_MONEY);
		MAPPED_FLAGS.put("set-global-flags",     SET_GLOBAL_FLAGS);
		MAPPED_FLAGS.put("set-world-flags",      SET_WORLD_FLAGS);
		MAPPED_FLAGS.put("set-member-flags",     SET_MEMBER_FLAGS);
		MAPPED_FLAGS.put("set-spawn",            SET_SPAWN);
		MAPPED_FLAGS.put("manage-subareas",      MANAGE_SUBAREAS);
		MAPPED_FLAGS.put("rename-region",        RENAME_REGION);
		MAPPED_FLAGS.put("set-description",      SET_DESCRIPTION);
		MAPPED_FLAGS.put("manage-logs",          MANAGE_LOGS);
		MAPPED_FLAGS.put("kick-players",         KICK_PLAYERS);
		MAPPED_FLAGS.put("set-weather-and-time", SET_WEATHER_AND_TIME);
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