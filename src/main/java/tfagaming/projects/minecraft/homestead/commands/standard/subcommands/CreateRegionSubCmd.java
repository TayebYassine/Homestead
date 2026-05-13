package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionCreateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;

import java.util.List;

public class CreateRegionSubCmd extends SubCommandBuilder {
	public CreateRegionSubCmd() {
		super("create");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.create"
		));
		setUsage("/region create [name]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			reply(player, "create.0");
			return true;
		}

		String regionName = args[0];

		if (!StringUtils.isValidRegionName(regionName)) {
			reply(player, "create.1");
			return true;
		}

		if (RegionManager.isNameUsed(regionName)) {
			reply(player, "create.2");
			return true;
		}

		if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
			reply(player, "create.3");
			return true;
		}

		Region region = RegionManager.createRegion(regionName, player);

		reply(player, "create.4", regionName);

		TargetRegionSession.newSession(player, region);

		Homestead.callEvent(new RegionCreateEvent(region, player));

		return true;
	}
}
