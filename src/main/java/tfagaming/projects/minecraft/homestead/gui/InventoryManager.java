package tfagaming.projects.minecraft.homestead.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;

import java.util.*;

public final class InventoryManager {
	private static final Map<Player, Object> playerMenus = new HashMap<>();
	private static final Map<Player, Long> clickCooldowns = new HashMap<>();

	private static final Map<Player, List<Long>> clickTimestamps = new HashMap<>();
	private static final Map<Player, Long> banExpiry = new HashMap<>();

	private static final long CLICK_COOLDOWN_MS = 500L;
	private static final int CLICK_THRESHOLD = 20;
	private static final long DETECTION_WINDOW_MS = 1000L;
	private static final long BAN_DURATION_MS = 300_000L;

	public static void register(Player player, Menu menu) {
		playerMenus.put(player, menu);
	}

	public static void register(Player player, PaginationMenu menu) {
		playerMenus.put(player, menu);
	}

	public static void unregister(Player player) {
		playerMenus.remove(player);
		clickCooldowns.remove(player);
	}

	public static Object getMenu(Player player) {
		return playerMenus.get(player);
	}

	public static boolean hasMenu(Player player) {
		return playerMenus.containsKey(player);
	}

	public static boolean isOnCooldown(Player player) {
		Long last = clickCooldowns.get(player);
		return last != null && (System.currentTimeMillis() - last) < CLICK_COOLDOWN_MS;
	}

	public static void updateCooldown(Player player) {
		clickCooldowns.put(player, System.currentTimeMillis());
	}

	public static boolean recordAndCheckClick(Player player) {
		long now = System.currentTimeMillis();

		if (isBanned(player)) return false;

		List<Long> timestamps = clickTimestamps.computeIfAbsent(player, k -> new ArrayList<>());
		timestamps.add(now);
		timestamps.removeIf(ts -> (now - ts) > DETECTION_WINDOW_MS);

		if (timestamps.size() >= CLICK_THRESHOLD) {
			banPlayer(player);
			timestamps.clear();
			return false;
		}
		return true;
	}

	public static boolean isBanned(Player player) {
		Long expiry = banExpiry.get(player);
		if (expiry == null) return false;
		if (System.currentTimeMillis() >= expiry) {
			banExpiry.remove(player);
			return false;
		}
		return true;
	}

	private static void banPlayer(Player player) {
		long expiry = System.currentTimeMillis() + BAN_DURATION_MS;
		banExpiry.put(player, expiry);

		String reason = "Autoclicker Detection";
		Date banUntil = new Date(expiry);

		Homestead.getInstance().runPlayerTask(player, () -> {
			if (player.isOnline()) {
				player.kickPlayer("Autoclicker Detection");
			}

			player.banPlayer(reason, banUntil, "Autoclicker", true);

			Logger.warning("[Anti-Autoclicker] Player '" + player.getName() + "' (UUID: " + player.getUniqueId() + ") has been banned for 5 minutes for using auto-clicker within menus.");
			Logger.warning("[Anti-Autoclicker] This is a defensive mechanism to avoid server lagging exploits. Cannot be turned off.");
		});
	}
}