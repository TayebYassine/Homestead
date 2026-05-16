package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionChatEvent;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.sessions.PrivateChatSession;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;
import java.util.stream.Collectors;

public final class PrivateRegionChatListener implements Listener {
	private static void sendMessage(Player player, String path) {
		Object obj = Resources.<LanguageFile>get(ResourceType.Language).getRaw("commands." + path);

		if (obj == null) obj = "NULL";

		if (obj instanceof String message) {
			Messages.send(player, message);
		} else if (obj instanceof List<?> list) {
			String message = list.stream().map(String::valueOf).collect(Collectors.joining("\n"));

			Messages.send(player, message);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		if (!PrivateChatSession.hasSession(player)) {
			return;
		}

		event.setCancelled(true);

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			sendMessage(player, "chat.0");
			return;
		}

		String message = event.getMessage();

		if (ColorTranslator.containsMiniMessageTag(message)) {
			sendMessage(player, "chat.3");
			return;
		}

		RegionManager.sendPrivateChat(region, player, message);

		Homestead.callEvent(new RegionChatEvent(region, player, message));
	}
}
