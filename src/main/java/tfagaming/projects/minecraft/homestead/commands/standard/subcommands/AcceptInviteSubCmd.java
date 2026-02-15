package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;

import java.util.ArrayList;
import java.util.List;

public class AcceptInviteSubCmd extends SubCommandBuilder {
	public AcceptInviteSubCmd() {
		super("accept");
		setUsage("/region accept [region]");
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
		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return true;
		}

		if (!region.isPlayerInvited(player)) {
			Messages.send(player, 45, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			Messages.send(player, 116);
			return true;
		}

		region.removePlayerInvite(player);
		region.addMember(player);

		Messages.send(player, 46, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", player.getName())
		);

		if (region.getOwner().isOnline()) {
			Messages.send(region.getOwner().getPlayer(), 138, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", player.getName())
			);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 0) {
			suggestions.addAll(
					RegionsManager.getRegionsInvitedPlayer(player).stream()
							.map(Region::getName)
							.toList()
			);
		}

		return suggestions;
	}
}