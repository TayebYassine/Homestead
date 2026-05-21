package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.models.RegionChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//TODO make this configable
public final class PositionIndexedChunkCache {

    private final ConcurrentHashMap<ChunkPositionKey, RegionChunk> cache = new ConcurrentHashMap<>();

    public void add(RegionChunk chunk) {
        cache.put(new ChunkPositionKey(chunk.getWorldId(), chunk.getX(), chunk.getZ()), chunk);
    }

    public void remove(RegionChunk chunk) {
        cache.remove(new ChunkPositionKey(chunk.getWorldId(), chunk.getX(), chunk.getZ()), chunk);
    }

    public RegionChunk get(ChunkPositionKey key) {
        return cache.get(key);
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