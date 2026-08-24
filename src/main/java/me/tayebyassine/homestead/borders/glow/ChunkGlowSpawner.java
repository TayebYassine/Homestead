package me.tayebyassine.homestead.borders.glow;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.borders.ChunkBorder;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionChunk;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spawns glowing border particles around claimed region chunks for a specific player.
 * <p>
 * This class manages the visual representation of region borders using {@link BlockDisplay}
 * entities (iron chains for edges, copper grates for corners). It efficiently computes
 * exterior edges and corners of claimed chunk regions and renders them as glowing blocks.
 * <p>
 * The corner detection algorithm counts connected edges at each chunk boundary intersection.
 * A corner is rendered only if it has 2 or more connected edges, which correctly handles
 * complex region shapes including squares, L-shapes, T-junctions, and other configurations.
 * <p>
 * Each player can have at most one active glowing task. The task auto-cancels
 * after a configurable timeout (default 3 minutes) to prevent resource leaks.
 *
 * @see SelectedAreaGlowSpawner
 * @see ChunkBorder
 * @see RegionManager
 * @see ChunkManager
 */
public final class ChunkGlowSpawner {

    private static final Map<UUID, TaskHandle> ACTIVE_TASKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<Entity>> PLAYER_ENTITIES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, Long> LAST_REGION_HASH = new ConcurrentHashMap<>();

    private static final boolean IS_FOLIA = Homestead.isFolia();

    private static final String ENTITY_TAG = "homestead_border_glow";
    private static final BlockData CHAIN_X;
    private static final BlockData CHAIN_Z;
    private static final BlockData CORNER_BLOCK;

    static {
        CHAIN_X = Bukkit.createBlockData("minecraft:iron_chain[axis=x]");
        CHAIN_Z = Bukkit.createBlockData("minecraft:iron_chain[axis=z]");
        CORNER_BLOCK = Bukkit.createBlockData("minecraft:copper_grate");
    }

    private final Player player;

    /**
     * Creates a new chunk glow spawner for the given player.
     *
     * @param player the player to show borders for; must not be null
     * @throws IllegalArgumentException if player is null
     */
    public ChunkGlowSpawner(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        this.player = player;

        if (Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled() && !ACTIVE_TASKS.containsKey(player.getUniqueId())) {
            startRepeatingEffect();
        }
    }

    /**
     * Cancels the glow task and removes all spawned entities for the given player.
     * Safe to call multiple times; no-op if no task is running.
     *
     * @param player the player whose border glow should be cancelled; null-safe
     */
    public static void cancelTask(Player player) {
        if (player == null) return;
        TaskHandle task = ACTIVE_TASKS.remove(player.getUniqueId());
        if (task != null) task.cancel();
        removePlayerEntities(player.getUniqueId());
        LAST_REGION_HASH.remove(player.getUniqueId());
    }

    /**
     * Checks if a glow task is currently running for the given player.
     *
     * @param player the player to check; null-safe
     * @return true if a border glow task is active for this player
     */
    public static boolean isTaskRunning(Player player) {
        return player != null && ACTIVE_TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Gets the active task handle for the given player, if any.
     *
     * @param player the player to get the task for; null-safe
     * @return the task handle, or null if no task is running
     */
    public static TaskHandle getTask(Player player) {
        return player != null ? ACTIVE_TASKS.get(player.getUniqueId()) : null;
    }

    /**
     * Creates a unique 64-bit key from chunk coordinates.
     * X in upper 32 bits, Z in lower 32 bits.
     *
     * @param x chunk X coordinate
     * @param z chunk Z coordinate
     * @return combined chunk key
     */
    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * Maps a RGB color to the closest Minecraft ChatColor.
     * <p>
     * Uses simple threshold-based matching for primary/secondary colors
     * and common variants. Defaults to WHITE for unmatched colors.
     *
     * @param color the RGB color to map
     * @return the closest ChatColor
     */
    private static org.bukkit.ChatColor getClosestChatColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        if (r > 200 && g < 100 && b < 100) return org.bukkit.ChatColor.RED;
        if (r < 100 && g > 200 && b < 100) return org.bukkit.ChatColor.GREEN;
        if (r < 100 && g < 100 && b > 200) return org.bukkit.ChatColor.BLUE;
        if (r > 200 && g > 200 && b < 100) return org.bukkit.ChatColor.YELLOW;
        if (r > 200 && g < 100 && b > 200) return org.bukkit.ChatColor.LIGHT_PURPLE;
        if (r < 100 && g > 200 && b > 200) return org.bukkit.ChatColor.AQUA;
        if (r > 200 && g > 100 && b < 100) return org.bukkit.ChatColor.GOLD;
        if (r > 200 && g > 200 && b > 200) return org.bukkit.ChatColor.WHITE;
        if (r < 100 && g < 100 && b < 100) return org.bukkit.ChatColor.BLACK;

        return org.bukkit.ChatColor.WHITE;
    }

    /**
     * Removes all border entities for a player.
     * <p>
     * Runs on the main thread (or region scheduler for Folia) to safely
     * remove entities. Only removes entities that are still valid.
     *
     * @param playerId the player UUID whose entities to remove
     */
    private static void removePlayerEntities(UUID playerId) {
        Set<Entity> entities = PLAYER_ENTITIES.remove(playerId);
        if (entities != null && !entities.isEmpty()) {
            Runnable removeTask = () -> {
                for (Entity e : entities) {
                    if (e != null && e.isValid()) {
                        e.remove();
                    }
                }
            };

            if (Homestead.isFolia()) {
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getUniqueId().equals(playerId))
                        .findFirst()
                        .ifPresent(p -> p.getScheduler().run(Homestead.getInstance(), t -> removeTask.run(), null));
            } else {
                Bukkit.getScheduler().runTask(Homestead.getInstance(), removeTask);
            }
        }
    }

    /**
     * Spawns or refreshes the glowing border for all regions in the player's world.
     * <p>
     * Runs asynchronously. Computes a hash of all regions in the player's world
     * and only re-spawns entities if the region layout has changed. Otherwise,
     * just refreshes the glowing effect on existing entities.
     */
    public void spawnGlow() {
        Homestead.getInstance().runAsyncTask(() -> {
            List<MarkerData> markersToSpawn = new ArrayList<>();

            long regionHash = computeRegionHash();
            Long lastHash = LAST_REGION_HASH.get(player.getUniqueId());

            if (lastHash == null || lastHash != regionHash) {
                LAST_REGION_HASH.put(player.getUniqueId(), regionHash);

                for (Region region : RegionManager.getAll()) {
                    if (!isRegionInPlayerWorld(region)) continue;
                    collectMarkersForRegion(region, markersToSpawn);
                }
            } else {
                refreshExistingEntities();
                return;
            }

            if (!markersToSpawn.isEmpty()) {
                scheduleMarkerSpawn(markersToSpawn);
            }
        });
    }

    /**
     * Computes a hash of all regions in the player's world for change detection.
     * <p>
     * The hash combines region UUIDs and world flags using XOR and prime multipliers.
     * Used to avoid re-computing and re-spawning borders when nothing has changed.
     *
     * @return a hash value representing the current region layout
     */
    private long computeRegionHash() {
        long hash = 0;
        for (Region region : RegionManager.getAll()) {
            if (isRegionInPlayerWorld(region)) {
                hash ^= region.getUniqueId() * 31L + region.getWorldFlags() * 17L;
            }
        }
        return hash;
    }

    /**
     * Refreshes the glowing effect on all existing border entities for this player.
     * <p>
     * Runs on the main thread (or region scheduler for Folia) to safely modify entities.
     * Only updates entities that are still valid.
     */
    private void refreshExistingEntities() {
        Set<Entity> entities = PLAYER_ENTITIES.get(player.getUniqueId());
        if (entities == null) return;

        Runnable refreshTask = () -> {
            for (Entity entity : entities) {
                if (entity != null && entity.isValid()) {
                    entity.setGlowing(true);
                }
            }
        };

        if (IS_FOLIA) {
            player.getScheduler().run(Homestead.getInstance(), t -> refreshTask.run(), null);
        } else {
            Bukkit.getScheduler().runTask(Homestead.getInstance(), refreshTask);
        }
    }

    /**
     * Checks if a region has any chunks in the player's world.
     *
     * @param region the region to check
     * @return true if the region has chunks in the player's world
     */
    private boolean isRegionInPlayerWorld(Region region) {
        List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(region);
        if (chunks.isEmpty()) return false;
        return player.getWorld().getUID().equals(chunks.getFirst().getWorldId());
    }

    /**
     * Collects all border markers (edges and corners) for a single region.
     * <p>
     * Computes exterior edges by checking each chunk's four neighbors.
     * For Folia, uses async chunk loading to check neighbor ownership.
     * Then computes corners based on connected edges (2+ connections = corner).
     *
     * @param region  the region to collect markers for
     * @param markers list to add computed markers to
     */
    private void collectMarkersForRegion(Region region, List<MarkerData> markers) {
        region = RegionManager.findRegion(region.getUniqueId());
        if (region == null) return;

        List<RegionChunk> chunks = ChunkManager.getChunksOfRegion(region);
        World world = player.getWorld();
        Color regionColor = resolveGlowColor(region);

        int yOffset = (int) Math.floor(player.getLocation().getY()) + 1;

        Set<Long> regionChunkKeys = new HashSet<>();
        for (RegionChunk rc : chunks) {
            regionChunkKeys.add(chunkKey(rc.getX(), rc.getZ()));
        }

        Set<Edge> exteriorEdges = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (RegionChunk chunk : chunks) {
            if (!world.getUID().equals(chunk.getWorldId())) continue;

            int cx = chunk.getX();
            int cz = chunk.getZ();
            int minX = cx * 16;
            int minZ = cz * 16;

            checkAndCollectEdge(world, region, cx, cz - 1, minX, minZ, Direction.NORTH, regionChunkKeys, exteriorEdges, futures);
            checkAndCollectEdge(world, region, cx, cz + 1, minX, minZ + 16, Direction.SOUTH, regionChunkKeys, exteriorEdges, futures);
            checkAndCollectEdge(world, region, cx - 1, cz, minX, minZ, Direction.WEST, regionChunkKeys, exteriorEdges, futures);
            checkAndCollectEdge(world, region, cx + 1, cz, minX + 16, minZ, Direction.EAST, regionChunkKeys, exteriorEdges, futures);
        }

        if (IS_FOLIA) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        Set<Corner> corners = computeCorners(regionChunkKeys, exteriorEdges);

        for (Edge edge : exteriorEdges) {
            addEdgeChains(edge, yOffset, regionColor, markers);
        }
        for (Corner corner : corners) {
            markers.add(new MarkerData(corner.x, yOffset, corner.z, regionColor, CORNER_BLOCK));
        }
    }

    /**
     * Computes corner positions from exterior edges using connection counting.
     * <p>
     * Each edge contributes to two corners (its endpoints). A corner is rendered
     * only if it has 2 or more connected edges. This correctly handles all region
     * shapes: squares (4 corners, 2 edges each), L-shapes (inner corner has 2),
     * T-junctions (3 edges), and complex polygons.
     *
     * @param regionChunkKeys set of claimed chunk keys (unused, kept for API compatibility)
     * @param exteriorEdges   set of exterior edges (chains) bounding the region
     * @return set of corner positions to render
     */
    private Set<Corner> computeCorners(Set<Long> regionChunkKeys, Set<Edge> exteriorEdges) {
        Map<CornerKey, Integer> cornerConnectionCount = new HashMap<>();

        for (Edge edge : exteriorEdges) {
            int minX = edge.minX;
            int minZ = edge.minZ;

            if (edge.direction == Direction.NORTH || edge.direction == Direction.SOUTH) {
                CornerKey c1 = new CornerKey(minX, minZ);
                CornerKey c2 = new CornerKey(minX + 16, minZ);
                cornerConnectionCount.merge(c1, 1, Integer::sum);
                cornerConnectionCount.merge(c2, 1, Integer::sum);
            } else {
                CornerKey c1 = new CornerKey(minX, minZ);
                CornerKey c2 = new CornerKey(minX, minZ + 16);
                cornerConnectionCount.merge(c1, 1, Integer::sum);
                cornerConnectionCount.merge(c2, 1, Integer::sum);
            }
        }

        Set<Corner> corners = new HashSet<>();
        for (Map.Entry<CornerKey, Integer> entry : cornerConnectionCount.entrySet()) {
            if (entry.getValue() >= 2) {
                corners.add(new Corner(entry.getKey().x, entry.getKey().z));
            }
        }
        return corners;
    }

    /**
     * Checks a neighboring chunk and collects the edge if it's an exterior boundary.
     * <p>
     * If the neighbor chunk is not claimed by this region, the shared edge is
     * an exterior border. On Folia, uses async chunk loading; otherwise checks
     * synchronously if the chunk is loaded.
     *
     * @param world           the world
     * @param region          the region being checked
     * @param neighborChunkX  neighbor chunk X coordinate
     * @param neighborChunkZ  neighbor chunk Z coordinate
     * @param minX            world X coordinate of the edge start
     * @param minZ            world Z coordinate of the edge start
     * @param direction       direction of the edge (relative to current chunk)
     * @param regionChunkKeys set of this region's chunk keys
     * @param exteriorEdges   set to add exterior edges to
     * @param futures         list of async futures (for Folia)
     */
    private void checkAndCollectEdge(
            World world, Region region,
            int neighborChunkX, int neighborChunkZ,
            int minX, int minZ, Direction direction,
            Set<Long> regionChunkKeys,
            Set<Edge> exteriorEdges,
            List<CompletableFuture<Void>> futures
    ) {
        long neighborKey = chunkKey(neighborChunkX, neighborChunkZ);
        if (regionChunkKeys.contains(neighborKey)) return;

        Edge edge = new Edge(minX, minZ, direction);
        if (exteriorEdges.contains(edge)) return;

        if (IS_FOLIA) {
            final Region finalRegion = region;
            final Direction finalDir = direction;
            final int finalMinX = minX;
            final int finalMinZ = minZ;
            long finalNeighborKey = neighborKey;

            CompletableFuture<Void> future = world.getChunkAtAsync(neighborChunkX, neighborChunkZ, false)
                    .thenAccept(neighbor -> {
                        if (neighbor == null) return;
                        if (ChunkManager.isChunkClaimedByRegion(finalRegion, neighbor)) return;
                        exteriorEdges.add(new Edge(finalMinX, finalMinZ, finalDir));
                    });
            futures.add(future);
        } else {
            if (!world.isChunkLoaded(neighborChunkX, neighborChunkZ)) return;

            Chunk neighbor = world.getChunkAt(neighborChunkX, neighborChunkZ);
            if (ChunkManager.isChunkClaimedByRegion(region, neighbor)) return;

            exteriorEdges.add(edge);
        }
    }

    /**
     * Adds chain markers along an edge to the marker list.
     * <p>
     * Places iron chain block displays at 1-block intervals along the edge,
     * skipping the corner positions (which are handled separately).
     *
     * @param edge    the edge to add chains for
     * @param yOffset Y coordinate for the chains
     * @param color   glow color for the region
     * @param markers list to add chain markers to
     */
    private void addEdgeChains(Edge edge, int yOffset, Color color, List<MarkerData> markers) {
        if (edge.direction == Direction.NORTH || edge.direction == Direction.SOUTH) {
            int z = edge.minZ;
            for (int x = edge.minX + 1; x < edge.minX + 16; x++) {
                markers.add(new MarkerData(x, yOffset, z, color, CHAIN_X));
            }
        } else {
            int x = edge.minX;
            for (int z = edge.minZ + 1; z < edge.minZ + 16; z++) {
                markers.add(new MarkerData(x, yOffset, z, color, CHAIN_Z));
            }
        }
    }

    /**
     * Schedules spawning of all border markers on the main thread.
     * <p>
     * First removes any existing entities for this player, then spawns
     * new BlockDisplay entities for each marker. Runs on main thread
     * (or region scheduler for Folia) for thread-safe entity creation.
     *
     * @param markers list of markers to spawn
     */
    private void scheduleMarkerSpawn(List<MarkerData> markers) {
        World world = player.getWorld();

        removePlayerEntities(player.getUniqueId());

        Runnable spawnTask = () -> {
            for (MarkerData data : markers) {
                spawnMarker(world, data.x, data.y, data.z, data.color, data.blockData);
            }
        };

        if (IS_FOLIA) {
            player.getScheduler().run(Homestead.getInstance(), t -> spawnTask.run(), null);
        } else {
            Bukkit.getScheduler().runTask(Homestead.getInstance(), spawnTask);
        }
    }

    /**
     * Resolves the glow color for a region based on the player's relationship.
     *
     * @param region the region to get color for
     * @return owner color if player owns region, member color if member, visitor color otherwise
     */
    private Color resolveGlowColor(Region region) {
        RegionsFile config = Resources.get(ResourceType.Regions);

        if (region != null) {
            if (region.isOwner(player)) {
                return config.getDustColor(RegionsFile.DustColorType.OWNER);
            } else if (MemberManager.isMemberOfRegion(region, player)) {
                return config.getDustColor(RegionsFile.DustColorType.MEMBER);
            }
        }
        return config.getDustColor(RegionsFile.DustColorType.VISITOR);
    }

    /**
     * Spawns a single glowing block display marker at the given position.
     * <p>
     * Creates an invisible, invulnerable, non-gravity BlockDisplay with the
     * specified block data and glowing effect. Tags it for later cleanup and
     * applies the glow color via scoreboard team.
     *
     * @param world     the world to spawn in
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param z         Z coordinate
     * @param color     glow color
     * @param blockData block data to display (chain or corner)
     */
    private void spawnMarker(World world, double x, double y, double z, Color color, BlockData blockData) {
        Location loc = new Location(world, x, y, z);
        BlockDisplay display = world.spawn(loc, BlockDisplay.class, entity -> {
            BlockDisplay bd = entity;
            bd.setBlock(blockData);
            bd.setInvisible(true);
            bd.setInvulnerable(true);
            bd.setGravity(false);
            bd.setSilent(true);
            bd.setGlowing(true);

            bd.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(Homestead.getInstance(), ENTITY_TAG),
                    PersistentDataType.BYTE, (byte) 1
            );

            applyGlowColor(bd, color);
        });

        PLAYER_ENTITIES.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                .add(display);
    }

    /**
     * Applies a glow color to an entity using scoreboard teams.
     * <p>
     * Creates or reuses a team named by the color's RGB value, sets the
     * team's chat color to the closest Minecraft chat color, and adds
     * the entity to the team. This makes the entity glow in that color.
     *
     * @param entity the entity to apply glow to
     * @param color  the color to glow
     */
    private void applyGlowColor(Entity entity, Color color) {
        org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "homestead_glow_" + color.asRGB();

        org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setColor(getClosestChatColor(color));
            team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }

        team.addEntity(entity);
    }

    /**
     * Starts the repeating border glow effect task.
     * <p>
     * Schedules {@link #spawnGlow()} to run every 20 ticks (1 second).
     * On Folia, uses the player's region scheduler; otherwise uses Bukkit scheduler.
     * Registers the task in {@link #ACTIVE_TASKS} and schedules auto-cancellation
     * after 3 minutes (180 seconds) of inactivity.
     */
    private void startRepeatingEffect() {
        Homestead instance = Homestead.getInstance();
        TaskHandle task;

        if (IS_FOLIA) {
            var foliaTask = player.getScheduler().runAtFixedRate(
                    instance,
                    t -> spawnGlow(),
                    () -> cancelTask(player),
                    1L,
                    20L
            );
            task = foliaTask != null ? new TaskHandle(foliaTask) : null;
        } else {
            task = new TaskHandle(
                    Bukkit.getScheduler().runTaskTimer(instance, this::spawnGlow, 0L, 20L)
            );
        }

        if (task == null) return;

        ACTIVE_TASKS.put(player.getUniqueId(), task);
        instance.runAsyncTaskLater(() -> cancelTask(player), 60 * 3);
    }

    /**
     * Cardinal directions for border edges.
     */
    private enum Direction {NORTH, SOUTH, EAST, WEST}

    /**
     * Key representing a corner position in world coordinates.
     */
    private record CornerKey(int x, int z) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CornerKey(int x1, int z1))) return false;
            return x == x1 && z == z1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    /**
     * Represents an exterior edge (border chain) of a region.
     * Edges are defined by their minimum world coordinates and direction.
     * Used for deduplication via equals/hashCode.
     */
    private record Edge(int minX, int minZ, Direction direction) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge(int x, int z, Direction direction1))) return false;
            return minX == x && minZ == z && direction == direction1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(minX, minZ, direction);
        }
    }

    /**
     * Represents a corner position in world coordinates.
     * Rendered as a copper grate block display.
     */
    private record Corner(int x, int z) {
    }

    /**
     * Data for a single border marker to spawn.
     * Contains position, color, and block data (chain or corner).
     */
    private record MarkerData(double x, double y, double z, Color color, BlockData blockData) {
    }
}
