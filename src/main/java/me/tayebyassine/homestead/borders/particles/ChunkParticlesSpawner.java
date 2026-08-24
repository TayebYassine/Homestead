package me.tayebyassine.homestead.borders.particles;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionChunk;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles particle spawning around claimed region chunks for a specific player.
 * <p>
 * Creates a visual outline of region borders by spawning dust particles along
 * chunk edges that border unclaimed or other regions. Runs on a repeating task
 * that refreshes particles periodically.
 * </p>
 * <p>
 * Each player can have at most one active particle task. The task auto-cancels
 * after a configurable timeout (default 3 minutes) to prevent resource leaks.
 * </p>
 *
 * @see SelectedAreaParticlesSpawner
 * @see TaskHandle
 */
public final class ChunkParticlesSpawner {

    private static final Map<UUID, TaskHandle> ACTIVE_TASKS = new ConcurrentHashMap<>();
    private static final boolean IS_FOLIA = Homestead.isFolia();

    private final Player player;

    /**
     * Creates a new chunk particles spawner for the given player.
     * <p>
     * If a task is already running for this player, the existing task continues.
     * The task will automatically stop after the configured timeout.
     * </p>
     *
     * @param player the player to show region borders for
     * @throws IllegalArgumentException if player is null
     */
    public ChunkParticlesSpawner(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        this.player = player;

        if (Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled() && !ACTIVE_TASKS.containsKey(player.getUniqueId())) {
            startRepeatingEffect();
        }
    }

    /**
     * Cancels the active particle task for the given player.
     *
     * @param player the player whose task to cancel
     */
    public static void cancelTask(Player player) {
        if (player == null) return;
        TaskHandle task = ACTIVE_TASKS.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    /**
     * Checks if a particle task is currently running for the given player.
     *
     * @param player the player to check
     * @return true if a task is running
     */
    public static boolean isTaskRunning(Player player) {
        return player != null && ACTIVE_TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Gets the current task handle for a player, if any.
     *
     * @param player the player
     * @return the task handle, or null if none
     */
    public static TaskHandle getTask(Player player) {
        return player != null ? ACTIVE_TASKS.get(player.getUniqueId()) : null;
    }

    /**
     * Spawns border particles for all regions in the player's world.
     * <p>
     * Runs asynchronously to avoid blocking the main thread during region iteration.
     * </p>
     */
    public void spawnParticles() {
        Homestead.getInstance().runAsyncTask(() -> {
            for (Region region : RegionManager.getAll()) {
                if (!isRegionInPlayerWorld(region)) {
                    continue;
                }

                spawnParticlesForRegionAsync(region);
            }
        });
    }

    /**
     * Checks if a region has chunks in the player's current world.
     *
     * @param region the region to check
     * @return true if the region exists in the player's world
     */
    private boolean isRegionInPlayerWorld(Region region) {
        List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(region);
        if (chunks.isEmpty()) return false;
        return player.getWorld().getUID().equals(chunks.getFirst().getWorldId());
    }

    /**
     * Spawns particles for a specific region asynchronously.
     *
     * @param region the region to spawn particles for
     */
    private void spawnParticlesForRegionAsync(Region region) {
        region = RegionManager.findRegion(region.getUniqueId());
        if (region == null) return;

        List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(region);
        World world = player.getWorld();
        double yOffset = player.getLocation().getY() + 1;
        DustOptions dustOptions = resolveDustOptions(region);

        for (RegionChunk chunk : chunks) {
            if (!world.getUID().equals(chunk.getWorldId())) continue;

            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();

            if (IS_FOLIA) {
                Region finalRegion = region;
                world.getChunkAtAsync(chunkX, chunkZ, false)
                        .thenAccept(loadedChunk -> {
                            if (loadedChunk == null) return;
                            player.getScheduler().run(
                                    Homestead.getInstance(),
                                    t -> spawnParticlesForChunk(world, finalRegion, chunkX, chunkZ, yOffset, dustOptions),
                                    null
                            );
                        });
            } else {
                if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
                spawnParticlesForChunk(world, region, chunkX, chunkZ, yOffset, dustOptions);
            }
        }
    }

    /**
     * Determines the dust color based on player's relationship to the region.
     *
     * @param region the region
     * @return dust options with appropriate color
     */
    private DustOptions resolveDustOptions(Region region) {
        RegionsFile config = Resources.get(ResourceType.Regions);
        float size = config.getDustSize();

        if (region.isOwner(player)) {
            return new DustOptions(config.getDustColor(RegionsFile.DustColorType.OWNER), size);
        } else if (MemberManager.isMemberOfRegion(region, player)) {
            return new DustOptions(config.getDustColor(RegionsFile.DustColorType.MEMBER), size);
        } else {
            return new DustOptions(config.getDustColor(RegionsFile.DustColorType.VISITOR), size);
        }
    }

    /**
     * Spawns particles for all four edges of a chunk.
     *
     * @param world       the world
     * @param region      the region
     * @param chunkX      chunk X coordinate
     * @param chunkZ      chunk Z coordinate
     * @param yOffset     Y level for particles
     * @param dustOptions particle appearance
     */
    private void spawnParticlesForChunk(
            World world, Region region,
            int chunkX, int chunkZ,
            double yOffset, DustOptions dustOptions
    ) {
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;

        checkAndSpawn(world, region, chunkX, chunkZ - 1, minX, minZ, yOffset, dustOptions, Direction.NORTH);
        checkAndSpawn(world, region, chunkX, chunkZ + 1, minX, minZ + 16, yOffset, dustOptions, Direction.SOUTH);
        checkAndSpawn(world, region, chunkX - 1, chunkZ, minX, minZ, yOffset, dustOptions, Direction.WEST);
        checkAndSpawn(world, region, chunkX + 1, chunkZ, minX + 16, minZ, yOffset, dustOptions, Direction.EAST);
    }

    /**
     * Checks if a neighboring chunk border should have particles and spawns them.
     *
     * @param world          the world
     * @param region         the region owning the current chunk
     * @param neighborChunkX neighbor chunk X
     * @param neighborChunkZ neighbor chunk Z
     * @param minX           minimum world X of the border edge
     * @param minZ           minimum world Z of the border edge
     * @param yOffset        Y level for particles
     * @param dustOptions    particle appearance
     * @param direction      which edge to spawn
     */
    private void checkAndSpawn(
            World world, Region region,
            int neighborChunkX, int neighborChunkZ,
            int minX, int minZ,
            double yOffset, DustOptions dustOptions,
            Direction direction
    ) {
        if (IS_FOLIA) {
            world.getChunkAtAsync(neighborChunkX, neighborChunkZ, false).thenAccept(neighbor -> {
                if (neighbor == null) return;
                if (ChunkManager.isChunkClaimedByRegion(region, neighbor)) return;

                player.getScheduler().run(
                        Homestead.getInstance(),
                        t -> spawnEdgeParticles(minX, minZ, yOffset, dustOptions, direction),
                        null
                );
            });
        } else {
            if (!world.isChunkLoaded(neighborChunkX, neighborChunkZ)) return;

            Chunk neighbor = world.getChunkAt(neighborChunkX, neighborChunkZ);
            if (ChunkManager.isChunkClaimedByRegion(region, neighbor)) return;

            spawnEdgeParticles(minX, minZ, yOffset, dustOptions, direction);
        }
    }

    /**
     * Spawns dust particles along a chunk edge.
     *
     * @param minX        minimum X of the edge
     * @param minZ        minimum Z of the edge
     * @param yOffset     Y level
     * @param dustOptions particle appearance
     * @param direction   edge direction
     */
    private void spawnEdgeParticles(int minX, int minZ, double yOffset, DustOptions dustOptions, Direction direction) {
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            for (int x = minX; x < minX + 16; x++) {
                player.spawnParticle(Particle.DUST, x + 0.5, yOffset, minZ + 0.5, 1, dustOptions);
            }
        } else {
            for (int z = minZ; z < minZ + 16; z++) {
                player.spawnParticle(Particle.DUST, minX + 0.5, yOffset, z + 0.5, 1, dustOptions);
            }
        }
    }

    /**
     * Starts the repeating particle task with auto-timeout.
     * <p>
     * Task runs every 20 ticks (1 second) and auto-cancels after 3 minutes.
     * </p>
     */
    private void startRepeatingEffect() {
        Homestead instance = Homestead.getInstance();
        TaskHandle task;

        if (IS_FOLIA) {
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

        if (task == null) return;

        ACTIVE_TASKS.put(player.getUniqueId(), task);
        instance.runAsyncTaskLater(() -> cancelTask(player), 60 * 3);
    }

    /**
     * Cardinal directions for chunk edges.
     */
    private enum Direction {NORTH, SOUTH, EAST, WEST}
}