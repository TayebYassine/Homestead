package tfagaming.projects.minecraft.homestead.commands.standard;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.ChunkUnclaimEvent;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class UnclaimCommand extends CommandBuilder {
	public UnclaimCommand() {
		super("unclaim");
		setPermission(List.of(
				"homestead.commands.unclaim",
				"homestead.actions.regions.chunks.unclaim"
		));
		setUsage("/unclaim");
		setPlayerOnly();
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		Chunk chunk = player.getLocation().getChunk();

		if (ChunkManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, "commands.unclaim.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.unclaim.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(
				region.getUniqueId(),
				player,
				ControlFlags.UNCLAIM_CHUNKS)) {
			Messages.send(player, "commands.unclaim.2");
			return true;
		}

		Region regionOwnsThisChunk = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (regionOwnsThisChunk == null) {
			Messages.send(player, "commands.unclaim.3");
			return true;
		}

		if (regionOwnsThisChunk.getUniqueId() != region.getUniqueId()) {
			Messages.send(player, "commands.unclaim.4", region.getName());
			return true;
		}

		Cooldown.startCooldown(player, Cooldown.Type.REGION_CHUNK_UNCLAIM);

		ChunkManager.Error error = ChunkManager.unclaimChunk(region, chunk);

		if (error == null) {
			double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("chunk-price");
			if (chunkPrice > 0) {
				PlayerBank.deposit(region.getOwner(), chunkPrice);
			}

			Messages.send(player, "commands.unclaim.5", region.getName(), Formatter.getBalance(chunkPrice));

			LogManager.addLog(region, player, LogManager.PredefinedLog.UNCLAIM_CHUNK);

			ChunkBorder.show(player);

			Homestead.callEvent(new ChunkUnclaimEvent(region, chunk));
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> Messages.send(player, "commands.unclaim.6");
				case CHUNK_WOULD_SPLIT_REGION -> Messages.send(player, "commands.unclaim.7");
			}
		}

		return true;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}