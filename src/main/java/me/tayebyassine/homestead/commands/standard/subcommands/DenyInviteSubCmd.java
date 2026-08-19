package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RevokePlayerInviteEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.InviteManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionInvite;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class DenyInviteSubCmd extends SubCommandBuilder {
	public DenyInviteSubCmd() {
		super("deny");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs deny [region]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		// TODO settings to disable invitation

		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.deny.0");
			return true;
		}

		String regionName = args[0];

		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, "commands.deny.1");
			return true;
		}

		if (!InviteManager.isInvited(region, player)) {
			Messages.send(player, "commands.deny.2");
			return true;
		}

		InviteManager.deleteInvitesOfPlayer(region, player);

		Messages.send(player, "commands.deny.3");

		Homestead.callEvent(new RevokePlayerInviteEvent(region, player));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(
					InviteManager.getInvitesOfPlayer(player).stream()
							.map(RegionInvite::getRegionName)
							.toList()
			);
		}

		return suggestions;
	}
}
