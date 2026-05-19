package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.DelayedTeleport;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

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
