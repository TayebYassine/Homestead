package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public final class DelayedTeleportListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		boolean cancelOnMove = Homestead.config.getBoolean("delayed-teleport.cancel-on-move");
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
			Messages.send(player, 201);
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