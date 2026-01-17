package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public class SerializableRent {
	private final double price;
	private final long startAt;
	private final long untilAt;
	private UUID playerId;

	public SerializableRent(OfflinePlayer player, double price, long untilAt) {
		this.playerId = player.getUniqueId();
		this.price = price;
		this.startAt = System.currentTimeMillis();
		this.untilAt = untilAt;
	}

	public SerializableRent(UUID playerId, double price, long startedAt, long untilAt) {
		this.playerId = playerId;
		this.price = price;
		this.startAt = startedAt;
		this.untilAt = untilAt;
	}

	public static SerializableRent fromString(String string) {
		if (string == null) {
			return null;
		}

		String[] splitted = string.split(",");

		return new SerializableRent(UUID.fromString(splitted[0]), Double.parseDouble(splitted[1]), Long.parseLong(splitted[2]), Long.parseLong(splitted[3]));
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}

	public OfflinePlayer getPlayer() {
		return Homestead.getInstance().getOfflinePlayerSync(playerId);
	}

	public double getPrice() {
		return price;
	}

	public long getStartAt() {
		return startAt;
	}

	public long getUntilAt() {
		return untilAt;
	}

	@Override
	public String toString() {
		return (playerId + "," + price + "," + startAt + "," + untilAt);
	}

	public OfflinePlayer getBukkitOfflinePlayer() {
		return Homestead.getInstance().getOfflinePlayerSync(playerId);
	}
}
