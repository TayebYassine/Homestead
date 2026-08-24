package me.tayebyassine.homestead.borders.particles;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.models.serialize.SeBlock;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles particle spawning around a selected cuboid area for a specific player.
 * <p>
 * Visualizes a selection by spawning dust particles along all
 * twelve edges of the cuboid defined by two corner blocks.
 * </p>
 * <p>
 * Task auto-cancels after 1 minute to prevent resource leaks from abandoned selections.
 * </p>
 *
 * @see ChunkParticlesSpawner
 * @see SeBlock
 * @see TaskHandle
 */
public final class SelectedAreaParticlesSpawner {

    private static final Map<UUID, SelectedAreaParticlesSpawner> ACTIVE_SPAWNERS = new ConcurrentHashMap<>();

    private final Player player;
    private TaskHandle task;
    private volatile SeBlock firstBlock;
    private volatile SeBlock secondBlock;

    /**
     * Creates or updates the particle spawner for the given player using raw
     * {@link Block} corners.
     *
     * @param player      the player to show the selection border for
     * @param firstBlock  one corner of the selected area
     * @param secondBlock the opposite corner of the selected area
     * @throws IllegalArgumentException if player is null
     */
    public SelectedAreaParticlesSpawner(Player player, Block firstBlock, Block secondBlock) {
        this(player, new SeBlock(firstBlock), new SeBlock(secondBlock));
    }

    /**
     * Creates or updates the particle spawner for the given player using
     * {@link SeBlock} corners (serializable block positions).
     *
     * @param player      the player to show the selection border for
     * @param firstBlock  one corner of the selected area
     * @param secondBlock the opposite corner of the selected area
     * @throws IllegalArgumentException if player is null
     */
    public SelectedAreaParticlesSpawner(Player player, SeBlock firstBlock, SeBlock secondBlock) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        this.player = player;
        this.firstBlock = firstBlock;
        this.secondBlock = secondBlock;

        if (Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled()) {
            SelectedAreaParticlesSpawner existing = ACTIVE_SPAWNERS.get(player.getUniqueId());

            if (existing != null) {
                existing.firstBlock = firstBlock;
                existing.secondBlock = secondBlock;
            } else {
                ACTIVE_SPAWNERS.put(player.getUniqueId(), this);
                startRepeatingEffect();
            }
        }
    }

    /**
     * Cancels the particle task for the given player.
     *
     * @param player the player whose task to cancel
     */
    public static void cancelTask(Player player) {
        if (player == null) return;
        SelectedAreaParticlesSpawner spawner = ACTIVE_SPAWNERS.remove(player.getUniqueId());
        if (spawner != null && spawner.task != null) {
            spawner.task.cancel();
        }
    }

    /**
     * Checks if a particle task is currently running for the given player.
     *
     * @param player the player to check
     * @return true if a task is running
     */
    public static boolean isTaskRunning(Player player) {
        return player != null && ACTIVE_SPAWNERS.containsKey(player.getUniqueId());
    }

    /**
     * Gets the current spawner instance for a player, if any.
     *
     * @param player the player
     * @return the spawner, or null if none
     */
    public static SelectedAreaParticlesSpawner getSpawner(Player player) {
        return player != null ? ACTIVE_SPAWNERS.get(player.getUniqueId()) : null;
    }

    /**
     * Updates the selection corners for an existing spawner.
     * <p>
     * If no spawner exists for the player, this does nothing.
     * </p>
     *
     * @param player      the player
     * @param firstBlock  new first corner
     * @param secondBlock new second corner
     */
    public static void updateSelection(Player player, SeBlock firstBlock, SeBlock secondBlock) {
        SelectedAreaParticlesSpawner spawner = getSpawner(player);
        if (spawner != null) {
            spawner.firstBlock = firstBlock;
            spawner.secondBlock = secondBlock;
        }
    }

    /**
     * Spawns dust particles along all twelve edges of the selected cuboid.
     * <p>
     * Particles are spawned at block centers (offset by 0.5) for visual alignment.
     * </p>
     */
    public void spawnParticles() {
        SeBlock fb = this.firstBlock;
        SeBlock sb = this.secondBlock;

        if (fb == null || sb == null) return;
        if (!fb.getWorld().equals(sb.getWorld())) return;

        int minX = Math.min(fb.getX(), sb.getX());
        int minY = Math.min(fb.getY(), sb.getY());
        int minZ = Math.min(fb.getZ(), sb.getZ());
        int maxX = Math.max(fb.getX(), sb.getX());
        int maxY = Math.max(fb.getY(), sb.getY());
        int maxZ = Math.max(fb.getZ(), sb.getZ());

        DustOptions dustOptions = new DustOptions(
                Resources.<RegionsFile>get(ResourceType.Regions).getDustColor(RegionsFile.DustColorType.SUB_AREA),
                Resources.<RegionsFile>get(ResourceType.Regions).getDustSize()
        );

        // Bottom and top edges along X (4 edges)
        for (int x = minX; x <= maxX; x++) {
            spawnDustParticle(x, minY, minZ, dustOptions);
            spawnDustParticle(x, minY, maxZ, dustOptions);
            spawnDustParticle(x, maxY, minZ, dustOptions);
            spawnDustParticle(x, maxY, maxZ, dustOptions);
        }

        // Vertical edges along Y (4 edges)
        for (int y = minY; y <= maxY; y++) {
            spawnDustParticle(minX, y, minZ, dustOptions);
            spawnDustParticle(minX, y, maxZ, dustOptions);
            spawnDustParticle(maxX, y, minZ, dustOptions);
            spawnDustParticle(maxX, y, maxZ, dustOptions);
        }

        // Bottom and top edges along Z (4 edges)
        for (int z = minZ; z <= maxZ; z++) {
            spawnDustParticle(minX, minY, z, dustOptions);
            spawnDustParticle(maxX, minY, z, dustOptions);
            spawnDustParticle(minX, maxY, z, dustOptions);
            spawnDustParticle(maxX, maxY, z, dustOptions);
        }
    }

    /**
     * Spawns a single dust particle at the given block coordinates.
     *
     * @param x           block X
     * @param y           block Y
     * @param z           block Z
     * @param dustOptions particle appearance
     */
    private void spawnDustParticle(int x, int y, int z, DustOptions dustOptions) {
        Location location = new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5);
        player.spawnParticle(Particle.DUST, location, 1, dustOptions);
    }

    /**
     * Starts the repeating particle task with auto-timeout.
     * <p>
     * Task runs every 20 ticks (1 second) and auto-cancels after 1 minute.
     * </p>
     */
    private void startRepeatingEffect() {
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
            ACTIVE_SPAWNERS.remove(player.getUniqueId());
            return;
        }

        instance.runAsyncTaskLater(() -> cancelTask(player), 60); // 1 minute timeout
    }
}