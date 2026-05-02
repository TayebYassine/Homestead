package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.gui.menus.MapColorMenu;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

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
		setUsage("/region setmapcolor [color]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (args.length < 1) {
			new MapColorMenu(player, region);

			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		String colorInput = args[0].toLowerCase();

		if (!MapColor.getAll().contains(colorInput)) {
			Messages.send(player, 18);
			return true;
		}

		int color = MapColor.parseFromString(colorInput);

		if (region.getMapColor() == color) {
			Messages.send(player, 11);
			return true;
		}

		final int oldColor = region.getMapColor();

		Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

		region.setMapColor(color);

		Messages.send(player, 19, new Placeholder()
				.add("{oldcolor}", MapColor.toString(oldColor))
				.add("{newcolor}", MapColor.toString(region.getMapColor()))
		);

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
