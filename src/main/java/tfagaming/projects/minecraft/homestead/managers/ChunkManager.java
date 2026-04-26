package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.ChunkClaimEvent;
import tfagaming.projects.minecraft.homestead.api.events.ChunkUnclaimEvent;
import tfagaming.projects.minecraft.homestead.integrations.FastAsyncWorldEditAPI;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionChunk;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.PersistentChunkTicket;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles claiming, unclaiming, and managing chunks in regions.
 * Includes adjacency enforcement, anti-split protection, and data handling.<br>
 * This is a utility class that helps manage chunks more easily.
 */
public final class ChunkManager {
	private static final Random RANDOM = new Random();

	private ChunkManager() {
	}

	/**
	 * Creates a new chunk entry for a region.<br>
	 * This method applies no verification and may cause two or more regions owning the same chunk, which could create a fatal exploit.
	 * @param region The region
	 * @param chunk The chunk
	 * @return The created RegionChunk
	 */
	public static RegionChunk createChunk(Region region, Chunk chunk) {
		return createChunk(region.getUniqueId(), chunk);
	}

	/**
	 * Creates a new chunk entry for a region.<br>
	 * This method applies no verification and may cause two or more regions owning the same chunk, which could create a fatal exploit.
	 * @param regionId The region ID
	 * @param chunk The chunk
	 * @return The created RegionChunk
	 */
	public static RegionChunk createChunk(long regionId, Chunk chunk) {
		RegionChunk regionChunk = new RegionChunk(
				Homestead.getSnowflake().nextId(),
				regionId,
				chunk.getWorld().getUID(),
				chunk.getX(),
				chunk.getZ(),
				System.currentTimeMillis(),
				false
		);
		Homestead.regionChunkCache.putOrUpdate(regionChunk);
		return regionChunk;
	}

	/**
	 * Claims a chunk for a specific region with normal protection checks.
	 * @param region The region
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, <code>null</code> otherwise.
	 */
	public static Error claimChunk(Region region, Chunk chunk) {
		return claimChunk(region.getUniqueId(), chunk);
	}

	/**
	 * Claims a chunk for a specific region with normal protection checks.
	 * @param regionId The region ID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, <code>null</code> otherwise.
	 */
	public static Error claimChunk(long regionId, Chunk chunk) {
		Region region = RegionManager.findRegion(regionId);

		if (region == null) {
			return Error.REGION_NOT_FOUND;
		}

		if (isChunkInDisabledWorld(chunk)) {
			return Error.CHUNK_IN_DISABLED_WORLD;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isAdjacentChunksRuleEnabled()
				&& !getChunksOfRegion(regionId).isEmpty()
				&& !hasAdjacentOwnedChunk(regionId, chunk)) {
			return Error.CHUNK_NOT_ADJACENT_TO_REGION;
		}

		RegionChunk regionChunk = createChunk(regionId, chunk);

		ChunkClaimEvent event = new ChunkClaimEvent(region, chunk);
		Location chunkLoc = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
		Homestead.getInstance().runLocationTask(chunkLoc, () -> Bukkit.getPluginManager().callEvent(event));

		return null;
	}

	/**
	 * Unclaims a chunk with normal protection checks.
	 * @param region The region
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, <code>null</code> otherwise.
	 */
	public static Error unclaimChunk(Region region, Chunk chunk) {
		return unclaimChunk(region.getUniqueId(), chunk);
	}

	/**
	 * Unclaims a chunk with normal protection checks.
	 * @param regionId The region ID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, <code>null</code> otherwise.
	 */
	public static Error unclaimChunk(long regionId, Chunk chunk) {
		return unclaimChunkInternal(regionId, chunk, false);
	}

	/**
	 * Unclaims a chunk bypassing split/topology protection and ownership checks.
	 * @param region The region
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, <code>null</code> otherwise.
	 */
	public static Error forceUnclaimChunk(Region region, Chunk chunk) {
		return unclaimChunkInternal(region.getUniqueId(), chunk, true);
	}

	/**
	 * Unclaims a chunk bypassing split/topology protection and ownership checks.
	 * @param regionId The region ID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, <code>null</code> otherwise.
	 */
	public static Error forceUnclaimChunk(long regionId, Chunk chunk) {
		return unclaimChunkInternal(regionId, chunk, true);
	}

	private static Error unclaimChunkInternal(long regionId, Chunk chunk, boolean force) {
		Region region = RegionManager.findRegion(regionId);

		if (region == null) {
			return Error.REGION_NOT_FOUND;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isAdjacentChunksRuleEnabled()
				&& !force && wouldSplitRegion(regionId, chunk)) {
			return Error.CHUNK_WOULD_SPLIT_REGION;
		}

		deleteChunk(chunk);

		PersistentChunkTicket.removePersistent(Homestead.getInstance(), chunk);

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(regionId)) {
			for (Chunk subAreaChunk : ChunkUtility.getChunksInArea(subArea.getPoint1(), subArea.getPoint2())) {
				if (ChunkUtility.areEqual(subAreaChunk, chunk)) {
					SubAreaManager.deleteSubArea(subArea.getUniqueId());
					break;
				}
			}
		}

		if (Resources.<ConfigFile>get(ResourceType.Config).regenerateChunksWithFAWE() && !Homestead.isFolia()) {
			Homestead.getInstance().runAsyncTask(() -> {
				FastAsyncWorldEditAPI.regenerateChunk(chunk.getWorld(), chunk);
			});
		}

		Location location = region.getLocation() != null ? region.getLocation().toBukkit() : null;

		if (location != null && ChunkUtility.areEqual(location.getChunk(), chunk)) {
			region.resetLocation();
		}

		ChunkUnclaimEvent event = new ChunkUnclaimEvent(region, chunk);
		Location chunkLoc = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
		Homestead.getInstance().runLocationTask(chunkLoc, () -> Bukkit.getPluginManager().callEvent(event));

		return null;
	}

	/**
	 * Permanently deletes a chunk.
	 * @param chunk The chunk
	 */
	public static void deleteChunk(Chunk chunk) {
		RegionChunk chunkData = findChunk(chunk);

		if (chunkData != null) {
			deleteChunk(chunkData);
		}
	}

	/**
	 * Permanently deletes a chunk.
	 * @param chunk The chunk
	 */
	public static void deleteChunk(RegionChunk chunk) {
		deleteChunk(chunk.getUniqueId());
	}

	/**
	 * Permanently deletes a chunk.
	 * @param id The chunk ID
	 */
	public static void deleteChunk(long id) {
		Homestead.regionChunkCache.remove(id);
	}

	/**
	 * Returns all chunks belonging to a region.
	 * @param region The region
	 * @return List of region chunks.
	 */
	public static List<RegionChunk> getChunksOfRegion(Region region) {
		return getChunksOfRegion(region.getUniqueId());
	}

	/**
	 * Returns all chunks belonging to a region.
	 * @param regionId The region ID
	 * @return List of region chunks.
	 */
	public static List<RegionChunk> getChunksOfRegion(long regionId) {
		List<RegionChunk> chunks = new ArrayList<>();
		for (RegionChunk chunk : getAll()) {
			if (chunk.getRegionId() == regionId) {
				chunks.add(chunk);
			}
		}
		return chunks;
	}

	/**
	 * Returns the number of chunks owned by a region.
	 * @param region The region
	 * @return The chunk count.
	 */
	public static int getChunkCount(Region region) {
		return getChunkCount(region.getUniqueId());
	}

	/**
	 * Returns the number of chunks owned by a region.
	 * @param regionId The region ID
	 * @return The chunk count.
	 */
	public static int getChunkCount(long regionId) {
		return getChunksOfRegion(regionId).size();
	}

	/**
	 * Returns all claimed chunks in a specific world.
	 * @param world The world
	 * @return List of chunks in the world.
	 */
	public static List<RegionChunk> getChunksInWorld(World world) {
		return getChunksInWorld(world.getUID());
	}

	/**
	 * Returns all claimed chunks in a specific world.
	 * @param worldId The world UUID
	 * @return List of chunks in the world.
	 */
	public static List<RegionChunk> getChunksInWorld(UUID worldId) {
		List<RegionChunk> result = new ArrayList<>();
		for (RegionChunk chunk : getAll()) {
			if (chunk.getWorldId().equals(worldId)) {
				result.add(chunk);
			}
		}
		return result;
	}

	/**
	 * Returns all force-loaded chunks belonging to a region.
	 * @param region The region
	 * @return List of force-loaded chunks.
	 */
	public static List<RegionChunk> getForceLoadedChunks(Region region) {
		return getForceLoadedChunks(region.getUniqueId());
	}

	/**
	 * Returns all force-loaded chunks belonging to a region.
	 * @param regionId The region ID
	 * @return List of force-loaded chunks.
	 */
	public static List<RegionChunk> getForceLoadedChunks(long regionId) {
		List<RegionChunk> result = new ArrayList<>();
		for (RegionChunk chunk : getChunksOfRegion(regionId)) {
			if (chunk.isForceLoaded()) {
				result.add(chunk);
			}
		}
		return result;
	}

	/**
	 * Calculates the centroid (average center) of all chunks in a region.
	 * @param region The region
	 * @return The centroid chunk coordinates as {@code int[2]} (x, z), or {@code null} if no chunks.
	 */
	public static int[] getChunkCentroid(Region region) {
		return getChunkCentroid(region.getUniqueId());
	}

	/**
	 * Calculates the centroid (average center) of all chunks in a region.
	 * @param regionId The region ID
	 * @return The centroid chunk coordinates as {@code int[2]} (x, z), or {@code null} if no chunks.
	 */
	public static int[] getChunkCentroid(long regionId) {
		List<RegionChunk> chunks = getChunksOfRegion(regionId);
		if (chunks.isEmpty()) return null;

		long sumX = 0, sumZ = 0;
		for (RegionChunk chunk : chunks) {
			sumX += chunk.getX();
			sumZ += chunk.getZ();
		}
		return new int[]{(int) (sumX / chunks.size()), (int) (sumZ / chunks.size())};
	}

	/**
	 * Returns the bounding box of a region's chunks in chunk coordinates.
	 * @param region The region
	 * @return {@code int[4]} as {minX, minZ, maxX, maxZ}, or {@code null} if no chunks.
	 */
	public static int[] getBoundingChunks(Region region) {
		return getBoundingChunks(region.getUniqueId());
	}

	/**
	 * Returns the bounding box of a region's chunks in chunk coordinates.
	 * @param regionId The region ID
	 * @return {@code int[4]} as {minX, minZ, maxX, maxZ}, or {@code null} if no chunks.
	 */
	public static int[] getBoundingChunks(long regionId) {
		List<RegionChunk> chunks = getChunksOfRegion(regionId);
		if (chunks.isEmpty()) return null;

		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

		for (RegionChunk chunk : chunks) {
			minX = Math.min(minX, chunk.getX());
			minZ = Math.min(minZ, chunk.getZ());
			maxX = Math.max(maxX, chunk.getX());
			maxZ = Math.max(maxZ, chunk.getZ());
		}
		return new int[]{minX, minZ, maxX, maxZ};
	}

	/**
	 * Returns the total chunk area (width * depth) of a region's bounding box.
	 * @param region The region
	 * @return The area in chunks, or {@code 0} if no chunks.
	 */
	public static int getRegionChunkArea(Region region) {
		return getRegionChunkArea(region.getUniqueId());
	}

	/**
	 * Returns the total chunk area (width * depth) of a region's bounding box.
	 * @param regionId The region ID
	 * @return The area in chunks, or {@code 0} if no chunks.
	 */
	public static int getRegionChunkArea(long regionId) {
		int[] bounds = getBoundingChunks(regionId);
		if (bounds == null) return 0;
		return (bounds[2] - bounds[0] + 1) * (bounds[3] - bounds[1] + 1);
	}

	/**
	 * Checks if a chunk is on the border (edge) of a region.
	 * @param regionId The region ID
	 * @param chunk The chunk to check
	 * @return {@code true} if the chunk touches the region edge.
	 */
	public static boolean isChunkOnBorder(long regionId, Chunk chunk) {
		List<RegionChunk> regionChunks = getChunksOfRegion(regionId);
		UUID worldId = chunk.getWorld().getUID();
		int x = chunk.getX(), z = chunk.getZ();

		boolean found = false;
		for (RegionChunk rc : regionChunks) {
			if (rc.getWorldId().equals(worldId) && rc.getX() == x && rc.getZ() == z) {
				found = true;
				break;
			}
		}
		if (!found) return false;

		int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		for (int[] dir : dirs) {
			int nx = x + dir[0], nz = z + dir[1];
			boolean hasNeighbor = false;
			for (RegionChunk rc : regionChunks) {
				if (rc.getWorldId().equals(worldId) && rc.getX() == nx && rc.getZ() == nz) {
					hasNeighbor = true;
					break;
				}
			}
			if (!hasNeighbor) return true;
		}
		return false;
	}

	/**
	 * Returns all chunks on the border of a region.
	 * @param region The region
	 * @return List of border chunks.
	 */
	public static List<RegionChunk> getBorderChunks(Region region) {
		return getBorderChunks(region.getUniqueId());
	}

	/**
	 * Returns all chunks on the border of a region.
	 * @param regionId The region ID
	 * @return List of border chunks.
	 */
	public static List<RegionChunk> getBorderChunks(long regionId) {
		List<RegionChunk> border = new ArrayList<>();
		for (RegionChunk chunk : getChunksOfRegion(regionId)) {
			Chunk bukkit = chunk.toBukkit();
			if (bukkit != null && isChunkOnBorder(regionId, bukkit)) {
				border.add(chunk);
			}
		}
		return border;
	}

	/**
	 * Returns all unique regions that are adjacent (share a chunk border) to the given region.
	 * @param region The region
	 * @return List of neighboring regions.
	 */
	public static List<Region> getNeighborsOfRegion(Region region) {
		return getNeighborsOfRegion(region.getUniqueId());
	}

	/**
	 * Returns all unique regions that are adjacent (share a chunk border) to the given region.
	 * @param regionId The region ID
	 * @return List of neighboring regions.
	 */
	public static List<Region> getNeighborsOfRegion(long regionId) {
		Set<Long> neighborIds = new HashSet<>();
		List<RegionChunk> chunks = getChunksOfRegion(regionId);

		for (RegionChunk chunk : chunks) {
			World world = chunk.getWorld();
			if (world == null) continue;

			int x = chunk.getX(), z = chunk.getZ();
			int[][] dirs = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};

			for (int[] dir : dirs) {
				RegionChunk neighbor = findChunk(world.getUID(), dir[0], dir[1]);
				if (neighbor != null && neighbor.getRegionId() != regionId) {
					neighborIds.add(neighbor.getRegionId());
				}
			}
		}

		List<Region> neighbors = new ArrayList<>();
		for (Long id : neighborIds) {
			Region r = RegionManager.findRegion(id);
			if (r != null) neighbors.add(r);
		}
		return neighbors;
	}

	/**
	 * Transfers a chunk from one region to another.
	 * @param chunk The chunk to transfer
	 * @param fromRegionId The current owning region
	 * @param toRegionId The target region
	 * @return {@code true} if the transfer was successful.
	 */
	public static boolean transferChunk(Chunk chunk, long fromRegionId, long toRegionId) {
		RegionChunk rc = findChunk(chunk);
		if (rc == null || rc.getRegionId() != fromRegionId) return false;
		if (RegionManager.findRegion(toRegionId) == null) return false;

		rc.setRegionId(toRegionId);
		return true;
	}

	/**
	 * Merges all chunks from one region into another. The source region will have no chunks left.
	 * @param fromRegionId The source region
	 * @param toRegionId The destination region
	 * @return The number of chunks transferred.
	 */
	public static int mergeRegions(long fromRegionId, long toRegionId) {
		if (RegionManager.findRegion(fromRegionId) == null || RegionManager.findRegion(toRegionId) == null) {
			return 0;
		}

		List<RegionChunk> toTransfer = getChunksOfRegion(fromRegionId);
		for (RegionChunk chunk : toTransfer) {
			chunk.setRegionId(toRegionId);
			Homestead.regionChunkCache.putOrUpdate(chunk);
		}
		return toTransfer.size();
	}

	/**
	 * Calculates the Manhattan distance from a chunk to the nearest chunk owned by a region.
	 * @param chunk The starting chunk
	 * @param region The target region
	 * @return The distance, or {@code -1} if the region has no chunks.
	 */
	public static int getChunkDistanceToRegion(Chunk chunk, Region region) {
		return getChunkDistanceToRegion(chunk, region.getUniqueId());
	}

	/**
	 * Calculates the Manhattan distance from a chunk to the nearest chunk owned by a region.
	 * @param chunk The starting chunk
	 * @param regionId The target region ID
	 * @return The distance, or {@code -1} if the region has no chunks.
	 */
	public static int getChunkDistanceToRegion(Chunk chunk, long regionId) {
		List<RegionChunk> regionChunks = getChunksOfRegion(regionId);
		if (regionChunks.isEmpty()) return -1;

		int minDist = Integer.MAX_VALUE;
		for (RegionChunk rc : regionChunks) {
			if (!rc.getWorldId().equals(chunk.getWorld().getUID())) continue;
			int dist = Math.abs(rc.getX() - chunk.getX()) + Math.abs(rc.getZ() - chunk.getZ());
			minDist = Math.min(minDist, dist);
		}
		return minDist == Integer.MAX_VALUE ? -1 : minDist;
	}

	/**
	 * Returns the total number of claimed chunks across all regions.
	 * @return The total claimed chunk count.
	 */
	public static int getTotalClaimedChunks() {
		return getAll().size();
	}

	/**
	 * Returns the percentage of a world's chunks that are claimed.
	 * @param world The world
	 * @return Percentage from 0.0 to 100.0, or {@code 0.0} if world has no border.
	 */
	public static double getClaimedPercentage(World world) {
		int claimed = getChunksInWorld(world).size();
		WorldBorder border = world.getWorldBorder();
		double size = border.getSize();
		if (size <= 0) return 0.0;
		long total = (long) ((size / 16.0) * (size / 16.0));
		return total == 0 ? 0.0 : (claimed * 100.0 / total);
	}

	/**
	 * Checks if a region is completely surrounded by other regions (no unclaimed neighbors).
	 * @param region The region
	 * @return {@code true} if every border chunk neighbor is claimed by another region.
	 */
	public static boolean isRegionSurrounded(Region region) {
		return isRegionSurrounded(region.getUniqueId());
	}

	/**
	 * Checks if a region is completely surrounded by other regions (no unclaimed neighbors).
	 * @param regionId The region ID
	 * @return {@code true} if every border chunk neighbor is claimed by another region.
	 */
	public static boolean isRegionSurrounded(long regionId) {
		for (RegionChunk borderChunk : getBorderChunks(regionId)) {
			World world = borderChunk.getWorld();
			if (world == null) continue;

			int x = borderChunk.getX(), z = borderChunk.getZ();
			int[][] dirs = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};

			for (int[] dir : dirs) {
				RegionChunk neighbor = findChunk(world.getUID(), dir[0], dir[1]);
				if (neighbor == null || neighbor.getRegionId() == regionId) {
					return false;
				}
			}
		}
		return true;
	}

	/** Returns an immutable view of every loaded chunk. */
	public static List<RegionChunk> getAll() {
		return Homestead.regionChunkCache.getAll();
	}

	/**
	 * Retrieves the chunk with the exact ID, or null if none exists.
	 * @param id The chunk ID
	 * @return The RegionChunk, or {@code null}.
	 */
	public static RegionChunk findChunk(long id) {
		return Homestead.regionChunkCache.get(id);
	}

	/**
	 * Finds a chunk by its world and coordinates.
	 * @param worldId The world UUID
	 * @param x The chunk X
	 * @param z The chunk Z
	 * @return The RegionChunk, or {@code null}.
	 */
	public static RegionChunk findChunk(UUID worldId, int x, int z) {
		for (RegionChunk chunk : getAll()) {
			if (chunk.getWorldId().equals(worldId) && chunk.getX() == x && chunk.getZ() == z) {
				return chunk;
			}
		}
		return null;
	}

	/**
	 * Finds a chunk by its Bukkit chunk.
	 * @param chunk The Bukkit chunk
	 * @return The RegionChunk, or {@code null}.
	 */
	public static RegionChunk findChunk(Chunk chunk) {
		return findChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
	}

	/**
	 * Determines whether removing the specified chunk would split the region
	 * into multiple disconnected areas.
	 * @param regionId The region ID
	 * @param chunkToRemove The chunk that will be removed
	 * @return {@code true} if removal would split the region.
	 */
	public static boolean wouldSplitRegion(long regionId, Chunk chunkToRemove) {
		List<RegionChunk> chunks = getChunksOfRegion(regionId);
		if (chunks.isEmpty()) return false;

		List<RegionChunk> remaining = new ArrayList<>(chunks);
		remaining.removeIf(c -> c.getWorldId().equals(chunkToRemove.getWorld().getUID())
				&& c.getX() == chunkToRemove.getX()
				&& c.getZ() == chunkToRemove.getZ());

		if (remaining.isEmpty()) return false;

		UUID worldId = remaining.getFirst().getWorldId();
		Set<RegionChunk> visited = new HashSet<>();
		Queue<RegionChunk> queue = new LinkedList<>();

		queue.add(remaining.getFirst());
		visited.add(remaining.getFirst());

		while (!queue.isEmpty()) {
			RegionChunk current = queue.poll();

			for (RegionChunk neighbor : remaining) {
				if (!neighbor.getWorldId().equals(worldId)) continue;
				if (visited.contains(neighbor)) continue;

				if (areAdjacent(current, neighbor)) {
					visited.add(neighbor);
					queue.add(neighbor);
				}
			}
		}

		return visited.size() != remaining.size();
	}

	private static boolean areAdjacent(RegionChunk a, RegionChunk b) {
		if (!a.getWorldId().equals(b.getWorldId())) return false;
		int dx = Math.abs(a.getX() - b.getX());
		int dz = Math.abs(a.getZ() - b.getZ());
		return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
	}

	/**
	 * Returns true if the chunk's world is on the exact list OR matches any
	 * configured glob-style pattern (supports * and ?).
	 * @param chunk The chunk
	 * @return {@code true} if the world is disabled.
	 */
	public static boolean isChunkInDisabledWorld(Chunk chunk) {
		String worldName = chunk.getWorld().getName();

		List<String> exact = Resources.<RegionsFile>get(ResourceType.Regions).getStringList("disabled-worlds-exact");
		if (exact.contains(worldName)) {
			return true;
		}

		List<String> patterns = Resources.<RegionsFile>get(ResourceType.Regions).getStringList("disabled-worlds-pattern");
		for (String pat : patterns) {
			if (!pat.contains("*") && !pat.contains("?")) {
				if (pat.equals(worldName)) return true;
				continue;
			}
			String regex = "\\Q" + pat.replace("*", "\\E.*\\Q")
					.replace("?", "\\E.\\Q") + "\\E";
			if (worldName.matches(regex)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether a given chunk is already claimed by any region.
	 * @param chunk The chunk
	 * @return {@code true} if claimed.
	 */
	public static boolean isChunkClaimed(Chunk chunk) {
		return findChunk(chunk) != null;
	}

	/**
	 * Returns the region that owns a given chunk, or null if it is unclaimed.
	 * @param chunk The chunk
	 * @return The owning region, or {@code null}.
	 */
	public static Region getRegionOwnsTheChunk(Chunk chunk) {
		RegionChunk rc = findChunk(chunk);
		if (rc == null) return null;
		return RegionManager.findRegion(rc.getRegionId());
	}

	/**
	 * Returns {@code true} if the region owns a given chunk, {@code false} otherwise.
	 * @param region The region
	 * @param chunk The chunk
	 * @return {@code true} if owned by the region.
	 */
	public static boolean isChunkClaimedByRegion(Region region, Chunk chunk) {
		return isChunkClaimedByRegion(region.getUniqueId(), chunk);
	}

	/**
	 * Returns {@code true} if the region owns a given chunk, {@code false} otherwise.
	 * @param regionId The region ID
	 * @param chunk The chunk
	 * @return {@code true} if owned by the region.
	 */
	public static boolean isChunkClaimedByRegion(long regionId, Chunk chunk) {
		RegionChunk rc = findChunk(chunk);
		return rc != null && rc.getRegionId() == regionId;
	}

	/**
	 * Returns true if the provided chunk is adjacent to any chunk owned by the same region.
	 * @param regionId The region ID
	 * @param chunk The chunk
	 * @return {@code true} if an adjacent owned chunk exists.
	 */
	public static boolean hasAdjacentOwnedChunk(long regionId, Chunk chunk) {
		World world = chunk.getWorld();
		int x = chunk.getX();
		int z = chunk.getZ();

		int[][] dirs = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};
		for (int[] dir : dirs) {
			int nx = dir[0], nz = dir[1];
			if (!world.isChunkLoaded(nx, nz)) continue;

			Chunk neighbor = world.getChunkAt(nx, nz);
			if (isChunkClaimedByRegion(regionId, neighbor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds a nearby unclaimed chunk around the player's current chunk within a radius up to 30.
	 * @param player The player
	 * @return An unclaimed chunk, or {@code null}.
	 */
	public static Chunk findNearbyUnclaimedChunk(Player player) {
		Chunk start = player.getLocation().getChunk();
		World world = player.getWorld();
		int sx = start.getX(), sz = start.getZ();

		for (int radius = 1; radius <= 30; radius++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
					if (!world.isChunkLoaded(sx + x, sz + z)) continue;

					Chunk current = world.getChunkAt(sx + x, sz + z);
					if (!isChunkClaimed(current)) return current;
				}
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the player has any neighboring claimed chunks that belong to another region.
	 * @param player The player
	 * @return {@code true} if a foreign neighbor exists.
	 */
	public static boolean hasNeighbor(Player player) {
		Chunk chunk = player.getLocation().getChunk();
		World world = player.getWorld();
		int x = chunk.getX();
		int z = chunk.getZ();

		Chunk[] neighbors = {
				world.getChunkAt(x, z - 1),
				world.getChunkAt(x, z + 1),
				world.getChunkAt(x - 1, z),
				world.getChunkAt(x + 1, z)
		};

		for (Chunk neighbor : neighbors) {
			if (isChunkClaimed(neighbor)) {
				Region r = getRegionOwnsTheChunk(neighbor);
				if (r != null && !r.isOwner(player)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the chunk object for the given world and chunk coordinates.
	 * @param world The world
	 * @param x The chunk coordinates (X axis)
	 * @param z The chunk coordinates (Z axis)
	 * @return The Bukkit Chunk.
	 */
	public static Chunk getFromLocation(World world, int x, int z) {
		Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
		return location.getChunk();
	}

	/**
	 * Removes a random chunk from the region.
	 * @param region The region
	 */
	public static void removeRandomChunk(Region region) {
		removeRandomChunk(region.getUniqueId());
	}

	/**
	 * Removes a random chunk from the region.
	 * @param regionId The region ID
	 */
	public static void removeRandomChunk(long regionId) {
		List<RegionChunk> chunks = getChunksOfRegion(regionId);
		if (chunks.isEmpty()) return;

		int index = RANDOM.nextInt(chunks.size());
		forceUnclaimChunk(regionId, chunks.get(index).toBukkit());
	}

	/**
	 * Re-registers force-loaded chunks after a server restart.
	 */
	public static void reregisterForceLoadedChunks() {
		for (RegionChunk chunk : getAll()) {
			if (chunk.isForceLoaded()) {
				World world = Bukkit.getWorld(chunk.getWorldId());
				if (world != null) {
					PersistentChunkTicket.addPersistent(Homestead.getInstance(), world, chunk.getX(), chunk.getZ());
				}
			}
		}
	}

	/**
	 * Clean up force loaded chunks that are not claimed by any region.
	 * @return The number of orphaned chunks cleaned up.
	 */
	public static int cleanupOrphanedForceLoadedChunks() {
		int count = 0;
		for (Chunk chunk : PersistentChunkTicket.getAllForceLoadedChunks()) {
			if (!isChunkClaimed(chunk)) {
				PersistentChunkTicket.removePersistent(Homestead.getInstance(), chunk);
				count++;
			}
		}
		return count;
	}

	/**
	 * Removes all claimed chunks with invalid references:<br>
	 * - Worlds that no longer exist<br>
	 * - Regions that no longer exist<br><br>
	 * Also cleans orphaned force-loaded chunks.
	 * @return Number of corrupted chunks removed.
	 */
	public static int cleanupInvalidChunks() {
		Set<UUID> validWorlds = Bukkit.getWorlds().stream()
				.map(World::getUID)
				.collect(Collectors.toSet());

		List<Long> toRemove = new ArrayList<>();

		for (RegionChunk chunk : getAll()) {
			boolean invalidWorld = !validWorlds.contains(chunk.getWorldId());
			boolean invalidRegion = chunk.getRegion() == null;

			if (invalidWorld || invalidRegion) {
				World world = chunk.getWorld();
				if (world != null) {
					PersistentChunkTicket.removePersistent(Homestead.getInstance(), world, chunk.getX(), chunk.getZ());
				}
				toRemove.add(chunk.getUniqueId());
			}
		}

		for (Long id : toRemove) {
			deleteChunk(id);
		}
		return toRemove.size();
	}

	public enum Error {
		REGION_NOT_FOUND,
		CHUNK_NOT_FOUND,
		CHUNK_IN_DISABLED_WORLD,
		CHUNK_NOT_ADJACENT_TO_REGION,
		CHUNK_WOULD_SPLIT_REGION
	}
}