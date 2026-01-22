package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.mergingregion.MergeRegionSession;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class MergeRegionSubCmd extends SubCommandBuilder {
	public MergeRegionSubCmd() {
		super("merge");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			PlayerUtils.sendMessage(player, 4);
			return true;
		}

		if (args.length < 2) {
			PlayerUtils.sendMessage(player, 0);
			return true;
		}

		String regionName = args[1];

		Region targetRegion = RegionsManager.findRegion(regionName);

		if (targetRegion == null) {
			PlayerUtils.sendMessage(player, 9);
			return false;
		}

		if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
			PlayerUtils.sendMessage(player, 30);
			return false;
		}

		if (region.getUniqueId().equals(targetRegion.getUniqueId())) {
			PlayerUtils.sendMessage(player, 176);
			return false;
		}

		if (targetRegion.getOwner() != null && !targetRegion.getOwner().isOnline()) {
			PlayerUtils.sendMessage(player, 179);
			return false;
		}

		if (MergeRegionSession.isFromHaveRequest(region)) {
			PlayerUtils.sendMessage(player, 177);
			return false;
		}

		if (MergeRegionSession.isToHaveRequest(targetRegion)) {
			PlayerUtils.sendMessage(player, 178);
			return false;
		}

		MergeRegionSession.newMergeRequest(region, targetRegion);

		PlayerUtils.sendMessage(player, 180);

		if (targetRegion.getOwner() != null && targetRegion.getOwner().isOnline()) {
			Map<String, String> replacements = new HashMap<>();
			replacements.put("{region}", region.getName());
			replacements.put("{targetregion}", targetRegion.getName());
			replacements.put("{player}", player.getName());

			PlayerUtils.sendMessage((Player) targetRegion.getOwner(), 181, replacements);
		}

		return true;
	}
}
