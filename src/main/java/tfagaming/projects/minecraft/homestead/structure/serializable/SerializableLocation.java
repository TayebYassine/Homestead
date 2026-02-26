package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class SerializableLocation {
	private UUID worldId;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;

	public SerializableLocation(Location location) {
		this.worldId = location.getWorld() != null ? location.getWorld().getUID() : null;
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public SerializableLocation(World world, double x, double y, double z, float yaw, float pitch) {
		this.worldId = world != null ? world.getUID() : null;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public SerializableLocation(UUID worldId, double x, double y, double z, float yaw, float pitch) {
		this.worldId = worldId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	/**
	 * Resolves a world identifier that may be either a UUID string (new format)
	 * or a plain world name (legacy format). Returns null if unresolvable.
	 */
	private static UUID resolveWorldId(String token) {
		if (token == null || token.isBlank()) return null;
		try {
			return UUID.fromString(token.trim());
		} catch (IllegalArgumentException ignored) {
			World w = Bukkit.getWorld(token.trim());
			return w != null ? w.getUID() : null;
		}
	}

	public static SerializableLocation fromString(String string) {
		if (string == null) {
			return null;
		}

		String[] splitted = string.split(",");

		UUID worldId = resolveWorldId(splitted[0]);
		if (worldId == null) return null;

		return new SerializableLocation(worldId, Double.parseDouble(splitted[1]), Double.parseDouble(splitted[2]),
				Double.parseDouble(splitted[3]), Float.parseFloat(splitted[4]), Float.parseFloat(splitted[5]));
	}

	public static String toString(Location location) {
		return location.getWorld().getUID() + "," + location.getX() + "," + location.getY() + "," + location.getYaw() + "," + location.getPitch();
	}

	public World getWorld() {
		return worldId == null ? null : Bukkit.getWorld(worldId);
	}

	public void setWorld(World world) {
		this.worldId = world.getUID();
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	@Override
	public String toString() {
		return (worldId + "," + x + "," + y + "," + z + "," + yaw + "," + pitch);
	}

	public Location bukkit() {
		World world = Bukkit.getWorld(worldId);

		if (world == null) {
			return null;
		}

		Location location = new Location(world, x, y, z);

		location.setYaw(yaw);
		location.setPitch(pitch);

		return location;
	}
}