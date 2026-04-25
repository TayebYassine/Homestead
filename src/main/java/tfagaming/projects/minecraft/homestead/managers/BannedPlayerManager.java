package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionBannedPlayer;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionBannedPlayer}.
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
		RegionBannedPlayer ban = new RegionBannedPlayer(regionId, player, reason);
		Homestead.regionBannedPlayerCache.putOrUpdate(ban);
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
		Homestead.regionBannedPlayerCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId && b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.ifPresent(b -> Homestead.regionBannedPlayerCache.remove(b.getUniqueId()));
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
	 * Returns an instance of {@link RegionBannedPlayer} of a banned player from a region.
	 * @param region The region
	 * @param player The player
	 * @return {@link RegionBannedPlayer} if the player is banned, {@code null} otherwise.
	 */
	public static RegionBannedPlayer getBannedPlayer(Region region, OfflinePlayer player) {
		return getBannedPlayer(region.getUniqueId(), player);
	}

	/**
	 * Returns an instance of {@link RegionBannedPlayer} of a banned player from a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @return {@link RegionBannedPlayer} if the player is banned, {@code null} otherwise.
	 */
	public static RegionBannedPlayer getBannedPlayer(long regionId, OfflinePlayer player) {
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
	public static List<RegionBannedPlayer> getBansOfRegion(Region region) {
		return getBansOfRegion(region.getUniqueId());
	}

	/**
	 * Returns a list of banned players from a region.
	 * @param regionId The region ID
	 * @return List of banned players from a region.
	 */
	public static List<RegionBannedPlayer> getBansOfRegion(long regionId) {
		return Homestead.regionBannedPlayerCache.getAll().stream()
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
		Homestead.regionBannedPlayerCache.getAll().stream()
				.filter(b -> b.getRegionId() == regionId)
				.findFirst()
				.ifPresent(b -> Homestead.regionBannedPlayerCache.remove(b.getUniqueId()));
	}
}