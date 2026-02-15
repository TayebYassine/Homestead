package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

public class HomeSubCmd extends LegacySubCommandBuilder {
	public HomeSubCmd() {
		super("home");
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

		if (region.getLocation() == null) {
			Messages.send(player, 71, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		if (!PlayerUtils.isOperator(player)
				&& !region.isOwner(player)
				&& !(PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN, true)
				&& PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true))) {
			Messages.send(player, 45, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		new DelayedTeleport(player, region.getLocation().getBukkitLocation());

		return true;
	}
}
