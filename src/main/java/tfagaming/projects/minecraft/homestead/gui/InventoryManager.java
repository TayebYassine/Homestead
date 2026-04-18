package tfagaming.projects.minecraft.homestead.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class InventoryManager {
	private static final Map<Player, Object> playerMenus = new HashMap<>();
	private static final Map<Player, Long> clickCooldowns = new HashMap<>();

	private static final long CLICK_COOLDOWN_MS = 800L;

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
}