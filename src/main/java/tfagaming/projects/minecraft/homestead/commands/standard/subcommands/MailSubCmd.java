package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.PlayerMailEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;


import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionLog;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MailSubCmd extends SubCommandBuilder {
	public MailSubCmd() {
		super("mail");
		setUsage("/region mail [region] [message]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.mail")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String regionName = args[0];

		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return true;
		}

		int mailsCount = (int) LogManager.getUnreadLogs(region).stream().filter((l) -> l.getAuthor().equals(player.getName())).count();

		if (mailsCount >= 5) {
			Messages.send(player, 165);
			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(1, args.length);
		String message = String.join(" ", messageList);

		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, 30);
			return true;
		}

		LogManager.addLog(region, player.getName(), message);

		Messages.send(player, 166, new Placeholder()
				.add("{region-owner}", region.getOwnerName())
		);

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
