package tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DelayedTeleport {
	public static ConcurrentHashMap<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, Location> initialLocations = new ConcurrentHashMap<>();

	public DelayedTeleport(Player player, Location location) {
		UUID playerId = player.getUniqueId();

		if (tasks.containsKey(playerId)) {
			cancelTeleport(playerId);
			return;
		}

		boolean delayedTeleportEnabled = Homestead.config.getBoolean("delayed-teleport.enabled");

		if (!delayedTeleportEnabled) {
			teleportPlayer(player, location);
			return;
		}

		boolean ignoreOperators = Homestead.config.getBoolean("delayed-teleport.ignore-operators");

		if (ignoreOperators && PlayerUtils.isOperator(player)) {
			teleportPlayer(player, location);
			return;
		}

		Messages.send(player, 53);

		int delay = Homestead.config.getInt("delayed-teleport.delay");

		initialLocations.put(playerId, player.getLocation().clone());

		BukkitTask task = Homestead.getInstance().runSyncTaskLater(() -> {
			tasks.remove(playerId);
			initialLocations.remove(playerId);

			teleportPlayer(player, location);
		}, delay);

		tasks.put(playerId, task);
	}

	public static void cancelTeleport(UUID playerId) {
		BukkitTask task = tasks.get(playerId);
		if (task != null) {
			task.cancel();
			tasks.remove(playerId);
			initialLocations.remove(playerId);
		}
	}

	private void teleportPlayer(Player player, Location location) {
		if (location == null) {
			Messages.send(player, 52);
			return;
		}

		player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 500.0f, 1.0f);

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{location}", Formatters.formatLocation(location));

		Messages.send(player, 51, replacements);
	}
}