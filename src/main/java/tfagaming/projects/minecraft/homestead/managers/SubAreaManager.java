package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SubAreaManager {
	private SubAreaManager() {
	}

	/**
	 * Create a new sub-area.
	 * @param regionId The region ID
	 * @param name The sub-area name
	 * @param world The world
	 * @param point1 The first corner point
	 * @param point2 The second corner point
	 * @param flags Default global player flags
	 */
	public static SubArea createSubArea(UUID regionId, String name, World world, Block point1, Block point2, long flags) {
		SubArea subArea = new SubArea(regionId, name, world, point1, point2, flags);

		Homestead.subAreasCache.putOrUpdate(subArea);

		return subArea;
	}

	/**
	 * Returns an immutable view of every loaded sub-areas.
	 */
	public static List<SubArea> getAll() {
		return Homestead.subAreasCache.getAll();
	}

	/**
	 * Get sub-areas of a region.
	 * @param regionId The region ID
	 */
	public static List<SubArea> getSubAreasOfRegion(UUID regionId) {
		List<SubArea> subAreas = new ArrayList<>();

		for (SubArea area : getAll()) {
			if (area.getRegionId().equals(regionId)) {
				subAreas.add(area);
			}
		}

		return subAreas;
	}

	/**
	 * Retrieves the sub-area with the exact UUID, or null if none exists.
	 * @param id The sub-area ID
	 */
	public static SubArea findSubArea(UUID id) {
		return Homestead.subAreasCache.get(id);
	}

	/**
	 * Retrieves the sub-area with the exact name (case-insensitive), or null if none exists.
	 * @param regionId The region ID
	 * @param name The sub-area name
	 */
	public static SubArea findSubArea(UUID regionId, String name) {
		for (SubArea area : getAll()) {
			if (area.getRegionId().equals(regionId) && area.getName().equals(name)) {
				return area;
			}
		}
		return null;
	}

	/** Returns the sub-area containing the given block, or null if none. */
	public static SubArea findSubAreaHasBlockInside(Block block) {
		return findSubAreaHasLocationInside(block.getLocation());
	}

	/** Returns the sub-area containing the given location, or null if none. */
	public static SubArea findSubAreaHasLocationInside(Location location) {
		for (SubArea subArea : getAll()) {
			if (subArea.isLocationInside(location)) {
				return subArea;
			}
		}

		return null;
	}

	/**
	 * Permanently deletes the specified sub-area.
	 * @param id The sub-area ID
	 */
	public static void deleteSubArea(UUID id) {
		Homestead.subAreasCache.remove(id);
	}

	/** Checks whether any sub-area already carries the supplied name, ignoring case. */
	public static boolean isNameUsed(UUID regionId, String name) {
		return getAll().stream()
				.anyMatch(a -> a.getRegionId().equals(regionId) && a.getName().equalsIgnoreCase(name));
	}

	public static void cleanStartup() {
		Logger.debug("Cleaning up sub-areas data...");

		List<SubArea> subAreasToDelete = new ArrayList<>();
		int updated = 0;

		for (SubArea subArea : Homestead.subAreasCache.getAll()) {
			World world = subArea.getWorld();

			if (world == null) {
				subAreasToDelete.add(subArea);
				continue;
			}

			if (RegionManager.findRegion(subArea.getRegionId()) == null) {
				subAreasToDelete.add(subArea);
				continue;
			}

			List<SerializableMember> membersToRemove = new ArrayList<>();
			for (SerializableMember member : subArea.getMembers()) {
				if (member.bukkit() == null) {
					membersToRemove.add(member);
				}
			}

			for (SerializableMember member : membersToRemove) {
				subArea.removeMember(member);
				updated++;
			}
		}

		for (SubArea subArea : subAreasToDelete) {
			SubAreaManager.deleteSubArea(subArea.getUniqueId());
			updated++;
		}

		if (updated == 0) {
			Logger.debug("No data corruption was found!");
		} else {
			Logger.debug(updated + " updates have been applied to sub-areas data.");
		}
	}
}
