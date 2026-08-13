package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.gui.menus.MapColorMenu;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.plugins.MapColor;

import java.util.ArrayList;
import java.util.List;

public class SetMapColorSubCmd extends SubCommandBuilder {
	public SetMapColorSubCmd() {
		super("setmapcolor");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.map_color"
		));
		setUsage("/hs setmapcolor [color]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.setmapcolor.0");
			return true;
		}

		if (!Resources.<ConfigFile>get(ResourceType.Config).getBoolean("dynamic-maps.enabled")) {
			Messages.send(player, "commands.setmapcolor.1");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		if (args.length < 1) {
			new MapColorMenu(player, region);

			return true;
		}

		String colorInput = args[0].toLowerCase();

		if (!MapColor.getAll().contains(colorInput)) {
			Messages.send(player, "commands.setmapcolor.2");
			return true;
		}

		int color = MapColor.parseFromString(colorInput);

		if (region.getMapColor() == color) {
			Messages.send(player, "commands.setmapcolor.3");
			return true;
		}

		final int oldColor = region.getMapColor();

		Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

		region.setMapColor(color);

		Messages.send(player, "commands.setmapcolor.4", MapColor.toString(oldColor), MapColor.toString(color));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1)
			suggestions.addAll(MapColor.getAll());

		return suggestions;
	}
}
