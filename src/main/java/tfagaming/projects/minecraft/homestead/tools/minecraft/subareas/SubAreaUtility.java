package tfagaming.projects.minecraft.homestead.tools.minecraft.subareas;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;

import java.util.UUID;

public final class SubAreaUtility {
	private SubAreaUtility() {
	}

	public static int getMinX(Block firstPoint, Block secondPoint) {
		return Math.min(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMinX(SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return Math.min(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMaxX(Block firstPoint, Block secondPoint) {
		return Math.max(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMaxX(SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return Math.max(firstPoint.getX(), secondPoint.getX());
	}

	public static int getMinY(Block firstPoint, Block secondPoint) {
		return Math.min(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMinY(SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return Math.min(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMaxY(Block firstPoint, Block secondPoint) {
		return Math.max(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMaxY(SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return Math.max(firstPoint.getY(), secondPoint.getY());
	}

	public static int getMinZ(Block firstPoint, Block secondPoint) {
		return Math.min(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getMinZ(SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return Math.min(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getMaxZ(Block firstPoint, Block secondPoint) {
		return Math.max(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getMaxZ(SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return Math.max(firstPoint.getZ(), secondPoint.getZ());
	}

	public static int getVolume(Block firstPoint, Block secondPoint) {
		int width = getMaxX(firstPoint, secondPoint) - getMinX(firstPoint, secondPoint) + 1;
		int height = getMaxY(firstPoint, secondPoint) - getMinY(firstPoint, secondPoint) + 1;
		int depth = getMaxZ(firstPoint, secondPoint) - getMinZ(firstPoint, secondPoint) + 1;

		return width * height * depth;
	}

	public static int getVolume(SerializableBlock firstPoint, SerializableBlock secondPoint) {
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

	public static boolean isIntersectingOtherSubArea(UUID id, SerializableBlock firstPoint, SerializableBlock secondPoint) {
		return getIntersectedSubArea(id, firstPoint, secondPoint) != null;
	}

	public static SubArea getIntersectedSubArea(UUID id, SerializableBlock firstPoint, SerializableBlock secondPoint) {
		Region region = RegionManager.findRegion(id);

		if (region == null) {
			return null;
		}

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(id)) {
			if (subArea.isIntersectingOtherSubArea(firstPoint, secondPoint)) {
				return subArea;
			}
		}

		return null;
	}
}
