package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionNameUpdateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.List;

public class RenameRegionSubCmd extends SubCommandBuilder {
	public RenameRegionSubCmd() {
		super("rename");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.name"
		));
		setUsage("/hs rename [new-name]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.rename.0");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.rename.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.RENAME_REGION)) {
			return true;
		}

		String regionName = args[0];

		if (!StringUtils.isValidRegionName(regionName)) {
			Messages.send(player, "commands.rename.3");
			return true;
		}

		if (regionName.equalsIgnoreCase(region.getName())) {
			Messages.send(player, "commands.rename.4");
			return true;
		}

		if (RegionManager.isNameUsed(regionName)) {
			Messages.send(player, "commands.rename.5");
			return true;
		}

		if (ColorTranslator.containsMiniMessageTag(regionName)) {
			Messages.send(player, "commands.rename.6");
			return true;
		}

		final String oldName = region.getName();

		Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);

		RegionManager.renameRegion(region, regionName);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_NAME, regionName);

		Messages.send(player, "commands.rename.7", oldName, regionName);

		Homestead.callEvent(new RegionNameUpdateEvent(region, oldName, regionName));

		return true;
	}
}
