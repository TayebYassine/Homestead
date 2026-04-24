package tfagaming.projects.minecraft.homestead.models.serialize;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class SeLocation {
	private UUID worldId;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;

	public SeLocation(Location location) {
		this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public SeLocation(World world, double x, double y, double z, float yaw, float pitch) {
		this(world.getUID(), x, y, z, yaw, pitch);
	}

	public SeLocation(UUID worldId, double x, double y, double z, float yaw, float pitch) {
		this.worldId = worldId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public static SeLocation deserialize(@NotNull String serialized) {
		String[] split = serialized.split("§");

		try {
			UUID worldId = UUID.fromString(split[0]);

			if (Bukkit.getWorld(worldId) == null) return null;

			double x = Double.parseDouble(split[1]);
			double y = Double.parseDouble(split[2]);
			double z = Double.parseDouble(split[3]);

			float yaw = Float.parseFloat(split[4]);
			float pitch = Float.parseFloat(split[5]);

			return new SeLocation(worldId, x, y, z, yaw, pitch);
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
		return Bukkit.getWorld(this.worldId);
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

	public String serialize() {
		return String.format("%s§%s§%s§%s§%s§%s",
				worldId.toString(),
				x,
				y,
				z,
				yaw,
				pitch
		);
	}

	public Location toBukkit() {
		World world = getWorld();

		if (world == null) {
			return null;
		}

		return new Location(world, x, y, z, pitch, yaw);
	}

	@Override
	public String toString() {
		return serialize();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SeLocation other)) return false;
		return this.x == other.x && this.y == other.y && this.z == other.z &&
				this.worldId.equals(other.worldId);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(worldId, x, y, z, yaw, pitch);
	}
}
