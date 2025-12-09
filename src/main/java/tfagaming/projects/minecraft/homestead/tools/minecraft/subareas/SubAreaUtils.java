package tfagaming.projects.minecraft.homestead.tools.minecraft.subareas;

import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableSubArea;

import java.util.UUID;

public class SubAreaUtils {
	public static boolean isIntersectingOtherSubArea(UUID id, SerializableBlock firstPoint, SerializableBlock secondPoint) {
		Region region = RegionsManager.findRegion(id);

		if (region == null) {
			return false;
		}

		for (SerializableSubArea subArea : region.getSubAreas()) {
			if (subArea.isIntersectingOtherSubArea(firstPoint, secondPoint)) {
				return true;
			}
		}

		return false;
	}
}
