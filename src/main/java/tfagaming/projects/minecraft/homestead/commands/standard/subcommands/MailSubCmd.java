package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.PlayerMailEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MailSubCmd extends SubCommandBuilder {
	public MailSubCmd() {
		super("mail");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.mail"
		));
		setUsage("/region mail [region] [message]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 2) {
			reply(player, "mail.0");
			return true;
		}

		String regionName = args[0];

		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			reply(player, "mail.1", regionName);
			return true;
		}

		int mailsCount = (int) LogManager.getUnreadLogs(region).stream().filter((l) -> l.getAuthor().equals(player.getName())).count();

		if (mailsCount >= 10) {
			reply(player, "mail.2");
			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(1, args.length);
		String message = String.join(" ", messageList);

		if (ColorTranslator.containsMiniMessageTag(message)) {
			reply(player, "mail.3");
			return true;
		}

		LogManager.addLog(region, player.getName(), message);

		reply(player, "mail.4");

		Homestead.callEvent(new PlayerMailEvent(region, player, message));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(RegionManager.getRegionNames());
		}

		return suggestions;
	}
}
