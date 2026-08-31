package me.tayebyassine.homestead.listeners.player;

import me.tayebyassine.homestead.borders.ChunkBorder;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.integrations.WorldGuardAPI;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.AutoClaimSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

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

    private static final long CLAIM_COOLDOWN_MS = 500;
    private final Map<Player, Chunk> lastChunks = new WeakHashMap<>();
    private final Map<Player, Long> lastClaimAttempt = new WeakHashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Chunk currentChunk = location.getChunk();

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

        if (ChunkManager.isChunkInDisabledWorld(chunk)) {
            Messages.send(player, "commands.claim.1");
            return;
        }

        if (Resources.<ConfigFile>get(ResourceType.Config).protectWorldGuardRegions() && WorldGuardAPI.isChunkInRegion(chunk)) {
            Messages.send(player, "commands.claim.2");
            return;
        }

        Region region = TargetRegionSession.getRegion(player);
        if (region == null) {
            if (!RegionManager.getRegionsOwnedByPlayer(player).isEmpty()) {
                TargetRegionSession.randomizeRegion(player);
                region = TargetRegionSession.getRegion(player);
            } else {
                if (!player.hasPermission("homestead.actions.regions.create")) {
                    Messages.send(player, "common.no_permission");
                    return;
                }

                if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
                    Messages.send(player, "commands.claim.13");
                    return;
                }

                region = RegionManager.createRegion(player.getName(), player);

                TargetRegionSession.newSession(player, region);
            }
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.CLAIM_CHUNKS, true)) {
            return;
        }

        Region owner = ChunkManager.getRegionOwnsTheChunk(chunk);
        if (owner != null) {
            Messages.send(player, "commands.claim.3");
            return;
        }

        if (Limits.hasReachedLimit(null, region, Limits.LimitType.CHUNKS_PER_REGION)) {
            Messages.send(player, "commands.claim.8");
            return;
        }

        double chunkPrice = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("chunk-price");

        if (chunkPrice > 0 && PlayerBank.get(region.getOwner()) < chunkPrice) {
            Messages.send(player, "commands.claim.7", Formatter.getBalance(chunkPrice));
            return;
        }

        int before = ChunkManager.getChunksOfRegion(region).size();

        ChunkManager.Error error = ChunkManager.claimChunk(region.getUniqueId(), chunk);

        int after = ChunkManager.getChunksOfRegion(region).size();

        if (error == null) {
            if (chunkPrice > 0) {
                PlayerBank.withdraw(region.getOwner(), chunkPrice);
            }

            if (after > before) {
                Messages.send(player, "commands.claim.11", region.getName(), Formatter.getBalance(chunkPrice));
            }

            if (region.getLocation() == null) {
                region.setLocation(player.getLocation());
            }

            ChunkBorder.show(player);
        } else {
            switch (error) {
                case REGION_NOT_FOUND -> Messages.send(player, "commands.claim.9");
                case CHUNK_NOT_ADJACENT_TO_REGION -> Messages.send(player, "commands.claim.10");
            }
        }
    }
}

