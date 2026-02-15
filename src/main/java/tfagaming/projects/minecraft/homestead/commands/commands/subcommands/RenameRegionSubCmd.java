package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RenameRegionSubCmd extends SubCommandBuilder {
	public RenameRegionSubCmd() {
		super("rename");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
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
				RegionControlFlags.RENAME_REGION)) {
			return true;
		}

		String regionName = args[1];

		if (!StringUtils.isValidRegionName(regionName)) {
			Messages.send(player, 1);
			return true;
		}

		if (regionName.equalsIgnoreCase(region.getName())) {
			Messages.send(player, 11);
			return true;
		}

		if (RegionsManager.isNameUsed(regionName)) {
			Messages.send(player, 2);
			return true;
		}

		final String oldName = region.getName();

		region.setName(regionName);

		Messages.send(player, 13, new Placeholder()
				.add("{oldname}", oldName)
				.add("{newname}", regionName)
		);

		// TODO Fix this
		// RegionsManager.addNewLog(region.getUniqueId(), 0, replacements);

		return true;
	}
}
