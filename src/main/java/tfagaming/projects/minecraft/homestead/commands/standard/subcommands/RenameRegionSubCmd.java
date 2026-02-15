package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

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

import java.util.ArrayList;
import java.util.List;

public class RenameRegionSubCmd extends SubCommandBuilder {
	public RenameRegionSubCmd() {
		super("rename");
		setUsage("/region rename [new-name]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
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

		String regionName = args[0];

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

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 0) {

		}

		return suggestions;
	}
}
