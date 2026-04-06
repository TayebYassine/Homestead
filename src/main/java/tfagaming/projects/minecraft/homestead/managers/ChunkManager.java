package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.ChunkClaimEvent;
import tfagaming.projects.minecraft.homestead.api.events.ChunkUnclaimEvent;
import tfagaming.projects.minecraft.homestead.integrations.ChunkyAPI;
import tfagaming.projects.minecraft.homestead.integrations.WorldEditAPI;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.PersistentChunkTicket;

import java.util.*;

/**
 * Handles claiming, unclaiming, and managing chunks in regions.
 * Includes adjacency enforcement, anti-split protection, and data handling.<br>
 * This is a utility class that helps manage chunks more easily. Updating and setting data to regions is generally done to the {@link Region} object.
 */
public final class ChunkManager {
	private ChunkManager() {
	}

	/**
	 * Claims a chunk for a specific region. Returns true if it was successfully claimed, otherwise false.
	 * @param id The region UUID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, otherwise <code>null</code>.
	 */
	public static Error claimChunk(UUID id, Chunk chunk) {
		Region region = RegionManager.findRegion(id);

		if (region == null) {
			return Error.REGION_NOT_FOUND;
		}

		if (isChunkInDisabledWorld(chunk)) {
			return Error.CHUNK_IN_DISABLED_WORLD;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isAdjacentChunksRuleEnabled() && !region.getChunks().isEmpty() && !hasAdjacentOwnedChunk(region, chunk)) {
			return Error.CHUNK_NOT_ADJACENT_TO_REGION;
		}

		region.addChunk(chunk);

		ChunkClaimEvent event = new ChunkClaimEvent(region, chunk);
		Location chunkLoc = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
		Homestead.getInstance().runLocationTask(chunkLoc, () -> Bukkit.getPluginManager().callEvent(event));

		return null;
	}

	/**
	 * Unclaims a chunk with normal protection checks.
	 * @param id The region UUID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, otherwise <code>null</code>.
	 */
	public static Error unclaimChunk(UUID id, Chunk chunk) {
		return unclaimChunkInternal(id, chunk, false);
	}

	/**
	 * Unclaims a chunk bypassing split/topology protection and ownership checks.
	 * @param id The region UUID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, otherwise <code>null</code>.
	 */
	public static Error forceUnclaimChunk(UUID id, Chunk chunk) {
		return unclaimChunkInternal(id, chunk, true);
	}

	private static Error unclaimChunkInternal(UUID id, Chunk chunk, boolean force) {
		Region region = RegionManager.findRegion(id);

		if (region == null) {
			return Error.REGION_NOT_FOUND;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isAdjacentChunksRuleEnabled() && !force && wouldSplitRegion(region, chunk)) {
			return Error.CHUNK_WOULD_SPLIT_REGION;
		}

		removeChunk(id, chunk);

		if (Resources.<ConfigFile>get(ResourceType.Config).regenerateChunksWithWorldEdit()) {
			ChunkyAPI.regenerateChunk(chunk.getWorld(), chunk);
		}

		if (region.getLocation() != null && ChunkUtils.areEqual(region.getLocation().bukkit().getChunk(), chunk)) {
			region.setLocationToNull();
		}

		ChunkUnclaimEvent event = new ChunkUnclaimEvent(region, chunk);
		Location chunkLoc = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
		Homestead.getInstance().runLocationTask(chunkLoc, () -> Bukkit.getPluginManager().callEvent(event));

		return null;
	}

	/**
	 * Removes a claimed chunk from a region and its sub-areas.
	 * @param id The region UUID
	 * @param chunk The chunk
	 * @return {@link Error} if there is an error, otherwise <code>null</code>.
	 */
	public static Error removeChunk(UUID id, Chunk chunk) {
		Region region = RegionManager.findRegion(id);

		if (region == null) {
			return Error.REGION_NOT_FOUND;
		}

		region.removeChunk(chunk);

		PersistentChunkTicket.removePersistent(Homestead.getInstance(), chunk);

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(id)) {
			for (Chunk subAreaChunk : ChunkUtils.getChunksInArea(subArea.getFirstPoint(), subArea.getSecondPoint())) {
				if (ChunkUtils.areEqual(subAreaChunk, chunk)) {
					SubAreaManager.deleteSubArea(subArea.getUniqueId());
					break;
				}
			}
		}

		return null;
	}

	/**
	 * Determines whether removing the specified chunk would split the region
	 * into multiple disconnected areas.
	 * Used for unclaiming chunks.
	 * @param region The region
	 * @param chunkToRemove The chunk that will be removed
	 */
	public static boolean wouldSplitRegion(Region region, Chunk chunkToRemove) {
		if (region == null || region.getChunks() == null || region.getChunks().isEmpty()) return false;

		List<SerializableChunk> normalized = new ArrayList<>();
		for (SerializableChunk c : region.getChunks()) {
			if (c == null || c.getWorldId() == null) continue;
			normalized.add(c);
		}

		Map<String, SerializableChunk> byKey = new LinkedHashMap<>();
		for (SerializableChunk c : normalized) {
			byKey.putIfAbsent(c.toString(true), c);
		}

		List<SerializableChunk> chunks = new ArrayList<>(byKey.values());
		chunks.removeIf(c -> ChunkUtils.areEqual(c.bukkit(), chunkToRemove));

		if (chunks.isEmpty()) return false;

		UUID worldId = chunks.getFirst().getWorldId();
		Set<SerializableChunk> visited = new HashSet<>();
		Queue<SerializableChunk> queue = new LinkedList<>();

		queue.add(chunks.getFirst());
		visited.add(chunks.getFirst());

		while (!queue.isEmpty()) {
			SerializableChunk current = queue.poll();

			for (SerializableChunk neighbor : chunks) {
				if (neighbor == null) continue;
				if (!neighbor.getWorldId().equals(worldId)) continue;
				if (visited.contains(neighbor)) continue;

				if (areAdjacent(current, neighbor)) {
					visited.add(neighbor);
					queue.add(neighbor);
				}
			}
		}

		return visited.size() != chunks.size();
	}

	/**
	 * Returns whether two chunks are directly adjacent (N, S, E, W) in the same world.
	 * @param a The first chunk
	 * @param b The second chunk
	 */
	private static boolean areAdjacent(SerializableChunk a, SerializableChunk b) {
		if (!a.getWorldId().equals(b.getWorldId())) return false;
		int dx = Math.abs(a.getX() - b.getX());
		int dz = Math.abs(a.getZ() - b.getZ());
		return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
	}

	/**
	 * Returns true if the chunk's world is on the exact list OR matches any
	 * configured glob-style pattern (supports * and ?).
	 * @param chunk The chunk
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
	 */
	public static boolean isChunkClaimed(Chunk chunk) {
		String key = SerializableChunk.convertToString(chunk);
		for (Region region : RegionManager.getAll()) {
			for (SerializableChunk serialized : region.getChunks()) {
				if (serialized == null) continue;
				if (serialized.toString(true).equals(key)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns the region that owns a given chunk, or null if it is unclaimed.
	 * @param chunk The chunk
	 */
	public static Region getRegionOwnsTheChunk(Chunk chunk) {
		String key = SerializableChunk.convertToString(chunk);
		for (Region region : RegionManager.getAll()) {
			for (SerializableChunk serialized : region.getChunks()) {
				if (serialized == null) continue;
				if (serialized.toString(true).equals(key)) return region;
			}
		}
		return null;
	}

	/**
	 * Returns true if the region owns a given chunk, otherwise false.
	 * @param chunk The chunk
	 */
	public static boolean isChunkClaimedByRegion(Region region, Chunk chunk) {
		for (SerializableChunk each : region.getChunks()) {
			if (SerializableChunk.equals(each, chunk)) return true;
		}

		return false;
	}

	/**
	 * Returns true if the provided chunk is adjacent to any chunk owned by the same region.
	 * @param region The region
	 * @param chunk The chunk
	 */
	public static boolean hasAdjacentOwnedChunk(Region region, Chunk chunk) {
		World world = chunk.getWorld();
		int x = chunk.getX();
		int z = chunk.getZ();

		int[][] dirs = {{x + 1, z}, {x - 1, z}, {x, z + 1}, {x, z - 1}};
		for (int[] dir : dirs) {
			int nx = dir[0], nz = dir[1];
			if (!world.isChunkLoaded(nx, nz)) continue;

			Chunk neighbor = world.getChunkAt(nx, nz);
			if (ChunkManager.isChunkClaimedByRegion(region, neighbor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds a nearby unclaimed chunk around the player's current chunk within a radius up to 30.
	 * @param player The player
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
	 */
	public static Chunk getFromLocation(World world, int x, int z) {
		Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
		return location.getChunk();
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
			loc = findSafeNetherLocation(world, x, z);
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
	 * Returns a safe location for a SerializableChunk.
	 * @param player The player
	 * @param chunk The chunk
	 */
	public static Location getLocation(Player player, SerializableChunk chunk) {
		World world = chunk.getWorld();
		if (world == null) return null;

		int x = chunk.getX() * 16 + 8;
		int z = chunk.getZ() * 16 + 8;

		Location loc;
		if (world.getEnvironment() == World.Environment.NETHER) {
			loc = findSafeNetherLocation(world, x, z);
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
	private static Location findSafeNetherLocation(World world, int x, int z) {
		for (int y = 32; y < 127; y++) {
			Block block = world.getBlockAt(x, y, z);
			Block above = world.getBlockAt(x, y + 1, z);
			if (block.getType() == Material.AIR && above.getType() == Material.AIR) {
				return new Location(world, x + 0.5, y, z + 0.5);
			}
		}
		return null;
	}

	/**
	 * Removes a random chunk from the region.
	 * @param id The region UUID
	 */
	public static void removeRandomChunk(UUID id) {
		Region region = RegionManager.findRegion(id);
		if (region == null) return;

		List<SerializableChunk> chunks = region.getChunks();
		if (chunks == null || chunks.isEmpty()) return;

		int index = new Random().nextInt(chunks.size());

		forceUnclaimChunk(id, chunks.get(index).bukkit());
	}

	/**
	 * Removes chunks belonging to worlds that no longer exist on the server.
	 */
	public static int deleteInvalidChunks() {
		int count = 0;
		Set<UUID> worlds = new HashSet<>();
		for (World w : Bukkit.getWorlds()) worlds.add(w.getUID());

		for (Region region : RegionManager.getAll()) {
			if (region == null || region.getChunks() == null || region.getChunks().isEmpty()) continue;

			Iterator<SerializableChunk> it = region.getChunks().iterator();
			while (it.hasNext()) {
				SerializableChunk sc = it.next();

				if (sc == null) {
					continue;
				}

				if (sc.getWorldId() == null || !worlds.contains(sc.getWorldId())) {
					World world = Bukkit.getWorld(sc.getWorldId());

					if (world != null) {
						PersistentChunkTicket.removePersistent(Homestead.getInstance(), world, sc.getX(), sc.getZ());
					}

					it.remove();
					count++;
				}
			}
		}
		return count;
	}

	public static void reregisterForceLoadedChunks() {
		for (Region region : RegionManager.getAll()) {
			for (SerializableChunk chunk : region.getChunks()) {
				if (chunk.isForceLoaded()) {
					World world = chunk.getWorld();
					if (world != null) {
						PersistentChunkTicket.addPersistent(Homestead.getInstance(), world, chunk.getX(), chunk.getZ());
					}
				}
			}
		}
	}

	/**
	 * Clean up force loaded chunks that are not claimed by any region.
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

	public enum Error {
		REGION_NOT_FOUND,
		CHUNK_IN_DISABLED_WORLD,
		CHUNK_NOT_ADJACENT_TO_REGION,
		CHUNK_WOULD_SPLIT_REGION
	}
}
