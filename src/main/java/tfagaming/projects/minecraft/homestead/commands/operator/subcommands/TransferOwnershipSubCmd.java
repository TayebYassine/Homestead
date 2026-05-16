package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class TransferOwnershipSubCmd extends SubCommandBuilder {
	public TransferOwnershipSubCmd() {
		super("transfer");
		setPermission(List.of(
				"homestead.commands.homesteadadmin",
				"homestead.commands.homesteadadmin." + getName()
		));
		setUsage("/hsadmin transfer [region] [new-owner]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 2) {
			Messages.send(player, "commands.op_transfer.0", getUsage());
			return true;
		}

		String regionName = args[0];
		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, "commands.op_transfer.1");
			return true;
		}

		String playerName = args[1];
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

		if (target == null) {
			Messages.send(player, "commands.op_transfer.2");
			return true;
		}

		if (region.isOwner(target)) {
			Messages.send(player, "commands.op_transfer.3");
			return true;
		}

		BanManager.unbanPlayer(region, target);
		InviteManager.deleteInvitesOfPlayer(region, target);
		MemberManager.removeMemberFromRegion(target, region);

		for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(region)) {
			MemberManager.removeMemberFromSubArea(target, subArea);
		}

		region.setOwner(target);

		Messages.send(player, "commands.op_transfer.4");

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(
					RegionManager.getRegionNames()
			);
		} else if (args.length == 2) {
			suggestions.addAll(
					Homestead.getInstance().getOfflinePlayerNamesSync()
			);
		}

		return suggestions;
	}
}