package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.gui.menus.MapIconMenu;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapIcon;

import java.util.ArrayList;
import java.util.List;

public class SetMapIconSubCmd extends SubCommandBuilder {
	public SetMapIconSubCmd() {
		super("setmapicon");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.map_icon"
		));
		setUsage("/region setmapicon [icon]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "setmapicon.0");
			return true;
		}

		if (!(Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.enabled") && Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.icons.enabled"))) {
			reply(player, "setmapicon.1");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		if (args.length < 1) {
			new MapIconMenu(player, region);

			return true;
		}

		String iconInput = args[0];

		if (iconInput.equals("Default")) {
			region.setMapIcon(iconInput);

			reply(player, "setmapicon.4");

			return true;
		}

		if (!MapIcon.isValidIcon(iconInput)) {
			reply(player, "setmapicon.2");
			return true;
		}

		if (region.getMapIcon() != null && region.getMapIcon().equals(iconInput)) {
			reply(player, "setmapicon.3");
			return true;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

		region.setMapIcon(iconInput);

		reply(player, "setmapicon.5");

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(MapIcon.getAllIcons());
			suggestions.add("Default");
		}

		return suggestions;
	}
}
