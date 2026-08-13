package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionDeleteEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.java.Formatter;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerUtility;

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
		setUsage("/hs delete [confirm]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.delete.0");
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, "commands.delete.1");
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, "commands.delete.2");
			return true;
		}

		String confirmInput = args[0];

		if (!confirmInput.equalsIgnoreCase("confirm")) {
			Messages.send(player, "commands.delete.1");
			return true;
		}

		final double bankAmount = region.getBank();
		final String regionName = region.getName();

		RegionManager.deleteRegion(region.getUniqueId(), player);

		PlayerBank.deposit(region.getOwner(), bankAmount);

		Messages.send(player, "commands.delete.3", regionName);

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
