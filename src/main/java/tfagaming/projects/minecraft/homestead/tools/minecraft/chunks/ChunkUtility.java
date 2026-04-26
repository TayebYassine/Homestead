package tfagaming.projects.minecraft.homestead.tools.minecraft.chunks;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;


import java.util.ArrayList;
import java.util.List;

public final class ChunkUtility {
	private ChunkUtility() {
	}

	public static List<Chunk> getChunksInArea(SeBlock corner1, SeBlock corner2) {
		if (corner1 == null || corner2 == null) return new ArrayList<>();

		return getChunksInArea(corner1.toBukkit(), corner2.toBukkit());
	}

	public static List<Chunk> getChunksInArea(Block corner1, Block corner2) {
		if (corner1 == null || corner2 == null) return new ArrayList<>();

		World world = corner1.getWorld();
		List<Chunk> chunks = new ArrayList<>();

		int minX = Math.min(corner1.getX(), corner2.getX());
		int maxX = Math.max(corner1.getX(), corner2.getX());
		int minZ = Math.min(corner1.getZ(), corner2.getZ());
		int maxZ = Math.max(corner1.getZ(), corner2.getZ());

		int minChunkX = minX >> 4;
		int maxChunkX = maxX >> 4;
		int minChunkZ = minZ >> 4;
		int maxChunkZ = maxZ >> 4;

		for (int x = minChunkX; x <= maxChunkX; x++) {
			for (int z = minChunkZ; z <= maxChunkZ; z++) {
				Chunk chunk = world.getChunkAt(x, z);
				chunks.add(chunk);
			}
		}

		return chunks;
	}

	/**
	 * Find nearby unclaimed chunk by a location.
	 * @param location The location
	 * @param maxRadius The maximum radius
	 */
	public static Chunk findNearbyUnclaimedChunk(Location location, int maxRadius) {
		Chunk startChunk = location.getChunk();
		World world = location.getWorld();
		int startX = startChunk.getX();
		int startZ = startChunk.getZ();

		int radius = 1;

		while (radius <= maxRadius) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.abs(x) != radius && Math.abs(z) != radius) {
						continue;
					}

					Chunk currentChunk = world.getChunkAt(startX + x, startZ + z);

					if (!ChunkManager.isChunkClaimed(currentChunk)) {
						return currentChunk;
					}
				}
			}

			radius++;
		}

		return null;
	}

	public static String getIdentifierString(Chunk chunk) {
		return String.format("%s,%s,%s", chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
	}

	/**
	 * Returns {@code true} if two chunks are equal; same X, Z, and world ID.
	 * @param chunk1 Chunk 1
	 * @param chunk2 Chunk 2
	 */
	public static boolean areEqual(Chunk chunk1, Chunk chunk2) {
		return getIdentifierString(chunk1).equals(getIdentifierString(chunk2));
	}

	/**
	 * Returns a safe location inside the given chunk (overworld/end).
	 * @param player The player
	 * @param chunk The chunk
	 */
	public static Location getLocation(Player player, RegionChunk chunk) {
		return getLocation(player, chunk.toBukkit());
	}

	/**
	 * Returns a safe location inside the given chunk (overworld/end).
	 * @param player The player
	 * @param chunk The chunk
	 */
	public static Location getLocation(Player player, Chunk chunk) {
		World world = chunk.getWorld();
		if (world == null) return null;

		int x = chunk.getX() * 16 + 8;
		int z = chunk.getZ() * 16 + 8;

		Location loc;
		if (world.getEnvironment() == World.Environment.NETHER) {
			loc = ChunkUtility.findSafeNetherLocation(world, x, z);
		} else {
			int highest = world.getHighestBlockYAt(x, z);
			loc = new Location(world, x, highest + 2, z);
		}

		if (loc != null) {
			loc.setPitch(player.getLocation().getPitch());
			loc.setYaw(player.getLocation().getYaw());
		}
		return loc;
	}

	/**
	 * Finds a safe standable location in the Nether near (x, z).
	 * @param world The world
	 * @param x The location (X axis)
	 * @param z    The location (Z axis)
	 */
	public static Location findSafeNetherLocation(World world, int x, int z) {
		int minY = 32;
		int maxY = 124;

		for (int y = maxY; y >= minY; y--) {
			Block block = world.getBlockAt(x, y, z);
			Block above = world.getBlockAt(x, y + 1, z);
			Block aboveAbove = world.getBlockAt(x, y + 2, z);

			if ((block.getType() != Material.AIR && block.getType() != Material.LAVA)
					&& above.getType() == Material.AIR
					&& aboveAbove.getType() == Material.AIR) {
				return new Location(world, x + 0.5, y + 1, z + 0.5);
			}
		}

		return null;
	}
}