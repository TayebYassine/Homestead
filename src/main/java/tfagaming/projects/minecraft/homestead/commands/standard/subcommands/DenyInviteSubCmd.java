package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RevokePlayerInviteEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class DenyInviteSubCmd extends SubCommandBuilder {
	public DenyInviteSubCmd() {
		super("deny");
		setUsage("/region deny [region]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length < 1) {
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

		if (!InviteManager.isInvited(region, player)) {
			Messages.send(player, 45, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		InviteManager.deleteInvitesOfPlayer(region, player);

		Messages.send(player, 47, new Placeholder()
				.add("{region}", region.getName())
		);

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
