package tfagaming.projects.minecraft.homestead.commands.operator;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class ForceUnclaimCommand extends CommandBuilder {

	public ForceUnclaimCommand() {
		super("forceunclaim");
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			Messages.send(sender, 8);
			return false;
		}

		if (!PlayerUtils.isOperator(player)) {
			Messages.send(sender, 8);
			return true;
		}

		Chunk chunk = player.getLocation().getChunk();

		if (ChunksManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, 20);
			return true;
		}

		Region owningRegion = ChunksManager.getRegionOwnsTheChunk(chunk);

		if (owningRegion == null) {
			Messages.send(player, 25);
			return true;
		}

		ChunksManager.Error error = ChunksManager.forceUnclaimChunk(owningRegion.getUniqueId(), chunk);

		if (error == null) {
			Messages.send(player, 24, new Placeholder()
					.add("{region}", owningRegion.getName())
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
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}