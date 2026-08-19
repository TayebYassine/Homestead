package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.InvitePlayerEvent;
import me.tayebyassine.homestead.api.events.PlayerJoinRegionEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.InviteManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

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
		setUsage("/hs trust [player]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.trust.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.trust.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.TRUST_PLAYERS)) {
			return true;
		}

		String targetName = args[0];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Messages.send(player, "commands.trust.2", targetName);
			return true;
		}

		if (BanManager.isBanned(region, target)) {
			Messages.send(player, "commands.trust.3");
			return true;
		}

		if (MemberManager.isMemberOfRegion(region, target)) {
			Messages.send(player, "commands.trust.4");
			return true;
		}

		if (InviteManager.isInvited(region, target)) {
			Messages.send(player, "commands.trust.5");
			return true;
		}

		if (region.isOwner(target)) {
			Messages.send(player, "commands.trust.6");
			return true;
		}

		SeRent rent = region.getRent();

		if (rent != null && rent.getRenterId().equals(target.getUniqueId())) {
			Messages.send(player, "commands.trust.7");
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			Messages.send(player, "commands.trust.8");
			return true;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
			MemberManager.addMemberToRegion(target, region);

			LogManager.addLog(region, target, LogManager.PredefinedLog.JOIN_REGION);

			Messages.send(player, "commands.trust.9");

			Homestead.callEvent(new PlayerJoinRegionEvent(region, target));
		} else {
			InviteManager.invitePlayer(region, target);

			LogManager.addLog(region, player, LogManager.PredefinedLog.INVITE_PLAYER, target.getName());

			Messages.send(player, "commands.trust.10");

			if (target.isOnline()) {
				Messages.send(target.getPlayer(), "commands.trust.11");
			}

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
