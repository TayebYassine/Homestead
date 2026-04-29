package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionUntrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;


import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class UntrustPlayerSubCmd extends SubCommandBuilder {
	public UntrustPlayerSubCmd() {
		super("untrust");
		setUsage("/region untrust [player]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.players.untrust")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNTRUST_PLAYERS)) {
			return true;
		}

		String targetName = args[0];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Messages.send(player, 29, new Placeholder()
					.add("{playername}", targetName)
			);
			return true;
		}

		if (InviteManager.isInvited(region, target)) {
			InviteManager.deleteInvitesOfPlayer(region, target);

			Messages.send(player, 37, new Placeholder()
					.add("{playername}", target.getName())
			);
		} else if (MemberManager.isMemberOfRegion(region, target)) {
			MemberManager.removeMemberFromRegion(target, region);

			for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(region)) {
				MemberManager.removeMemberFromSubArea(target, subArea);
			}

			Messages.send(player, 38, new Placeholder()
					.add("{region}", region.getName())
					.add("{player}", target.getName())
			);

			LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, target.getName());

			RegionUntrustPlayerEvent _event = new RegionUntrustPlayerEvent(region, player, target, RegionUntrustPlayerEvent.UntrustReason.EXECUTION);
			Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
		} else {
			Messages.send(player, 39, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", target.getName())
			);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			Region region = TargetRegionSession.getRegion(player);

			if (region != null) {
				for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
					suggestions.add(member.getPlayerName());
				}

				suggestions.addAll(InviteManager.getInvitesOfRegion(region).stream().map(RegionInvite::getPlayerName).toList());
			}
		}

		return suggestions;
	}
}
