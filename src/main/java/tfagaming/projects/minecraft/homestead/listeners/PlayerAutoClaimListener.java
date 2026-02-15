package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.borders.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.integrations.WorldGuardAPI;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.autoclaim.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Listener that manages automatic chunk claiming when a player moves between chunks.
 * <p>
 * This system automatically claims chunks during an active AutoClaim session,
 * ensures safe performance by applying cooldowns, and prevents duplicate particle tasks.
 * </p>
 */
public final class PlayerAutoClaimListener implements Listener {

	/**
	 * Minimum delay between automatic claim attempts in milliseconds.
	 */
	private static final long CLAIM_COOLDOWN_MS = 500;
	/**
	 * Stores the last chunk location of each player to detect when they enter a new chunk.
	 */
	private final Map<Player, Chunk> lastChunks = new WeakHashMap<>();
	/**
	 * Tracks the timestamp of the player's last claim attempt to prevent spam.
	 */
	private final Map<Player, Long> lastClaimAttempt = new WeakHashMap<>();

	/**
	 * Triggered whenever a player moves.
	 * <p>
	 * If AutoClaim mode is enabled for the player and they move into a new chunk,
	 * the system attempts to claim that chunk for the player's current or active region.
	 * </p>
	 *
	 * @param event The player movement event.
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Chunk currentChunk = player.getLocation().getChunk();

		if (!AutoClaimSession.hasSession(player)) {
			return;
		}

		Chunk lastChunk = lastChunks.get(player);
		if (!currentChunk.equals(lastChunk)) {
			tryToClaim(player, currentChunk);
			lastChunks.put(player, currentChunk);
		}
	}

	/**
	 * Attempts to claim a chunk for the player's region.
	 * <p>
	 * The method enforces cooldowns, permission checks, and chunk adjacency rules.
	 * It also ensures the player owns or has rights to modify the region.
	 * If successful, a claim success message is sent and border borders are displayed.
	 * </p>
	 *
	 * @param player The player attempting to claim.
	 * @param chunk  The chunk being claimed.
	 */
	private void tryToClaim(Player player, Chunk chunk) {
		long now = System.currentTimeMillis();

		if (lastClaimAttempt.containsKey(player)
				&& (now - lastClaimAttempt.get(player)) < CLAIM_COOLDOWN_MS) {
			return;
		}
		lastClaimAttempt.put(player, now);

		if (ChunksManager.isChunkInDisabledWorld(chunk)) {
			Messages.send(player, 20);
			return;
		}

		boolean wgEnabled = Homestead.config.getBoolean("worldguard.protect-existing-regions");
		if (wgEnabled && WorldGuardAPI.isChunkInWorldGuardRegion(chunk)) {
			Messages.send(player, 133);
			return;
		}

		Region region = TargetRegionSession.getRegion(player);
		if (region == null) {
			if (!RegionsManager.getRegionsOwnedByPlayer(player).isEmpty()) {
				TargetRegionSession.randomizeRegion(player);
				region = TargetRegionSession.getRegion(player);
			} else {
				if (!player.hasPermission("homestead.region.create")) {
					Messages.send(player, 8);
					return;
				}

				if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
					Messages.send(player, 116);
					return;
				}

				region = RegionsManager.createRegion(player.getName(), player, true);
				TargetRegionSession.newSession(player, region);
			}
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.CLAIM_CHUNKS)) {
			return;
		}

		Region owner = ChunksManager.getRegionOwnsTheChunk(chunk);
		if (owner != null) {
			Messages.send(player, 21, new Placeholder()
					.add("{region}", owner.getName())
			);
			return;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.CHUNKS_PER_REGION)) {
			Messages.send(player, 116);
			return;
		}

		int before = region.getChunks().size();

		ChunksManager.Error error = ChunksManager.claimChunk(region.getUniqueId(), chunk);

		int after = region.getChunks().size();

		if (error == null) {
			if (after > before) {
				Messages.send(player, 22, new Placeholder()
						.add("{region}", region.getName())
				);
			}

			if (region.getLocation() == null) {
				region.setLocation(player.getLocation());
			}

			if (!ChunkParticlesSpawner.isTaskRunning(player)) {
				ChunkBorder.show(player);
			}
		} else {
			switch (error) {
				case REGION_NOT_FOUND -> Messages.send(player, 9);
				case CHUNK_NOT_ADJACENT_TO_REGION -> Messages.send(player, 140);
			}
		}
	}
}
