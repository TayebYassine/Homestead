package tfagaming.projects.minecraft.homestead.managers;

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

	public static void createSubArea(String name, UUID regionId, World world, Block point1, Block point2, long flags) {
		SubArea subArea = new SubArea(regionId, name, world, point1, point2, flags);

		Homestead.subAreasCache.putOrUpdate(subArea);
	}

	public static List<SubArea> getAll() {
		return Homestead.subAreasCache.getAll();
	}

	public static List<SubArea> getSubAreasOfRegion(UUID regionId) {
		List<SubArea> subAreas = new ArrayList<>();

		for (SubArea area : getAll()) {
			if (area.getRegionId().equals(regionId)) {
				subAreas.add(area);
			}
		}

		return subAreas;
	}

	public static SubArea findSubArea(UUID id) {
		for (SubArea area : getAll()) {
			if (area.getUniqueId().equals(id)) {
				return area;
			}
		}

		return null;
	}

	public static void deleteSubArea(UUID id) {
		SubArea subArea = findSubArea(id);

		if (subArea == null) {
			return;
		}

		Homestead.subAreasCache.remove(id);
	}
}
