package tfagaming.projects.minecraft.homestead.tools.minecraft.subareas;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;

public final class SubAreaUtility {
	private SubAreaUtility() {
	}

	public static int getMinX(Block firstPoint, Block secondPoint) {
		return Math.min(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMinX(SeBlock firstPoint, SeBlock secondPoint) {
		return Math.min(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMaxX(Block firstPoint, Block secondPoint) {
		return Math.max(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMaxX(SeBlock firstPoint, SeBlock secondPoint) {
		return Math.max(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMinY(Block firstPoint, Block secondPoint) {
		return Math.min(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMinY(SeBlock firstPoint, SeBlock secondPoint) {
		return Math.min(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMaxY(Block firstPoint, Block secondPoint) {
		return Math.max(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMaxY(SeBlock firstPoint, SeBlock secondPoint) {
		return Math.max(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMinZ(Block firstPoint, Block secondPoint) {
		return Math.min(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getMinZ(SeBlock firstPoint, SeBlock secondPoint) {
		return Math.min(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getMaxZ(Block firstPoint, Block secondPoint) {
		return Math.max(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getMaxZ(SeBlock firstPoint, SeBlock secondPoint) {
		return Math.max(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getVolume(Block firstPoint, Block secondPoint) {
		int width = getMaxX(firstPoint, secondPoint) - getMinX(firstPoint, secondPoint) + 1;
		int height = getMaxY(firstPoint, secondPoint) - getMinY(firstPoint, secondPoint) + 1;
		int depth = getMaxZ(firstPoint, secondPoint) - getMinZ(firstPoint, secondPoint) + 1;

		return width * height * depth;
	}

	public static int getVolume(SeBlock firstPoint, SeBlock secondPoint) {
		int width = getMaxX(firstPoint, secondPoint) - getMinX(firstPoint, secondPoint) + 1;
		int height = getMaxY(firstPoint, secondPoint) - getMinY(firstPoint, secondPoint) + 1;
		int depth = getMaxZ(firstPoint, secondPoint) - getMinZ(firstPoint, secondPoint) + 1;

		return width * height * depth;
	}

	public static String toStringBlockLocation(World world, int[] coords) {
		return toStringBlockLocation(world, parseBlockLocation(world, coords));
	}

	public static String toStringBlockLocation(World world, Block block) {
		return (world.getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ());
	}

	public static Block parseBlockLocation(World world, int[] coords) {
		return new Location(world, coords[0], coords[1], coords[2]).getBlock();
	}

	public static Block parseBlockLocation(World world, String coordsString) {
		String[] splitted = coordsString.split(";");

		int[] coords = {Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]),
				Integer.parseInt(splitted[3])};

		return parseBlockLocation(world, coords);
	}

	public static int[] getBlockLocation(Block block) {
		return new int[]{block.getX(), block.getY(), block.getZ()};
	}

	public static boolean isIntersectingOtherSubArea(long regionId, SeBlock firstPoint, SeBlock secondPoint) {
		return getIntersectedSubArea(regionId, firstPoint, secondPoint) != null;
	}

	public static SubArea getIntersectedSubArea(long regionId, SeBlock firstPoint, SeBlock secondPoint) {
		Region region = RegionManager.findRegion(regionId);

		if (region == null) {
			return null;
		}

		int minX1 = Math.min(firstPoint.getX(), secondPoint.getX());
		int minY1 = Math.min(firstPoint.getY(), secondPoint.getY());
		int minZ1 = Math.min(firstPoint.getZ(), secondPoint.getZ());
		int maxX1 = Math.max(firstPoint.getX(), secondPoint.getX());
		int maxY1 = Math.max(firstPoint.getY(), secondPoint.getY());
		int maxZ1 = Math.max(firstPoint.getZ(), secondPoint.getZ());

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(regionId)) {
			int minX2 = subArea.getMinX();
			int minY2 = subArea.getMinY();
			int minZ2 = subArea.getMinZ();
			int maxX2 = subArea.getMaxX();
			int maxY2 = subArea.getMaxY();
			int maxZ2 = subArea.getMaxZ();

			if ((minX1 <= maxX2 && maxX1 >= minX2) &&
					(minY1 <= maxY2 && maxY1 >= minY2) &&
					(minZ1 <= maxZ2 && maxZ1 >= minZ2)) {
				return subArea;
			}
		}

		return null;
	}
}
