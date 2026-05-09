package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.PlayerJoinRegionEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionBan;
import tfagaming.projects.minecraft.homestead.models.RegionInvite;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;

import java.util.ArrayList;
import java.util.List;

public class AcceptInviteSubCmd extends SubCommandBuilder {
	public AcceptInviteSubCmd() {
		super("accept");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/region accept [region]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			reply(player, "accept_invite.0", getUsage());

			return true;
		}

		String regionName = args[0];
		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			reply(player, "accept_invite.1", regionName);
			return true;
		}

		if (MemberManager.isMemberOfRegion(region, player)) {
			reply(player, "accept_invite.2");
			return true;
		}

		if (!InviteManager.isInvited(region, player)) {
			reply(player, "accept_invite.3");
			return true;
		}

		if (BanManager.isBanned(region, player)) {
			reply(player, "accept_invite.4");
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			reply(player, "accept_invite.5");
			return true;
		}

		MemberManager.addMemberToRegion(player, region);
		LogManager.addLog(region, player, LogManager.PredefinedLog.JOIN_REGION);

		reply(player, "accept_invite.6", regionName);

		Homestead.callEvent(new PlayerJoinRegionEvent(region, player));

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