package tfagaming.projects.minecraft.homestead.models.serialize;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class SeBlock {
	private UUID worldId;
	private int x;
	private int y;
	private int z;

	public SeBlock(Block block) {
		this(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
	}

	public SeBlock(World world, int x, int y, int z) {
		this(world.getUID(), x, y, z);
	}

	public SeBlock(UUID worldId, int x, int y, int z) {
		this.worldId = worldId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static SeBlock deserialize(@NotNull String serialized) {
		String[] split = serialized.split(",");

		try {
			UUID worldId = UUID.fromString(split[0]);

			if (Bukkit.getWorld(worldId) == null) return null;

			int x = Integer.parseInt(split[1]);
			int y = Integer.parseInt(split[2]);
			int z = Integer.parseInt(split[3]);

			return new SeBlock(worldId, x, y, z);
		} catch (Exception e) {
			throw new IllegalArgumentException("Serialized string cannot be parsed, invalid format");
		}
	}

	public UUID getWorldId() {
		return worldId;
	}

	public void setWorldId(UUID worldId) {
		this.worldId = worldId;
	}

	public World getWorld() {
		return Bukkit.getWorld(worldId);
	}

	public void setWorld(World world) {
		this.worldId = world.getUID();
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public String serialize() {
		return String.format("%s,%s,%s,%s",
				worldId.toString(),
				x,
				y,
				z
		);
	}

	public Block toBukkit() {
		World world = getWorld();

		if (world == null) {
			return null;
		}

		return world.getBlockAt(x, y, z);
	}

	@Override
	public String toString() {
		return serialize();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SeBlock other)) return false;
		return this.x == other.x && this.y == other.y && this.z == other.z &&
				this.worldId.equals(other.worldId);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(worldId, x, y, z);
	}
}