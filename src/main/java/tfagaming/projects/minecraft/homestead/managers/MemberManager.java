package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.RegionMember.LinkageType;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionMember}.
 */
public final class MemberManager {
	private MemberManager() {
	}

	/**
	 * Retrieves a member by its unique ID.
	 * @param id The member ID
	 * @return The {@link RegionMember}, or {@code null} if not found.
	 */
	public static RegionMember getMember(long id) {
		return Homestead.MEMBER_CACHE.get(id);
	}

	/**
	 * Returns an immutable view of every loaded member.
	 * @return List of all members.
	 */
	public static List<RegionMember> getAllMembers() {
		return Homestead.MEMBER_CACHE.getAll();
	}

	/**
	 * Add a player as member of a region or a member of a sub-area.
	 * @param player The player
	 * @param type The linkage type from {@link LinkageType}
	 * @param linkageId The linkage ID; region ID or sub-area ID
	 * @return Instance of the new member
	 */
	public static RegionMember addMember(OfflinePlayer player, LinkageType type, long linkageId) {
		RegionMember member = new RegionMember(player, type, linkageId);

		switch (type) {
			case REGION -> {
				if (getFlagsConfig().allowFlagsOnPlayerTrust()) {
					member.setPlayerFlags(getFlagsConfig().getAllAllowedPlayerFlagsExcludeDisabledOnes());
				} else {
					Region region = RegionManager.findRegion(linkageId);

					if (region != null) member.setPlayerFlags(region.getPlayerFlags());
				}
			}
			case SUBAREA -> {
				if (getFlagsConfig().allowFlagsOnPlayerTrust()) {
					member.setPlayerFlags(getFlagsConfig().getAllAllowedPlayerFlagsExcludeDisabledOnes());
				} else {
					SubArea subArea = SubAreaManager.findSubArea(linkageId);

					if (subArea != null) member.setPlayerFlags(subArea.getPlayerFlags());
				}
			}
		}

		Homestead.MEMBER_CACHE.putOrUpdate(member);
		return member;
	}

	private static FlagsFile getFlagsConfig() {
		return Resources.get(ResourceType.Flags);
	}

	/**
	 * Add a player as a member of a region.
	 * @param player The player
	 * @param region The region
	 * @return Instance of the new member
	 */
	public static RegionMember addMemberToRegion(OfflinePlayer player, Region region) {
		return addMemberToRegion(player, region.getUniqueId());
	}

	/**
	 * Add a player as a member of a region.
	 * @param player The player
	 * @param regionId The region ID
	 * @return Instance of the new member
	 */
	public static RegionMember addMemberToRegion(OfflinePlayer player, long regionId) {
		return addMember(player, LinkageType.REGION, regionId);
	}

	/**
	 * Add a player as a member of a sub-area.
	 * @param player The player
	 * @param subArea The sub-area
	 * @return Instance of the new member
	 */
	public static RegionMember addMemberToSubArea(OfflinePlayer player, SubArea subArea) {
		return addMemberToSubArea(player, subArea.getUniqueId());
	}

	/**
	 * Add a player as a member of a sub-area.
	 * @param player The player
	 * @param subAreaId The sub-area ID
	 * @return Instance of the new member
	 */
	public static RegionMember addMemberToSubArea(OfflinePlayer player, long subAreaId) {
		return addMember(player, LinkageType.SUBAREA, subAreaId);
	}

	/**
	 * Returns list of members of a region.
	 * @param region The region
	 * @return List of members of a region
	 */
	public static List<RegionMember> getMembersOfRegion(Region region) {
		return getMembersOfRegion(region.getUniqueId());
	}

	/**
	 * Returns list of members of a region.
	 * @param regionId The region ID
	 * @return List of members of a region
	 */
	public static List<RegionMember> getMembersOfRegion(long regionId) {
		return Homestead.MEMBER_CACHE.getAll().stream()
				.filter(m -> m.getRegionId() == regionId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of members in the server.
	 * @return Member count.
	 */
	public static int getMemberCount() {
		return getAllMembers().size();
	}

	/**
	 * Returns the number of members in a region.
	 * @param region The region
	 * @return Member count.
	 */
	public static int getMemberCount(Region region) {
		return getMemberCount(region.getUniqueId());
	}

	/**
	 * Returns the number of members in a region.
	 * @param regionId The region ID
	 * @return Member count.
	 */
	public static int getMemberCount(long regionId) {
		return getMembersOfRegion(regionId).size();
	}

	/**
	 * Returns list of members of a sub-area.
	 * @param subArea The sub-area
	 * @return List of members of a sub-area
	 */
	public static List<RegionMember> getMembersOfSubArea(SubArea subArea) {
		return getMembersOfSubArea(subArea.getUniqueId());
	}

	/**
	 * Returns list of members of a sub-area.
	 * @param subAreaId The sub-area ID
	 * @return List of members of a sub-area
	 */
	public static List<RegionMember> getMembersOfSubArea(long subAreaId) {
		return Homestead.MEMBER_CACHE.getAll().stream()
				.filter(m -> m.getSubAreaId() == subAreaId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the number of members in a sub-area.
	 * @param subArea The sub-area
	 * @return Member count.
	 */
	public static int getSubAreaMemberCount(SubArea subArea) {
		return getSubAreaMemberCount(subArea.getUniqueId());
	}

	/**
	 * Returns the number of members in a sub-area.
	 * @param subAreaId The sub-area ID
	 * @return Member count.
	 */
	public static int getSubAreaMemberCount(long subAreaId) {
		return getMembersOfSubArea(subAreaId).size();
	}

	/**
	 * Returns all memberships for a specific player across regions and sub-areas.
	 * @param player The player
	 * @return List of all member entries.
	 */
	public static List<RegionMember> getAllMembersOfPlayer(OfflinePlayer player) {
		return getAllMembersOfPlayer(player.getUniqueId());
	}

	/**
	 * Returns all memberships for a specific player across regions and sub-areas.
	 * @param playerId The player UUID
	 * @return List of all member entries.
	 */
	public static List<RegionMember> getAllMembersOfPlayer(UUID playerId) {
		return Homestead.MEMBER_CACHE.getAll().stream()
				.filter(m -> m.getPlayerId().equals(playerId))
				.collect(Collectors.toList());
	}

	/**
	 * Returns all region IDs that a player is a member of.
	 * @param player The player
	 * @return List of region IDs.
	 */
	public static List<Long> getRegionsOfPlayer(OfflinePlayer player) {
		return getAllMembersOfPlayer(player).stream()
				.filter(m -> m.getLinkageType() == LinkageType.REGION)
				.map(RegionMember::getRegionId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Returns all sub-area IDs that a player is a member of.
	 * @param player The player
	 * @return List of sub-area IDs.
	 */
	public static List<Long> getSubAreasOfPlayer(OfflinePlayer player) {
		return getAllMembersOfPlayer(player).stream()
				.filter(m -> m.getLinkageType() == LinkageType.SUBAREA)
				.map(RegionMember::getSubAreaId)
				.distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Returns only online members of a region.
	 * @param region The region
	 * @return List of online players.
	 */
	public static List<Player> getOnlineMembers(Region region) {
		return getOnlineMembers(region.getUniqueId());
	}

	/**
	 * Returns only online members of a region.
	 * @param regionId The region ID
	 * @return List of online players.
	 */
	public static List<Player> getOnlineMembers(long regionId) {
		List<Player> online = new ArrayList<>();
		for (RegionMember member : getMembersOfRegion(regionId)) {
			Player player = Bukkit.getPlayer(member.getPlayerId());
			if (player != null && player.isOnline()) {
				online.add(player);
			}
		}
		return online;
	}

	/**
	 * Returns the display names of all members in a region for GUI usage.
	 * @param region The region
	 * @return List of player names.
	 */
	public static List<String> getMemberNames(Region region) {
		return getMemberNames(region.getUniqueId());
	}

	/**
	 * Returns the display names of all members in a region for GUI usage.
	 * @param regionId The region ID
	 * @return List of player names.
	 */
	public static List<String> getMemberNames(long regionId) {
		return getMembersOfRegion(regionId).stream()
				.map(m -> {
					OfflinePlayer p = m.getPlayer();
					return p != null ? p.getName() : m.getPlayerId().toString();
				})
				.collect(Collectors.toList());
	}

	/**
	 * Remove a member.
	 * @param id The member ID
	 */
	public static void removeMember(long id) {
		Homestead.MEMBER_CACHE.remove(id);
	}

	/**
	 * Remove a member from a region.
	 * @param player The player
	 * @param region The region
	 */
	public static void removeMemberFromRegion(OfflinePlayer player, Region region) {
		removeMemberFromRegion(player, region.getUniqueId());
	}

	/**
	 * Remove a member from a region.
	 * @param player The player
	 * @param regionId The region ID
	 */
	public static void removeMemberFromRegion(OfflinePlayer player, long regionId) {
		for (RegionMember member : getMembersOfRegion(regionId)) {
			if (member.getPlayerId().equals(player.getUniqueId())) {
				removeMember(member.getUniqueId());
			}
		}
	}

	/**
	 * Remove a member from a sub-area.
	 * @param player The player
	 * @param subArea The sub-area
	 */
	public static void removeMemberFromSubArea(OfflinePlayer player, SubArea subArea) {
		removeMemberFromSubArea(player, subArea.getUniqueId());
	}

	/**
	 * Remove a member from a sub-area.
	 * @param player The player
	 * @param subAreaId The sub-area ID
	 */
	public static void removeMemberFromSubArea(OfflinePlayer player, long subAreaId) {
		for (RegionMember member : getMembersOfSubArea(subAreaId)) {
			if (member.getPlayerId().equals(player.getUniqueId())) {
				removeMember(member.getUniqueId());
			}
		}
	}

	/**
	 * Removes all members from a region.
	 * @param region The region
	 * @return The number of members removed.
	 */
	public static int removeAllMembersOfRegion(Region region) {
		return removeAllMembersOfRegion(region.getUniqueId());
	}

	/**
	 * Removes all members from a region.
	 * @param regionId The region ID
	 * @return The number of members removed.
	 */
	public static int removeAllMembersOfRegion(long regionId) {
		List<Long> toRemove = getMembersOfRegion(regionId).stream()
				.map(RegionMember::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			removeMember(id);
		}
		return toRemove.size();
	}

	/**
	 * Removes all members from a sub-area.
	 * @param subArea The sub-area
	 * @return The number of members removed.
	 */
	public static int removeAllMembersOfSubArea(SubArea subArea) {
		return removeAllMembersOfSubArea(subArea.getUniqueId());
	}

	/**
	 * Removes all members from a sub-area.
	 * @param subAreaId The sub-area ID
	 * @return The number of members removed.
	 */
	public static int removeAllMembersOfSubArea(long subAreaId) {
		List<Long> toRemove = getMembersOfSubArea(subAreaId).stream()
				.map(RegionMember::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			removeMember(id);
		}
		return toRemove.size();
	}

	/**
	 * Removes all memberships for a specific player across all regions and sub-areas.
	 * Useful for player quit or ban cleanup.
	 * @param player The player
	 * @return The number of memberships removed.
	 */
	public static int removeAllMembersOfPlayer(OfflinePlayer player) {
		List<Long> toRemove = getAllMembersOfPlayer(player).stream()
				.map(RegionMember::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			removeMember(id);
		}
		return toRemove.size();
	}

	/**
	 * Checks if a player is a member of a region.
	 * @param region The region
	 * @param player The player
	 * @return {@code true} if the player is member, {@code false} otherwise.
	 */
	public static boolean isMemberOfRegion(Region region, OfflinePlayer player) {
		return isMemberOfRegion(region.getUniqueId(), player);
	}

	/**
	 * Checks if a player is a member of a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @return {@code true} if the player is member, {@code false} otherwise.
	 */
	public static boolean isMemberOfRegion(long regionId, OfflinePlayer player) {
		return isMemberOfRegion(regionId, player.getUniqueId());
	}

	/**
	 * Checks if a player is a member of a region.
	 * @param regionId The region ID
	 * @param playerId The player UUID
	 * @return {@code true} if the player is member, {@code false} otherwise.
	 */
	public static boolean isMemberOfRegion(long regionId, UUID playerId) {
		return getMembersOfRegion(regionId).stream()
				.anyMatch(b -> b.getPlayerId().equals(playerId));
	}

	/**
	 * Checks if a player is a member of any region.
	 * @param player The player
	 * @return {@code true} if the player is a member of at least one region.
	 */
	public static boolean isMemberOfAnyRegion(OfflinePlayer player) {
		return !getRegionsOfPlayer(player).isEmpty();
	}

	/**
	 * Checks if a player is a member of a sub-area.
	 * @param subArea The sub-area
	 * @param player The player
	 * @return {@code true} if the player is member, {@code false} otherwise.
	 */
	public static boolean isMemberOfSubArea(SubArea subArea, OfflinePlayer player) {
		return isMemberOfSubArea(subArea.getUniqueId(), player);
	}

	/**
	 * Checks if a player is a member of a sub-area.
	 * @param subAreaId The sub-area ID
	 * @param player The player
	 * @return {@code true} if the player is member, {@code false} otherwise.
	 */
	public static boolean isMemberOfSubArea(long subAreaId, OfflinePlayer player) {
		return getMembersOfSubArea(subAreaId).stream()
				.anyMatch(b -> b.getPlayerId().equals(player.getUniqueId()));
	}

	/**
	 * Checks if a player is a member of any sub-area.
	 * @param player The player
	 * @return {@code true} if the player is a member of at least one sub-area.
	 */
	public static boolean isMemberOfAnySubArea(OfflinePlayer player) {
		return !getSubAreasOfPlayer(player).isEmpty();
	}

	/**
	 * Retrieves the member entry for a specific player in a region.
	 * @param region The region
	 * @param player The player
	 * @return The {@link RegionMember}, or {@code null} if not found.
	 */
	public static RegionMember getMemberOfRegion(Region region, OfflinePlayer player) {
		return getMemberOfRegion(region.getUniqueId(), player);
	}

	/**
	 * Retrieves the member entry for a specific player in a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @return The {@link RegionMember}, or {@code null} if not found.
	 */
	public static RegionMember getMemberOfRegion(long regionId, OfflinePlayer player) {
		return getMembersOfRegion(regionId).stream()
				.filter(b -> b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Retrieves the member entry for a specific player in a sub-area.
	 * @param subArea The sub-area
	 * @param player The player
	 * @return The {@link RegionMember}, or {@code null} if not found.
	 */
	public static RegionMember getMemberOfSubArea(SubArea subArea, OfflinePlayer player) {
		return getMemberOfSubArea(subArea.getUniqueId(), player);
	}

	/**
	 * Retrieves the member entry for a specific player in a sub-area.
	 * @param subAreaId The sub-area ID
	 * @param player The player
	 * @return The {@link RegionMember}, or {@code null} if not found.
	 */
	public static RegionMember getMemberOfSubArea(long subAreaId, OfflinePlayer player) {
		return getMembersOfSubArea(subAreaId).stream()
				.filter(b -> b.getPlayerId().equals(player.getUniqueId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Sets the player flags for a member in a region.
	 * @param region The region
	 * @param player The player
	 * @param flags The new flag bitmask
	 * @return {@code true} if the member was found and updated.
	 */
	public static boolean setPlayerFlags(Region region, OfflinePlayer player, long flags) {
		return setPlayerFlags(region.getUniqueId(), player, flags);
	}

	/**
	 * Sets the player flags for a member in a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @param flags The new flag bitmask
	 * @return {@code true} if the member was found and updated.
	 */
	public static boolean setPlayerFlags(long regionId, OfflinePlayer player, long flags) {
		RegionMember member = getMemberOfRegion(regionId, player);
		if (member == null) return false;
		member.setPlayerFlags(flags);
		return true;
	}

	/**
	 * Sets the control flags for a member in a region.
	 * @param region The region
	 * @param player The player
	 * @param flags The new flag bitmask
	 * @return {@code true} if the member was found and updated.
	 */
	public static boolean setControlFlags(Region region, OfflinePlayer player, long flags) {
		return setControlFlags(region.getUniqueId(), player, flags);
	}

	/**
	 * Sets the control flags for a member in a region.
	 * @param regionId The region ID
	 * @param player The player
	 * @param flags The new flag bitmask
	 * @return {@code true} if the member was found and updated.
	 */
	public static boolean setControlFlags(long regionId, OfflinePlayer player, long flags) {
		RegionMember member = getMemberOfRegion(regionId, player);
		if (member == null) return false;
		member.setControlFlags(flags);
		return true;
	}

	/**
	 * Checks if a member in a region has a specific player flag set.
	 * @param region The region
	 * @param player The player
	 * @param flag The flag to check
	 * @return {@code true} if the flag is set, {@code false} if member not found or flag not set.
	 */
	public static boolean hasPlayerFlag(Region region, OfflinePlayer player, long flag) {
		return hasPlayerFlag(region.getUniqueId(), player, flag);
	}

	/**
	 * Checks if a member in a region has a specific player flag set.
	 * @param regionId The region ID
	 * @param player The player
	 * @param flag The flag to check
	 * @return {@code true} if the flag is set, {@code false} if member not found or flag not set.
	 */
	public static boolean hasPlayerFlag(long regionId, OfflinePlayer player, long flag) {
		RegionMember member = getMemberOfRegion(regionId, player);
		return member != null && (member.getPlayerFlags() & flag) != 0;
	}

	/**
	 * Checks if a member in a region has a specific control flag set.
	 * @param region The region
	 * @param player The player
	 * @param flag The flag to check
	 * @return {@code true} if the flag is set, {@code false} if member not found or flag not set.
	 */
	public static boolean hasControlFlag(Region region, OfflinePlayer player, long flag) {
		return hasControlFlag(region.getUniqueId(), player, flag);
	}

	/**
	 * Checks if a member in a region has a specific control flag set.
	 * @param regionId The region ID
	 * @param player The player
	 * @param flag The flag to check
	 * @return {@code true} if the flag is set, {@code false} if member not found or flag not set.
	 */
	public static boolean hasControlFlag(long regionId, OfflinePlayer player, long flag) {
		RegionMember member = getMemberOfRegion(regionId, player);
		return member != null && (member.getControlFlags() & flag) != 0;
	}

	/**
	 * Removes all member entries with invalid references:<br>
	 * - Players whose UUID no longer maps to a known player<br>
	 * - Regions that no longer exist (for REGION linkage)<br>
	 * - Sub-areas that no longer exist (for SUBAREA linkage)<br>
	 * @return Number of corrupted members removed.
	 */
	public static int cleanupInvalidMembers() {
		List<Long> toRemove = new ArrayList<>();

		for (RegionMember member : Homestead.MEMBER_CACHE.getAll()) {
			OfflinePlayer player = member.getPlayer();

			boolean invalidPlayer = player == null || player.getName() == null;

			boolean invalidLinkage = false;
			if (member.getLinkageType() == RegionMember.LinkageType.REGION) {
				invalidLinkage = member.getRegion() == null;
			} else if (member.getLinkageType() == RegionMember.LinkageType.SUBAREA) {
				invalidLinkage = member.getSubArea() == null;
			}

			if (invalidPlayer || invalidLinkage) {
				toRemove.add(member.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			Homestead.MEMBER_CACHE.remove(id);
		}
		return toRemove.size();
	}
}