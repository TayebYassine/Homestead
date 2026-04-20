package tfagaming.projects.minecraft.homestead.borders;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.minecraft.threads.TaskHandle;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles particle spawning around claimed region chunks for a specific player.
 */
public class ChunkParticlesSpawner {

	private static final Map<UUID, TaskHandle> tasks = new ConcurrentHashMap<>();
	private final Player player;

	/**
	 * Creates a new {@code ChunkParticlesSpawner} for the given player.
	 *
	 * @param player The player to show region borders for
	 */
	public ChunkParticlesSpawner(Player player) {
		this.player = player;

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled();

		if (!isEnabled) {
			return;
		}

		if (!tasks.containsKey(player.getUniqueId())) {
			startRepeatingEffect();
		}
	}

	/**
	 * Cancels the given task and removes it from the tracking map.
	 *
	 * @param task The task handle to cancel
	 * @param player The player associated with the task
	 */
	public static void cancelTask(TaskHandle task, Player player) {
		if (task != null) {
			tasks.remove(player.getUniqueId());
			task.cancel();
		}
	}

	/**
	 * Cancels the active particle task for the given player, if any.
	 *
	 * @param player The player whose task should be cancelled
	 */
	public static void cancelTask(Player player) {
		TaskHandle task = tasks.remove(player.getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}

	/**
	 * Returns {@code true} if a particle task is currently running for the
	 * given player, otherwise {@code false}.
	 *
	 * @param player The player to check
	 */
	public static boolean isTaskRunning(Player player) {
		return tasks.containsKey(player.getUniqueId());
	}

	/**
	 * Re-fetches all regions from {@link RegionManager} and spawns border
	 * particles for each one.
	 */
	public void spawnParticles() {
		for (Region region : RegionManager.getAll()) {
			spawnParticlesForRegion(region);
		}
	}

	/**
	 * Spawns border particles around the chunk boundaries of the given region.
	 *
	 * @param region The region whose chunk borders should be visualised
	 */
	public void spawnParticlesForRegion(Region region) {
		region = RegionManager.findRegion(region.getUniqueId());
		if (region == null) {
			return;
		}

		List<SerializableChunk> chunks = region.getChunks();
		World world = player.getWorld();
		double yOffset = player.getLocation().getY() + 1;

		DustOptions dustOptions;
		if (region.isOwner(player)) {
			dustOptions = new DustOptions(Resources.<RegionsFile>get(ResourceType.Regions).getDustColor(RegionsFile.DustColorType.OWNER), Resources.<RegionsFile>get(ResourceType.Regions).getDustSize());
		} else if (region.isPlayerMember(player)) {
			dustOptions = new DustOptions(Resources.<RegionsFile>get(ResourceType.Regions).getDustColor(RegionsFile.DustColorType.MEMBER), Resources.<RegionsFile>get(ResourceType.Regions).getDustSize());
		} else {
			dustOptions = new DustOptions(Resources.<RegionsFile>get(ResourceType.Regions).getDustColor(RegionsFile.DustColorType.VISITOR), Resources.<RegionsFile>get(ResourceType.Regions).getDustSize());
		}

		for (SerializableChunk chunk : chunks) {
			if (!world.getUID().equals(chunk.getWorldId())) {
				continue;
			}

			int chunkX = chunk.getX();
			int chunkZ = chunk.getZ();

			if (!world.isChunkLoaded(chunkX, chunkZ)) {
				continue;
			}

			int minX = chunkX * 16;
			int minZ = chunkZ * 16;

			checkAndSpawn(world, region, chunkX, chunkZ - 1, minX, minZ, yOffset, dustOptions, Direction.NORTH);
			checkAndSpawn(world, region, chunkX, chunkZ + 1, minX, minZ + 16, yOffset, dustOptions, Direction.SOUTH);
			checkAndSpawn(world, region, chunkX - 1, chunkZ, minX, minZ, yOffset, dustOptions, Direction.WEST);
			checkAndSpawn(world, region, chunkX + 1, chunkZ, minX + 16, minZ, yOffset, dustOptions, Direction.EAST);
		}
	}

	/**
	 * Checks whether the neighbouring chunk is loaded and, if the border
	 * between it and the current region chunk is exposed, spawns particles
	 * along that edge.
	 *
	 * @param world The world instance
	 * @param region The region owning the current chunk
	 * @param chunkX X coordinate of the neighbour chunk
	 * @param chunkZ Z coordinate of the neighbour chunk
	 * @param minX Minimum world-space X of the border edge
	 * @param minZ Minimum world-space Z of the border edge
	 * @param yOffset Y level at which particles appear
	 * @param dustOptions Colour and size of the dust particle
	 * @param direction Which side of the current chunk is being tested
	 */
	private void checkAndSpawn(World world, Region region,
							   int chunkX, int chunkZ,
							   int minX, int minZ,
							   double yOffset, DustOptions dustOptions,
							   Direction direction) {
		if (!world.isChunkLoaded(chunkX, chunkZ)) {
			return;
		}

		Chunk neighbor = world.getChunkAt(chunkX, chunkZ);

		if (ChunkManager.isChunkClaimedByRegion(region, neighbor)) {
			return;
		}

		if (direction == Direction.NORTH || direction == Direction.SOUTH) {
			for (int x = minX; x < minX + 16; x++) {
				player.spawnParticle(Particle.DUST, x, yOffset, minZ, 1, dustOptions);
			}
		} else {
			for (int z = minZ; z < minZ + 16; z++) {
				player.spawnParticle(Particle.DUST, minX, yOffset, z, 1, dustOptions);
			}
		}
	}

	/**
	 * Starts the repeating task that drives particle spawning.
	 */
	public void startRepeatingEffect() {
		Homestead instance = Homestead.getInstance();
		final TaskHandle task;

		if (Homestead.isFolia()) {
			var foliaTask = player.getScheduler().runAtFixedRate(
					instance,
					t -> spawnParticles(),
					() -> cancelTask(player),
					1L,
					20L
			);
			task = foliaTask != null ? new TaskHandle(foliaTask) : null;
		} else {
			task = new TaskHandle(
					Bukkit.getScheduler().runTaskTimer(instance, this::spawnParticles, 0L, 20L)
			);
		}

		if (task == null) {
			return;
		}

		tasks.put(player.getUniqueId(), task);

		instance.runAsyncTaskLater(() -> cancelTask(task, player), 60 * 3);
	}

	private enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
}