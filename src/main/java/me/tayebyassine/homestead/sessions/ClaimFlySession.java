package me.tayebyassine.homestead.sessions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

/**
 * Tracks which players are in claim-fly mode.
 *
 * <p>When a player has an active session, they are allowed to fly within
 * their claimed regions. Flight is revoked when the player leaves a
 * claimed region, toggles the command off, or the plugin disables.
 * </p>
 */
public final class ClaimFlySession {

    public static final HashSet<UUID> SESSIONS = new HashSet<>();

    private ClaimFlySession() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Enable claim-fly mode for the given player.
     *
     * @param player the player to enable claim-fly for
     */
    public static void newSession(Player player) {
        SESSIONS.add(player.getUniqueId());
    }

    /**
     * Check whether the given player is in claim-fly mode.
     *
     * @param player the player to check
     * @return {@code true} if the player has an active claim-fly session
     */
    public static boolean hasSession(Player player) {
        return SESSIONS.contains(player.getUniqueId());
    }

    /**
     * Disable claim-fly mode for the given player and revoke their flight
     * ability.
     *
     * @param player the player to disable claim-fly for
     */
    public static void removeSession(Player player) {
        SESSIONS.remove(player.getUniqueId());
        player.setAllowFlight(false);
    }

    /**
     * Disable claim-fly mode for all online players and revoke their
     * flight ability.
     *
     * <p>Called during plugin shutdown to ensure no player is left flying
     * after the plugin disables.
     * </p>
     */
    public static void cleanupAll() {
        for (UUID uuid : SESSIONS) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.setAllowFlight(false);
            }
        }
        SESSIONS.clear();
    }
}
