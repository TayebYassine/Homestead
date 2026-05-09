package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionBannedPlayers;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class BanlistSubCmd extends SubCommandBuilder {
	public BanlistSubCmd() {
		super("banlist");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/region banlist");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "banlist.0");
			return true;
		}

		if (args.length == 1 && args[0].equals("gui")) {
			new RegionBannedPlayers(player, region);
			return true;
		}

		reply(player, "banlist.1", region.getName(), BanManager.getBanCount(region));

		return true;
	}
}
