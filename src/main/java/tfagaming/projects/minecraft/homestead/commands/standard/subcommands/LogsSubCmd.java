package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionLogs;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class LogsSubCmd extends SubCommandBuilder {
	public LogsSubCmd() {
		super("logs");
		setUsage("/region logs");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		new RegionLogs(player, region);

		return true;
	}
}
