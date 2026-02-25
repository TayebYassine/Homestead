package tfagaming.projects.minecraft.homestead.tools.minecraft.subareas;

import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;

import java.util.UUID;

public class SubAreaUtils {
	public static boolean isIntersectingOtherSubArea(UUID id, SerializableBlock firstPoint, SerializableBlock secondPoint) {
		Region region = RegionManager.findRegion(id);

		if (region == null) {
			return false;
		}

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(id)) {
			if (subArea.isIntersectingOtherSubArea(firstPoint, secondPoint)) {
				return true;
			}
		}

		return false;
	}
}
