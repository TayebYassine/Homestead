package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLog;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
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
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.mail")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 3) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String regionName = args[0];

		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return true;
		}

		int mailsCount = 0;

		for (SerializableLog log : region.getLogs()) {
			if (log.getAuthor().equals(player.getName()) && !log.isRead()) {
				mailsCount++;
			}
		}

		if (mailsCount >= 5) {
			Messages.send(player, 165);
			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(2, args.length);
		String message = String.join(" ", messageList);

		RegionsManager.addNewLog(region.getUniqueId(), player.getName(), message);

		Messages.send(player, 166, new Placeholder()
				.add("{region-owner}", region.getOwner().getName())
		);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 0) {
			suggestions.addAll(RegionsManager.getAll().stream().map(Region::getName).toList());
		}

		return suggestions;
	}
}
