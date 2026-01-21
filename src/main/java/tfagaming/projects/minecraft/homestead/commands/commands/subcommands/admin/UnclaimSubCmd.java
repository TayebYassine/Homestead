package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnclaimSubCmd extends SubCommandBuilder {
	public UnclaimSubCmd() {
		super("unclaim");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (args.length < 3) {
			PlayerUtils.sendMessage(player, 0);
			return true;
		}

		String regionName = args[1];

		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			PlayerUtils.sendMessage(player, 9);
			return true;
		}

		World world;
		int centreChunkX, centreChunkZ;
		int radius = 1;

		if (args[2].equalsIgnoreCase("here")) {
			Location loc = player.getLocation();
			world = loc.getWorld();
			centreChunkX = loc.getChunk().getX();
			centreChunkZ = loc.getChunk().getZ();

			if (args.length >= 4) {
				try {
					radius = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					PlayerUtils.sendMessage(player, 185);
					return true;
				}

				if (radius < 1 || radius > 20) {
					PlayerUtils.sendMessage(player, 189);
					return true;
				}
			}
		} else {
			if (args.length < 4) {
				PlayerUtils.sendMessage(player, 0);
				return true;
			}

			world = player.getWorld();

			try {
				centreChunkX = Integer.parseInt(args[2]);
				centreChunkZ = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				PlayerUtils.sendMessage(player, 184);
				return true;
			}

			if (args.length >= 5) {
				try {
					radius = Integer.parseInt(args[4]);
				} catch (NumberFormatException e) {
					PlayerUtils.sendMessage(player, 185);
					return true;
				}

				if (radius < 1 || radius > 20) {
					PlayerUtils.sendMessage(player, 189);
					return true;
				}
			}
		}

		if (world == null) {
			PlayerUtils.sendMessage(player, 188);
			return true;
		}

		List<Chunk> toUnclaim = new ArrayList<>();
		int minCX = centreChunkX - radius;
		int maxCX = centreChunkX + radius;
		int minCZ = centreChunkZ - radius;
		int maxCZ = centreChunkZ + radius;

		for (int cx = minCX; cx <= maxCX; cx++) {
			for (int cz = minCZ; cz <= maxCZ; cz++) {
				if (Math.abs(cx - centreChunkX) + Math.abs(cz - centreChunkZ) > radius) {
					continue;
				}

				if (!world.isChunkLoaded(cx, cz)) {
					continue;
				}

				Chunk chunk = world.getChunkAt(cx, cz);
				Region regionOwnsChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

				if (regionOwnsChunk != null && regionOwnsChunk.getUniqueId().equals(region.getUniqueId())) {
					toUnclaim.add(chunk);
				}
			}
		}

		if (toUnclaim.isEmpty()) {
			PlayerUtils.sendMessage(player, 190);
			return true;
		}

		int success = 0;
		for (Chunk chunk : toUnclaim) {
			ChunksManager.Error err = ChunksManager.forceUnclaimChunk(region.getUniqueId(), chunk);
			if (err == null) success++;
		}

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{region}", region.getName());
		replacements.put("{chunks}", String.valueOf(success));
		replacements.put("{total}", String.valueOf(toUnclaim.size()));

		PlayerUtils.sendMessage(player, 191, replacements);

		return true;
	}
}
