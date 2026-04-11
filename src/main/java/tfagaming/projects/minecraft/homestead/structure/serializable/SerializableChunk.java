package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.UUID;

/**
 * Represents a serializable Minecraft chunk with claim metadata.
 * <p>
 * Supports backward compatibility for older database formats (missing claimedAt).
 * </p>
 */
public class SerializableChunk {
	private final long claimedAt;
	private UUID worldId;
	private int x;
	private int z;
	private boolean isForceLoaded;

	public SerializableChunk(Chunk chunk) {
		this.worldId = chunk.getWorld().getUID();
		this.x = chunk.getX();
		this.z = chunk.getZ();
		this.claimedAt = System.currentTimeMillis();
		this.isForceLoaded = false;
	}

	public SerializableChunk(World world, int x, int z) {
		this.worldId = world.getUID();
		this.x = x;
		this.z = z;
		this.claimedAt = System.currentTimeMillis();
		this.isForceLoaded = false;
	}

	public SerializableChunk(UUID worldId, int x, int z) {
		this.worldId = worldId;
		this.x = x;
		this.z = z;
		this.claimedAt = System.currentTimeMillis();
		this.isForceLoaded = false;
	}

	public SerializableChunk(UUID worldId, int x, int z, long claimedAt, boolean isForceLoaded) {
		this.worldId = worldId;
		this.x = x;
		this.z = z;
		this.claimedAt = claimedAt;
		this.isForceLoaded = isForceLoaded;
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

	public static SerializableChunk fromString(String string) {
		if (string == null || string.isEmpty()) return null;

		try {
			String[] split = string.split(",");

			// Must have at least world, x, z
			if (split.length < 3) return null;

			UUID worldId = resolveWorldId(split[0].trim());
			if (worldId == null) return null;
			int x = Integer.parseInt(split[1].trim());
			int z = Integer.parseInt(split[2].trim());

			long claimedAt = System.currentTimeMillis();

			if (split.length >= 4 && !split[3].trim().isEmpty()) {
				try {
					claimedAt = Long.parseLong(split[3].trim());
				} catch (NumberFormatException ignored) {
					// fallback to current timestamp
				}
			}

			boolean isForceLoaded = false;

			if (split.length >= 5 && !split[4].trim().isEmpty()) {
				isForceLoaded = Boolean.parseBoolean(split[4].trim());
			}

			return new SerializableChunk(worldId, x, z, claimedAt, isForceLoaded);
		} catch (Exception e) {
			return null;
		}
	}

	public static String convertToString(Chunk chunk) {
		return chunk.getWorld().getUID() + "," + chunk.getX() + "," + chunk.getZ();
	}

	public static boolean equals(Chunk chunk1, Chunk chunk2) {
		return convertToString(chunk1).equals(convertToString(chunk2));
	}

	public static boolean equals(SerializableChunk chunk1, Chunk chunk2) {
		return chunk1.toString(true).equals(convertToString(chunk2));
	}

	public static boolean equals(SerializableChunk chunk1, SerializableChunk chunk2) {
		return chunk1.toString(true).equals(chunk2.toString(true));
	}

	public UUID getWorldId() {
		return worldId;
	}

	public void setWorldId(UUID worldId) {
		this.worldId = worldId;
	}

	public World getWorld() {
		return worldId == null ? null : Bukkit.getWorld(worldId);
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

	public long getClaimedAt() {
		return claimedAt;
	}

	public boolean isForceLoaded() {
		return isForceLoaded;
	}

	public void setForceLoaded(boolean isForceLoaded) {
		this.isForceLoaded = isForceLoaded;
	}

	@Override
	public String toString() {
		return worldId + "," + x + "," + z + "," + claimedAt + "," + isForceLoaded;
	}

	public String toString(boolean noDetails) {
		if (noDetails) {
			return worldId + "," + x + "," + z;
		} else {
			return toString();
		}
	}

	public Location bukkitLocation() {
		World world = Bukkit.getWorld(worldId);
		if (world == null) return null;

		Location location = new Location(world, x * 16 + 8, 64, z * 16 + 8);
		location.setY(world.getHighestBlockYAt(location) + 2);

		if (world.getEnvironment() == World.Environment.NETHER) {
			Location newLocation = findSafeNetherLocation(world, x * 16 + 8, z * 16 + 8);
			if (newLocation != null) {
				location = newLocation;
			}
		}
		return location;
	}

	public Chunk bukkit() {
		Location loc = bukkitLocation();
		return (loc != null) ? loc.getChunk() : null;
	}

	private Location findSafeNetherLocation(World world, int x, int z) {
		int minY = 32;
		int maxY = 124; // Prevents ceiling spawn issues

		for (int y = maxY; y >= minY; y--) {
			Block block = world.getBlockAt(x, y, z);
			Block above = world.getBlockAt(x, y + 1, z);
			Block aboveAbove = world.getBlockAt(x, y + 2, z);

			if ((block.getType() != Material.AIR && block.getType() != Material.LAVA)
					&& above.getType() == Material.AIR
					&& aboveAbove.getType() == Material.AIR) {
				return new Location(world, x + 0.5, y + 1, z + 0.5);
			}
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SerializableChunk other)) return false;
		return this.x == other.x && this.z == other.z &&
				this.worldId.equals(other.worldId);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(worldId, x, z);
	}
}