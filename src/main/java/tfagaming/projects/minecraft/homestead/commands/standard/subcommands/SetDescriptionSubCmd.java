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
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
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
		setUsage("/hs setdescription [description]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.setdescription.0");
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, "commands.setdescription.1");
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
			return true;
		}

		if (!StringUtils.isValidRegionDescription(description)) {
			Messages.send(player, "commands.setdescription.3");
			return true;
		}

		final String oldDescription = region.getDescription();

		if (oldDescription != null && oldDescription.equals(description)) {
			Messages.send(player, "commands.setdescription.4");
			return true;
		}

		if (ColorTranslator.containsMiniMessageTag(description)) {
			Messages.send(player, "commands.setdescription.5");
			return true;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE);

		region.setDescription(description);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DESCRIPTION, description);

		Messages.send(player, "commands.setdescription.6");

		Homestead.callEvent(new RegionDescriptionUpdateEvent(region, oldDescription == null ? Formatter.getNone() : oldDescription, description));

		return true;
	}
}
