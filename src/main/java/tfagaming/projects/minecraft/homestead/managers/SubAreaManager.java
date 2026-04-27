package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.SubArea;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link SubArea} creation, deletion, and queries.
 */
public final class SubAreaManager {
	private SubAreaManager() {
	}

	/**
	 * Create a new sub-area.
	 * @param region The region
	 * @param name The sub-area name
	 * @param world The world
	 * @param point1 The first corner point
	 * @param point2 The second corner point
	 * @return The created SubArea.
	 */
	public static SubArea createSubArea(Region region, String name, World world, Block point1, Block point2) {
		return createSubArea(region.getUniqueId(), name, world, point1, point2, region.getPlayerFlags());
	}

	/**
	 * Create a new sub-area.
	 * @param regionId The region ID
	 * @param name The sub-area name
	 * @param world The world
	 * @param point1 The first corner point
	 * @param point2 The second corner point
	 * @param flags Default global player flags
	 * @return The created SubArea.
	 */
	public static SubArea createSubArea(long regionId, String name, World world, Block point1, Block point2, long flags) {
		SubArea subArea = new SubArea(
				regionId,
				name,
				world,
				point1,
				point2,
				flags);
		Homestead.SUBAREA_CACHE.putOrUpdate(subArea);
		return subArea;
	}

	/**
	 * Returns an immutable view of every loaded sub-area.
	 * @return List of all sub-areas.
	 */
	public static List<SubArea> getAll() {
		return Homestead.SUBAREA_CACHE.getAll();
	}

	/**
	 * Returns the number of sub-areas in the server.
	 * @return Sub-area count.
	 */
	public static int getSubAreaCount() {
		return getAll().size();
	}

	/**
	 * Returns the number of sub-areas in a region.
	 * @param region The region
	 * @return Sub-area count.
	 */
	public static int getSubAreaCount(Region region) {
		return getSubAreaCount(region.getUniqueId());
	}

	/**
	 * Returns the number of sub-areas in a region.
	 * @param regionId The region ID
	 * @return Sub-area count.
	 */
	public static int getSubAreaCount(long regionId) {
		return getSubAreasOfRegion(regionId).size();
	}

	/**
	 * Checks if a region has any sub-areas.
	 * @param region The region
	 * @return {@code true} if sub-areas exist.
	 */
	public static boolean hasSubAreas(Region region) {
		return hasSubAreas(region.getUniqueId());
	}

	/**
	 * Checks if a region has any sub-areas.
	 * @param regionId The region ID
	 * @return {@code true} if sub-areas exist.
	 */
	public static boolean hasSubAreas(long regionId) {
		return !getSubAreasOfRegion(regionId).isEmpty();
	}

	/**
	 * Get sub-areas of a region.
	 * @param region The region
	 * @return List of sub-areas.
	 */
	public static List<SubArea> getSubAreasOfRegion(Region region) {
		return getSubAreasOfRegion(region.getUniqueId());
	}

	/**
	 * Get sub-areas of a region.
	 * @param regionId The region ID
	 * @return List of sub-areas.
	 */
	public static List<SubArea> getSubAreasOfRegion(long regionId) {
		List<SubArea> subAreas = new ArrayList<>();
		for (SubArea area : getAll()) {
			if (area.getRegionId() == regionId) {
				subAreas.add(area);
			}
		}
		return subAreas;
	}

	/**
	 * Returns all sub-areas in a specific world.
	 * @param world The world
	 * @return List of sub-areas.
	 */
	public static List<SubArea> getSubAreasInWorld(World world) {
		return getSubAreasInWorld(world.getUID());
	}

	/**
	 * Returns all sub-areas in a specific world.
	 * @param worldId The world UUID
	 * @return List of sub-areas.
	 */
	public static List<SubArea> getSubAreasInWorld(UUID worldId) {
		List<SubArea> result = new ArrayList<>();
		for (SubArea area : getAll()) {
			if (area.getWorldId().equals(worldId)) {
				result.add(area);
			}
		}
		return result;
	}

	/**
	 * Returns all sub-area names for a region (useful for GUIs).
	 * @param region The region
	 * @return List of names.
	 */
	public static List<String> getSubAreaNames(Region region) {
		return getSubAreaNames(region.getUniqueId());
	}

	/**
	 * Returns all sub-area names for a region.
	 * @param regionId The region ID
	 * @return List of names.
	 */
	public static List<String> getSubAreaNames(long regionId) {
		return getSubAreasOfRegion(regionId).stream()
				.map(SubArea::getName)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the sub-area with the exact ID, or null if none exists.
	 * @param id The sub-area ID
	 * @return The SubArea, or {@code null}.
	 */
	public static SubArea findSubArea(long id) {
		return Homestead.SUBAREA_CACHE.get(id);
	}

	/**
	 * Retrieves the sub-area with the exact name (case-insensitive) within a region, or null if none exists.
	 * @param regionId The region ID
	 * @param name The sub-area name
	 * @return The SubArea, or {@code null}.
	 */
	public static SubArea findSubArea(long regionId, String name) {
		for (SubArea area : getAll()) {
			if (area.getRegionId() == regionId && area.getName().equalsIgnoreCase(name)) {
				return area;
			}
		}
		return null;
	}

	/**
	 * Finds the sub-area containing the given block.
	 * @param block The block
	 * @return The SubArea, or {@code null}.
	 */
	public static SubArea findSubAreaHasBlockInside(Block block) {
		return findSubAreaHasLocationInside(block.getLocation());
	}

	/**
	 * Finds the sub-area containing the given location.
	 * @param location The location
	 * @return The SubArea, or {@code null}.
	 */
	public static SubArea findSubAreaHasLocationInside(Location location) {
		for (SubArea subArea : getAll()) {
			if (subArea.isLocationInside(location)) {
				return subArea;
			}
		}
		return null;
	}

	/**
	 * Finds the sub-area containing the given location, scoped to a specific region.
	 * @param location The location
	 * @param regionId The region ID to search within
	 * @return The SubArea, or {@code null}.
	 */
	public static SubArea findSubAreaByLocationInRegion(Location location, long regionId) {
		for (SubArea subArea : getSubAreasOfRegion(regionId)) {
			if (subArea.isLocationInside(location)) {
				return subArea;
			}
		}
		return null;
	}

	/**
	 * Checks if a location is inside any sub-area on the server.
	 * @param location The location
	 * @return {@code true} if inside any sub-area.
	 */
	public static boolean isLocationInAnySubArea(Location location) {
		return findSubAreaHasLocationInside(location) != null;
	}

	/**
	 * Checks if a block is inside any sub-area on the server.
	 * @param block The block
	 * @return {@code true} if inside any sub-area.
	 */
	public static boolean isBlockInAnySubArea(Block block) {
		return isLocationInAnySubArea(block.getLocation());
	}

	/**
	 * Checks if a player is currently inside a specific sub-area.
	 * @param player The player
	 * @param subArea The sub-area
	 * @return {@code true} if the player is inside.
	 */
	public static boolean isPlayerInSubArea(Player player, SubArea subArea) {
		return subArea.isLocationInside(player.getLocation());
	}

	/**
	 * Returns all sub-areas that intersect (overlap) with the given sub-area.
	 * @param subArea The sub-area to check
	 * @return List of intersecting sub-areas (excluding itself).
	 */
	public static List<SubArea> getSubAreasIntersecting(SubArea subArea) {
		List<SubArea> intersecting = new ArrayList<>();
		for (SubArea other : getAll()) {
			if (other.getUniqueId() == subArea.getUniqueId()) continue;
			if (other.isIntersecting(subArea)) {
				intersecting.add(other);
			}
		}
		return intersecting;
	}

	/**
	 * Returns all sub-areas in a region that intersect with region chunks.
	 * @param region The region
	 * @return List of sub-areas that overlap with the region's claimed chunks.
	 */
	public static List<SubArea> getSubAreasIntersectingRegion(Region region) {
		return getSubAreasIntersectingRegion(region.getUniqueId());
	}

	/**
	 * Returns all sub-areas in a region that intersect with region chunks.
	 * @param regionId The region ID
	 * @return List of sub-areas that overlap with the region's claimed chunks.
	 */
	public static List<SubArea> getSubAreasIntersectingRegion(long regionId) {
		List<SubArea> result = new ArrayList<>();
		List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(regionId);

		for (SubArea subArea : getSubAreasOfRegion(regionId)) {
			World world = subArea.getWorld();
			if (world == null) continue;

			for (RegionChunk chunk : chunks) {
				if (!chunk.getWorldId().equals(subArea.getWorldId())) continue;

				int minX = subArea.getMinX(), maxX = subArea.getMaxX();
				int minZ = subArea.getMinZ(), maxZ = subArea.getMaxZ();
				int chunkMinX = chunk.getX() * 16, chunkMaxX = chunkMinX + 15;
				int chunkMinZ = chunk.getZ() * 16, chunkMaxZ = chunkMinZ + 15;

				if (minX <= chunkMaxX && maxX >= chunkMinX && minZ <= chunkMaxZ && maxZ >= chunkMinZ) {
					result.add(subArea);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the total volume (in blocks) of all sub-areas in a region.
	 * @param region The region
	 * @return Total volume.
	 */
	public static int getTotalVolume(Region region) {
		return getTotalVolume(region.getUniqueId());
	}

	/**
	 * Returns the total volume (in blocks) of all sub-areas in a region.
	 * @param regionId The region ID
	 * @return Total volume.
	 */
	public static int getTotalVolume(long regionId) {
		return getSubAreasOfRegion(regionId).stream()
				.mapToInt(SubArea::getVolume)
				.sum();
	}

	/**
	 * Returns the largest sub-area in a region by volume.
	 * @param region The region
	 * @return The largest SubArea, or {@code null}.
	 */
	public static SubArea getLargestSubArea(Region region) {
		return getLargestSubArea(region.getUniqueId());
	}

	/**
	 * Returns the largest sub-area in a region by volume.
	 * @param regionId The region ID
	 * @return The largest SubArea, or {@code null}.
	 */
	public static SubArea getLargestSubArea(long regionId) {
		return getSubAreasOfRegion(regionId).stream()
				.max(Comparator.comparingInt(SubArea::getVolume))
				.orElse(null);
	}

	/**
	 * Returns sub-areas filtered by volume range.
	 * @param region The region
	 * @param minVolume Minimum volume (inclusive)
	 * @param maxVolume Maximum volume (inclusive)
	 * @return List of matching sub-areas.
	 */
	public static List<SubArea> getSubAreasByVolumeRange(Region region, int minVolume, int maxVolume) {
		return getSubAreasByVolumeRange(region.getUniqueId(), minVolume, maxVolume);
	}

	/**
	 * Returns sub-areas filtered by volume range.
	 * @param regionId The region ID
	 * @param minVolume Minimum volume (inclusive)
	 * @param maxVolume Maximum volume (inclusive)
	 * @return List of matching sub-areas.
	 */
	public static List<SubArea> getSubAreasByVolumeRange(long regionId, int minVolume, int maxVolume) {
		return getSubAreasOfRegion(regionId).stream()
				.filter(a -> {
					int vol = a.getVolume();
					return vol >= minVolume && vol <= maxVolume;
				})
				.collect(Collectors.toList());
	}

	/**
	 * Calculates the center location of a sub-area.
	 * @param subArea The sub-area
	 * @return The center location, or {@code null} if world is unloaded.
	 */
	public static Location getSubAreaCenter(SubArea subArea) {
		World world = subArea.getWorld();
		if (world == null) return null;

		double x = (subArea.getMinX() + subArea.getMaxX()) / 2.0 + 0.5;
		double y = (subArea.getMinY() + subArea.getMaxY()) / 2.0;
		double z = (subArea.getMinZ() + subArea.getMaxZ()) / 2.0 + 0.5;
		return new Location(world, x, y, z);
	}

	/**
	 * Returns all 8 corner blocks of a sub-area.
	 * @param subArea The sub-area
	 * @return List of corner locations.
	 */
	public static List<Location> getSubAreaCorners(SubArea subArea) {
		World world = subArea.getWorld();
		if (world == null) return Collections.emptyList();

		List<Location> corners = new ArrayList<>();
		int[] xs = {subArea.getMinX(), subArea.getMaxX()};
		int[] ys = {subArea.getMinY(), subArea.getMaxY()};
		int[] zs = {subArea.getMinZ(), subArea.getMaxZ()};

		for (int x : xs) {
			for (int y : ys) {
				for (int z : zs) {
					corners.add(new Location(world, x + 0.5, y, z + 0.5));
				}
			}
		}
		return corners;
	}

	/**
	 * Returns all sub-areas a player is a member of.
	 * @param player The player
	 * @return List of sub-areas.
	 */
	public static List<SubArea> getPlayerSubAreas(Player player) {
		return getPlayerSubAreas(player.getUniqueId());
	}

	/**
	 * Returns all sub-areas a player is a member of.
	 * @param playerId The player UUID
	 * @return List of sub-areas.
	 */
	public static List<SubArea> getPlayerSubAreas(UUID playerId) {
		List<Long> subAreaIds = MemberManager.getAllMembersOfPlayer(playerId).stream()
				.map(RegionMember::getSubAreaId)
				.filter(subAreaId -> subAreaId != -1L)
				.distinct()
				.toList();

		List<SubArea> result = new ArrayList<>();
		for (Long id : subAreaIds) {
			SubArea area = findSubArea(id);
			if (area != null) result.add(area);
		}
		return result;
	}

	/**
	 * Safely renames a sub-area, ensuring uniqueness within the region.
	 * @param subArea The sub-area to rename
	 * @param newName The desired name
	 * @return The actual name assigned (may have counter appended).
	 */
	public static String renameSubArea(SubArea subArea, String newName) {
		String actualName = newName;
		int counter = 1;

		while (isNameUsed(subArea.getRegionId(), actualName) && !actualName.equalsIgnoreCase(subArea.getName())) {
			actualName = newName + counter;
			counter++;
		}

		subArea.setName(actualName);
		return actualName;
	}

	/**
	 * Resizes a sub-area to new corner points.
	 * @param subArea The sub-area
	 * @param point1 The new first corner
	 * @param point2 The new second corner
	 */
	public static void resizeSubArea(SubArea subArea, Block point1, Block point2) {
		subArea.setPoint1(point1);
		subArea.setPoint2(point2);
	}

	/**
	 * Expands a sub-area by the given amount in all directions.
	 * @param subArea The sub-area
	 * @param amount Blocks to expand (must be positive)
	 */
	public static void expandSubArea(SubArea subArea, int amount) {
		if (amount <= 0) return;

		World world = subArea.getWorld();
		if (world == null) return;

		Block p1 = world.getBlockAt(subArea.getMinX() - amount, Math.max(0, subArea.getMinY() - amount), subArea.getMinZ() - amount);
		Block p2 = world.getBlockAt(subArea.getMaxX() + amount, Math.min(world.getMaxHeight(), subArea.getMaxY() + amount), subArea.getMaxZ() + amount);

		subArea.setPoint1(p1);
		subArea.setPoint2(p2);
	}

	/**
	 * Permanently deletes the specified sub-area and its related members.
	 * @param id The sub-area ID
	 */
	public static void deleteSubArea(long id) {
		Homestead.SUBAREA_CACHE.remove(id);
	}

	/**
	 * Deletes all sub-areas belonging to a region.
	 * @param region The region
	 * @return The number of sub-areas deleted.
	 */
	public static int deleteSubAreasOfRegion(Region region) {
		return deleteSubAreasOfRegion(region.getUniqueId());
	}

	/**
	 * Deletes all sub-areas belonging to a region.
	 * @param regionId The region ID
	 * @return The number of sub-areas deleted.
	 */
	public static int deleteSubAreasOfRegion(long regionId) {
		List<Long> toRemove = getSubAreasOfRegion(regionId).stream()
				.map(SubArea::getUniqueId)
				.toList();

		for (Long id : toRemove) {
			deleteSubArea(id);
		}
		return toRemove.size();
	}

	/**
	 * Checks whether any sub-area in the region already carries the supplied name, ignoring case.
	 * @param regionId The region ID
	 * @param name The name to check
	 * @return {@code true} if the name is used.
	 */
	public static boolean isNameUsed(long regionId, String name) {
		return getAll().stream()
				.anyMatch(a -> a.getRegionId() == regionId && a.getName().equalsIgnoreCase(name));
	}

	/**
	 * Removes all sub-areas with invalid references:
	 * - Worlds that no longer exist
	 * - Regions that no longer exist
	 * - Members whose player UUID no longer maps to a known player
	 * @return Number of corrupted sub-areas removed + member fixes.
	 */
	public static int cleanupInvalidSubAreas() {
		Set<UUID> validWorlds = Bukkit.getWorlds().stream()
				.map(World::getUID)
				.collect(Collectors.toSet());

		List<Long> toRemove = new ArrayList<>();
		int memberFixes = 0;

		for (SubArea subArea : Homestead.SUBAREA_CACHE.getAll()) {
			boolean invalidWorld = !validWorlds.contains(subArea.getWorldId());

			boolean invalidRegion = subArea.getRegion() == null;

			if (invalidWorld || invalidRegion) {
				toRemove.add(subArea.getUniqueId());
				continue;
			}

			for (RegionMember member : MemberManager.getMembersOfSubArea(subArea.getUniqueId())) {
				OfflinePlayer player = member.getPlayer();

				if (player == null || player.getName() == null) {
					MemberManager.removeMember(member.getUniqueId());
					memberFixes++;
				}
			}
		}

		for (Long id : toRemove) {
			deleteSubArea(id);
		}
		return toRemove.size() + memberFixes;
	}
}