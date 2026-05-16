package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.MergeRegionSession;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class MergeRegionSubCmd extends SubCommandBuilder {
	public MergeRegionSubCmd() {
		super("merge");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs merge [region]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.merge.0");
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, "commands.merge.1");
			return true;
		}

		String regionName = args[0];

		Region targetRegion = RegionManager.findRegion(regionName);

		if (targetRegion == null) {
			Messages.send(player, "commands.merge.2", regionName);
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, "commands.merge.3");
			return true;
		}

		if (region.getUniqueId() == targetRegion.getUniqueId()) {
			Messages.send(player, "commands.merge.4");
			return true;
		}

		OfflinePlayer targetOfflineOwner = targetRegion.getOwner();
		Player targetOwner = targetOfflineOwner != null && targetOfflineOwner.isOnline() ? targetOfflineOwner.getPlayer() : null;

		if (targetOwner == null) {
			Messages.send(player, "commands.merge.5");
			return true;
		}

		if (MergeRegionSession.isFromHaveRequest(region)) {
			Messages.send(player, "commands.merge.6");
			return true;
		}

		if (MergeRegionSession.isToHaveRequest(targetRegion)) {
			Messages.send(player, "commands.merge.6");
			return true;
		}

		MergeRegionSession.newMergeRequest(region, targetRegion);

		Messages.send(player, "commands.merge.7");
		Messages.send(targetOwner, "commands.merge.8", player.getName(), region.getName());

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(RegionManager.getRegionNames());
		}

		return suggestions;
	}
}
