package me.tayebyassine.homestead.database.cache;

import java.util.UUID;

public record ChunkPositionKey(UUID worldId, int x, int z) {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkPositionKey c) {
            return c.x() == this.x && c.z() == this.z && c.worldId().equals(this.worldId);
        }
        return false;
    }

}
