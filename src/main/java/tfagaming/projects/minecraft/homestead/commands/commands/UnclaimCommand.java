package tfagaming.projects.minecraft.homestead.commands.commands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnclaimCommand extends CommandBuilder {
	public UnclaimCommand() {
		super("unclaim");
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

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			PlayerUtils.sendMessage(player, 4);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNCLAIM_CHUNKS)) {
			return true;
		}

		Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

		if (regionOwnsThisChunk == null) {
			PlayerUtils.sendMessage(player, 25);
			return true;
		}

		if (!regionOwnsThisChunk.getUniqueId().equals(region.getUniqueId())) {
			PlayerUtils.sendMessage(player, 23);
			return true;
		}

		ChunksManager.Error error = ChunksManager.unclaimChunk(region.getUniqueId(), chunk);

		if (error == null) {
			double chunkPrice = Homestead.config.getDouble("chunk-price");

			if (chunkPrice > 0) {
				PlayerUtils.addBalance(region.getOwner(), chunkPrice);
			}

			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{region}", region.getName());

			PlayerUtils.sendMessage(player, 24, replacements);

			ChunkBorder.show(player);
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> PlayerUtils.sendMessage(player, 9);
				case CHUNK_WOULD_SPLIT_REGION -> PlayerUtils.sendMessage(player, 141);
			}
		}

		return true;
	}

	@Override
	public List<String> onAutoComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
