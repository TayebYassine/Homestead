package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIconTools;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetRegionSubCmd extends SubCommandBuilder {
	public SetRegionSubCmd() {
		super("set");
		setUsage("/region set [setting] [value]");
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

		String setType = args[0].toLowerCase();

		switch (setType) {
			case "displayname": {
				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				List<String> regionDisplayNameList = Arrays.asList(args).subList(2, args.length);
				String regionDisplayName = String.join(" ", regionDisplayNameList);

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.RENAME_REGION)) {
					return true;
				}

				if (!StringUtils.isValidRegionDisplayName(regionDisplayName)) {
					Messages.send(player, 14);
					return true;
				}

				if (region.getDisplayName().equals(regionDisplayName)) {
					Messages.send(player, 11);
					return true;
				}

				final String oldDisplayName = region.getDisplayName();

				region.setDisplayName(regionDisplayName);

				Messages.send(player, 15, new Placeholder()
						.add("{olddisplayname}", oldDisplayName)
						.add("{newdisplayname}", region.getDisplayName())
				);

				break;
			}
			case "description": {
				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				List<String> descriptionList = Arrays.asList(args).subList(2, args.length);
				String description = String.join(" ", descriptionList);

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.SET_DESCRIPTION)) {
					return true;
				}

				if (!StringUtils.isValidRegionDescription(description)) {
					Messages.send(player, 16);
					return true;
				}

				if (region.getDescription().equals(description)) {
					Messages.send(player, 11);
					return true;
				}

				final String oldDescription = region.getDescription();

				region.setDescription(description);

				Messages.send(player, 17, new Placeholder()
						.add("{olddescription}", oldDescription)
						.add("{newdescription}", region.getDescription())
				);

				break;
			}
			case "mapcolor": {
				if (!player.hasPermission("homestead.region.dynamicmaps.color")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String colorInput = args[1].toLowerCase();

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

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

				region.setMapColor(color);

				Messages.send(player, 19, new Placeholder()
						.add("{oldcolor}", MapColor.convertToColoredStringWithColorName(oldColor))
						.add("{newcolor}", MapColor.convertToColoredStringWithColorName(region.getMapColor()))
				);

				break;
			}
			case "spawn": {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.SET_SPAWN)) {
					return true;
				}

				Location location = player.getLocation();

				Chunk chunk = location.getChunk();

				if (ChunksManager.getRegionOwnsTheChunk(chunk) == null || !ChunksManager.getRegionOwnsTheChunk(chunk).getUniqueId().equals(region.getUniqueId())) {
					Messages.send(player, 142);
					return true;
				}

				region.setLocation(location);

				Messages.send(player, 72, new Placeholder()
						.add("{region}", region.getName())
						.add("{location}", Formatters.formatLocation(location))
				);

				// TODO Fix this
				// RegionsManager.addNewLog(region.getUniqueId(), 1, replacements);

				break;
			}
			case "icon": {
				if (!player.hasPermission("homestead.region.dynamicmaps.icon")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				boolean isEnabled = Homestead.config.getBoolean("dynamic-maps.icons.enabled");

				if (!isEnabled) {
					Messages.send(player, 105);

					return true;
				}

				String iconInput = args[1];

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (iconInput.equalsIgnoreCase("none") || iconInput.equalsIgnoreCase("default")) {
					region.setIcon(iconInput);

					Messages.send(player, 100, new Placeholder()
							.add("{region}", region.getName())
					);

					return true;
				}

				if (!RegionIconTools.isValidIcon(iconInput)) {
					Messages.send(player, 99);

					return true;
				}

				region.setIcon(iconInput);

				Messages.send(player, 100, new Placeholder()
						.add("{region}", region.getName())
				);

				break;
			}
			case "tax": {
				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				if (!Homestead.vault.isEconomyReady()) {
					Messages.send(player, 69);
					Logger.warning("The player \"" + player.getName() + "\" (UUID: " + player.getUniqueId()
							+ ") executed a command that requires economy implementation, but it's disabled.");
					Logger.warning(
							"The execution has been ignored, you may resolve this issue by installing a plugin that implements economy on the server.");

					return true;
				}

				boolean isEnabled = Homestead.config.getBoolean("taxes.enabled");

				if (!isEnabled) {
					Messages.send(player, 105);

					return true;
				}

				String taxInput = args[1];

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!NumberUtils.isValidDouble(taxInput)) {
					Messages.send(player, 102);

					return true;
				}

				double taxAmount = Double.parseDouble(taxInput);

				double minTax = Homestead.config.getDouble("taxes.min-tax");
				double maxTax = Homestead.config.getDouble("taxes.max-tax");

				if (taxAmount <= minTax || taxAmount > maxTax) {
					Messages.send(player, 104, new Placeholder()
							.add("{min}", Formatters.getBalance(minTax))
							.add("{max}", Formatters.getBalance(maxTax))
					);

					return true;
				}

				region.setTaxesAmount(taxAmount);

				Messages.send(player, 103, new Placeholder()
						.add("{region}", region.getName())
						.add("{tax-amount}", Formatters.getBalance(taxAmount))
				);

				break;
			}
			case "target": {
				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String regionName = args[1];

				Region region = RegionsManager.findRegion(regionName);

				if (region == null) {
					Messages.send(player, 9);
					return true;
				}

				if (!PlayerUtils.isOperator(player)
						&& !(region.isOwner(player) || region.isPlayerMember(player))) {
					Messages.send(player, 10);
					return true;
				}

				if (TargetRegionSession.hasSession(player)
						&& TargetRegionSession.getRegion(player).getUniqueId().equals(region.getUniqueId())) {
					Messages.send(player, 11);
					return true;
				}

				TargetRegionSession.newSession(player, region);

				Messages.send(player, 12, new Placeholder()
						.add("{region}", region.getName())
				);

				break;
			}
			default: {
				Messages.send(player, 0, new Placeholder()
						.add("{usage}", getUsage())
				);
				break;
			}
		}

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
