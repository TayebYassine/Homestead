package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//TODO make this configable
public final class RegionIndexedChunkCache {

    private final ConcurrentHashMap<Long, List<RegionChunk>> cache = new ConcurrentHashMap<>();

    public void add(RegionChunk chunk) {
        cache.compute(chunk.getRegionId(), (id, chunks) -> {
            if (chunks == null) {
                chunks = Collections.synchronizedList(new ArrayList<>());
            }
            chunks.remove(chunk);

            chunks.add(chunk);

            return chunks;
        });
    }

    public void remove(RegionChunk chunk) {
        cache.computeIfPresent(chunk.getRegionId(), (id, chunks) -> {
            chunks.remove(chunk);

            return chunks.isEmpty() ? null : chunks;
        });
    }

    public List<RegionChunk> get(long regionId) {
        List<RegionChunk> list = cache.get(regionId);

        if (list == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(list);
    }

    public void clear() {
        cache.clear();
    }

    public void putAll(List<RegionChunk> items) {
        for (RegionChunk item : items) {
            add(item);
        }
    }
}