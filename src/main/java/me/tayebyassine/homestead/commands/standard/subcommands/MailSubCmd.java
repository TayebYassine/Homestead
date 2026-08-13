package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerMailEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;

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
		setUsage("/hs mail [region] [message]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 2) {
			Messages.send(player, "commands.mail.0");
			return true;
		}

		String regionName = args[0];

		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, "commands.mail.1", regionName);
			return true;
		}

		int mailsCount = (int) LogManager.getUnreadLogs(region).stream().filter((l) -> l.getAuthor().equals(player.getName())).count();

		if (mailsCount >= 10) {
			Messages.send(player, "commands.mail.2");
			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(1, args.length);
		String message = String.join(" ", messageList);

		if (ColorTranslator.containsMiniMessageTag(message)) {
			Messages.send(player, "commands.mail.3");
			return true;
		}

		LogManager.addLog(region, player.getName(), message);

		Messages.send(player, "commands.mail.4");

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
