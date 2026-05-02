package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class SetRegionSubCmd extends SubCommandBuilder {
	public SetRegionSubCmd() {
		super("set");
		setUsage("/region set [region]");
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

		if (!PlayerUtility.isOperator(player)
				&& !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player))) {
			Messages.send(player, 10);
			return true;
		}

		if (TargetRegionSession.hasSession(player)
				&& TargetRegionSession.getRegion(player).getUniqueId() == region.getUniqueId()) {
			Messages.send(player, 11);
			return true;
		}

		TargetRegionSession.newSession(player, region);

		Messages.send(player, 12, new Placeholder()
				.add("{region}", region.getName())
		);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			if (PlayerUtility.isOperator(player)) {
				suggestions.addAll(RegionManager.getRegionNames());
			} else {
				suggestions.addAll(
						RegionManager.getRegionsOwnedByPlayer(player).stream().map(Region::getName).toList());
				suggestions.addAll(
						RegionManager.getRegionsHasPlayerAsMember(player).stream().map(Region::getName).toList());
			}
		}

		return suggestions;
	}
}
