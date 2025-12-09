package tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DelayedTeleport {
	public static ConcurrentHashMap<UUID, BukkitTask> tasks = new ConcurrentHashMap<UUID, BukkitTask>();

	public DelayedTeleport(Player player, Location location) {
		if (tasks.containsKey(player.getUniqueId())) {
			return;
		}

		boolean delayedTeleportEnabled = Homestead.config.get("delayed-teleport.enabled");

		if (!delayedTeleportEnabled) {
			teleportPlayer(player, location);
			return;
		}

		boolean ignoreOperators = Homestead.config.get("delayed-teleport.ignore-operators");

		if (ignoreOperators && PlayerUtils.isOperator(player)) {
			teleportPlayer(player, location);
			return;
		}

		PlayerUtils.sendMessage(player, 53);

		int delay = Homestead.config.get("delayed-teleport.delay");
		long ticks = (delay * 1000L) / 50;

		BukkitTask task = new BukkitRunnable() {
			@Override
			public void run() {
				teleportPlayer(player, location);

				BukkitTask playerTask = tasks.get(player.getUniqueId());

				if (playerTask != null) {
					playerTask.cancel();

					tasks.remove(player.getUniqueId());
				}
			}
		}.runTaskLater(Homestead.getInstance(), ticks);

		tasks.put(player.getUniqueId(), task);
	}

	private void teleportPlayer(Player player, Location location) {
		if (location == null) {
			PlayerUtils.sendMessage(player, 52);
			return;
		}

		player.teleport(location);

		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 500.0f, 1.0f);

		HashMap<String, String> replacements = new HashMap<>();

		replacements.put("{location}", Formatters.formatLocation(location));

		PlayerUtils.sendMessage(player, 51, replacements);
	}
}
