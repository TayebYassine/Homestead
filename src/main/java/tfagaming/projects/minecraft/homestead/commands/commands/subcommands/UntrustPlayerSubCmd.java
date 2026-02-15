package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class UntrustPlayerSubCmd extends SubCommandBuilder {
	public UntrustPlayerSubCmd() {
		super("untrust");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.players.untrust")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNTRUST_PLAYERS)) {
			return true;
		}

		String targetName = args[1];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Messages.send(player, 29, new Placeholder()
					.add("{playername}", targetName)
			);
			return true;
		}

		if (region.isPlayerInvited(target)) {
			region.removePlayerInvite(target);

			Messages.send(player, 37, new Placeholder()
					.add("{playername}", target.getName())
			);
		} else if (region.isPlayerMember(target)) {
			region.removeMember(target);

			Messages.send(player, 38, new Placeholder()
					.add("{region}", region.getName())
					.add("{player}", target.getName())
			);

			// TODO Fix this
			// RegionsManager.addNewLog(region.getUniqueId(), 3, replacements);
		} else {
			Messages.send(player, 39, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", target.getName())
			);
		}

		return true;
	}
}
