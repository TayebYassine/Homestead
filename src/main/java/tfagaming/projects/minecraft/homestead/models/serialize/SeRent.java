package tfagaming.projects.minecraft.homestead.models.serialize;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.UUID;

public final class SeRent {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private UUID renterId;
	private long startedAt;
	private long untilAt;
	private double price;

	public SeRent(OfflinePlayer renter, long startedAt, long untilAt, double price) {
		this(renter.getUniqueId(), startedAt, untilAt, price);
	}

	public SeRent(UUID renterId, long startedAt, long untilAt, double price) {
		this.renterId = renterId;
		this.startedAt = startedAt;
		this.untilAt = untilAt;
		this.price = price;
	}

	public static SeRent deserialize(@NotNull String serialized) {
		String[] split = serialized.split("§");

		try {
			UUID playerId = UUID.fromString(split[0]);

			if (INSTANCE.getOfflinePlayerSync(playerId) == null) return null;

			long startedAt = Long.parseLong(split[1]);
			long untilAt = Long.parseLong(split[2]);
			double price = Double.parseDouble(split[3]);

			return new SeRent(playerId, startedAt, untilAt, price);
		} catch (Exception e) {
			throw new IllegalArgumentException("Serialized string cannot be parsed, invalid format");
		}
	}

	public UUID getRenterId() {
		return renterId;
	}

	public void setRenterId(UUID renterId) {
		this.renterId = renterId;
	}

	public OfflinePlayer getRenter() {
		if (INSTANCE == null) return null;

		return INSTANCE.getOfflinePlayerSync(renterId);
	}

	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}

	public long getUntilAt() {
		return untilAt;
	}

	public void setUntilAt(long untilAt) {
		this.untilAt = untilAt;
	}

	public boolean isManual() {
		return untilAt == -1L;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String serialize() {
		return String.format("%s§%s§%s§%s",
				renterId.toString(),
				startedAt,
				untilAt,
				price
		);
	}

	@Override
	public String toString() {
		return serialize();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SeRent other)) return false;
		return this.renterId.equals(other.getRenterId()) && this.startedAt == other.startedAt && this.untilAt == other.untilAt
				&& this.price == other.price;
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(renterId, startedAt, untilAt, price);
	}
}
