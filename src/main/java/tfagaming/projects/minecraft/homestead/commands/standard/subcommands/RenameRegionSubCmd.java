package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionRenameEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RenameRegionSubCmd extends SubCommandBuilder {
	public RenameRegionSubCmd() {
		super("rename");
		setUsage("/region rename [new-name]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length < 1) {
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

		if (RegionManager.isNameUsed(regionName)) {
			Messages.send(player, 2);
			return true;
		}

		if (ColorTranslator.containsMiniMessageTag(regionName)) {
			Messages.send(player, 30);
			return true;
		}

		final String oldName = region.getName();

		region.setName(regionName);

		Messages.send(player, 13, new Placeholder()
				.add("{oldname}", oldName)
				.add("{newname}", regionName)
		);

		RegionManager.addNewLog(region.getUniqueId(), 0, new Placeholder()
				.add("{executor}", player.getName())
				.add("{newname}", regionName)
		);

		RegionRenameEvent _event = new RegionRenameEvent(region, player, oldName, regionName);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

		return true;
	}
}
