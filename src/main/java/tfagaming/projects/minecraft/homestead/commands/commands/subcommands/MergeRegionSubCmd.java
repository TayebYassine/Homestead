package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.mergingregion.MergeRegionSession;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

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
			Messages.send(player, 4);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0);
			return true;
		}

		String regionName = args[1];

		Region targetRegion = RegionsManager.findRegion(regionName);

		if (targetRegion == null) {
			Messages.send(player, 9);
			return false;
		}

		if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 30);
			return false;
		}

		if (region.getUniqueId().equals(targetRegion.getUniqueId())) {
			Messages.send(player, 176);
			return false;
		}

		if (targetRegion.getOwner() != null && !targetRegion.getOwner().isOnline()) {
			Messages.send(player, 179);
			return false;
		}

		if (MergeRegionSession.isFromHaveRequest(region)) {
			Messages.send(player, 177);
			return false;
		}

		if (MergeRegionSession.isToHaveRequest(targetRegion)) {
			Messages.send(player, 178);
			return false;
		}

		MergeRegionSession.newMergeRequest(region, targetRegion);

		Messages.send(player, 180);

		if (targetRegion.getOwner() != null && targetRegion.getOwner().isOnline()) {
			Messages.send((Player) targetRegion.getOwner(), 181, new Placeholder()
					.add("{region}", region.getName())
					.add("{targetregion}", targetRegion.getName())
					.add("{player}", player.getName())
			);
		}

		return true;
	}
}
