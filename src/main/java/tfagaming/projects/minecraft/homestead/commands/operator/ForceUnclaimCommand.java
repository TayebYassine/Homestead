package tfagaming.projects.minecraft.homestead.commands.operator;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;

import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class ForceUnclaimCommand extends CommandBuilder {

	public ForceUnclaimCommand() {
		super("forceunclaim");
		setUsage("/forceunclaim");
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			Messages.send(sender, 8);
			return false;
		}

		if (!PlayerUtility.isOperator(player)) {
			Messages.send(sender, 8);
			return true;
		}

		Chunk chunk = player.getLocation().getChunk();

		if (ChunkManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, 20);
			return true;
		}

		Region owningRegion = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (owningRegion == null) {
			Messages.send(player, 25);
			return true;
		}

		ChunkManager.Error error = ChunkManager.forceUnclaimChunk(owningRegion.getUniqueId(), chunk);

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