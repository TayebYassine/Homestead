package me.tayebyassine.homestead.gui;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.logs.Logger;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static registry mapping players to their open menus, with anti-autoclicker protection.
 *
 * <p>Tracks open menu instances per player, detects rapid click patterns, and temporarily bans
 * players who exceed the click threshold to prevent menu exploits.</p>
 */
public final class InventoryManager {
    private static final Map<Player, Object> PLAYER_MENUS = new HashMap<>();
    private static final Map<Player, Long> CLICK_COOLDOWNS = new HashMap<>();

    private static final Map<Player, List<Long>> CLICK_TIMESTAMPS = new ConcurrentHashMap<>();
    private static final Map<Player, Long> BAN_EXPIRY = new ConcurrentHashMap<>();

    private static final long CLICK_COOLDOWN_MS = 500L;
    private static final int CLICK_THRESHOLD = 20;
    private static final long DETECTION_WINDOW_MS = 1000L;
    private static final long BAN_DURATION_MS = 300_000L;

    /**
     * Registers a standard menu as the currently open menu for a player.
     *
     * @param player the player whose menu to track
     * @param menu   the menu instance to register
     */
    public static void register(Player player, Menu menu) {
        PLAYER_MENUS.put(player, menu);
    }

    /**
     * Registers a pagination menu as the currently open menu for a player.
     *
     * @param player the player whose menu to track
     * @param menu   the pagination menu instance to register
     */
    public static void register(Player player, PaginationMenu menu) {
        PLAYER_MENUS.put(player, menu);
    }

    /**
     * Unregisters a player's open menu and clears their click cooldown.
     *
     * @param player the player to unregister
     */
    public static void unregister(Player player) {
        PLAYER_MENUS.remove(player);
        CLICK_COOLDOWNS.remove(player);
    }

    /**
     * Returns the currently open menu object for a player, or {@code null} if none is registered.
     *
     * @param player the player to query
     * @return the open menu (either {@link Menu} or {@link PaginationMenu}), or {@code null}
     */
    public static Object getMenu(Player player) {
        return PLAYER_MENUS.get(player);
    }

    /**
     * Checks whether the player has an open menu registered.
     *
     * @param player the player to check
     * @return {@code true} if a menu is currently tracked for this player
     */
    public static boolean hasMenu(Player player) {
        return PLAYER_MENUS.containsKey(player);
    }

    /**
     * Checks whether the player is still within the per-click cooldown period.
     *
     * @param player the player to check
     * @return {@code true} if the player clicked too recently (within {@link #CLICK_COOLDOWN_MS})
     */
    public static boolean isOnCooldown(Player player) {
        Long last = CLICK_COOLDOWNS.get(player);
        return last != null && (System.currentTimeMillis() - last) < CLICK_COOLDOWN_MS;
    }

    /**
     * Updates the player's click cooldown timestamp to the current time.
     *
     * @param player the player to update
     */
    public static void updateCooldown(Player player) {
        CLICK_COOLDOWNS.put(player, System.currentTimeMillis());
    }

    /**
     * Records a click event and checks whether the player has exceeded the click threshold.
     * If the player exceeds {@link #CLICK_THRESHOLD} clicks within {@link #DETECTION_WINDOW_MS},
     * they are automatically kicked and temporarily banned.
     *
     * @param player the player who clicked
     * @return {@code true} if the click is allowed, {@code false} if the player was banned
     */
    public static boolean recordAndCheckClick(Player player) {
        long now = System.currentTimeMillis();

        if (isBanned(player)) return false;

        List<Long> timestamps = CLICK_TIMESTAMPS.computeIfAbsent(player, k -> new ArrayList<>());
        timestamps.add(now);
        timestamps.removeIf(ts -> (now - ts) > DETECTION_WINDOW_MS);

        if (timestamps.size() >= CLICK_THRESHOLD) {
            banPlayer(player);
            timestamps.clear();
            return false;
        }
        return true;
    }

    /**
     * Checks whether the player is currently banned by the anti-autoclicker system.
     * Automatically clears expired bans.
     *
     * @param player the player to check
     * @return {@code true} if the player is banned and the ban has not yet expired
     */
    public static boolean isBanned(Player player) {
        Long expiry = BAN_EXPIRY.get(player);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            BAN_EXPIRY.remove(player);
            return false;
        }
        return true;
    }

    /**
     * Kicks and temporarily bans a player for suspected autoclicker usage.
     * The ban lasts {@link #BAN_DURATION_MS} milliseconds and cannot be turned off.
     *
     * @param player the player to ban
     */
    private static void banPlayer(Player player) {
        long expiry = System.currentTimeMillis() + BAN_DURATION_MS;
        BAN_EXPIRY.put(player, expiry);

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