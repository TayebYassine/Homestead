package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.PlayerFlags;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.DelayedTeleport;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

import java.util.List;

public class HomeSubCmd extends SubCommandBuilder {
	public HomeSubCmd() {
		super("home");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.teleport"
		));
		setUsage("/hs home");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.home.0");
			return true;
		}

		SeLocation loc = region.getLocation();

		if (loc == null) {
			Messages.send(player, "commands.home.1");
			return true;
		}

		if (!PlayerUtility.isOperator(player)
				&& !region.isOwner(player)
				&& !(PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN, true)
				&& PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true))) {
			Messages.send(player, "commands.home.2");
			return true;
		}

		new DelayedTeleport(player, loc.toBukkit());

		return true;
	}
}
