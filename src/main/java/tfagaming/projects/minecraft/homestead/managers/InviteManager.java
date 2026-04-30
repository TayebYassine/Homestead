package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.BulkDeleteInvitesEvent;
import tfagaming.projects.minecraft.homestead.api.events.InvitePlayerEvent;
import tfagaming.projects.minecraft.homestead.api.events.RevokePlayerInviteEvent;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionInvite}.
 */
public final class InviteManager {
	private InviteManager() {
	}

	/**
	 * Invite a player to a region.
	 * @param region The region
	 * @param player The player
	 */
	public static void invitePlayer(Region region, OfflinePlayer player) {
		invitePlayer(region.getUniqueId(), player);
	}

	/**
	 * Invite a player to a region.
	 * @param regionId The region ID
	 * @param player The player
	 */
	public static void invitePlayer(long regionId, OfflinePlayer player) {
		RegionInvite invite = new RegionInvite(regionId, player);
		Homestead.INVITE_CACHE.putOrUpdate(invite);
	}

	/**
	 * Retrieves a specific invite by its unique ID.
	 * @param id The invite ID
	 * @return The {@link RegionInvite}, or {@code null} if not found.
	 */
	public static RegionInvite getInvite(long id) {
		return Homestead.INVITE_CACHE.get(id);
	}

	/**
	 * Retrieves the invite for a specific player in a specific region.
	 * @param region The region
	 * @param player The player
	 * @return The {@link RegionInvite}, or {@code null} if not found.
	 */
	public static RegionInvite getInvite(Region region, OfflinePlayer player) {
		return getInvite(region.getUniqueId(), player.getUniqueId());
	}

	/**
	 * Retrieves the invite for a specific player in a specific region using UUIDs.
	 * @param regionId The region ID
	 * @param playerId The player UUID
	 * @return The {@link RegionInvite}, or {@code null} if not found.
	 */
	public static RegionInvite getInvite(long regionId, UUID playerId) {
		return getInvitesOfRegion(regionId).stream()
				.filter(i -> i.getPlayerId().equals(playerId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Returns a list of invites created by a region.
	 * @param region The region
	 * @return List of invites
	 */
	public static List<RegionInvite> getInvitesOfRegion(Region region) {
		return getInvitesOfRegion(region.getUniqueId());
	}

	/**
	 * Returns a list of invites created by a region.
	 * @param regionId The region ID
	 * @return List of invites
	 */
	public static List<RegionInvite> getInvitesOfRegion(long regionId) {
		return Homestead.INVITE_CACHE.getAll().stream()
				.filter(i -> i.getRegionId() == regionId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of pending invites in the server.
	 * @return The invite count.
	 */
	public static int getInviteCount() {
		return Homestead.INVITE_CACHE.getAll().size();
	}

	/**
	 * Returns the number of pending invites for a region.
	 * @param region The region
	 * @return The invite count.
	 */
	public static int getInviteCount(Region region) {
		return getInviteCount(region.getUniqueId());
	}

	/**
	 * Returns the number of pending invites for a region.
	 * @param regionId The region ID
	 * @return The invite count.
	 */
	public static int getInviteCount(long regionId) {
		return (int) Homestead.INVITE_CACHE.getAll().stream()
				.filter(i -> i.getRegionId() == regionId)
				.count();
	}

	/**
	 * Returns a list of invites that invited a specific player.
	 * @param player The player
	 * @return List of invites
	 */
	public static List<RegionInvite> getInvitesOfPlayer(OfflinePlayer player) {
		return getInvitesOfPlayer(player.getUniqueId());
	}

	/**
	 * Returns a list of invites that invited a specific player.
	 * @param playerId The player UUID
	 * @return List of invites
	 */
	public static List<RegionInvite> getInvitesOfPlayer(UUID playerId) {
		return Homestead.INVITE_CACHE.getAll().stream()
				.filter(i -> i.getPlayerId().equals(playerId))
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of pending invites a player has across all regions.
	 * @param player The player
	 * @return The invite count.
	 */
	public static int getInviteCountOfPlayer(OfflinePlayer player) {
		return getInvitesOfPlayer(player).size();
	}

	/**
	 * Returns all unique player UUIDs that have been invited to a region.
	 * @param region The region
	 * @return List of player UUIDs.
	 */
	public static List<UUID> getInvitedPlayers(Region region) {
		return getInvitedPlayers(region.getUniqueId());
	}

	/**
	 * Returns all unique player UUIDs that have been invited to a region.
	 * @param regionId The region ID
	 * @return List of player UUIDs.
	 */
	public static List<UUID> getInvitedPlayers(long regionId) {
		return getInvitesOfRegion(regionId).stream()
				.map(RegionInvite::getPlayerId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Returns all region IDs that a player has been invited to.
	 * @param player The player
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsInvitedTo(OfflinePlayer player) {
		return getRegionsInvitedTo(player.getUniqueId());
	}

	/**
	 * Returns all region IDs that a player has been invited to.
	 * @param playerId The player UUID
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsInvitedTo(UUID playerId) {
		return getInvitesOfPlayer(playerId).stream()
				.map(RegionInvite::getRegionId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Returns the most recent invites for a region, sorted newest first.
	 * @param region The region
	 * @param limit Maximum number of results
	 * @return List of recent invites.
	 */
	public static List<RegionInvite> getRecentInvites(Region region, int limit) {
		return getRecentInvites(region.getUniqueId(), limit);
	}

	/**
	 * Returns the most recent invites for a region, sorted newest first.
	 * @param regionId The region ID
	 * @param limit Maximum number of results
	 * @return List of recent invites.
	 */
	public static List<RegionInvite> getRecentInvites(long regionId, int limit) {
		return getInvitesOfRegion(regionId).stream()
				.sorted(Comparator.comparingLong(RegionInvite::getInvitedAt).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	/**
	 * Delete a player invitation.
	 * @param id The invite ID
	 */
	public static void deleteInvite(long id) {
		Homestead.INVITE_CACHE.remove(id);
	}

	/**
	 * Deletes all invites created by a region.
	 * @param region The region
	 */
	public static void deleteInvitesOfRegion(Region region) {
		deleteInvitesOfRegion(region.getUniqueId());
	}

	/**
	 * Deletes all invites created by a region.
	 * @param regionId The region ID
	 */
	public static void deleteInvitesOfRegion(long regionId) {
		for (RegionInvite invite : getInvitesOfRegion(regionId)) {
			deleteInvite(invite.getUniqueId());
		}
	}

	/**
	 * Deletes all invites that invited a specific player.
	 * @param player The player
	 */
	public static void deleteInvitesOfPlayer(OfflinePlayer player) {
		deleteInvitesOfPlayer(player.getUniqueId());
	}

	/**
	 * Deletes all invites that invited a specific player.
	 * @param playerId The player UUID
	 */
	public static void deleteInvitesOfPlayer(UUID playerId) {
		for (RegionInvite invite : getInvitesOfPlayer(playerId)) {
			deleteInvite(invite.getUniqueId());
		}
	}

	/**
	 * Deletes all invites sent by a region that invited a specific player.
	 * @param region The region
	 * @param player The player
	 */
	public static void deleteInvitesOfPlayer(Region region, OfflinePlayer player) {
		deleteInvitesOfPlayer(region.getUniqueId(), player);
	}

	/**
	 * Deletes all invites sent by a region that invited a specific player.
	 * @param regionId The region ID
	 * @param player The player
	 */
	public static void deleteInvitesOfPlayer(long regionId, OfflinePlayer player) {
		for (RegionInvite invite : getInvitesOfPlayer(player)) {
			if (invite.getRegionId() == regionId) {
				deleteInvite(invite.getUniqueId());
			}
		}
	}

	/**
	 * Checks if a player is invited by a region.
	 * @param region The region
	 * @param player The player
	 * @return {@code true} if the player is invited, {@code false} otherwise.
	 */
	public static boolean isInvited(Region region, OfflinePlayer player) {
		return isInvited(region.getUniqueId(), player);
	}

	/**
	 * Checks if a player is invited by a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @return {@code true} if the player is invited, {@code false} otherwise.
	 */
	public static boolean isInvited(long regionId, OfflinePlayer player) {
		return getInvitesOfRegion(regionId).stream()
				.anyMatch(i -> i.getPlayerId().equals(player.getUniqueId()));
	}

	/**
	 * Checks if a player has a pending invite from a region.
	 * This is an alias for {@link #isInvited(Region, OfflinePlayer)} for semantic clarity.
	 * @param region The region
	 * @param player The player
	 * @return {@code true} if a pending invite exists.
	 */
	public static boolean hasPendingInvite(Region region, OfflinePlayer player) {
		return isInvited(region, player);
	}

	/**
	 * Checks if an invitation has expired based on a maximum age in milliseconds.
	 * @param invite The invite
	 * @param maxAgeMillis Maximum age in milliseconds
	 * @return {@code true} if the invite is older than the specified age.
	 */
	public static boolean isInviteExpired(RegionInvite invite, long maxAgeMillis) {
		return System.currentTimeMillis() - invite.getInvitedAt() > maxAgeMillis;
	}

	/**
	 * Deletes all invites older than the specified maximum age.
	 * @param maxAge Maximum age in seconds
	 * @return The number of deleted invites.
	 */
	public static int deleteExpiredInvites(long maxAge) {
		long now = System.currentTimeMillis();
		List<Long> toRemove = Homestead.INVITE_CACHE.getAll().stream()
				.filter(i -> now - i.getInvitedAt() > maxAge * 1000)
				.map(RegionInvite::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			deleteInvite(id);
		}
		return toRemove.size();
	}

	/**
	 * Deletes every invite in the cache. Use with caution.
	 * @return The number of deleted invites.
	 */
	public static int deleteAllInvites() {
		List<Long> ids = Homestead.INVITE_CACHE.getAll().stream()
				.map(RegionInvite::getUniqueId)
				.toList();

		for (Long id : ids) {
			deleteInvite(id);
		}
		return ids.size();
	}

	/**
	 * Removes all invites with invalid references:<br>
	 * - Players whose UUID no longer maps to a known player<br>
	 * - Regions that no longer exist
	 * @return Number of corrupted invites removed.
	 */
	public static int cleanupInvalidInvites() {
		List<Long> toRemove = new ArrayList<>();

		for (RegionInvite invite : Homestead.INVITE_CACHE.getAll()) {
			OfflinePlayer player = invite.getPlayer();

			boolean invalidRegion = invite.getRegion() == null;
			boolean invalidPlayer = player == null || player.getName() == null;

			if (invalidRegion || invalidPlayer) {
				toRemove.add(invite.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			deleteInvite(id);
		}
		return toRemove.size();
	}
}