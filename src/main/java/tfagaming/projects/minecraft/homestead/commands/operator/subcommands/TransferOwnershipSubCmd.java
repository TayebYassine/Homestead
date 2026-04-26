package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTransferOwnershipEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class TransferOwnershipSubCmd extends SubCommandBuilder {
	public TransferOwnershipSubCmd() {
		super("transfer", null, false);
		setUsage("/hsadmin transfer [region] [new-owner]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 2) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String regionName = args[0];
		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			Messages.send(sender, 9);
			return true;
		}

		String playerName = args[1];
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

		if (target == null) {
			Messages.send(sender, 29, new Placeholder()
					.add("{playername}", playerName)
			);
			return true;
		}

		if (region.isOwner(target.getUniqueId())) {
			Messages.send(sender, 192);
			return true;
		}

		BanManager.unbanPlayer(region, target);

		region.setOwner(target);

		MemberManager.removeMemberFromRegion(target, region);
		InviteManager.deleteInvitesOfPlayer(region, target);

		Messages.send(sender, 193, new Placeholder()
				.add("{region}", region.getName())
				.add("{player}", target.getName())
		);

		RegionTransferOwnershipEvent _event = new RegionTransferOwnershipEvent(region, sender instanceof Player ? (Player) sender : target, target);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(
					RegionManager.getAll().stream()
							.map(Region::getName)
							.toList()
			);
		} else if (args.length == 2) {
			suggestions.addAll(
					Homestead.getInstance().getOfflinePlayersSync().stream()
							.map(OfflinePlayer::getName)
							.toList()
			);
		}

		return suggestions;
	}
}