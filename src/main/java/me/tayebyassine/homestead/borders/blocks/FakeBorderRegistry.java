package me.tayebyassine.homestead.borders.blocks;

import me.tayebyassine.homestead.models.Region;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry for managing fake border blocks shown to specific players.
 * <p>
 * This class tracks temporary block changes sent to individual players to visualize
 * region borders. Each fake block is associated with a specific viewer and region,
 * allowing independent border visualization for multiple players simultaneously.
 * </p>
 *
 * @see BorderBlockRenderer
 */
public final class FakeBorderRegistry {

    /**
     * Maps region ID to the set of fake border blocks for that region.
     */
    private static final ConcurrentMap<Long, Set<FakeBorderBlock>> REGION_MAP = new ConcurrentHashMap<>();

    /**
     * Maps location to the fake border block at that location.
     * Used for quick lookup and deduplication.
     */
    private static final ConcurrentMap<Location, FakeBorderBlock> LOCATION_MAP = new ConcurrentHashMap<>();

    private FakeBorderRegistry() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Registers a fake border block for a specific viewer and region.
     *
     * @param block the fake border block to register
     */
    public static void add(FakeBorderBlock block) {
        REGION_MAP.computeIfAbsent(block.regionId(), k -> ConcurrentHashMap.newKeySet())
                .add(block);
        LOCATION_MAP.put(block.loc(), block);
    }

    /**
     * Retrieves all fake border blocks for a given region.
     *
     * @param region the region
     * @return an unmodifiable set of fake border blocks, or empty set if none
     */
    public static Set<FakeBorderBlock> getRegionBlocks(Region region) {
        return getRegionBlocks(region.getUniqueId());
    }

    /**
     * Retrieves all fake border blocks for a given region.
     *
     * @param regionId the region ID
     * @return an unmodifiable set of fake border blocks, or empty set if none
     */
    public static Set<FakeBorderBlock> getRegionBlocks(long regionId) {
        Set<FakeBorderBlock> blocks = REGION_MAP.get(regionId);
        return blocks != null ? Collections.unmodifiableSet(blocks) : Collections.emptySet();
    }

    /**
     * Retrieves the fake border block at a specific location.
     *
     * @param loc the location to check
     * @return the fake border block, or null if none exists at that location
     */
    public static FakeBorderBlock getByLocation(Location loc) {
        return LOCATION_MAP.get(loc);
    }

    /**
     * Removes all fake border blocks for a region and sends block restoration
     * packets to the associated viewers.
     *
     * @param region the region to remove blocks for
     * @return the set of removed blocks
     */
    public static Set<FakeBorderBlock> removeRegion(Region region) {
        return removeRegion(region.getUniqueId());
    }

    /**
     * Removes all fake border blocks for a region and sends block restoration
     * packets to the associated viewers.
     *
     * @param regionId the region ID to remove blocks for
     * @return the set of removed blocks
     */
    public static Set<FakeBorderBlock> removeRegion(long regionId) {
        Set<FakeBorderBlock> set = REGION_MAP.remove(regionId);
        if (set != null) {
            set.forEach(b -> LOCATION_MAP.remove(b.loc()));
        }
        return set == null ? Collections.emptySet() : set;
    }

    /**
     * Removes a specific fake border block for a viewer.
     *
     * @param block the block to remove
     * @return true if the block was found and removed
     */
    public static boolean remove(FakeBorderBlock block) {
        boolean removed = LOCATION_MAP.remove(block.loc(), block);
        if (removed) {
            Set<FakeBorderBlock> regionBlocks = REGION_MAP.get(block.regionId());
            if (regionBlocks != null) {
                regionBlocks.remove(block);
                if (regionBlocks.isEmpty()) {
                    REGION_MAP.remove(block.regionId());
                }
            }
        }
        return removed;
    }

    /**
     * Clears all registered fake border blocks.
     */
    public static void clear() {
        REGION_MAP.clear();
        LOCATION_MAP.clear();
    }

    /**
     * Retrieves all fake border blocks for a specific viewer across all regions.
     *
     * @param viewerId the viewer's unique ID
     * @return list of fake border blocks for the viewer
     */
    public static List<FakeBorderBlock> getViewerBlocks(UUID viewerId) {
        List<FakeBorderBlock> result = new ArrayList<>();
        REGION_MAP.values().forEach(set -> set.forEach(b -> {
            if (b.viewerId().equals(viewerId)) result.add(b);
        }));
        return result;
    }

    /**
     * Record representing a fake border block shown to a specific viewer.
     *
     * @param loc          the block location
     * @param originalData the original block data before modification
     * @param regionId     the region ID this border belongs to
     * @param viewerId     the unique ID of the player seeing this fake block
     */
    public record FakeBorderBlock(
            Location loc,
            BlockData originalData,
            long regionId,
            UUID viewerId
    ) {
        /**
         * Creates a location copy to prevent external modification.
         *
         * @return a new Location instance with the same coordinates
         */
        public Location loc() {
            return loc.clone();
        }
    }
}