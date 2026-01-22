package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

import java.util.HashMap;
import java.util.Map;

public class HomeSubCmd extends SubCommandBuilder {
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
			PlayerUtils.sendMessage(player, 4);
			return true;
		}

		if (region.getLocation() == null) {
			Map<String, String> replacements = new HashMap<>();
			replacements.put("{region}", region.getName());

			PlayerUtils.sendMessage(player, 71, replacements);
			return true;
		}

		if (!PlayerUtils.isOperator(player)
				&& !region.isOwner(player)
				&& !(PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN, true)
				&& PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true))) {
			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{region}", region.getName());

			PlayerUtils.sendMessage(player, 45, replacements);
			return true;
		}

		new DelayedTeleport(player, region.getLocation().getBukkitLocation());

		return true;
	}
}
