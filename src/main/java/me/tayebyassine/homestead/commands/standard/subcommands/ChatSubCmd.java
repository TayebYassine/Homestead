package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionChatEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.ClaimFlySession;
import me.tayebyassine.homestead.sessions.PrivateChatSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;

import java.util.Arrays;
import java.util.List;

public class ChatSubCmd extends SubCommandBuilder {
	public ChatSubCmd() {
		super("chat");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.chat"
		));
		setUsage("/hs chat [message]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.chat.0");
			return true;
		}

		if (args.length < 1) {
			if (PrivateChatSession.hasSession(player)) {
				PrivateChatSession.removeSession(player);

				Messages.send(player, "commands.chat.2");
			} else {
				PrivateChatSession.newSession(player);

				Messages.send(player, "commands.chat.1");
			}

			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(0, args.length);
		String message = String.join(" ", messageList);

		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, "commands.chat.3");
			return true;
		}

		RegionManager.sendPrivateChat(region, player, message);

		Homestead.callEvent(new RegionChatEvent(region, player, message));

		return true;
	}
}
