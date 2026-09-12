package me.tayebyassine.homestead.managers;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.database.cache.ChunkPositionKey;
import me.tayebyassine.homestead.integrations.FastAsyncWorldEditAPI;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionChunk;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkUtility;
import me.tayebyassine.homestead.util.minecraft.chunks.PersistentChunkTicket;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link RegionChunk}.
 */
public final class ChunkManager {
    private static final Random RANDOM = new Random();

    private ChunkManager() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Creates a new chunk entry for a region.<br>
     * This method applies no verification and may cause two or more regions owning the same chunk, which could create a fatal exploit.
     *
     * @param region the region
     * @param chunk  the chunk
     * @return the created RegionChunk
     */
    private static RegionChunk createChunk(Region region, Chunk chunk) {
        return createChunk(region.getUniqueId(), chunk);
    }

    /**
     * Creates a new chunk entry for a region.<br>
     * This method applies no verification and may cause two or more regions owning the same chunk, which could create a fatal exploit.
     *
     * @param regionId the region ID
     * @param chunk    the chunk
     * @return the created RegionChunk
     */
    private static RegionChunk createChunk(long regionId, Chunk chunk) {
        RegionChunk regionChunk = new RegionChunk(
                Homestead.getSnowflake().nextId(),
                regionId,
                chunk.getWorld().getUID(),
                chunk.getX(),
                chunk.getZ(),
                System.currentTimeMillis(),
                false
        );
        Homestead.CHUNK_CACHE.putOrUpdate(regionChunk);
        Homestead.REGION_INDEXED_CHUNK_CACHE.add(regionChunk);
        Homestead.POSITION_INDEXED_CHUNK_CACHE.add(regionChunk);
        return regionChunk;
    }

    /**
     * Claims a chunk for a specific region with normal protection checks.
     *
     * @param region the region
     * @param chunk  the chunk
     * @return {@link Error} if there is an error, <code>null</code> otherwise
     */
    public static Error claimChunk(Region region, Chunk chunk) {
        return claimChunk(region.getUniqueId(), chunk);
    }

    /**
     * Claims a chunk for a specific region with normal protection checks.
     *
     * @param regionId the region ID
     * @param chunk    the chunk
     * @return {@link Error} if there is an error, <code>null</code> otherwise
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

        createChunk(regionId, chunk);

        return null;
    }

    /**
     * Unclaims a chunk with normal protection checks.
     *
     * @param region the region
     * @param chunk  the chunk
     * @return {@link Error} if there is an error, <code>null</code> otherwise
     */
    public static Error unclaimChunk(Region region, Chunk chunk) {
        return unclaimChunk(region.getUniqueId(), chunk);
    }

    /**
     * Unclaims a chunk with normal protection checks.
     *
     * @param regionId the region ID
     * @param chunk    the chunk
     * @return {@link Error} if there is an error, <code>null</code> otherwise
     */
    public static Error unclaimChunk(long regionId, Chunk chunk) {
        return unclaimChunkInternal(regionId, chunk, false);
    }

    /**
     * Unclaims a chunk bypassing split/topology protection and ownership checks.
     *
     * @param region the region
     * @param chunk  the chunk
     * @return {@link Error} if there is an error, <code>null</code> otherwise
     */
    public static Error forceUnclaimChunk(Region region, Chunk chunk) {
        return unclaimChunkInternal(region.getUniqueId(), chunk, true);
    }

    /**
     * Unclaims a chunk bypassing split/topology protection and ownership checks.
     *
     * @param regionId the region ID
     * @param chunk    the chunk
     * @return {@link Error} if there is an error, <code>null</code> otherwise
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

        deleteChunk(chunk);

        return null;
    }

    /**
     * Permanently deletes a chunk.
     *
     * @param chunk the chunk
     */
    private static void deleteChunk(Chunk chunk) {
        RegionChunk chunkData = findChunk(chunk);

        if (chunkData != null) {
            deleteChunk(chunkData);
        }
    }

    /**
     * Permanently deletes a chunk.
     *
     * @param chunk the region chunk
     */
    private static void deleteChunk(RegionChunk chunk) {
        deleteChunk(chunk.getUniqueId());
    }

    /**
     * Permanently deletes a chunk.
     *
     * @param id the chunk ID
     */
    private static void deleteChunk(long id) {
        RegionChunk r = Homestead.CHUNK_CACHE.remove(id);
        if (r != null) {
            Homestead.REGION_INDEXED_CHUNK_CACHE.remove(r);
            Homestead.POSITION_INDEXED_CHUNK_CACHE.remove(r);
        }
    }

    /**
     * Returns all chunks belonging to a region.
     *
     * @param region the region
     * @return list of region chunks
     */
    public static List<RegionChunk> getChunksOfRegion(Region region) {
        return getChunksOfRegion(region.getUniqueId());
    }

    /**
     * Returns all chunks belonging to a region.
     *
     * @param regionId the region ID
     * @return list of region chunks
     */
    public static List<RegionChunk> getChunksOfRegion(long regionId) {
        return Homestead.REGION_INDEXED_CHUNK_CACHE.get(regionId);
    }

    /**
     * Returns the number of chunks in the server.
     *
     * @return the chunk count
     */
    public static int getChunkCount() {
        return getAll().size();
    }

    /**
     * Returns the number of chunks owned by a region.
     *
     * @param region the region
     * @return the chunk count
     */
    public static int getChunkCount(Region region) {
        return getChunkCount(region.getUniqueId());
    }

    /**
     * Returns the number of chunks owned by a region.
     *
     * @param regionId the region ID
     * @return the chunk count
     */
    public static int getChunkCount(long regionId) {
        return getChunksOfRegion(regionId).size();
    }

    /**
     * Returns all claimed chunks in a specific world.
     *
     * @param world the world
     * @return list of chunks in the world
     */
    public static List<RegionChunk> getChunksInWorld(World world) {
        return getChunksInWorld(world.getUID());
    }

    /**
     * Returns all claimed chunks in a specific world.
     *
     * @param worldId the world UUID
     * @return list of chunks in the world
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
     *
     * @param region the region
     * @return list of force-loaded chunks
     */
    public static List<RegionChunk> getForceLoadedChunks(Region region) {
        return getForceLoadedChunks(region.getUniqueId());
    }

    /**
     * Returns all force-loaded chunks belonging to a region.
     *
     * @param regionId the region ID
     * @return list of force-loaded chunks
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
     * Returns whether a given chunk is already claimed by any region.
     *
     * @param chunk the chunk
     * @return {@code true} if claimed
     */
    public static boolean isChunkClaimed(Chunk chunk) {
        return findChunk(chunk) != null;
    }

    /**
     * Returns the region that owns a given chunk, or null if it is unclaimed.
     *
     * @param chunk the chunk
     * @return the owning region, or {@code null}
     */
    public static Region getRegionOwnsTheChunk(Chunk chunk) {
        RegionChunk rc = findChunk(chunk);
        if (rc == null) return null;
        return RegionManager.findRegion(rc.getRegionId());
    }

    /**
     * Returns {@code true} if the region owns a given chunk, {@code false} otherwise.
     *
     * @param region the region
     * @param chunk  the chunk
     * @return {@code true} if owned by the region
     */
    public static boolean isChunkClaimedByRegion(Region region, Chunk chunk) {
        return isChunkClaimedByRegion(region.getUniqueId(), chunk);
    }

    /**
     * Returns {@code true} if the region owns a given chunk, {@code false} otherwise.
     *
     * @param regionId the region ID
     * @param chunk    the chunk
     * @return {@code true} if owned by the region
     */
    public static boolean isChunkClaimedByRegion(long regionId, Chunk chunk) {
        RegionChunk rc = findChunk(chunk);
        return rc != null && rc.getRegionId() == regionId;
    }

    /**
     * Returns true if the provided chunk is adjacent to any chunk owned by the same region.
     *
     * @param regionId the region ID
     * @param chunk    the chunk
     * @return {@code true} if an adjacent owned chunk exists
     */
    public static boolean hasAdjacentOwnedChunk(long regionId, Chunk chunk) {
        UUID worldId = chunk.getWorld().getUID();
        int x = chunk.getX();
        int z = chunk.getZ();

        int[][] dirs = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};
        for (int[] dir : dirs) {
            RegionChunk neighbor = findChunk(worldId, dir[0], dir[1]);
            if (neighbor != null && neighbor.getRegionId() == regionId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the chunk's world is on the exact list OR matches any
     * configured glob-style pattern (supports * and ?).
     *
     * @param chunk the chunk
     * @return {@code true} if the world is disabled
     */
    public static boolean isChunkInDisabledWorld(Chunk chunk) {
        String worldName = chunk.getWorld().getName();

        List<String> exact = Resources.<RegionsFile>get(ResourceType.Regions).getDisabledWorldsExact();
        if (exact.contains(worldName)) {
            return true;
        }

        List<String> patterns = Resources.<RegionsFile>get(ResourceType.Regions).getDisabledWorldsPattern();
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
     * Retrieves the chunk with the exact ID, or null if none exists.
     *
     * @param id the chunk ID
     * @return the RegionChunk, or {@code null}
     */
    public static RegionChunk findChunk(long id) {
        return Homestead.CHUNK_CACHE.get(id);
    }

    /**
     * Finds a chunk by its world and coordinates.
     *
     * @param worldId the world UUID
     * @param x       the chunk X
     * @param z       the chunk Z
     * @return the RegionChunk, or {@code null}
     */
    public static RegionChunk findChunk(UUID worldId, int x, int z) {
        return Homestead.POSITION_INDEXED_CHUNK_CACHE.get(new ChunkPositionKey(worldId, x, z));
    }

    /**
     * Finds a chunk by its Bukkit chunk.
     *
     * @param chunk the Bukkit chunk
     * @return the RegionChunk, or {@code null}
     */
    public static RegionChunk findChunk(Chunk chunk) {
        if (chunk == null) return null;

        return findChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

    /**
     * Returns an immutable view of every loaded chunk.
     *
     * @return list of all chunks
     */
    public static List<RegionChunk> getAll() {
        return Homestead.CHUNK_CACHE.getAll();
    }

    /**
     * Calculates the centroid (average center) of all chunks in a region.
     *
     * @param region the region
     * @return the centroid chunk coordinates as {@code int[2]} (x, z), or {@code null} if no chunks
     */
    public static int[] getChunkCentroid(Region region) {
        return getChunkCentroid(region.getUniqueId());
    }

    /**
     * Calculates the centroid (average center) of all chunks in a region.
     *
     * @param regionId the region ID
     * @return the centroid chunk coordinates as {@code int[2]} (x, z), or {@code null} if no chunks
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
     *
     * @param region the region
     * @return {@code int[4]} as {minX, minZ, maxX, maxZ}, or {@code null} if no chunks
     */
    public static int[] getBoundingChunks(Region region) {
        return getBoundingChunks(region.getUniqueId());
    }

    /**
     * Returns the bounding box of a region's chunks in chunk coordinates.
     *
     * @param regionId the region ID
     * @return {@code int[4]} as {minX, minZ, maxX, maxZ}, or {@code null} if no chunks
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
     *
     * @param region the region
     * @return the area in chunks, or {@code 0} if no chunks
     */
    public static int getRegionChunkArea(Region region) {
        return getRegionChunkArea(region.getUniqueId());
    }

    /**
     * Returns the total chunk area (width * depth) of a region's bounding box.
     *
     * @param regionId the region ID
     * @return the area in chunks, or {@code 0} if no chunks
     */
    public static int getRegionChunkArea(long regionId) {
        int[] bounds = getBoundingChunks(regionId);
        if (bounds == null) return 0;
        return (bounds[2] - bounds[0] + 1) * (bounds[3] - bounds[1] + 1);
    }

    /**
     * Checks if a chunk is on the border (edge) of a region.
     *
     * @param regionId the region ID
     * @param chunk    the chunk to check
     * @return {@code true} if the chunk touches the region edge
     */
    public static boolean isChunkOnBorder(long regionId, RegionChunk chunk) {
        int x = chunk.getX(), z = chunk.getZ();
        RegionChunk rc = findChunk(chunk.getWorldId(), x, z);
        if (rc == null || rc.getRegionId() != regionId) return false;

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            RegionChunk neighbor = findChunk(chunk.getWorldId(), x + dir[0], z + dir[1]);
            if (neighbor == null || neighbor.getRegionId() != regionId) return true;
        }
        return false;
    }

    /**
     * Returns all chunks on the border of a region.
     *
     * @param region the region
     * @return list of border chunks
     */
    public static List<RegionChunk> getBorderChunks(Region region) {
        return getBorderChunks(region.getUniqueId());
    }

    /**
     * Returns all chunks on the border of a region.
     *
     * @param regionId the region ID
     * @return list of border chunks
     */
    public static List<RegionChunk> getBorderChunks(long regionId) {
        List<RegionChunk> border = new ArrayList<>();
        for (RegionChunk chunk : getChunksOfRegion(regionId)) {
            if (isChunkOnBorder(regionId, chunk)) {
                border.add(chunk);
            }
        }
        return border;
    }

    /**
     * Returns all unique regions that are adjacent (share a chunk border) to the given region.
     *
     * @param region the region
     * @return list of neighboring regions
     */
    public static List<Region> getNeighborsOfRegion(Region region) {
        return getNeighborsOfRegion(region.getUniqueId());
    }

    /**
     * Returns all unique regions that are adjacent (share a chunk border) to the given region.
     *
     * @param regionId the region ID
     * @return list of neighboring regions
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
     * Determines whether removing the specified chunk would split the region
     * into multiple disconnected areas.
     *
     * @param regionId      the region ID
     * @param chunkToRemove the chunk that will be removed
     * @return {@code true} if removal would split the region
     */
    public static boolean wouldSplitRegion(long regionId, Chunk chunkToRemove) {
        List<RegionChunk> chunks = getChunksOfRegion(regionId);
        if (chunks.isEmpty()) return false;

        UUID removeWorldId = chunkToRemove.getWorld().getUID();
        int rx = chunkToRemove.getX(), rz = chunkToRemove.getZ();

        List<RegionChunk> remaining = new ArrayList<>(chunks);
        remaining.removeIf(c -> c.getWorldId().equals(removeWorldId) && c.getX() == rx && c.getZ() == rz);
        if (remaining.isEmpty()) return false;

        Set<Long> globalVisited = new HashSet<>();
        Map<UUID, List<RegionChunk>> byWorld = remaining.stream()
                .collect(Collectors.groupingBy(RegionChunk::getWorldId));

        for (Map.Entry<UUID, List<RegionChunk>> entry : byWorld.entrySet()) {
            List<RegionChunk> worldChunks = entry.getValue();
            Set<Long> worldVisited = new HashSet<>();
            Queue<RegionChunk> queue = new LinkedList<>();

            queue.add(worldChunks.getFirst());
            worldVisited.add(worldChunks.getFirst().getUniqueId());

            while (!queue.isEmpty()) {
                RegionChunk current = queue.poll();
                int cx = current.getX(), cz = current.getZ();
                int[][] dirs = {{cx + 1, cz}, {cx - 1, cz}, {cx, cz + 1}, {cx, cz - 1}};

                for (int[] dir : dirs) {
                    RegionChunk neighbor = findChunk(entry.getKey(), dir[0], dir[1]);
                    if (neighbor == null || neighbor.getRegionId() != regionId) continue;
                    if (neighbor.getX() == rx && neighbor.getZ() == rz) continue;
                    if (worldVisited.add(neighbor.getUniqueId())) {
                        queue.add(neighbor);
                    }
                }
            }

            globalVisited.addAll(worldVisited);
            if (worldVisited.size() != worldChunks.size()) return true;
        }

        return globalVisited.size() != remaining.size();
    }

    /**
     * Checks if a region is completely surrounded by other regions (no unclaimed neighbors).
     *
     * @param region the region
     * @return {@code true} if every border chunk neighbor is claimed by another region
     */
    public static boolean isRegionSurrounded(Region region) {
        return isRegionSurrounded(region.getUniqueId());
    }

    /**
     * Checks if a region is completely surrounded by other regions (no unclaimed neighbors).
     *
     * @param regionId the region ID
     * @return {@code true} if every border chunk neighbor is claimed by another region
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

    /**
     * Transfers a chunk from one region to another.
     *
     * @param chunk        the chunk to transfer
     * @param fromRegionId the current owning region
     * @param toRegionId   the target region
     * @return {@code true} if the transfer was successful
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
     *
     * @param fromRegionId the source region
     * @param toRegionId   the destination region
     * @return the number of chunks transferred
     */
    public static int mergeRegions(long fromRegionId, long toRegionId) {
        if (RegionManager.findRegion(fromRegionId) == null || RegionManager.findRegion(toRegionId) == null) {
            return 0;
        }

        List<RegionChunk> toTransfer = new ArrayList<>(getChunksOfRegion(fromRegionId));

        for (RegionChunk chunk : toTransfer) {
            chunk.setRegionId(toRegionId);
        }

        return toTransfer.size();
    }

    /**
     * Calculates the Manhattan distance from a chunk to the nearest chunk owned by a region.
     *
     * @param chunk  the starting chunk
     * @param region the target region
     * @return the distance, or {@code -1} if the region has no chunks
     */
    public static int getChunkDistanceToRegion(Chunk chunk, Region region) {
        return getChunkDistanceToRegion(chunk, region.getUniqueId());
    }

    /**
     * Calculates the Manhattan distance from a chunk to the nearest chunk owned by a region.
     *
     * @param chunk    the starting chunk
     * @param regionId the target region ID
     * @return the distance, or {@code -1} if the region has no chunks
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
     *
     * @return the total claimed chunk count
     */
    public static int getTotalClaimedChunks() {
        return getAll().size();
    }

    /**
     * Returns the percentage of a world's chunks that are claimed.
     *
     * @param world the world
     * @return percentage from 0.0 to 100.0, or {@code 0.0} if world has no border
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
     * Finds a nearby unclaimed chunk around the player's current chunk within a radius up to 30.
     *
     * @param player the player
     * @return an unclaimed chunk, or {@code null}
     */
    public static Chunk findNearbyUnclaimedChunk(Player player) {
        World world = player.getWorld();
        UUID worldId = world.getUID();
        Chunk start = player.getLocation().getChunk();
        int sx = start.getX(), sz = start.getZ();

        for (int radius = 1; radius <= 30; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
                    int cx = sx + x, cz = sz + z;
                    if (findChunk(worldId, cx, cz) != null) continue;
                    return world.getChunkAt(cx, cz);
                }
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the player has any neighboring claimed chunks that belong to another region.
     *
     * @param player the player
     * @return {@code true} if a foreign neighbor exists
     */
    public static boolean hasNeighbor(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        UUID worldId = chunk.getWorld().getUID();
        int x = chunk.getX();
        int z = chunk.getZ();

        int[][] dirs = {{x, z - 1}, {x, z + 1}, {x - 1, z}, {x + 1, z}};
        for (int[] dir : dirs) {
            RegionChunk rc = findChunk(worldId, dir[0], dir[1]);
            if (rc != null) {
                Region r = RegionManager.findRegion(rc.getRegionId());
                if (r != null && !r.isOwner(player)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the chunk object for the given world and chunk coordinates.
     *
     * @param world the world
     * @param x     the chunk coordinates (X axis)
     * @param z     the chunk coordinates (Z axis)
     * @return the Bukkit Chunk
     */
    public static Chunk getFromLocation(World world, int x, int z) {
        Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
        return location.getChunk();
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
     * Removes a random chunk from the region.
     *
     * @param region the region
     */
    public static void removeRandomChunk(Region region) {
        removeRandomChunk(region.getUniqueId());
    }

    /**
     * Removes a random chunk from the region.
     *
     * @param regionId the region ID
     */
    public static void removeRandomChunk(long regionId) {
        List<RegionChunk> chunks = getChunksOfRegion(regionId);
        if (chunks.isEmpty()) return;

        int index = RANDOM.nextInt(chunks.size());
        forceUnclaimChunk(regionId, chunks.get(index).toBukkit());
    }

    /**
     * Clean up force loaded chunks that are not claimed by any region.
     *
     * @return the number of orphaned chunks cleaned up
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
     *
     * @return number of corrupted chunks removed
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
