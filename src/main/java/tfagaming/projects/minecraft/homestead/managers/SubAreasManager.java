package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.SubArea;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SubAreasManager {
	private SubAreasManager() {
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
		for (SubArea area : getAll()) {
			if (area.getUniqueId().equals(id)) {
				return area;
			}
		}

		return null;
	}

	/**
	 * Retrieves the sub-area with the exact name (case-insensitive), or null if none exists.
	 * @param regionId The region ID
	 * @param name The sub-area name
	 */
	public static SubArea findSubArea(UUID regionId, String name) {
		for (SubArea area : getSubAreasOfRegion(regionId)) {
			if (area.getName().equals(name)) {
				return area;
			}
		}

		return null;
	}

	public static SubArea findSubAreaHasBlockInside(Block block) {
		return findSubAreaHasLocationInside(block.getLocation());
	}

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
		SubArea subArea = findSubArea(id);

		if (subArea == null) {
			return;
		}

		Homestead.subAreasCache.remove(id);
	}

	/** Checks whether any sub-area already carries the supplied name, ignoring case. */
	public static boolean isNameUsed(UUID regionId, String name) {
		for (SubArea area : getSubAreasOfRegion(regionId)) {
			if (area.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}
}
