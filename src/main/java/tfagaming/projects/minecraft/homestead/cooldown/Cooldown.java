package tfagaming.projects.minecraft.homestead.cooldown;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Cooldown {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private static final Map<UUID, CooldownData> COOLDOWNS = new ConcurrentHashMap<>();

	public static void startCooldown(Player player, Type type) {
		addCooldown(player.getUniqueId(), type);
		startCooldownSync(player.getUniqueId(), type.getCooldown());
	}

	private static void addCooldown(UUID id, Type type) {
		long endTime = System.currentTimeMillis() + (type.getCooldown() * 1000L);
		COOLDOWNS.put(id, new CooldownData(type, endTime));
	}

	public static void removePlayerCooldown(UUID id) {
		COOLDOWNS.remove(id);
	}

	public static boolean hasCooldown(Player player) {
		UUID id = player.getUniqueId();

		if (COOLDOWNS.containsKey(id)) {
			CooldownData data = COOLDOWNS.get(id);

			return !(data.getType().ignoreOperators() && PlayerUtility.isOperator(player));
		}

		return false;
	}

	public static boolean hasCooldown(Player player, Type type) {
		UUID id = player.getUniqueId();

		if (COOLDOWNS.containsKey(id)) {
			CooldownData data = COOLDOWNS.get(id);

			return data.getType() == type && !(type.ignoreOperators() && PlayerUtility.isOperator(player));
		}

		return false;
	}

	public static long getRemainingTime(Player player) {
		CooldownData data = COOLDOWNS.get(player.getUniqueId());
		if (data == null) {
			return 0;
		}

		return data.getEndTime();
	}

	private static void startCooldownSync(UUID id, int duration) {
		if (duration <= 0) {
			return;
		}

		Homestead.getInstance().runSyncTaskLater(() -> {
			COOLDOWNS.remove(id);
		}, duration);
	}

	public static void sendCooldownMessage(Player player) {
		long remaining = getRemainingTime(player);

		Messages.send(player, 118, new Placeholder()
				.add("{remaining-time}", Formatter.getAgo(remaining))
		);
	}

	private static class CooldownData {
		private final Type type;
		private final long endTime;

		public CooldownData(Type type, long endTime) {
			this.type = type;
			this.endTime = endTime;
		}

		public Type getType() {
			return type;
		}

		public long getEndTime() {
			return endTime;
		}
	}

	public enum Type {
		FLAG_CHANGE_STATE("flag-change-state"),
		REGION_SPAWN_CHANGE("region-spawn-change"),
		REGION_RENAME_CHANGE("region-rename-change"),
		REGION_DESCRIPTION_CHANGE("region-description-change"),
		REGION_TRANSFER_OWNERSHIP("region-transfer-ownership"),
		REGION_DYNAMIC_MAP_SETTINGS_CHANGE("region-dynamic-map-settings-change"),
		REGION_CHUNK_CLAIM("region-chunk-claim"),
		REGION_CHUNK_UNCLAIM("region-chunk-unclaim"),
		REGION_TELEPORT("region-teleport"),
		WAR_FLAG_DISABLED("war-flag-disabled");

		private final String key;

		Type(String key) {
			this.key = key;
		}

		public boolean ignoreOperators() {
			return Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("cooldown." + key + ".ignore-operators", true);
		}

		public int getCooldown() {
			return Resources.<RegionsFile>get(ResourceType.Regions).getInt("cooldown." + key + ".value", 0);
		}
	}
}