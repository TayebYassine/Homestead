package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIcon;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

import java.util.ArrayList;
import java.util.List;

public class SetMapIconSubCmd extends SubCommandBuilder {
	public SetMapIconSubCmd() {
		super("setmapicon");
		setUsage("/region setmapicon [icon]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.dynamicmaps.icon")) {
			Messages.send(player, 8);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		boolean isEnabled = Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.icons.enabled");

		if (!isEnabled) {
			Messages.send(player, 105);

			return true;
		}

		String iconInput = args[0];

		if (iconInput.equals("None") || iconInput.equals("Default")) {
			region.setMapIcon(iconInput);

			Messages.send(player, 100, new Placeholder()
					.add("{region}", region.getName())
			);

			return true;
		}

		if (!RegionIcon.isValidIcon(iconInput)) {
			Messages.send(player, 99);

			return true;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

		region.setMapIcon(iconInput);

		Messages.send(player, 100, new Placeholder()
				.add("{region}", region.getName())
		);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(RegionIcon.getAllIcons());
			suggestions.add("Default");
			suggestions.add("None");
		}

		return suggestions;
	}
}
