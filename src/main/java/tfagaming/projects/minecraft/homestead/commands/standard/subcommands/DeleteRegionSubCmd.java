package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDeleteEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class DeleteRegionSubCmd extends SubCommandBuilder {
	public DeleteRegionSubCmd() {
		super("delete");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.delete"
		));
		setUsage("/region delete [confirm]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "delete.0");
			return true;
		}

		if (args.length < 1) {
			reply(player, "delete.1");
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			reply(player, "delete.2");
			return true;
		}

		String confirmInput = args[0];

		if (!confirmInput.equalsIgnoreCase("confirm")) {
			reply(player, "delete.1");
			return true;
		}

		final double bankAmount = region.getBank();
		final String regionName = region.getName();

		RegionManager.deleteRegion(region.getUniqueId(), player);

		PlayerBank.deposit(region.getOwner(), bankAmount);

		reply(player, "delete.3", regionName);

		TargetRegionSession.randomizeRegion(player);

		Homestead.callEvent(new RegionDeleteEvent(region));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.add("confirm");
		}

		return suggestions;
	}
}
