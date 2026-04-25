package tfagaming.projects.minecraft.homestead.database.providers;

import org.bukkit.Bukkit;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.*;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;

import java.util.UUID;

final class LegacyParsers {
	private LegacyParsers() {
	}

	static void splitAndParse(String raw, String sep, ThrowingConsumer<String> consumer) {
		if (raw == null || raw.isBlank()) return;
		for (String part : raw.split(sep)) {
			if (part == null || part.isBlank()) continue;
			try {
				consumer.accept(part);
			} catch (Exception ignored) {
			}
		}
	}

	static RegionChunk parseLegacyChunk(long regionId, String s) {
		try {
			String[] p = s.split(",");
			UUID worldId = UUID.fromString(p[0].trim());
			int x = Integer.parseInt(p[1].trim());
			int z = Integer.parseInt(p[2].trim());
			long claimedAt = p.length > 3 ? Long.parseLong(p[3].trim()) : System.currentTimeMillis();
			return new RegionChunk(Homestead.SNOWFLAKE.nextId(), regionId, worldId, x, z, claimedAt, false);
		} catch (Exception e) {
			return null;
		}
	}

	static RegionMember parseLegacyMember(long linkageId, RegionMember.LinkageType type, String s) {
		try {
			String[] p = s.split(",");
			UUID playerId = UUID.fromString(p[0].trim());
			long pFlags = p.length > 1 ? Long.parseLong(p[1].trim()) : 0L;
			long cFlags = p.length > 2 ? Long.parseLong(p[2].trim()) : 0L;
			long joinedAt = p.length > 3 ? Long.parseLong(p[3].trim()) : System.currentTimeMillis();
			long taxesAt = p.length > 4 ? Long.parseLong(p[4].trim()) : 0L;

			RegionMember member = new RegionMember(playerId, type, linkageId);
			member.setPlayerFlags(pFlags);
			member.setControlFlags(cFlags);
			member.setJoinedAt(joinedAt);
			member.setTaxesAt(taxesAt);
			return member;
		} catch (Exception e) {
			return null;
		}
	}

	static RegionRate parseLegacyRate(long regionId, String s) {
		try {
			String[] p = s.split(",");
			UUID playerId = UUID.fromString(p[0].trim());
			int rate = Integer.parseInt(p[1].trim());
			long ratedAt = Long.parseLong(p[2].trim());
			return new RegionRate(Homestead.SNOWFLAKE.nextId(), regionId, playerId, rate, ratedAt);
		} catch (Exception e) {
			return null;
		}
	}

	static RegionBan parseLegacyBannedPlayer(long regionId, String s) {
		try {
			String[] p = s.split(",", 3);
			UUID playerId = UUID.fromString(p[0].trim());
			String reason = p.length > 1 ? p[1] : null;
			long bannedAt = p.length > 2 ? Long.parseLong(p[2].trim()) : System.currentTimeMillis();
			return new RegionBan(Homestead.SNOWFLAKE.nextId(), regionId, playerId, reason, bannedAt);
		} catch (Exception e) {
			return null;
		}
	}

	static RegionLog parseLegacyLog(long regionId, String s) {
		try {
			String[] p = s.split("§", 4);
			String author = p[0];
			String message = p.length > 1 ? p[1] : "";
			long sentAt = p.length > 2 ? Long.parseLong(p[2].trim()) : System.currentTimeMillis();
			boolean read = p.length > 3 && Boolean.parseBoolean(p[3].trim());
			return new RegionLog(Homestead.SNOWFLAKE.nextId(), regionId, author, message, sentAt, read);
		} catch (Exception e) {
			return null;
		}
	}

	static SeBlock parseLegacyBlock(UUID worldId, String s) {
		try {
			String[] p = s.split(",");
			int x = Integer.parseInt(p[0].trim());
			int y = Integer.parseInt(p[1].trim());
			int z = Integer.parseInt(p[2].trim());
			return new SeBlock(worldId, x, y, z);
		} catch (Exception e) {
			return null;
		}
	}

	static UUID resolveWorldUUID(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			return UUID.fromString(value.trim());
		} catch (IllegalArgumentException e) {
			var world = Bukkit.getWorld(value.trim());
			return world != null ? world.getUID() : null;
		}
	}

	static boolean isNotBlank(String s) {
		return s != null && !s.isBlank();
	}

	@FunctionalInterface
	interface ThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}
}

