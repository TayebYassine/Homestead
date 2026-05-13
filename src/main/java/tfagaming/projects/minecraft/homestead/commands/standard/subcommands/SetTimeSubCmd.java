package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.weatherandtime.RegionTime;

import java.util.ArrayList;
import java.util.List;

public class SetTimeSubCmd extends SubCommandBuilder {
	public SetTimeSubCmd() {
		super("settime");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.time"
		));
		setUsage("/region settime [time]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "settime.0");
			return true;
		}

		if (args.length < 1) {
			reply(player, "settime.1");
			return true;
		}

		String timeInput = args[0];
		int time = RegionTime.parse(timeInput);

		if (time == -1) {
			reply(player, "settime.2");
			return true;
		}

		int newTime = RegionTime.next(region.getTime());

		region.setTime(newTime);

		reply(player, "settime.3", timeInput);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_TIME, RegionTime.from(newTime));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(RegionTime.getAll());
		}

		return suggestions;
	}
}
