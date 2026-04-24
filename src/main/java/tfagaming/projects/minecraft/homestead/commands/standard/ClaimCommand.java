package tfagaming.projects.minecraft.homestead.commands.standard;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldGuardAPI;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener.Selection;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class ClaimCommand extends CommandBuilder {
	public ClaimCommand() {
		super("claim");
		setUsage("/claim radius [radius]");
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (Cooldown.hasCooldown(player, Cooldown.Type.REGION_CHUNK_CLAIM)) {
			Cooldown.sendCooldownMessage(player);
			return true;
		}

		Selection session = SelectionToolListener.getPlayerSession(player);

		if (session != null) {
			return claimWithSelection(player, session);
		}

		return claimWithRadius(player, args);
	}

	private boolean claimWithSelection(Player player, Selection session) {
		Region region = getOrCreateRegion(player);
		if (region == null) {
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(
				region.getUniqueId(),
				player,
				RegionControlFlags.CLAIM_CHUNKS)) {
			return true;
		}

		Block firstCorner = session.getFirstPosition();
		Block secondCorner = session.getSecondPosition();

		if (!firstCorner.getWorld().equals(secondCorner.getWorld())) {
			Messages.send(player, 192);
			return true;
		}

		World world = firstCorner.getWorld();

		List<Chunk> chunksToClaim = new ArrayList<>();

		for (Chunk chunk : ChunkUtility.getChunksInArea(firstCorner, secondCorner)) {
			if (ChunkManager.isChunkInDisabledWorld(chunk)) {
				Messages.send(player, 20);
				return true;
			}

			if (Resources.<ConfigFile>get(ResourceType.Config).protectWorldGuardRegions() && WorldGuardAPI.isChunkInRegion(chunk)) {
				Messages.send(player, 133);
				return true;
			}

			Region regionOwnsThisChunk = ChunkManager.getRegionOwnsTheChunk(chunk);
			if (regionOwnsThisChunk != null) {
				Messages.send(player, 21, new Placeholder()
						.add("{region}", regionOwnsThisChunk.getName())
				);
				return true;
			}

			chunksToClaim.add(chunk);
		}

		if (chunksToClaim.isEmpty()) {
			Messages.send(player, 186);
			return true;
		}

		if (!validateAndClaim(player, region, chunksToClaim)) {
			return true;
		}

		SelectionToolListener.cancelPlayerSession(player);

		Cooldown.startCooldown(player, Cooldown.Type.REGION_CHUNK_CLAIM);

		return true;
	}

	private boolean claimWithRadius(Player player, String[] args) {
		int radius = 1;
		if (args.length > 1 && args[0].equalsIgnoreCase("radius")) {
			try {
				radius = Integer.parseInt(args[1]);
				if (radius < 1 || radius > 10) {
					Messages.send(player, 209);
					return true;
				}
			} catch (NumberFormatException e) {
				Messages.send(player, 185);
				return true;
			}
		}

		Chunk centerChunk = player.getLocation().getChunk();
		int centerX = centerChunk.getX();
		int centerZ = centerChunk.getZ();

		List<Chunk> chunksToClaim = new ArrayList<>();
		for (int x = centerX - (radius - 1); x <= centerX + (radius - 1); x++) {
			for (int z = centerZ - (radius - 1); z <= centerZ + (radius - 1); z++) {
				Chunk chunk = centerChunk.getWorld().getChunkAt(x, z);

				if (ChunkManager.isChunkInDisabledWorld(chunk)) {
					Messages.send(player, 20);
					return true;
				}

				if (Resources.<ConfigFile>get(ResourceType.Config).protectWorldGuardRegions() && WorldGuardAPI.isChunkInRegion(chunk)) {
					Messages.send(player, 133);
					return true;
				}

				Region regionOwnsThisChunk = ChunkManager.getRegionOwnsTheChunk(chunk);
				if (regionOwnsThisChunk != null) {
					Messages.send(player, 21, new Placeholder()
							.add("{region}", regionOwnsThisChunk.getName())
					);
					return true;
				}

				chunksToClaim.add(chunk);
			}
		}

		Region region = getOrCreateRegion(player);
		if (region == null) {
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(
				region.getUniqueId(),
				player,
				RegionControlFlags.CLAIM_CHUNKS)) {
			return true;
		}

		validateAndClaim(player, region, chunksToClaim);

		Cooldown.startCooldown(player, Cooldown.Type.REGION_CHUNK_CLAIM);

		return true;
	}

	private boolean validateAndClaim(Player player, Region region, List<Chunk> chunksToClaim) {
		double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("chunk-price");
		double totalPrice = chunkPrice * chunksToClaim.size();

		if (totalPrice > 0 && PlayerBank.get(region.getOwner()) < totalPrice) {
			Messages.send(player, 200, new Placeholder()
					.add("{price}", Formatter.getBalance(totalPrice))
					.add("{player}", region.getOwner().getName())
			);
			return false;
		}

		int currentChunks = region.getChunks().size();
		int maxChunks = Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION);
		if (currentChunks + chunksToClaim.size() > maxChunks) {
			Messages.send(player, 116);
			return false;
		}

		int claimedCount = 0;
		ChunkManager.Error lastError = null;

		for (Chunk chunk : chunksToClaim) {
			ChunkManager.Error error = ChunkManager.claimChunk(region.getUniqueId(), chunk);

			if (error != null) {
				lastError = error;
				break;
			}

			claimedCount++;
		}

		if (lastError != null && claimedCount < chunksToClaim.size()) {
			for (int i = 0; i < claimedCount; i++) {
				ChunkManager.forceUnclaimChunk(region.getUniqueId(), chunksToClaim.get(i));
			}

			switch (lastError) {
				case REGION_NOT_FOUND -> Messages.send(player, 9);
				case CHUNK_NOT_ADJACENT_TO_REGION -> Messages.send(player, 140);
			}
			return false;
		}

		if (claimedCount > 0) {
			if (totalPrice > 0) {
				PlayerBank.withdraw(region.getOwner(), totalPrice);
			}

			if (claimedCount == 1) {
				Messages.send(player, 22, new Placeholder()
						.add("{region}", region.getName())
				);
			} else {
				Messages.send(player, 187, new Placeholder()
						.add("{region}", region.getName())
						.add("{chunks}", claimedCount)
						.add("{total}", chunksToClaim.size())
				);
			}

			if (region.getLocation() == null) {
				region.setLocation(player.getLocation());
			}

			ChunkBorder.show(player);
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

				region = RegionManager.createRegion(player.getName(), player);
				TargetRegionSession.newSession(player, region);
			}
		}

		return region;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			for (int i = 1; i < 11; i++) {
				suggestions.add(String.valueOf(i));
			}
		}

		return suggestions;
	}
}