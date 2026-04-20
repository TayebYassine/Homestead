package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.sessions.MergeRegionSession;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

public class MergeAcceptRegionSubCmd extends SubCommandBuilder {
	public MergeAcceptRegionSubCmd() {
		super("mergeaccept");
		setUsage("/region mergeaccept");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!MergeRegionSession.isToHaveRequest(region)) {
			Messages.send(player, 182);
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 30);
			return true;
		}

		Region from = RegionManager.findRegion(MergeRegionSession.getFrom(region));

		if (from == null) {
			Messages.send(player, 182);
			return true;
		}

		RegionManager.mergeRegions(from, region);

		Messages.send(player, 183, new Placeholder()
				.add("{region}", from.getName())
		);

		return true;
	}
}
