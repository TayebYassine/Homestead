package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionLog;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.RegionMember.LinkageType;
import tfagaming.projects.minecraft.homestead.models.SubArea;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionMember}.
 */
public final class MemberManager {
	private MemberManager() {}

	/**
	 * Add a player as member of a region or a member of a sub-area.
	 * @param player The player
	 * @param type The linkage type from {@link LinkageType}
	 * @param linkageId The linkage ID; region ID or sub-area ID
	 * @return Instance of the new member
	 */
	public static RegionMember addMember(OfflinePlayer player, LinkageType type, long linkageId) {
		RegionMember member = new RegionMember(player, type, linkageId);
		Homestead.regionMemberCache.putOrUpdate(member);
		return member;
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
		return Homestead.regionMemberCache.getAll().stream()
				.filter(m -> m.getRegionId() == regionId)
				.collect(Collectors.toList());
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
		return Homestead.regionMemberCache.getAll().stream()
				.filter(m -> m.getSubAreaId() == subAreaId)
				.collect(Collectors.toList());
	}

	/**
	 * Remove a member.
	 * @param id The member ID
	 */
	public static void removeMember(long id) {
		Homestead.regionMemberCache.remove(id);
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
	 * Remove a member from a sub-area.
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
	 * Remove a member from a region.
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
		return getMembersOfRegion(regionId).stream()
				.anyMatch(b -> b.getPlayerId().equals(player.getUniqueId()));
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
}