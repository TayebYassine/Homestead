package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class CreateRegionSubCmd extends SubCommandBuilder {
	public CreateRegionSubCmd() {
		super("create");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.create")) {
			PlayerUtils.sendMessage(player, 8);
			return true;
		}

		if (args.length < 2) {
			PlayerUtils.sendMessage(player, 0);
			return true;
		}

		String regionName = args[1];

		if (!StringUtils.isValidRegionName(regionName)) {
			PlayerUtils.sendMessage(player, 1);
			return true;
		}

		if (RegionsManager.isNameUsed(regionName)) {
			PlayerUtils.sendMessage(player, 2);
			return true;
		}

		if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
			PlayerUtils.sendMessage(player, 116);
			return true;
		}

		Region region = RegionsManager.createRegion(regionName, player);

		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("{name}", region.getDisplayName());

		PlayerUtils.sendMessage(player, 3, replacements);

		TargetRegionSession.newSession(player, region);

		return true;
	}
}
