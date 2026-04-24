package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;
import tfagaming.projects.minecraft.homestead.resources.Resources;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionInvite}.
 */
public final class InviteManager {
	private InviteManager() {}

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

		Homestead.regionInviteCache.putOrUpdate(invite);
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
		return Homestead.regionInviteCache.getAll().stream()
				.filter(i -> i.getRegionId() == regionId)
				.collect(Collectors.toList());
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
		return Homestead.regionInviteCache.getAll().stream()
				.filter(i -> i.getPlayerId().equals(playerId))
				.collect(Collectors.toList());
	}

	/**
	 * Delete a player invitation.
	 * @param id The invite ID
	 */
	public static void deleteInvite(long id) {
		Homestead.regionInviteCache.remove(id);
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
			deleteInvite(invite.getRegionId());
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
}