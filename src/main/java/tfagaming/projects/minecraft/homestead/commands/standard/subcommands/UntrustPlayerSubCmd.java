package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.PlayerLeftRegionEvent;
import tfagaming.projects.minecraft.homestead.api.events.RevokePlayerInviteEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class UntrustPlayerSubCmd extends SubCommandBuilder {
	public UntrustPlayerSubCmd() {
		super("untrust");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.players.untrust"
		));
		setUsage("/region untrust [player]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			reply(player, "untrust.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "untrust.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.UNTRUST_PLAYERS)) {
			reply(player, "untrust.2");
			return true;
		}

		String targetName = args[0];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			reply(player, "untrust.3");
			return true;
		}

		if (InviteManager.isInvited(region, target)) {
			InviteManager.deleteInvitesOfPlayer(region, target);

			reply(player, "untrust.4");

			Homestead.callEvent(new RevokePlayerInviteEvent(region, target));
		} else if (MemberManager.isMemberOfRegion(region, target)) {
			MemberManager.removeMemberFromRegion(target, region);

			reply(player, "untrust.5");

			LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, target.getName());

			Homestead.callEvent(new PlayerLeftRegionEvent(region, target));
		} else {
			reply(player, "untrust.6");
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
