package me.tayebyassine.homestead.sessions;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

/**
 * Tracks which players are in auto-claim mode.
 *
 * <p>When a player has an active session, every chunk they enter is
 * automatically claimed for their target region.
 * </p>
 */
public final class AutoClaimSession {

    public static final HashSet<UUID> SESSIONS = new HashSet<>();

    private AutoClaimSession() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Enable auto-claim mode for the given player.
     *
     * @param player the player to enable auto-claim for
     */
    public static void newSession(Player player) {
        SESSIONS.add(player.getUniqueId());
    }

    /**
     * Check whether the given player is in auto-claim mode.
     *
     * @param player the player to check
     * @return {@code true} if the player has an active auto-claim session
     */
    public static boolean hasSession(Player player) {
        return SESSIONS.contains(player.getUniqueId());
    }

    /**
     * Disable auto-claim mode for the given player.
     *
     * @param player the player to disable auto-claim for
     */
    public static void removeSession(Player player) {
        SESSIONS.remove(player.getUniqueId());
    }
}
