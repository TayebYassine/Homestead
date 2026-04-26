package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionBan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionBan}.
 */
public final class BanManager {
	private BanManager() {}

	/**
	 * Ban a player from accessing to a region.
	 * @param region The region
	 * @param player The player
	 * @param reason The reason
	 */
	public static void banPlayer(Region region, OfflinePlayer player, @Nullable String reason) {
		banPlayer(region.getUniqueId(), player, reason);
	}

	/**
	 * Ban a player from accessing to a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @param reason The reason
	 */
	public static void banPlayer(long regionId, OfflinePlayer player, @Nullable String reason) {
		RegionBan ban = new RegionBan(regionId, player, reason);
		Homestead.regionBanCache.putOrUpdate(ban);
	}

	/**
	 * Unban a player.
	 * @param region The region
	 * @param player The player
	 */
	public static void unbanPlayer(Region region, OfflinePlayer player) {
		unbanPlayer(region.getUniqueId(), player);
	}

	/**
	 * Unban a player.
	 * @param regionId The region ID
	 * @param player The player
	 */
	public static void unbanPlayer(long regionId, OfflinePlayer player) {
		Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId && b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.ifPresent(b -> Homestead.regionBanCache.remove(b.getUniqueId()));
	}

	/**
	 * Unban a player using raw UUIDs (useful when the player is completely offline and no OfflinePlayer exists).
	 * @param regionId The region ID
	 * @param playerId The player UUID
	 */
	public static void unbanPlayer(long regionId, UUID playerId) {
		Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId && b.getPlayerId().equals(playerId))
				.findFirst()
				.ifPresent(b -> Homestead.regionBanCache.remove(b.getUniqueId()));
	}

	/**
	 * Checks if a player is currently being banned by a region.
	 * @param region The region
	 * @param player The player
	 * @return {@code true} if the player is banned, {@code false} otherwise.
	 */
	public static boolean isBanned(Region region, OfflinePlayer player) {
		return isBanned(region.getUniqueId(), player);
	}

	/**
	 * Checks if a player is currently being banned by a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @return {@code true} if the player is banned, {@code false} otherwise.
	 */
	public static boolean isBanned(long regionId, OfflinePlayer player) {
		return getBansOfRegion(regionId).stream()
				.anyMatch(b -> b.getPlayerId().equals(player.getUniqueId()));
	}

	/**
	 * Checks if a player is banned from a region using UUIDs only.
	 * @param regionId The region ID
	 * @param playerId The player UUID
	 * @return {@code true} if the player is banned, {@code false} otherwise.
	 */
	public static boolean isBanned(long regionId, UUID playerId) {
		return getBansOfRegion(regionId).stream()
				.anyMatch(b -> b.getPlayerId().equals(playerId));
	}

	/**
	 * Returns an instance of {@link RegionBan} of a banned player from a region.
	 * @param region The region
	 * @param player The player
	 * @return {@link RegionBan} if the player is banned, {@code null} otherwise.
	 */
	public static RegionBan getBannedPlayer(Region region, OfflinePlayer player) {
		return getBannedPlayer(region.getUniqueId(), player);
	}

	/**
	 * Returns an instance of {@link RegionBan} of a banned player from a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @return {@link RegionBan} if the player is banned, {@code null} otherwise.
	 */
	public static RegionBan getBannedPlayer(long regionId, OfflinePlayer player) {
		return getBansOfRegion(regionId).stream()
				.filter(b -> b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Returns an instance of {@link RegionBan} of a banned player from a region using UUIDs only.
	 * @param regionId The region ID
	 * @param playerId The player UUID
	 * @return {@link RegionBan} if the player is banned, {@code null} otherwise.
	 */
	public static RegionBan getBannedPlayer(long regionId, UUID playerId) {
		return getBansOfRegion(regionId).stream()
				.filter(b -> b.getPlayerId().equals(playerId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Returns the ban reason for a player in a region, or {@code null} if not banned.
	 * @param region The region
	 * @param player The player
	 * @return The ban reason, or {@code null} if not banned.
	 */
	public static String getBanReason(Region region, OfflinePlayer player) {
		return getBanReason(region.getUniqueId(), player);
	}

	/**
	 * Returns the ban reason for a player in a region, or {@code null} if not banned.
	 * @param regionId The region ID
	 * @param player The player
	 * @return The ban reason, or {@code null} if not banned.
	 */
	public static String getBanReason(long regionId, OfflinePlayer player) {
		RegionBan ban = getBannedPlayer(regionId, player);
		return ban != null ? ban.getReason() : null;
	}

	/**
	 * Returns the timestamp when a player was banned from a region, or {@code 0} if not banned.
	 * @param region The region
	 * @param player The player
	 * @return The ban timestamp, or {@code 0} if not banned.
	 */
	public static long getBanTime(Region region, OfflinePlayer player) {
		return getBanTime(region.getUniqueId(), player);
	}

	/**
	 * Returns the timestamp when a player was banned from a region, or {@code 0} if not banned.
	 * @param regionId The region ID
	 * @param player The player
	 * @return The ban timestamp, or {@code 0} if not banned.
	 */
	public static long getBanTime(long regionId, OfflinePlayer player) {
		RegionBan ban = getBannedPlayer(regionId, player);
		return ban != null ? ban.getBannedAt() : 0L;
	}

	/**
	 * Updates the ban reason for a player without recreating the ban entry.
	 * @param region The region
	 * @param player The player
	 * @param reason The new reason
	 * @return {@code true} if the ban was found and updated, {@code false} otherwise.
	 */
	public static boolean updateBanReason(Region region, OfflinePlayer player, @Nullable String reason) {
		return updateBanReason(region.getUniqueId(), player, reason);
	}

	/**
	 * Updates the ban reason for a player without recreating the ban entry.
	 * @param regionId The region ID
	 * @param player The player
	 * @param reason The new reason
	 * @return {@code true} if the ban was found and updated, {@code false} otherwise.
	 */
	public static boolean updateBanReason(long regionId, OfflinePlayer player, @Nullable String reason) {
		RegionBan ban = getBannedPlayer(regionId, player);
		if (ban == null) return false;
		ban.setReason(reason);
		return true;
	}

	/**
	 * Returns a list of banned players from a region.
	 * @param region The region
	 * @return List of banned players from a region.
	 */
	public static List<RegionBan> getBansOfRegion(Region region) {
		return getBansOfRegion(region.getUniqueId());
	}

	/**
	 * Returns a list of banned players from a region.
	 * @param regionId The region ID
	 * @return List of banned players from a region.
	 */
	public static List<RegionBan> getBansOfRegion(long regionId) {
		return Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of active bans in a region.
	 * @param region The region
	 * @return The ban count.
	 */
	public static int getBanCount(Region region) {
		return getBanCount(region.getUniqueId());
	}

	/**
	 * Returns the number of active bans in a region.
	 * @param regionId The region ID
	 * @return The ban count.
	 */
	public static int getBanCount(long regionId) {
		return (int) Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.count();
	}

	/**
	 * Returns all regions that a player is currently banned from.
	 * @param player The player
	 * @return List of region IDs the player is banned from.
	 */
	public static List<Long> getBannedRegions(OfflinePlayer player) {
		return getBannedRegions(player.getUniqueId());
	}

	/**
	 * Returns all regions that a player is currently banned from.
	 * @param playerId The player UUID
	 * @return List of region IDs the player is banned from.
	 */
	public static List<Long> getBannedRegions(UUID playerId) {
		return Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getPlayerId().equals(playerId))
				.map(RegionBan::getRegionId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Returns the most recent bans for a region, sorted newest first.
	 * @param region The region
	 * @param limit Maximum number of results
	 * @return List of recent bans.
	 */
	public static List<RegionBan> getRecentBans(Region region, int limit) {
		return getRecentBans(region.getUniqueId(), limit);
	}

	/**
	 * Returns the most recent bans for a region, sorted newest first.
	 * @param regionId The region ID
	 * @param limit Maximum number of results
	 * @return List of recent bans.
	 */
	public static List<RegionBan> getRecentBans(long regionId, int limit) {
		return getBansOfRegion(regionId).stream()
				.sorted(Comparator.comparingLong(RegionBan::getBannedAt).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	/**
	 * Checks if a region has any active bans.
	 * @param region The region
	 * @return {@code true} if at least one ban exists.
	 */
	public static boolean hasActiveBans(Region region) {
		return hasActiveBans(region.getUniqueId());
	}

	/**
	 * Checks if a region has any active bans.
	 * @param regionId The region ID
	 * @return {@code true} if at least one ban exists.
	 */
	public static boolean hasActiveBans(long regionId) {
		return Homestead.regionBanCache.getAll().stream()
				.anyMatch(b -> b.getRegionId() == regionId);
	}

	/**
	 * Returns an immutable view of every loaded ban entry.
	 * @return List of all bans.
	 */
	public static List<RegionBan> getAllBans() {
		return List.copyOf(Homestead.regionBanCache.getAll());
	}

	/**
	 * Unban all players from a region.
	 * @param region The region
	 */
	public static void unbanAllPlayers(Region region) {
		unbanAllPlayers(region.getUniqueId());
	}

	/**
	 * Unban all players from a region.
	 * @param regionId The region ID
	 */
	public static void unbanAllPlayers(long regionId) {
		List<Long> toRemove = Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.map(RegionBan::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			Homestead.regionBanCache.remove(id);
		}
	}

	/**
	 * Removes all ban entries with invalid references:<br>
	 * - Players whose UUID no longer maps to a known player<br>
	 * - Regions that no longer exist
	 * @return Number of corrupted entries removed.
	 */
	public static int cleanupInvalidBans() {
		List<Long> toRemove = new ArrayList<>();

		for (RegionBan ban : Homestead.regionBanCache.getAll()) {
			OfflinePlayer player = ban.getPlayer();

			boolean invalidRegion = ban.getRegion() == null;
			boolean invalidPlayer = player == null || player.getName() == null;

			if (invalidRegion || invalidPlayer) {
				toRemove.add(ban.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			Homestead.regionBanCache.remove(id);
		}
		return toRemove.size();
	}
}