package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDescriptionUpdateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.Arrays;
import java.util.List;

public class SetDescriptionSubCmd extends SubCommandBuilder {
	public SetDescriptionSubCmd() {
		super("setdescription");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.description"
		));
		setUsage("/region setdescription [description]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "setdescription.0");
			return true;
		}

		if (args.length < 1) {
			reply(player, "setdescription.1");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		List<String> descriptionList = Arrays.asList(args).subList(0, args.length);
		String description = String.join(" ", descriptionList);

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.SET_DESCRIPTION)) {
			reply(player, "setdescription.2");
			return true;
		}

		if (!StringUtils.isValidRegionDescription(description)) {
			reply(player, "setdescription.3");
			return true;
		}

		if (region.getDescription() != null && region.getDescription().equals(description)) {
			reply(player, "setdescription.4");
			return true;
		}

		if (ColorTranslator.containsMiniMessageTag(description)) {
			reply(player, "setdescription.5");
			return true;
		}

		final String oldDescription = region.getDescription();

		Cooldown.startCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE);

		region.setDescription(description);

		reply(player, "setdescription.6");

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DESCRIPTION, description);

		Homestead.callEvent(new RegionDescriptionUpdateEvent(region, oldDescription, description));

		return true;
	}
}
