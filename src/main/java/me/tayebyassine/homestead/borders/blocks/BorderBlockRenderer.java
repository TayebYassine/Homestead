package me.tayebyassine.homestead.borders.blocks;

import com.google.common.collect.Sets;
import me.tayebyassine.homestead.borders.ChunkBorder;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Renders region borders using fake block changes sent to individual players.
 * <p>
 * This class creates visual border outlines by sending {@link Player#sendBlockChange(Location, BlockData)}
 * packets to show temporary blocks at chunk boundaries. Each player sees their own independent
 * border visualization, allowing multiple players to view different regions simultaneously.
 * </p>
 *
 * @see FakeBorderRegistry
 * @see ChunkBorder
 */
public final class BorderBlockRenderer {

    private BorderBlockRenderer() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Shows region borders to a player by sending fake block changes.
     * <p>
     * This calculates the outer edges of all chunks in the region and places
     * border blocks along chunk boundaries that border unclaimed or other regions.
     * </p>
     *
     * @param player the player to show borders to
     * @param region the region whose borders to display
     * @throws IllegalArgumentException if player or region is null
     */
    public static void show(Player player, Region region) {
        if (player == null || region == null) {
            throw new IllegalArgumentException("Player and region must not be null");
        }

        removeAll(player);

        Set<FakeBorderRegistry.FakeBorderBlock> batch = Sets.newConcurrentHashSet();
        World world = player.getWorld();
        Material borderMat = ChunkBorder.getBlockType();

        for (RegionChunk sc : ChunkManager.getChunksOfRegion(region)) {
            if (!world.getUID().equals(sc.getWorldId())) continue;

            int cx = sc.getX();
            int cz = sc.getZ();

            if (isChunkNotInRegion(region, world, cx, cz - 1)) {
                addBorderColumn(batch, player, region, world, cx, cz, Direction.NORTH, borderMat);
            }
            if (isChunkNotInRegion(region, world, cx, cz + 1)) {
                addBorderColumn(batch, player, region, world, cx, cz, Direction.SOUTH, borderMat);
            }
            if (isChunkNotInRegion(region, world, cx - 1, cz)) {
                addBorderColumn(batch, player, region, world, cx, cz, Direction.WEST, borderMat);
            }
            if (isChunkNotInRegion(region, world, cx + 1, cz)) {
                addBorderColumn(batch, player, region, world, cx, cz, Direction.EAST, borderMat);
            }
        }

        batch.forEach(FakeBorderRegistry::add);
    }

    /**
     * Removes all fake border blocks shown to a specific player.
     * <p>
     * Sends block restoration packets to the player for all borders they currently see.
     * </p>
     *
     * @param player the player whose borders to remove
     */
    public static void removeAll(Player player) {
        if (player == null) return;

        UUID viewer = player.getUniqueId();
        List<FakeBorderRegistry.FakeBorderBlock> toRemove = FakeBorderRegistry.getViewerBlocks(viewer);

        toRemove.forEach(b -> player.sendBlockChange(b.loc(), b.originalData()));

        toRemove.forEach(FakeBorderRegistry::remove);
    }

    /**
     * Removes all fake border blocks for a specific region.
     * <p>
     * Sends block restoration packets to all viewers who had borders for this region.
     * </p>
     *
     * @param region the region to remove borders for
     */
    public static void removeRegion(Region region) {
        removeRegion(region.getUniqueId());
    }

    /**
     * Removes all fake border blocks for a specific region.
     * <p>
     * Sends block restoration packets to all viewers who had borders for this region.
     * </p>
     *
     * @param regionId the region ID to remove borders for
     */
    public static void removeRegion(long regionId) {
        Set<FakeBorderRegistry.FakeBorderBlock> blocks = FakeBorderRegistry.removeRegion(regionId);
        blocks.forEach(b -> {
            Player viewer = Bukkit.getPlayer(b.viewerId());
            if (viewer != null && viewer.isOnline()) {
                viewer.sendBlockChange(b.loc(), b.originalData());
            }
        });
    }

    /**
     * Checks if a chunk at the given coordinates is NOT claimed by the specified region.
     *
     * @param region the region to check against
     * @param world  the world containing the chunk
     * @param cx     chunk X coordinate
     * @param cz     chunk Z coordinate
     * @return true if the chunk is not claimed by the region
     */
    private static boolean isChunkNotInRegion(Region region, World world, int cx, int cz) {
        return !ChunkManager.isChunkClaimedByRegion(
                region.getUniqueId(),
                ChunkManager.getFromLocation(world, cx, cz)
        );
    }

    /**
     * Adds a vertical column of border blocks along a chunk edge.
     *
     * @param batch     the batch to add blocks to
     * @param player    the viewer
     * @param region    the region
     * @param world     the world
     * @param cx        chunk X coordinate
     * @param cz        chunk Z coordinate
     * @param dir       the direction (edge) to render
     * @param borderMat the border block material
     */
    private static void addBorderColumn(
            Set<FakeBorderRegistry.FakeBorderBlock> batch,
            Player player,
            Region region,
            World world,
            int cx, int cz,
            Direction dir,
            Material borderMat
    ) {
        int baseX = cx << 4;
        int baseZ = cz << 4;

        int startX, startZ, stepX, stepZ;

        switch (dir) {
            case NORTH -> {
                startX = baseX;
                startZ = baseZ;
                stepX = 1;
                stepZ = 0;
            }
            case SOUTH -> {
                startX = baseX;
                startZ = baseZ + 15;
                stepX = 1;
                stepZ = 0;
            }
            case WEST -> {
                startX = baseX;
                startZ = baseZ;
                stepX = 0;
                stepZ = 1;
            }
            case EAST -> {
                startX = baseX + 15;
                startZ = baseZ;
                stepX = 0;
                stepZ = 1;
            }
            default -> throw new IllegalStateException("Unexpected direction: " + dir);
        }

        BlockData fakeData = borderMat.createBlockData();

        for (int i = 0; i < 16; i++) {
            int x = startX + i * stepX;
            int z = startZ + i * stepZ;
            int y = world.getHighestBlockYAt(x, z);

            Location loc = new Location(world, x, y, z);
            BlockData original = loc.getBlock().getBlockData();

            player.sendBlockChange(loc, fakeData);

            batch.add(new FakeBorderRegistry.FakeBorderBlock(
                    loc, original, region.getUniqueId(), player.getUniqueId()
            ));
        }
    }

    /**
     * Cardinal directions for chunk border rendering.
     */
    private enum Direction {NORTH, SOUTH, EAST, WEST}
}