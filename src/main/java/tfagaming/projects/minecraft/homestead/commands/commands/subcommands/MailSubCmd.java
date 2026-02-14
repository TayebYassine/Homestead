package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableLog;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailSubCmd extends SubCommandBuilder {
	public MailSubCmd() {
		super("mail");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.mail")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 3) {
			Messages.send(player, 0);
			return true;
		}

		String regionName = args[1];

		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return false;
		}

		int mailsCount = 0;

		for (SerializableLog log : region.getLogs()) {
			if (log.getAuthor().equals(player.getName()) && !log.isRead()) {
				mailsCount++;
			}
		}

		if (mailsCount >= 5) {
			Messages.send(player, 165);
			return false;
		}

		List<String> messageList = Arrays.asList(args).subList(2, args.length);
		String message = String.join(" ", messageList);

		RegionsManager.addNewLog(region.getUniqueId(), player.getName(), message);

		Messages.send(player, 166, new Placeholder()
				.add("{region-owner}", region.getOwner().getName())
		);

		return true;
	}
}
