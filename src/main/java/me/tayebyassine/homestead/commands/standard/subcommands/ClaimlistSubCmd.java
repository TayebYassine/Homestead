package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionBannedPlayers;
import me.tayebyassine.homestead.gui.menus.RegionClaimedChunks;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;

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
