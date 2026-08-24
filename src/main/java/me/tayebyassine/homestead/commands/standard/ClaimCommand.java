package me.tayebyassine.homestead.commands.standard;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.ChunkClaimEvent;
import me.tayebyassine.homestead.api.events.RegionCreateEvent;
import me.tayebyassine.homestead.commands.CommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.integrations.WorldGuardAPI;
import me.tayebyassine.homestead.listeners.SelectionToolListener;
import me.tayebyassine.homestead.listeners.SelectionToolListener.Selection;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionChunk;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.borders.ChunkBorder;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkUtility;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Player command ({@code /claim}) that claims chunks, either from a wand selection
 * or around the player with an optional radius.
 */
public final class ClaimCommand extends CommandBuilder {

    private static final int MAX_RADIUS = 10;

    public ClaimCommand() {
        super("claim");
        setPermission(List.of(
                "homestead.commands.claim",
                "homestead.actions.regions.create",
                "homestead.actions.regions.chunks.claim"
        ));
        setUsage("/claim radius [radius]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onDefaultExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
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
                ControlFlag.CLAIM_CHUNKS.getBitmask())) {
            return true;
        }

        Block firstCorner = session.getFirstPosition();
        Block secondCorner = session.getSecondPosition();

        if (!firstCorner.getWorld().equals(secondCorner.getWorld())) {
            Messages.send(player, "commands.claim.0");
            return true;
        }

        List<Chunk> chunksToClaim = new ArrayList<>();

        for (Chunk chunk : ChunkUtility.getChunksInArea(firstCorner, secondCorner)) {
            if (ChunkManager.isChunkInDisabledWorld(chunk)) {
                Messages.send(player, "commands.claim.1");
                return true;
            }

            if (Resources.<ConfigFile>get(ResourceType.Config).protectWorldGuardRegions()
                    && WorldGuardAPI.isChunkInRegion(chunk)) {
                Messages.send(player, "commands.claim.2");
                return true;
            }

            if (ChunkManager.getRegionOwnsTheChunk(chunk) != null) {
                Messages.send(player, "commands.claim.3");
                return true;
            }

            chunksToClaim.add(chunk);
        }

        if (chunksToClaim.isEmpty()) {
            Messages.send(player, "commands.claim.4");
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

                if (radius < 1 || radius > MAX_RADIUS) {
                    Messages.send(player, "commands.claim.5");
                    return true;
                }
            } catch (NumberFormatException e) {
                Messages.send(player, "commands.claim.6");
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
                    Messages.send(player, radius == 1 ? "commands.claim.14" : "commands.claim.1");
                    return true;
                }

                if (Resources.<ConfigFile>get(ResourceType.Config).protectWorldGuardRegions()
                        && WorldGuardAPI.isChunkInRegion(chunk)) {
                    Messages.send(player, radius == 1 ? "commands.claim.15" : "commands.claim.2");
                    return true;
                }

                if (ChunkManager.getRegionOwnsTheChunk(chunk) != null) {
                    Messages.send(player, radius == 1 ? "commands.claim.16" : "commands.claim.3");
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
                ControlFlag.CLAIM_CHUNKS.getBitmask())) {
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
            Messages.send(player, "commands.claim.7", Formatter.getBalance(totalPrice));
            return false;
        }

        int currentChunks = ChunkManager.getChunkCount(region);
        int maxChunks = Limits.getRegionLimit(region, Limits.LimitType.CHUNKS_PER_REGION);

        if (currentChunks + chunksToClaim.size() > maxChunks) {
            Messages.send(player, "commands.claim.8");
            return false;
        }

        int claimedCount = 0;
        ChunkManager.Error lastError = null;

        for (Chunk chunk : chunksToClaim) {
            ChunkManager.Error error = ChunkManager.claimChunk(region, chunk);

            if (error != null) {
                lastError = error;
                break;
            }

            claimedCount++;
        }

        if (lastError != null && claimedCount < chunksToClaim.size()) {
            rollbackClaimedChunks(region, chunksToClaim, claimedCount);

            switch (lastError) {
                case REGION_NOT_FOUND -> Messages.send(player, "commands.claim.9");
                case CHUNK_NOT_ADJACENT_TO_REGION -> Messages.send(player, "commands.claim.10");
            }
            return false;
        }

        if (claimedCount > 0) {
            if (totalPrice > 0) {
                PlayerBank.withdraw(region.getOwner(), totalPrice);
            }

            if (claimedCount == 1) {
                Messages.send(player, "commands.claim.11", region.getName(), Formatter.getBalance(totalPrice));
            } else {
                Messages.send(player, "commands.claim.12",
                        claimedCount, chunksToClaim.size(), region.getName(), Formatter.getBalance(totalPrice));
            }

            LogManager.addLog(region, player, LogManager.PredefinedLog.CLAIM_CHUNK);

            if (region.getLocation() == null) {
                region.setLocation(player.getLocation());
            }

            ChunkBorder.show(player);

            Homestead.callEvent(new ChunkClaimEvent(region, chunksToClaim.getFirst()));
        }

        return true;
    }

    private void rollbackClaimedChunks(Region region, List<Chunk> chunksToClaim, int claimedCount) {
        for (int i = 0; i < claimedCount; i++) {
            Chunk claimed = chunksToClaim.get(i);
            RegionChunk regionChunk = ChunkManager.findChunk(claimed);

            if (regionChunk != null && regionChunk.getRegionId() == region.getUniqueId()) {
                ChunkManager.forceUnclaimChunk(region, claimed);
            }
        }
    }

    private Region getOrCreateRegion(Player player) {
        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            if (!RegionManager.getRegionsOwnedByPlayer(player).isEmpty()) {
                TargetRegionSession.randomizeRegion(player);
                region = TargetRegionSession.getRegion(player);
            } else {
                if (!player.hasPermission("homestead.actions.regions.create")) {
                    return null;
                }

                if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
                    Messages.send(player, "commands.claim.13");
                    return null;
                }

                region = RegionManager.createRegion(player.getName(), player);

                Homestead.callEvent(new RegionCreateEvent(region, player));

                TargetRegionSession.newSession(player, region);
            }
        }

        return region;
    }

    @Override
    public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            for (int i = 1; i <= MAX_RADIUS; i++) {
                suggestions.add(String.valueOf(i));
            }
        }

        return suggestions;
    }
}




