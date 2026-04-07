package tfagaming.projects.minecraft.homestead.borders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;
import tfagaming.projects.minecraft.homestead.tools.minecraft.papermc.TaskHandle;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles particle spawning around a selected cuboid area for a specific player.
 * <p>
 * Each player has at most one persistent task running at any time. If a task is
 * already active when the constructor is called, the selection corners are simply
 * updated in-place; the existing task continues without interruption and will use
 * the new coordinates on its very next tick. This avoids the cancel/restart cycle
 * that was previously a source of lag and task-leak bugs.
 * </p>
 * <p>
 * Particle spawning runs on the <b>main thread</b> (or the player's entity thread
 * on Folia) to keep all Bukkit API access thread-safe.
 * </p>
 */
public class SelectedAreaParticlesSpawner {

	private static final Map<UUID, SelectedAreaParticlesSpawner> spawners = new ConcurrentHashMap<>();
	private final Player player;
	private TaskHandle task;
	private volatile SerializableBlock firstBlock;
	private volatile SerializableBlock secondBlock;

	/**
	 * Creates or updates the particle spawner for the given player using raw
	 * {@link Block} corners.
	 *
	 * @param player     the player to show the selection border for
	 * @param firstBlock one corner of the selected area
	 * @param secondBlock the opposite corner of the selected area
	 */
	public SelectedAreaParticlesSpawner(Player player, Block firstBlock, Block secondBlock) {
		this(player, new SerializableBlock(firstBlock), new SerializableBlock(secondBlock));
	}

	/**
	 * Creates or updates the particle spawner for the given player using
	 * {@link SerializableBlock} corners.
	 *
	 * @param player The player to show the selection border for
	 * @param firstBlock One corner of the selected area
	 * @param secondBlock The opposite corner of the selected area
	 */
	public SelectedAreaParticlesSpawner(Player player, SerializableBlock firstBlock, SerializableBlock secondBlock) {
		this.player = player;
		this.firstBlock = firstBlock;
		this.secondBlock = secondBlock;

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled();

		if (!isEnabled) {
			return;
		}

		SelectedAreaParticlesSpawner existing = spawners.get(player.getUniqueId());

		if (existing != null) {
			existing.firstBlock = firstBlock;
			existing.secondBlock = secondBlock;
		} else {
			spawners.put(player.getUniqueId(), this);
			startRepeatingEffect();
		}
	}

	/**
	 * Cancels the given task handle and removes the associated spawner from the
	 * tracking map.
	 *
	 * @param task The task handle to cancel
	 * @param player The player associated with the task
	 */
	public static void cancelTask(TaskHandle task, Player player) {
		if (task != null) {
			spawners.remove(player.getUniqueId());
			task.cancel();
		}
	}

	/**
	 * Cancels the active particle task for the given player, if any.
	 *
	 * @param player The player whose task should be cancelled
	 */
	public static void cancelTask(Player player) {
		SelectedAreaParticlesSpawner spawner = spawners.remove(player.getUniqueId());
		if (spawner != null && spawner.task != null) {
			spawner.task.cancel();
		}
	}

	/**
	 * Returns {@code true} if a particle task is currently running for the given
	 * player, otherwise {@code false}.
	 *
	 * @param player The player to check
	 */
	public static boolean isTaskRunning(Player player) {
		return spawners.containsKey(player.getUniqueId());
	}

	/**
	 * Spawns dust particles along all twelve edges of the selected cuboid.
	 */
	public void spawnParticles() {
		SerializableBlock fb = this.firstBlock;
		SerializableBlock sb = this.secondBlock;

		int minX = Math.min(fb.getX(), sb.getX());
		int minY = Math.min(fb.getY(), sb.getY());
		int minZ = Math.min(fb.getZ(), sb.getZ());
		int maxX = Math.max(fb.getX(), sb.getX());
		int maxY = Math.max(fb.getY(), sb.getY());
		int maxZ = Math.max(fb.getZ(), sb.getZ());

		// Bottom and top edges along X
		for (int x = minX; x <= maxX; x++) {
			spawnDustParticle(x, minY, minZ);
			spawnDustParticle(x, minY, maxZ);
			spawnDustParticle(x, maxY, minZ);
			spawnDustParticle(x, maxY, maxZ);
		}

		// Vertical edges along Y
		for (int y = minY; y <= maxY; y++) {
			spawnDustParticle(minX, y, minZ);
			spawnDustParticle(minX, y, maxZ);
			spawnDustParticle(maxX, y, minZ);
			spawnDustParticle(maxX, y, maxZ);
		}

		// Bottom and top edges along Z
		for (int z = minZ; z <= maxZ; z++) {
			spawnDustParticle(minX, minY, z);
			spawnDustParticle(maxX, minY, z);
			spawnDustParticle(minX, maxY, z);
			spawnDustParticle(maxX, maxY, z);
		}
	}

	private void spawnDustParticle(int x, int y, int z) {
		Location location = new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5);
		DustOptions dustOptions = new DustOptions(Resources.<RegionsFile>get(ResourceType.Regions).getDustColor(RegionsFile.DustColorType.SUB_AREA), Resources.<RegionsFile>get(ResourceType.Regions).getDustSize());
		player.spawnParticle(Particle.DUST, location, 1, dustOptions);
	}

	/**
	 * Starts the repeating task that drives particle spawning.
	 */
	public void startRepeatingEffect() {
		Homestead instance = Homestead.getInstance();

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
			spawners.remove(player.getUniqueId());
			return;
		}

		instance.runAsyncTaskLater(() -> cancelTask(task, player), 60);
	}
}