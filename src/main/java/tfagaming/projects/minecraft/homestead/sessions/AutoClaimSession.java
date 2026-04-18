package tfagaming.projects.minecraft.homestead.sessions;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public final class AutoClaimSession {
	public static final HashSet<UUID> SESSIONS = new HashSet<UUID>();

	private AutoClaimSession() {

	}

	public static void newSession(Player player) {
		SESSIONS.add(player.getUniqueId());
	}

	public static boolean hasSession(Player player) {
		return SESSIONS.contains(player.getUniqueId());
	}

	public static void removeSession(Player player) {
		SESSIONS.remove(player.getUniqueId());
	}
}
