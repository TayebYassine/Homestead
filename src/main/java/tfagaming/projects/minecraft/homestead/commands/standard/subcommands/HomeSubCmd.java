package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.DelayedTeleport;

public class HomeSubCmd extends SubCommandBuilder {
	public HomeSubCmd() {
		super("home");
		setUsage("/region home");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.teleport")) {
			Messages.send(player, 212);
			return true;
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

		if (!PlayerUtility.isOperator(player)
				&& !region.isOwner(player)
				&& !(PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN, true)
				&& PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true)
				&& player.hasPermission("homestead.region.teleport"))) {
			Messages.send(player, 45, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		new DelayedTeleport(player, region.getLocation().toBukkit());

		return true;
	}
}
