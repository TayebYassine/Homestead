package tfagaming.projects.minecraft.homestead.sessions;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public final class ClaimFlySession {
	public static final HashSet<UUID> sessions = new HashSet<UUID>();

	private ClaimFlySession() {

	}

	public static void newSession(Player player) {
		sessions.add(player.getUniqueId());
	}

	public static boolean hasSession(Player player) {
		return sessions.contains(player.getUniqueId());
	}

	public static void removeSession(Player player) {
		sessions.remove(player.getUniqueId());
	}
}
