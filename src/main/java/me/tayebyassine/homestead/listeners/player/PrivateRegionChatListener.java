package me.tayebyassine.homestead.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionChatEvent;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.LanguageFile;
import me.tayebyassine.homestead.sessions.PrivateChatSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Routes chat messages sent by players in a private-region chat session to the region's members
 * only, and fires a {@link me.tayebyassine.homestead.api.events.RegionChatEvent}.
 */
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
