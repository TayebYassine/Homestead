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
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

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
			Messages.send(player, 20);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNCLAIM_CHUNKS)) {
			return true;
		}

		Region regionOwnsThisChunk = ChunksManager.getRegionOwnsTheChunk(chunk);

		if (regionOwnsThisChunk == null) {
			Messages.send(player, 25);
			return true;
		}

		if (!regionOwnsThisChunk.getUniqueId().equals(region.getUniqueId())) {
			Messages.send(player, 23);
			return true;
		}

		ChunksManager.Error error = ChunksManager.unclaimChunk(region.getUniqueId(), chunk);

		if (error == null) {
			double chunkPrice = Homestead.config.getDouble("chunk-price");

			PlayerBank.deposit(region.getOwner(), chunkPrice);

			Messages.send(player, 24, new Placeholder()
					.add("{region}", region.getName())
			);

			ChunkBorder.show(player);
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> Messages.send(player, 9);
				case CHUNK_WOULD_SPLIT_REGION -> Messages.send(player, 141);
			}
		}

		return true;
	}

	@Override
	public List<String> onAutoComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
