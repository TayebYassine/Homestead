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
			Messages.send(player, 4);
			return true;
		}

		if (!MergeRegionSession.isToHaveRequest(region)) {
			Messages.send(player, 182);
			return true;
		}

		if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 30);
			return false;
		}

		Region from = RegionsManager.findRegion(MergeRegionSession.getFrom(region));

		if (from == null) {
			Messages.send(player, 182);
			return false;
		}

		RegionsManager.mergeRegions(from, region);

		Messages.send(player, 183, new Placeholder()
				.add("{region}", from.getName())
		);

		return true;
	}
}
