package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsWithWelcomeSigns;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

import java.util.ArrayList;
import java.util.List;

public class VisitRegionSubCmd extends SubCommandBuilder {
	public VisitRegionSubCmd() {
		super("visit");
		setUsage("/region visit [region/playername] (index)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (Resources.<RegionsFile>get(ResourceType.Regions).isWelcomeSignEnabled()) {
			if (args.length < 1) {
				new RegionsWithWelcomeSigns(player);

				return true;
			}

			String playerName = args[0];

			OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

			if (target == null) {
				Messages.send(player, 29, new Placeholder()
						.add("{playername}", playerName)
				);
				return true;
			}

			String indexInput = args.length >= 2 ? args[1] : "0";

			if (!NumberUtils.isValidInteger(indexInput)) {
				Messages.send(player, 137);
				return true;
			}

			int index = Integer.parseInt(indexInput);

			List<Region> regions = RegionManager.getRegionsOwnedByPlayer(target);
			List<Region> filteredRegions = new ArrayList<>();

			for (Region region : regions) {
				if (region.getWelcomeSign() != null) {
					filteredRegions.add(region);
				}
			}

			if (filteredRegions.isEmpty()) {
				Messages.send(player, 137);
				return true;
			}

			if (index < 0 || index > filteredRegions.size() - 1) {
				Messages.send(player, 137);
				return true;
			}

			new DelayedTeleport(player, filteredRegions.get(index).getWelcomeSign().bukkit());
		} else {
			if (args.length < 1) {
				Messages.send(player, 0, new Placeholder()
						.add("{usage}", getUsage())
				);
				return true;
			}

			if(!player.hasPermission("homestead.region.teleport")){
				Messages.send(player, 212);
				return true;
			}

			String regionName = args[0];

			Region region = RegionManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return true;
			}

			if (region.getLocation() == null) {
				Messages.send(player, 71, new Placeholder()
						.add("{region}", region.getName())
				);
				return true;
			}

			if (!PlayerUtils.isOperator(player)
					&& !region.isOwner(player)
					&& !(PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.TELEPORT_SPAWN, true)
					&& PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true)
					&& player.hasPermission("homestead.region.teleport"))) {
				Messages.send(player, 131, new Placeholder()
						.add("{region}", region.getName())
				);
				return true;
			}

			new DelayedTeleport(player, region.getLocation().bukkit());
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			if (Resources.<RegionsFile>get(ResourceType.Regions).isWelcomeSignEnabled()) {
				suggestions.addAll(RegionManager.getPlayersWithRegionsHasWelcomeSigns().stream().map(OfflinePlayer::getName).toList());
			} else {
				if (PlayerUtils.isOperator(player)) {
					suggestions.addAll(
							RegionManager.getAll().stream().map(Region::getName).toList());
				} else {
					suggestions.addAll(
							RegionManager.getPublicRegions().stream().map(Region::getName).toList());
				}
			}
		} else if (args.length == 2 && Resources.<RegionsFile>get(ResourceType.Regions).isWelcomeSignEnabled()) {
			for (int i = 0; i < RegionManager.getPlayersWithRegionsHasWelcomeSigns().size(); i++) {
				suggestions.add(String.valueOf(i));
			}
		}

		return suggestions;
	}
}
