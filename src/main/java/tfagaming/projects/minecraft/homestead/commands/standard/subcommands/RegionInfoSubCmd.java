package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionInfoMenu;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;

import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class RegionInfoSubCmd extends SubCommandBuilder {
	public RegionInfoSubCmd() {
		super("info");
		setUsage("/region info (region)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length > 0) {
			String regionName = args[0];

			Region region = RegionManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return true;
			}

			new RegionInfoMenu(player, region, player::closeInventory);
		} else {
			Region region = ChunkManager.getRegionOwnsTheChunk(player.getLocation().getChunk());

			if (region == null) {
				Messages.send(player, 4);
				return true;
			}

			new RegionInfoMenu(player, region, player::closeInventory);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(RegionManager.getAll().stream().map(Region::getName).toList());
		}

		return suggestions;
	}
}
