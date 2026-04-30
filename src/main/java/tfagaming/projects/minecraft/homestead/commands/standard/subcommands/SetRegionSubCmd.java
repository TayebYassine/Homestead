package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionDescriptionUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionDisplaynameUpdateEvent;
import tfagaming.projects.minecraft.homestead.api.events.RegionLocationUpdateEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.MiscellaneousSettings;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIcon;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
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
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length < 1) {
			Region region = TargetRegionSession.getRegion(player);

			if (region == null) {
				Messages.send(player, 4);
				return true;
			}

			new MiscellaneousSettings(player, region);

			return true;
		}

		String setType = args[0].toLowerCase();

		switch (setType) {
			case "displayname": {
				if (args.length < 2) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE)) {
					Cooldown.sendCooldownMessage(player);
					return true;
				}

				List<String> regionDisplayNameList = Arrays.asList(args).subList(1, args.length);
				String regionDisplayName = String.join(" ", regionDisplayNameList);

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.RENAME_REGION)) {
					return true;
				}

				if (!StringUtils.isValidRegionDisplayName(regionDisplayName)) {
					Messages.send(player, 14);
					return true;
				}

				if (region.getDisplayName() != null && region.getDisplayName().equals(regionDisplayName)) {
					Messages.send(player, 11);
					return true;
				}

				if (ColorTranslator.containsMiniMessageTag(regionDisplayName)) {
					Messages.send(player, 30);
					return true;
				}

				final String oldDisplayName = region.getDisplayName() == null ? Formatter.getNone() : region.getDisplayName();

				Cooldown.startCooldown(player, Cooldown.Type.REGION_RENAME_CHANGE);

				region.setDisplayName(regionDisplayName);

				Messages.send(player, 15, new Placeholder()
						.add("{olddisplayname}", oldDisplayName)
						.add("{newdisplayname}", regionDisplayName)
				);

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DISPLAYNAME, regionDisplayName);

				Homestead.callEvent(new RegionDisplaynameUpdateEvent(region, oldDisplayName, regionDisplayName));

				break;
			}
			case "description": {
				if (args.length < 2) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE)) {
					Cooldown.sendCooldownMessage(player);
					return true;
				}

				List<String> descriptionList = Arrays.asList(args).subList(1, args.length);
				String description = String.join(" ", descriptionList);

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
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

				if (ColorTranslator.containsMiniMessageTag(description)) {
					Messages.send(player, 30);
					return true;
				}

				final String oldDescription = region.getDescription();

				Cooldown.startCooldown(player, Cooldown.Type.REGION_DESCRIPTION_CHANGE);

				region.setDescription(description);

				Messages.send(player, 17, new Placeholder()
						.add("{olddescription}", oldDescription)
						.add("{newdescription}", region.getDescription())
				);

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_DESCRIPTION, description);

				Homestead.callEvent(new RegionDescriptionUpdateEvent(region, oldDescription, description));

				break;
			}
			case "mapcolor": {
				if (!player.hasPermission("homestead.region.dynamicmaps.color")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 2) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
					Cooldown.sendCooldownMessage(player);
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

				Cooldown.startCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE);

				region.setMapColor(color);

				Messages.send(player, 19, new Placeholder()
						.add("{oldcolor}", MapColor.toString(oldColor))
						.add("{newcolor}", MapColor.toString(region.getMapColor()))
				);

				break;
			}
			case "spawn": {
				if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE)) {
					Cooldown.sendCooldownMessage(player);
					return true;
				}

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.SET_SPAWN)) {
					return true;
				}

				Location location = player.getLocation();

				Chunk chunk = location.getChunk();

				if (!ChunkManager.isChunkClaimedByRegion(region, chunk)) {
					Messages.send(player, 142);
					return true;
				}

				Cooldown.startCooldown(player, Cooldown.Type.REGION_SPAWN_CHANGE);

				final Location oldLocation = region.getLocation() == null ? null : region.getLocation().toBukkit();

				region.setLocation(location);

				Messages.send(player, 72, new Placeholder()
						.add("{region}", region.getName())
						.add("{location}", Formatter.getLocation(location))
				);

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_REGION_SPAWN);

				Homestead.callEvent(new RegionLocationUpdateEvent(region, oldLocation, location));

				break;
			}
			case "icon": {
				if (!player.hasPermission("homestead.region.dynamicmaps.icon")) {
					Messages.send(player, 8);
					return true;
				}

				if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_DYNAMIC_MAP_SETTINGS_CHANGE)) {
					Cooldown.sendCooldownMessage(player);
					return true;
				}

				if (args.length < 2) {
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

				String iconInput = args[1];

				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (iconInput.equalsIgnoreCase("none") || iconInput.equalsIgnoreCase("default")) {
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

				break;
			}
			case "tax": {
				if (args.length < 2) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				if (!Homestead.VAULT.isEconomyReady()) {
					Messages.send(player, 69);

					Logger.warning(Logger.PredefinedMessages.ECONOMY_INTEGRATION_DISABLED.getMessage());

					return true;
				}

				boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled");

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

				double minTax = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("taxes.min-tax");
				double maxTax = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("taxes.max-tax");

				if (taxAmount <= minTax || taxAmount > maxTax) {
					Messages.send(player, 104, new Placeholder()
							.add("{min}", Formatter.getBalance(minTax))
							.add("{max}", Formatter.getBalance(maxTax))
					);

					return true;
				}

				region.setTaxes(taxAmount);

				Messages.send(player, 103, new Placeholder()
						.add("{region}", region.getName())
						.add("{tax-amount}", Formatter.getBalance(taxAmount))
				);

				break;
			}
			case "target": {
				if (args.length < 2) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String regionName = args[1];

				Region region = RegionManager.findRegion(regionName);

				if (region == null) {
					Messages.send(player, 9);
					return true;
				}

				if (!PlayerUtility.isOperator(player)
						&& !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player))) {
					Messages.send(player, 10);
					return true;
				}

				if (TargetRegionSession.hasSession(player)
						&& TargetRegionSession.getRegion(player).getUniqueId() == region.getUniqueId()) {
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

		if (args.length == 1)
			suggestions.addAll(
					List.of("displayname", "target", "description", "mapcolor", "spawn", "icon", "tax"));
		else if (args.length == 2 && args[0].equalsIgnoreCase("target")) {
			if (PlayerUtility.isOperator(player)) {
				suggestions.addAll(RegionManager.getRegionNames());
			} else {
				suggestions.addAll(
						RegionManager.getRegionsOwnedByPlayer(player).stream().map(Region::getName).toList());
				suggestions.addAll(
						RegionManager.getRegionsHasPlayerAsMember(player).stream().map(Region::getName).toList());
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("mapcolor"))
			suggestions.addAll(MapColor.getAll());
		else if (args.length == 2 && args[0].equalsIgnoreCase("icon")) {
			suggestions.addAll(RegionIcon.getAllIcons());
			suggestions.add("Default");
		}

		return suggestions;
	}
}
