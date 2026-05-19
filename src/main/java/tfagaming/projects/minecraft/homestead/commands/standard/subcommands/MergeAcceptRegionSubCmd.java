package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

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

import java.util.List;

public class MergeAcceptRegionSubCmd extends SubCommandBuilder {
	public MergeAcceptRegionSubCmd() {
		super("mergeaccept");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.merge"
		));
		setUsage("/hs mergeaccept");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.mergeaccept.0");
			return true;
		}

		if (!MergeRegionSession.isToHaveRequest(region)) {
			Messages.send(player, "commands.mergeaccept.1");
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, "commands.mergeaccept.2");
			return true;
		}

		Region from = RegionManager.findRegion(MergeRegionSession.getFrom(region));

		if (from == null) {
			Messages.send(player, "commands.mergeaccept.3");
			return true;
		}

		RegionManager.mergeRegions(from, region);

		Messages.send(player, "commands.mergeaccept.4");

		return true;
	}
}
