package me.tayebyassine.homestead.commands.operator;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.CommandBuilder;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkBorder;

import java.util.ArrayList;
import java.util.List;

public class ForceUnclaimCommand extends CommandBuilder {

	public ForceUnclaimCommand() {
		super("forceunclaim");
		setPermission("homestead.admin.forceunclaim");
		setUsage("/forceunclaim");
		setPlayerOnly();
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Chunk chunk = player.getLocation().getChunk();

		if (ChunkManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, "commands.op_forceunclaim.0");
			return true;
		}

		Region owningRegion = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (owningRegion == null) {
			Messages.send(player, "commands.op_forceunclaim.1");
			return true;
		}

		ChunkManager.Error error = ChunkManager.forceUnclaimChunk(owningRegion, chunk);

		if (error == null) {
			Messages.send(player, "commands.op_forceunclaim.2");

			ChunkBorder.show(player);
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> Messages.send(player, "commands.op_forceunclaim.3");
				case CHUNK_WOULD_SPLIT_REGION -> Messages.send(player, "commands.op_forceunclaim.4");
			}
		}

		return true;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}