package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;

public class CreateRegionSubCmd extends SubCommandBuilder {
	public CreateRegionSubCmd() {
		super("create");
		setUsage("/region create [name]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.create")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String regionName = args[0];

		if (!StringUtils.isValidRegionName(regionName)) {
			Messages.send(player, 1);
			return true;
		}

		if (RegionManager.isNameUsed(regionName)) {
			Messages.send(player, 2);
			return true;
		}

		if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
			Messages.send(player, 116);
			return true;
		}

		Region region = RegionManager.createRegion(regionName, player);

		Messages.send(player, 3, new Placeholder()
				.add("{name}", region.getName())
		);

		TargetRegionSession.newSession(player, region);

		return true;
	}
}
