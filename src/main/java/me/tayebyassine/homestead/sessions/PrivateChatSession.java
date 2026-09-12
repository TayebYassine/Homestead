package me.tayebyassine.homestead.sessions;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

/**
 * Tracks which players are in private-region-chat mode.
 *
 * <p>When a player has an active session, their chat messages are routed
 * to the region's private chat channel instead of the global chat.
 * </p>
 */
public final class PrivateChatSession {

    public static final HashSet<UUID> SESSIONS = new HashSet<>();

    private PrivateChatSession() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Enable private chat mode for the given player.
     *
     * @param player the player to enable private chat for
     */
    public static void newSession(Player player) {
        SESSIONS.add(player.getUniqueId());
    }

    /**
     * Check whether the given player is in private chat mode.
     *
     * @param player the player to check
     * @return {@code true} if the player has an active private chat session
     */
    public static boolean hasSession(Player player) {
        return SESSIONS.contains(player.getUniqueId());
    }

    /**
     * Disable private chat mode for the given player.
     *
     * @param player the player to disable private chat for
     */
    public static void removeSession(Player player) {
        SESSIONS.remove(player.getUniqueId());
    }
}
