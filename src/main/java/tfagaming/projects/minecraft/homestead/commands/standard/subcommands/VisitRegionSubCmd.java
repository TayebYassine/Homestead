package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsWithWelcomeSignsMenu;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.teleportation.DelayedTeleport;

import java.util.ArrayList;
import java.util.List;

public class VisitRegionSubCmd extends LegacySubCommandBuilder {
	public VisitRegionSubCmd() {
		super("visit");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (Homestead.config.isWelcomeSignEnabled()) {
			if (args.length < 2) {
				new RegionsWithWelcomeSignsMenu(player);

				return true;
			}

			String playerName = args[1];

			OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

			if (target == null) {
				Messages.send(player, 29, new Placeholder()
						.add("{playername}", playerName)
				);
				return true;
			}

			String indexInput = args.length >= 3 ? args[2] : "0";

			if (!NumberUtils.isValidInteger(indexInput)) {
				Messages.send(player, 137);
				return true;
			}

			int index = Integer.parseInt(indexInput);

			List<Region> regions = RegionsManager.getRegionsOwnedByPlayer(target);
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

			new DelayedTeleport(player, filteredRegions.get(index).getWelcomeSign().getBukkitLocation());
		} else {
			if (args.length < 2) {
				Messages.send(player, 0);
				return true;
			}

			String regionName = args[1];

			Region region = RegionsManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return false;
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
					&& PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true))) {
				Messages.send(player, 131, new Placeholder()
						.add("{region}", region.getName())
				);
				return true;
			}

			new DelayedTeleport(player, region.getLocation().getBukkitLocation());
		}

		return true;
	}
}
