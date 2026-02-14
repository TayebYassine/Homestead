package tfagaming.projects.minecraft.homestead.commands.commands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldGuardAPI;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimCommand extends CommandBuilder {
	public ClaimCommand() {
		super("claim");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		Chunk chunk = player.getLocation().getChunk();

		if (ChunksManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, 20);
			return true;
		}

		boolean isWorldGuardProtectingRegionsEnabled = Homestead.config.getBoolean("worldguard.protect-existing-regions");

		if (isWorldGuardProtectingRegionsEnabled) {
			if (WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
				Messages.send(player, 133);
				return true;
			}
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			if (!RegionsManager.getRegionsOwnedByPlayer(player).isEmpty()) {
				TargetRegionSession.randomizeRegion(player);

				region = TargetRegionSession.getRegion(player);
			} else {
				if (!player.hasPermission("homestead.region.create")) {
					Messages.send(player, 8);
					return true;
				}

				if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
					Messages.send(player, 116);
					return true;
				}

				region = RegionsManager.createRegion(player.getName(),
						player, true);

				TargetRegionSession.newSession(player, region);
			}
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.CLAIM_CHUNKS)) {
			return true;
		}

		double chunkPrice = Homestead.config.getDouble("chunk-price");

		if (chunkPrice > 0 && PlayerBank.get(region.getOwner()) < chunkPrice) {
			Messages.send(player, 200, new Placeholder()
					.add("{price}", Formatters.getBalance(chunkPrice))
					.add("{player}", region.getOwner().getName())
			);

			return true;
		}

		Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

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

		ChunksManager.Error error = ChunksManager.claimChunk(region.getUniqueId(), chunk);

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

	@Override
	public List<String> onAutoComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
