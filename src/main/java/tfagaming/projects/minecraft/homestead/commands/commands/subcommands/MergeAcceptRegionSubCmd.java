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

public class MergeAcceptRegionSubCmd extends SubCommandBuilder {
	public MergeAcceptRegionSubCmd() {
		super("mergeaccept");
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

		if (!MergeRegionSession.isToHaveRequest(region)) {
			PlayerUtils.sendMessage(player, 182);
			return true;
		}

		if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
			PlayerUtils.sendMessage(player, 30);
			return false;
		}

		Region from = RegionsManager.findRegion(MergeRegionSession.getFrom(region));

		if (from == null) {
			PlayerUtils.sendMessage(player, 182);
			return false;
		}

		RegionsManager.mergeRegions(from, region);

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{region}", from.getName());

		PlayerUtils.sendMessage(player, 183, replacements);

		return true;
	}
}
