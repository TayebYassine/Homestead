package me.tayebyassine.homestead.borders.glow;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.borders.ChunkBorder;
import me.tayebyassine.homestead.models.serialize.SeBlock;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spawns glowing border particles around a player-selected cuboid area.
 * <p>
 * This class renders a 3D cuboid wireframe using {@link BlockDisplay} entities
 * (iron chains for edges on X, Y, and Z axes, copper grates for all 8 corners).
 * It's used for visual feedback when players are selecting sub-areas or regions.
 * <p>
 * The cuboid is defined by two corner blocks (first and second selection points).
 * The spawner computes all 12 edges and 8 corners of the axis-aligned bounding box
 * between these points and renders them as glowing blocks.
 * <p>
 * Task auto-cancels after 1 minute to prevent resource leaks from abandoned selections.
 *
 * @see ChunkGlowSpawner
 * @see ChunkBorder
 * @see SeBlock
 */
public final class SelectedAreaGlowSpawner {

    private static final Map<UUID, SelectedAreaGlowSpawner> ACTIVE_SPAWNERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<Entity>> PLAYER_ENTITIES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, String> LAST_SELECTION_HASH = new ConcurrentHashMap<>();

    private static final boolean IS_FOLIA = Homestead.isFolia();

    private static final String ENTITY_TAG = "homestead_border_glow";
    private static final BlockData CHAIN_X;
    private static final BlockData CHAIN_Y;
    private static final BlockData CHAIN_Z;
    private static final BlockData CORNER_BLOCK;

    static {
        CHAIN_X = Bukkit.createBlockData("minecraft:iron_chain[axis=x]");
        CHAIN_Y = Bukkit.createBlockData("minecraft:iron_chain[axis=y]");
        CHAIN_Z = Bukkit.createBlockData("minecraft:iron_chain[axis=z]");
        CORNER_BLOCK = Bukkit.createBlockData("minecraft:copper_grate");
    }

    private final Player player;
    private TaskHandle task;
    private volatile SeBlock firstBlock;
    private volatile SeBlock secondBlock;

    /**
     * Creates a new selection area glow spawner using Bukkit blocks.
     *
     * @param player      the player to show borders for; must not be null
     * @param firstBlock  first corner block of the selection
     * @param secondBlock second corner block of the selection
     * @throws IllegalArgumentException if player is null
     */
    public SelectedAreaGlowSpawner(Player player, org.bukkit.block.Block firstBlock, org.bukkit.block.Block secondBlock) {
        this(player, new SeBlock(firstBlock), new SeBlock(secondBlock));
    }

    /**
     * Creates a new selection area glow spawner.
     *
     * @param player      the player to show borders for; must not be null
     * @param firstBlock  first corner of the selection cuboid
     * @param secondBlock second corner of the selection cuboid
     * @throws IllegalArgumentException if player is null
     */
    public SelectedAreaGlowSpawner(Player player, SeBlock firstBlock, SeBlock secondBlock) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        this.player = player;
        this.firstBlock = firstBlock;
        this.secondBlock = secondBlock;

        if (Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled()) {
            SelectedAreaGlowSpawner existing = ACTIVE_SPAWNERS.get(player.getUniqueId());

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
     * Cancels the glow task and removes all spawned entities for the given player.
     *
     * @param player the player whose border glow should be cancelled; null-safe
     */
    public static void cancelTask(Player player) {
        if (player == null) return;
        SelectedAreaGlowSpawner spawner = ACTIVE_SPAWNERS.remove(player.getUniqueId());
        if (spawner != null && spawner.task != null) {
            spawner.task.cancel();
        }
        removePlayerEntities(player.getUniqueId());
        LAST_SELECTION_HASH.remove(player.getUniqueId());
    }

    /**
     * Checks if a glow task is currently running for the given player.
     *
     * @param player the player to check; null-safe
     * @return true if a border glow task is active for this player
     */
    public static boolean isTaskRunning(Player player) {
        return player != null && ACTIVE_SPAWNERS.containsKey(player.getUniqueId());
    }

    /**
     * Gets the active spawner for the given player, if any.
     *
     * @param player the player to get the spawner for; null-safe
     * @return the spawner instance, or null if no task is running
     */
    public static SelectedAreaGlowSpawner getSpawner(Player player) {
        return player != null ? ACTIVE_SPAWNERS.get(player.getUniqueId()) : null;
    }

    /**
     * Updates the selection bounds for an existing spawner.
     * <p>
     * If a spawner is running for the player, updates its corner blocks.
     * The new selection will be rendered on the next task iteration.
     *
     * @param player      the player whose selection to update
     * @param firstBlock  new first corner
     * @param secondBlock new second corner
     */
    public static void updateSelection(Player player, SeBlock firstBlock, SeBlock secondBlock) {
        SelectedAreaGlowSpawner spawner = getSpawner(player);
        if (spawner != null) {
            spawner.firstBlock = firstBlock;
            spawner.secondBlock = secondBlock;
        }
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

            if (IS_FOLIA) {
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
     * Spawns or refreshes the glowing cuboid border for the current selection.
     * <p>
     * Computes a hash of the two corner blocks and only re-spawns entities if
     * the selection has changed. Otherwise, just refreshes the glowing effect
     * on existing entities. Runs asynchronously for region/hash computation,
     * then schedules entity spawning on the main thread.
     */
    public void spawnGlow() {
        SeBlock fb = this.firstBlock;
        SeBlock sb = this.secondBlock;

        if (fb == null || sb == null) return;
        if (!fb.getWorld().equals(sb.getWorld())) return;

        String selectionHash = fb.getX() + "," + fb.getY() + "," + fb.getZ() + "|" +
                sb.getX() + "," + sb.getY() + "," + sb.getZ();
        String lastHash = LAST_SELECTION_HASH.get(player.getUniqueId());

        if (lastHash != null && lastHash.equals(selectionHash)) {
            refreshExistingEntities();
            return;
        }
        LAST_SELECTION_HASH.put(player.getUniqueId(), selectionHash);

        List<MarkerData> markers = new ArrayList<>();

        removePlayerEntities(player.getUniqueId());

        int minX = Math.min(fb.getX(), sb.getX());
        int minY = Math.min(fb.getY(), sb.getY());
        int minZ = Math.min(fb.getZ(), sb.getZ());
        int maxX = Math.max(fb.getX(), sb.getX());
        int maxY = Math.max(fb.getY(), sb.getY());
        int maxZ = Math.max(fb.getZ(), sb.getZ());

        Color color = Resources.<RegionsFile>get(ResourceType.Regions).getDustColor(RegionsFile.DustColorType.SUB_AREA);

        addCuboidEdges(minX, minY, minZ, maxX, maxY, maxZ, color, markers);
        addCuboidCorners(minX, minY, minZ, maxX, maxY, maxZ, color, markers);

        if (!markers.isEmpty()) {
            scheduleMarkerSpawn(markers);
        }
    }

    /**
     * Adds chain markers for all 12 edges of a cuboid to the marker list.
     * <p>
     * Places iron chain block displays at 1-block intervals along each edge,
     * skipping the corner positions (which are handled separately). Uses
     * CHAIN_X for X-axis edges, CHAIN_Y for Y-axis edges, CHAIN_Z for Z-axis edges.
     *
     * @param minX    minimum X coordinate
     * @param minY    minimum Y coordinate
     * @param minZ    minimum Z coordinate
     * @param maxX    maximum X coordinate
     * @param maxY    maximum Y coordinate
     * @param maxZ    maximum Z coordinate
     * @param color   glow color
     * @param markers list to add chain markers to
     */
    private void addCuboidEdges(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Color color, List<MarkerData> markers) {
        for (int x = minX + 1; x < maxX; x++) {
            markers.add(new MarkerData(x, minY, minZ, color, CHAIN_X));
            markers.add(new MarkerData(x, minY, maxZ, color, CHAIN_X));
            markers.add(new MarkerData(x, maxY, minZ, color, CHAIN_X));
            markers.add(new MarkerData(x, maxY, maxZ, color, CHAIN_X));
        }

        for (int y = minY + 1; y < maxY; y++) {
            markers.add(new MarkerData(minX, y, minZ, color, CHAIN_Y));
            markers.add(new MarkerData(minX, y, maxZ, color, CHAIN_Y));
            markers.add(new MarkerData(maxX, y, minZ, color, CHAIN_Y));
            markers.add(new MarkerData(maxX, y, maxZ, color, CHAIN_Y));
        }

        for (int z = minZ + 1; z < maxZ; z++) {
            markers.add(new MarkerData(minX, minY, z, color, CHAIN_Z));
            markers.add(new MarkerData(maxX, minY, z, color, CHAIN_Z));
            markers.add(new MarkerData(minX, maxY, z, color, CHAIN_Z));
            markers.add(new MarkerData(maxX, maxY, z, color, CHAIN_Z));
        }
    }

    /**
     * Adds corner markers for all 8 corners of a cuboid to the marker list.
     * <p>
     * Places copper grate block displays at each of the 8 vertices of the cuboid.
     *
     * @param minX    minimum X coordinate
     * @param minY    minimum Y coordinate
     * @param minZ    minimum Z coordinate
     * @param maxX    maximum X coordinate
     * @param maxY    maximum Y coordinate
     * @param maxZ    maximum Z coordinate
     * @param color   glow color
     * @param markers list to add corner markers to
     */
    private void addCuboidCorners(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Color color, List<MarkerData> markers) {
        markers.add(new MarkerData(minX, minY, minZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(minX, minY, maxZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(maxX, minY, minZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(maxX, minY, maxZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(minX, maxY, minZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(minX, maxY, maxZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(maxX, maxY, minZ, color, CORNER_BLOCK));
        markers.add(new MarkerData(maxX, maxY, maxZ, color, CORNER_BLOCK));
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
     * Schedules spawning of all border markers on the main thread.
     * <p>
     * First removes any existing entities for this player, then spawns
     * new BlockDisplay entities for each marker. Runs on main thread
     * (or region scheduler for Folia) for thread-safe entity creation.
     *
     * @param markers list of markers to spawn
     */
    private void scheduleMarkerSpawn(List<MarkerData> markers) {
        removePlayerEntities(player.getUniqueId());

        Runnable spawnTask = () -> {
            for (MarkerData data : markers) {
                spawnMarker(player.getWorld(), data.x, data.y, data.z, data.color, data.blockData);
            }
        };

        if (IS_FOLIA) {
            player.getScheduler().run(Homestead.getInstance(), t -> spawnTask.run(), null);
        } else {
            Bukkit.getScheduler().runTask(Homestead.getInstance(), spawnTask);
        }
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
     * Uses the player's region scheduler (Folia) or Bukkit scheduler.
     * Registers the task in {@link #ACTIVE_SPAWNERS} and schedules auto-cancellation
     * after 1 minute (60 seconds) of inactivity.
     */
    private void startRepeatingEffect() {
        Homestead instance = Homestead.getInstance();

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

        if (task == null) {
            ACTIVE_SPAWNERS.remove(player.getUniqueId());
            return;
        }

        instance.runAsyncTaskLater(() -> cancelTask(player), 60);
    }

    /**
     * Data for a single border marker to spawn.
     * Contains position, color, and block data (chain or corner).
     */
    private record MarkerData(double x, double y, double z, Color color, BlockData blockData) {
    }
}
