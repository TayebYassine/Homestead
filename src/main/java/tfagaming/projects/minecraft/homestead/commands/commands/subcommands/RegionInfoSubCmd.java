package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionInfoMenu;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionInfoSubCmd extends SubCommandBuilder {
	public RegionInfoSubCmd() {
		super("info");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (args.length > 1) {
			String regionName = args[1];

			Region region = RegionsManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return false;
			}

			new RegionInfoMenu(player, region, () -> {
				player.closeInventory();
			});
		} else {
			Region region = ChunksManager.getRegionOwnsTheChunk(player.getLocation().getChunk());

			if (region == null) {
				Messages.send(player, 4);
				return true;
			}

			new RegionInfoMenu(player, region, () -> {
				player.closeInventory();
			});
		}

		return true;
	}
}
