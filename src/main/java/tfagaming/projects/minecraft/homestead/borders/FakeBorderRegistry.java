package tfagaming.projects.minecraft.homestead.borders;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FakeBorderRegistry {
    public static final Map<UUID, Set<FakeBorderBlock>> REGION_MAP = new ConcurrentHashMap<>();

    public static final Map<Location, FakeBorderBlock> LOC_MAP = new ConcurrentHashMap<>();

    public static void add(FakeBorderBlock block) {
        REGION_MAP.computeIfAbsent(block.regionUUID(), k -> ConcurrentHashMap.newKeySet())
                .add(block);
        LOC_MAP.put(block.loc(), block);
    }

    public static Set<FakeBorderBlock> getRegionBlocks(UUID regionUUID) {
        return REGION_MAP.getOrDefault(regionUUID, Set.of());
    }

    public static FakeBorderBlock getByLocation(Location loc) {
        return LOC_MAP.get(loc);
    }

    public static Set<FakeBorderBlock> removeRegion(UUID regionUUID) {
        Set<FakeBorderBlock> set = REGION_MAP.remove(regionUUID);
        if (set != null) {
            set.forEach(b -> LOC_MAP.remove(b.loc()));
        }
        return set == null ? Set.of() : set;
    }

    public record FakeBorderBlock(Location loc,
                                  BlockData originalData,
                                  UUID regionUUID,
                                  UUID viewerUUID) {
    }

    private FakeBorderRegistry() {
    }
}
