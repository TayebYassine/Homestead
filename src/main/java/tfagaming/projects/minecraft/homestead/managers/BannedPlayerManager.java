package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionBan;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionBan}.
 */
public final class BannedPlayerManager {
	private BannedPlayerManager() {}

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
	 * Unban all players.
	 * @param region The region
	 */
	public static void unbanAllPlayers(Region region) {
		unbanAllPlayers(region.getUniqueId());
	}

	/**
	 * Unban all players.
	 * @param regionId The region ID
	 */
	public static void unbanAllPlayers(long regionId) {
		Homestead.regionBanCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.findFirst()
				.ifPresent(b -> Homestead.regionBanCache.remove(b.getUniqueId()));
	}
}