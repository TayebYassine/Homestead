package tfagaming.projects.minecraft.homestead.commands.commands;

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

/**
 * OP-only admin command to unclaim the chunk the executor is standing in,
 * regardless of region ownership and split protection.
 */
public class ForceUnclaimCommand extends CommandBuilder {

	/**
	 * Creates a new force-unclaim command instance.
	 */
	public ForceUnclaimCommand() {
		super("forceunclaim");
	}

	/**
	 * Executes the command.
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return true if execution was handled
	 */
	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
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

	/**
	 * Provides tab completion options.
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return a list of completion suggestions
	 */
	@Override
	public List<String> onAutoComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
