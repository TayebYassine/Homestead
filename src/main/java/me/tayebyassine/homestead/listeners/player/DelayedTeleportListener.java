package me.tayebyassine.homestead.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.DelayedTeleport;

/**
 * Cancels pending delayed teleports if the player moves (when configured) or quits the server.
 */
public final class DelayedTeleportListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		boolean cancelOnMove = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("delayed-teleport.cancel-on-move");
		if (!cancelOnMove) {
			return;
		}

		Player player = event.getPlayer();

		if (!DelayedTeleport.tasks.containsKey(player.getUniqueId())) {
			return;
		}

		if (event.getFrom() == null || event.getTo() == null) {
			return;
		}

		int fromX = event.getFrom().getBlockX();
		int fromZ = event.getFrom().getBlockZ();
		int toX = event.getTo().getBlockX();
		int toZ = event.getTo().getBlockZ();

		if (fromX != toX || fromZ != toZ) {
			DelayedTeleport.cancelTeleport(player.getUniqueId());
			Messages.send(player, "common.teleport_error_player_moved");
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (DelayedTeleport.tasks.containsKey(player.getUniqueId())) {
			DelayedTeleport.cancelTeleport(player.getUniqueId());
		}
	}
}