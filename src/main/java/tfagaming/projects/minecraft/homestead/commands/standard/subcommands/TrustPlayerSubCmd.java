package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.InvitePlayerEvent;
import tfagaming.projects.minecraft.homestead.api.events.PlayerJoinRegionEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class TrustPlayerSubCmd extends SubCommandBuilder {
	public TrustPlayerSubCmd() {
		super("trust");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.players.trust"
		));
		setUsage("/region trust [player]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			reply(player, "trust.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "trust.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.TRUST_PLAYERS)) {
			reply(player, "trust.12");
			return true;
		}

		String targetName = args[0];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			reply(player, "trust.2");
			return true;
		}

		if (BanManager.isBanned(region, target)) {
			reply(player, "trust.3");
			return true;
		}

		if (MemberManager.isMemberOfRegion(region, target)) {
			reply(player, "trust.4");
			return true;
		}

		if (InviteManager.isInvited(region, target)) {
			reply(player, "trust.5");
			return true;
		}

		if (region.isOwner(target)) {
			reply(player, "trust.6");
			return true;
		}

		SeRent rent = region.getRent();

		if (rent != null && rent.getRenterId().equals(target.getUniqueId())) {
			reply(player, "trust.7");
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			reply(player, "trust.8");
			return true;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
			MemberManager.addMemberToRegion(target, region);

			reply(player, "trust.9");

			LogManager.addLog(region, target, LogManager.PredefinedLog.JOIN_REGION);

			Homestead.callEvent(new PlayerJoinRegionEvent(region, target));
		} else {
			InviteManager.invitePlayer(region, target);

			reply(player, "trust.10");

			if (target.isOnline()) {
				reply(target.getPlayer(), "trust.11");
			}

			LogManager.addLog(region, player, LogManager.PredefinedLog.INVITE_PLAYER, target.getName());

			Homestead.callEvent(new InvitePlayerEvent(region, target));
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(Homestead.getInstance().getOfflinePlayerNamesSync());
		}

		return suggestions;
	}
}
