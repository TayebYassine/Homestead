package tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.papermc.TaskHandle;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerSound;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DelayedTeleport {
	public static Map<UUID, TaskHandle> tasks = new ConcurrentHashMap<>();
	public static Map<UUID, Location> initialLocations = new ConcurrentHashMap<>();
	public static Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
	public static Map<UUID, BukkitRunnable> bossBarTasks = new ConcurrentHashMap<>();
	public static Map<UUID, TaskHandle> bossBarTaskHandles = new ConcurrentHashMap<>();

	public DelayedTeleport(Player player, Location location) {
		this(player, location, null);
	}

	public DelayedTeleport(Player player, Location location, String locationName) {
		UUID playerId = player.getUniqueId();

		if (tasks.containsKey(playerId)) {
			cancelTeleport(playerId);
			return;
		}

		boolean delayedTeleportEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("delayed-teleport.enabled");

		if (!delayedTeleportEnabled) {
			teleportPlayer(player, location);
			return;
		}

		boolean ignoreOperators = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("delayed-teleport.ignore-operators");

		if (ignoreOperators && PlayerUtils.isOperator(player)) {
			teleportPlayer(player, location);
			return;
		}

		Messages.send(player, 53);

		int delay = Resources.<RegionsFile>get(ResourceType.Regions).getInt("delayed-teleport.delay");

		initialLocations.put(playerId, player.getLocation().clone());

		boolean bossBarEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("delayed-teleport.boss-bar.enabled", true);
		if (bossBarEnabled && delay > 0) {
			setupBossBar(player, delay);
		}

		TaskHandle task = Homestead.getInstance().runSyncTaskLater(() -> {
			tasks.remove(playerId);
			initialLocations.remove(playerId);
			removeBossBar(playerId);

			double price = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("delayed-teleport.price");
			if (price > 0 && PlayerBank.get(player) < price) {
				Messages.send(player, 203, new Placeholder()
						.add("{price}", Formatter.getBalance(price))
				);
				return;
			}

			if (price > 0) PlayerBank.withdraw(player, price);

			teleportPlayer(player, location);
		}, delay);

		tasks.put(playerId, task);
	}

	public static void cancelTeleport(UUID playerId) {
		TaskHandle task = tasks.get(playerId);
		if (task != null) {
			task.cancel();
			tasks.remove(playerId);
			initialLocations.remove(playerId);
			removeBossBar(playerId);
		}
	}

	private static void removeBossBar(UUID playerId) {
		TaskHandle countdownTask = bossBarTaskHandles.remove(playerId);
		if (countdownTask != null) {
			countdownTask.cancel();
		}

		BossBar bossBar = activeBossBars.remove(playerId);
		if (bossBar != null) {
			bossBar.removeAll();
		}
	}

	public static void cleanup() {
		for (UUID playerId : activeBossBars.keySet()) {
			removeBossBar(playerId);
		}
		tasks.clear();
		initialLocations.clear();
	}

	private void setupBossBar(Player player, int totalSeconds) {
		UUID playerId = player.getUniqueId();
		RegionsFile config = Resources.get(ResourceType.Regions);

		String titleTemplate = config.getString("delayed-teleport.boss-bar.title", "&cTeleporting in &e{seconds}s");
		String colorName = config.getString("delayed-teleport.boss-bar.color", "RED");
		String styleName = config.getString("delayed-teleport.boss-bar.style", "SEGMENTED_10");
		String countdownMode = config.getString("delayed-teleport.boss-bar.countdown-mode", "DEPLETE");

		BarColor color;
		try {
			color = BarColor.valueOf(colorName.toUpperCase());
		} catch (IllegalArgumentException e) {
			color = BarColor.RED;
		}

		BarStyle style;
		try {
			style = BarStyle.valueOf(styleName.toUpperCase());
		} catch (IllegalArgumentException e) {
			style = BarStyle.SEGMENTED_10;
		}

		String title = ColorTranslator.translate(titleTemplate.replace("{seconds}", String.valueOf(totalSeconds)));

		BossBar bossBar = Bukkit.createBossBar(title, color, style);
		bossBar.setProgress(1.0);
		bossBar.addPlayer(player);
		activeBossBars.put(playerId, bossBar);

		AtomicInteger ticksElapsed = new AtomicInteger(0);
		int totalTicks = totalSeconds * 20;
		Location playerLocation = player.getLocation();

		TaskHandle countdownTask = Homestead.getInstance().runLocationTaskTimer(
				playerLocation,
				() -> {
					int elapsed = ticksElapsed.incrementAndGet();

					if (elapsed >= totalTicks) {
						TaskHandle handle = bossBarTaskHandles.remove(playerId);
						if (handle != null) handle.cancel();
						return;
					}

					int remainingSeconds = (int) Math.ceil((totalTicks - elapsed) / 20.0);

					String updatedTitle = ColorTranslator.translate(titleTemplate.replace("{seconds}", String.valueOf(remainingSeconds)));
					bossBar.setTitle(updatedTitle);

					double progress = (double) (totalTicks - elapsed) / totalTicks;
					if (countdownMode.equalsIgnoreCase("FILL")) {
						progress = 1.0 - progress;
					}
					bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
				},
				1L,
				1L
		);

		bossBarTaskHandles.put(playerId, countdownTask);
	}

	private void teleportPlayer(Player player, Location location) {
		removeBossBar(player.getUniqueId());

		if (location == null) {
			Messages.send(player, 52);
			return;
		}

		if (Homestead.isFolia()) {
			player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
		} else {
			player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
		}

		PlayerSound.play(player, PlayerSound.PredefinedSound.TELEPORT);

		Messages.send(player, 51, new Placeholder()
				.add("{location}", Formatter.getLocation(location))
		);
	}
}