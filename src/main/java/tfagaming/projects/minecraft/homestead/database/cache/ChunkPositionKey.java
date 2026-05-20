package tfagaming.projects.minecraft.homestead.database.cache;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public class ChunkPositionKey {

    private UUID worldId;
    private int x;
    private int z;

    public ChunkPositionKey(UUID worldId, int x, int z) {
        this.worldId = worldId;
        this.x = x;
        this.z = z;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public void setWorldId(UUID worldId) {
        this.worldId = worldId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ChunkPositionKey){
            ChunkPositionKey c = (ChunkPositionKey) obj;
            return c.getX() == this.x && c.getZ() == this.z && c.getWorldId() == this.worldId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return worldId.hashCode() + x + z;
    }
}
