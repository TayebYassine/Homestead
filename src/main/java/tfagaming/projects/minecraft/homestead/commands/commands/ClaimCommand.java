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
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
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
			PlayerUtils.sendMessage(player, 20);
			return true;
		}

		boolean isWorldGuardProtectingRegionsEnabled = Homestead.config.get("worldguard.protect-existing-regions");

		if (isWorldGuardProtectingRegionsEnabled) {
			if (WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
				PlayerUtils.sendMessage(player, 133);
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
					PlayerUtils.sendMessage(player, 8);
					return true;
				}

				if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
					PlayerUtils.sendMessage(player, 116);
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

		double chunkPrice = Homestead.config.get("chunk-price");

		if (chunkPrice > 0 && PlayerUtils.getBalance(region.getOwner()) < chunkPrice) {
			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{price}", Formatters.formatBalance(chunkPrice));
			replacements.put("{player}", region.getOwner().getName());

			PlayerUtils.sendMessage(player, 200, replacements);
			return true;
		}

		Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

		if (regionOwnsThisChunk != null) {
			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{region}", regionOwnsThisChunk.getName());

			PlayerUtils.sendMessage(player, 21, replacements);
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.CHUNKS_PER_REGION)) {
			PlayerUtils.sendMessage(player, 116);
			return true;
		}

		ChunksManager.Error error = ChunksManager.claimChunk(region.getUniqueId(), chunk);

		if (error == null) {
			if (chunkPrice > 0) {
				PlayerUtils.removeBalance(region.getOwner(), chunkPrice);
			}

			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{region}", region.getName());

			PlayerUtils.sendMessage(player, 22, replacements);

			if (region.getLocation() == null) {
				region.setLocation(player.getLocation());
			}

			ChunkBorder.show(player);
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> PlayerUtils.sendMessage(player, 9);
				case CHUNK_NOT_ADJACENT_TO_REGION -> PlayerUtils.sendMessage(player, 140);
			}
		}

		return true;
	}

	@Override
	public List<String> onAutoComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
