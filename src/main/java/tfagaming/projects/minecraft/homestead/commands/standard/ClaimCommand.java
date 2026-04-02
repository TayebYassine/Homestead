package tfagaming.projects.minecraft.homestead.commands.standard;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldGuardAPI;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class ClaimCommand extends CommandBuilder {
	public ClaimCommand() {
		super("claim");
		setUsage("/claim");
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		Chunk chunk = player.getLocation().getChunk();

		if (ChunkManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, 20);
			return true;
		}

		boolean isWorldGuardProtectingRegionsEnabled =
				Resources.<ConfigFile>get(ResourceType.Config).getBoolean("worldguard.protect-existing-regions");

		if (isWorldGuardProtectingRegionsEnabled && WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
			Messages.send(player, 133);
			return true;
		}

		Region region = getOrCreateRegion(player);

		if (region == null) {
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(
				region.getUniqueId(),
				player,
				RegionControlFlags.CLAIM_CHUNKS)) {
			return true;
		}

		double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("chunk-price");

		if (chunkPrice > 0 && PlayerBank.get(region.getOwner()) < chunkPrice) {
			Messages.send(player, 200, new Placeholder()
					.add("{price}", Formatter.getBalance(chunkPrice))
					.add("{player}", region.getOwner().getName())
			);
			return true;
		}

		Region regionOwnsThisChunk = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (regionOwnsThisChunk != null) {
			Messages.send(player, 21, new Placeholder()
					.add("{region}", regionOwnsThisChunk.getName())
			);
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.CHUNKS_PER_REGION)) {
			Messages.send(player, 116);
			return true;
		}

		ChunkManager.Error error = ChunkManager.claimChunk(region.getUniqueId(), chunk);

		if (error == null) {
			PlayerBank.withdraw(region.getOwner(), chunkPrice);

			Messages.send(player, 22, new Placeholder()
					.add("{region}", region.getName())
			);

			if (region.getLocation() == null) {
				region.setLocation(player.getLocation());
			}

			ChunkBorder.show(player);
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> Messages.send(player, 9);
				case CHUNK_NOT_ADJACENT_TO_REGION -> Messages.send(player, 140);
			}
		}

		return true;
	}

	private Region getOrCreateRegion(Player player) {
		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			if (!RegionManager.getRegionsOwnedByPlayer(player).isEmpty()) {
				TargetRegionSession.randomizeRegion(player);
				region = TargetRegionSession.getRegion(player);
			} else {
				if (!player.hasPermission("homestead.region.create")) {
					Messages.send(player, 8);
					return null;
				}

				if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
					Messages.send(player, 116);
					return null;
				}

				region = RegionManager.createRegion(player.getName(), player, true);
				TargetRegionSession.newSession(player, region);
			}
		}

		return region;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}