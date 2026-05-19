package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionBannedPlayers;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionClaimedChunks;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class ClaimlistSubCmd extends SubCommandBuilder {
	public ClaimlistSubCmd() {
		super("claimlist");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs claimlist");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.claimlist.0");
			return true;
		}

		if (args.length == 1 && args[0].equals("gui")) {
			new RegionClaimedChunks(player, region);
			return true;
		}

		Messages.send(player, "commands.claimlist.2", region.getName(), ChunkManager.getChunkCount(region));

		return true;
	}
}
