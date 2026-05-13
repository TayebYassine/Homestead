package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionChatEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

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
		setUsage("/region chat [message]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		// TODO added toggle

		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "chat.0");
			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(0, args.length);
		String message = String.join(" ", messageList);

		if (ColorTranslator.containsMiniMessageTag(message)) {
			reply(player, "chat.3");
			return true;
		}

		RegionManager.sendPrivateChat(region, player, message);

		Homestead.callEvent(new RegionChatEvent(region, player, message));

		return true;
	}
}
