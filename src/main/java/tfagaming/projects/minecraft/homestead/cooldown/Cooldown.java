package tfagaming.projects.minecraft.homestead.cooldown;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Cooldown {
	private static final Homestead instance = Homestead.getInstance();
	private static final Map<UUID, CooldownData> COOLDOWNS = new ConcurrentHashMap<>();

	public static void startCooldown(UUID id, Type type) {
		addCooldown(id, type);
		startCooldownAsync(id, type.getCooldown());
	}

	public static void startCooldown(Player player, Type type) {
		startCooldown(player.getUniqueId(), type);
	}

	public static void startCooldown(Region region, Type type) {
		startCooldown(region.getUniqueId(), type);
	}

	public static void addCooldown(UUID id, Type type) {
		long endTime = System.currentTimeMillis() + (type.getCooldown() * 1000L);
		COOLDOWNS.put(id, new CooldownData(type, endTime));
	}

	public static void removeCooldown(UUID id) {
		COOLDOWNS.remove(id);
	}

	public static boolean hasCooldown(UUID id) {
		return COOLDOWNS.containsKey(id);
	}

	public static boolean hasCooldown(Player player) {
		UUID id = player.getUniqueId();

		if (COOLDOWNS.containsKey(id)) {
			CooldownData data = COOLDOWNS.get(id);

			return !(data.getType().ignoreOperators() && PlayerUtils.isOperator(player));
		}

		return false;
	}

	public static boolean hasCooldown(Region region) {
		UUID id = region.getUniqueId();

		if (COOLDOWNS.containsKey(id)) {
			CooldownData data = COOLDOWNS.get(id);

			OfflinePlayer owner = region.getOwner();

			return !(data.getType().ignoreOperators() && PlayerUtils.isOperator(owner));
		}

		return false;
	}

	public static boolean hasCooldown(Player player, Type type) {
		UUID id = player.getUniqueId();

		if (COOLDOWNS.containsKey(id)) {
			CooldownData data = COOLDOWNS.get(id);

			return data.getType() == type && !(type.ignoreOperators() && PlayerUtils.isOperator(player));
		}

		return false;
	}

	public static boolean hasCooldown(Region region, Type type) {
		UUID id = region.getUniqueId();

		if (COOLDOWNS.containsKey(id)) {
			CooldownData data = COOLDOWNS.get(id);

			OfflinePlayer owner = region.getOwner();

			return data.getType() == type && !(data.getType().ignoreOperators() && PlayerUtils.isOperator(owner));
		}

		return false;
	}

	public static long getRemainingTime(UUID id) {
		CooldownData data = COOLDOWNS.get(id);
		if (data == null) {
			return 0;
		}

		return data.getEndTime();
	}

	public static long getRemainingTime(Player player) {
		return getRemainingTime(player.getUniqueId());
	}

	public static void startCooldownAsync(UUID id, int duration) {
		if (duration <= 0) {
			return;
		}

		Homestead.getInstance().runAsyncTaskLater(() -> {
			COOLDOWNS.remove(id);
		}, duration);
	}

	// not used
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