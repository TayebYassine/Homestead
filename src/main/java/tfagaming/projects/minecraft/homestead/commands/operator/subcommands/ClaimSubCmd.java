package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class ClaimSubCmd extends SubCommandBuilder {
	public ClaimSubCmd() {
		super("claim");
		setUsage("/hsadmin claim [region] [location] [radius]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return false;
		}

		if (args.length < 2) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String regionName = args[0];
		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return true;
		}

		World world;
		int centreChunkX, centreChunkZ;
		int radius = 1;

		if (args[1].equalsIgnoreCase("here")) {
			Location loc = player.getLocation();
			world = loc.getWorld();
			centreChunkX = loc.getChunk().getX();
			centreChunkZ = loc.getChunk().getZ();

			if (args.length >= 3) {
				try {
					radius = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					Messages.send(player, 185);
					return true;
				}

				if (radius < 1 || radius > 20) {
					Messages.send(player, 189);
					return true;
				}
			}
		} else {
			if (args.length < 3) {
				Messages.send(player, 0, new Placeholder()
						.add("{usage}", getUsage())
				);
				return true;
			}

			world = player.getWorld();

			try {
				centreChunkX = Integer.parseInt(args[1]);
				centreChunkZ = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				Messages.send(player, 184);
				return true;
			}

			if (args.length >= 4) {
				try {
					radius = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					Messages.send(player, 185);
					return true;
				}

				if (radius < 1 || radius > 20) {
					Messages.send(player, 189);
					return true;
				}
			}
		}

		if (world == null) {
			Messages.send(player, 188);
			return true;
		}

		List<Chunk> toClaim = new ArrayList<>();
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

				if (!ChunksManager.isChunkClaimed(chunk)) {
					toClaim.add(chunk);
				}
			}
		}

		if (toClaim.isEmpty()) {
			Messages.send(player, 186);
			return true;
		}

		int success = 0;
		for (Chunk chunk : toClaim) {
			ChunksManager.Error err = ChunksManager.claimChunk(region.getUniqueId(), chunk);
			if (err == null) success++;
		}

		Messages.send(player, 187, new Placeholder()
				.add("{region}", region.getName())
				.add("{chunks}", success)
				.add("{total}", toClaim.size())
		);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(
					RegionsManager.getAll().stream()
							.map(Region::getName)
							.toList()
			);
		} else if (args.length == 2) {
			suggestions.add("here");
			Location loc = player.getLocation();
			suggestions.add(String.valueOf(loc.getChunk().getX()));
		} else if (args.length == 3 && !args[1].equalsIgnoreCase("here")) {
			Location loc = player.getLocation();
			suggestions.add(String.valueOf(loc.getChunk().getZ()));
		} else if ((args.length == 3 && args[1].equalsIgnoreCase("here")) || args.length == 4) {
			suggestions.addAll(List.of("1", "2", "3", "4", "5"));
		}

		return suggestions;
	}
}