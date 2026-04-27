package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.InviteManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

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
		Region region = RegionManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return true;
		}

		if (!InviteManager.isInvited(region, player) || MemberManager.isMemberOfRegion(region, player)) {
			Messages.send(player, 45, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		RegionBan ban = BanManager.getBannedPlayer(region, player);

		if (ban != null) {
			Messages.send(player, 28, new Placeholder()
					.add("{region}", region.getName())
					.add("{ban-reason}", ban.getReason())
			);
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			Messages.send(player, 116);
			return true;
		}

		InviteManager.deleteInvitesOfPlayer(region, player);
		MemberManager.addMemberToRegion(player, region);

		Messages.send(player, 46, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", player.getName())
		);

		OfflinePlayer owner = region.getOwner();

		if (owner != null && owner.isOnline()) {
			Messages.send(owner.getPlayer(), 138, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", player.getName())
			);
		}

		RegionTrustPlayerEvent _event = new RegionTrustPlayerEvent(region, player, player);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

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