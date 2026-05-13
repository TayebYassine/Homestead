package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDisplaynameUpdateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Arrays;
import java.util.List;

public class SetDisplayNameSubCmd extends SubCommandBuilder {
	public SetDisplayNameSubCmd() {
		super("setdisplayname");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.displayname"
		));
		setUsage("/region setdisplayname [name]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "setdisplayname.0");
			return true;
		}

		if (args.length < 1) {
			reply(player, "setdisplayname.1");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		List<String> regionDisplayNameList = Arrays.asList(args).subList(0, args.length);
		String regionDisplayName = String.join(" ", regionDisplayNameList);

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.RENAME_REGION)) {
			reply(player, "setdisplayname.2");
			return true;
		}

		if (!StringUtils.isValidRegionDisplayName(regionDisplayName)) {
			reply(player, "setdisplayname.3");
			return true;
		}

		if (region.getDisplayName() != null && region.getDisplayName().equals(regionDisplayName)) {
			reply(player, "setdisplayname.4");
			return true;
		}

		if (ColorTranslator.containsMiniMessageTag(regionDisplayName)) {
			reply(player, "setdisplayname.5");
			return true;
		}

		final String oldDisplayName = region.getDisplayName() == null ? Formatter.getNone() : region.getDisplayName();

		Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);

		region.setDisplayName(regionDisplayName);

		reply(player, "setdisplayname.6", oldDisplayName, regionDisplayName);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DISPLAYNAME, regionDisplayName);

		Homestead.callEvent(new RegionDisplaynameUpdateEvent(region, oldDisplayName, regionDisplayName));

		return true;
	}
}
