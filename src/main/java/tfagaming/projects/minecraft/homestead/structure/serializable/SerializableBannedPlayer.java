package tfagaming.projects.minecraft.homestead.structure.serializable;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;

import java.util.UUID;

public class SerializableBannedPlayer {
	private final long bannedAt;
	private UUID playerId;
	private String reason;

	public SerializableBannedPlayer(OfflinePlayer player) {
		this.playerId = player.getUniqueId();
		this.reason = null;
		this.bannedAt = System.currentTimeMillis();
	}

	public SerializableBannedPlayer(OfflinePlayer player, String reason) {
		this.playerId = player.getUniqueId();
		this.reason = reason;
		this.bannedAt = System.currentTimeMillis();
	}

	public SerializableBannedPlayer(OfflinePlayer player, String reason, long bannedAt) {
		this.playerId = player.getUniqueId();
		this.reason = reason;
		this.bannedAt = bannedAt;
	}

	public SerializableBannedPlayer(UUID playerId, String reason, long bannedAt) {
		this.playerId = playerId;
		this.reason = reason;
		this.bannedAt = bannedAt;
	}

	public static SerializableBannedPlayer fromString(String string) {
		String[] splitted = StringUtils.splitWithLimit(string, ",", 3);

		return new SerializableBannedPlayer(UUID.fromString(splitted[0]), splitted[2], Long.parseLong(splitted[1]));
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public long getBannedAt() {
		return bannedAt;
	}

	@Override
	public String toString() {
		return (playerId + "," + bannedAt + "," + reason);
	}

	public OfflinePlayer getBukkitOfflinePlayer() {
		return Homestead.getInstance().getOfflinePlayerSync(playerId);
	}
}
