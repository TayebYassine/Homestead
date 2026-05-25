package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public class ChunkPositionKey {

    private final UUID worldId;
    private final int x;
    private final int z;

    public ChunkPositionKey(UUID worldId, int x, int z) {
        this.worldId = worldId;
        this.x = x;
        this.z = z;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkPositionKey c){
            return c.getX() == this.x && c.getZ() == this.z && c.getWorldId().equals(this.worldId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldId, x, z);
    }
}
