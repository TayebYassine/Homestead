package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class PlayerJoinListener implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		boolean welcomeEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("welcome-message.enabled");

		if (!welcomeEnabled) return;

		String message = Resources.<RegionsFile>get(ResourceType.Regions).getString("welcome-message.message");

		long unreadLogs = 0;

		for (Region region : RegionManager.getRegionsOwnedByPlayer(player)) {
			unreadLogs += LogManager.getLogs(region).stream().filter(log -> !log.isRead()).count();
		}

		Messages.send(player, Formatter.applyPlaceholders(message, new Placeholder()
				.add("{unread-logs}", unreadLogs)
				.add("{regions-invited}", InviteManager.getInvitesOfPlayer(player).size())
		));
	}
}
