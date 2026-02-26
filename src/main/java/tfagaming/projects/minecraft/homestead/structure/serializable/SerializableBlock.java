package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

public class SerializableBlock {
	private final UUID worldId;
	private final int x;
	private final int y;
	private final int z;

	public SerializableBlock(Block block) {
		this.worldId = block.getWorld().getUID();
		this.x = block.getX();
		this.y = block.getY();
		this.z = block.getZ();
	}

	public SerializableBlock(UUID worldId, int x, int y, int z) {
		this.worldId = worldId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private static UUID resolveWorldId(String token) {
		if (token == null || token.isBlank()) return null;
		try {
			return UUID.fromString(token.trim());
		} catch (IllegalArgumentException ignored) {
			World w = Bukkit.getWorld(token.trim());
			return w != null ? w.getUID() : null;
		}
	}

	public static SerializableBlock fromString(String string) {
		String[] splitted = string.split(",");

		UUID worldId = resolveWorldId(splitted[0]);
		if (worldId == null) throw new IllegalArgumentException("Cannot resolve world from: " + splitted[0]);

		return new SerializableBlock(worldId, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]));
	}

	public UUID getWorldId() {
		return worldId;
	}

	public World getWorld() {
		return Bukkit.getWorld(worldId);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		return (worldId + "," + x + "," + y + "," + z);
	}

	public Block getBlock() {
		World world = getWorld();

		if (world == null) {
			return null;
		}

		return world.getBlockAt(x, y, z);
	}
}