package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.IOwnedLand;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.ChunkCoordinate;
import me.angeschossen.lands.api.land.Land;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.java.ListUtils;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import me.tayebyassine.homestead.util.minecraft.plugins.IntegrationUtility;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.ClaimWorld;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Admin sub-command ({@code /hsadmin import}) that imports regions and members from
 * another land-claiming plugin.
 */
public final class ImportSubCmd extends SubCommandBuilder {

    public ImportSubCmd() {
        super("import");
        setAdminPermission();
        setUsage("/hsadmin import [plugin]");
        setAllowedCommandSenders(CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Logger.error("Insufficient arguments, usage: ", getUsage());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "griefprevention" -> importFromGriefPrevention();
            case "landlord" -> importFromLandLord();
            case "claimchunk" -> importFromClaimChunk();
            case "lands" -> importFromLands();
            case "huskclaims" -> importFromHuskClaims();
            default -> {
                Logger.error("Invalid plugin name provided.");
                return true;
            }
        }

        Logger.info("Done.");

        ListUtils.printTable(new String[]{"Model", "Imported"}, DataStats.dataRows());

        return true;
    }

    private void importFromGriefPrevention() {
        if (!isInstalled("GriefPrevention")) {
            return;
        }

        Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();

        for (Claim claim : claims) {
            OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(claim.getOwnerID());

            if (owner == null) {
                continue;
            }

            Region region = RegionManager.createRegion(owner.getName(), owner);

            for (Chunk chunk : claim.getChunks()) {
                if (!ChunkManager.isChunkClaimed(chunk)) {
                    ChunkManager.claimChunk(region.getUniqueId(), chunk);
                }
            }

            for (OfflinePlayer player : Homestead.getInstance().getOfflinePlayersSync()) {
                if (PlayerUtility.equals(player, owner)) {
                    continue;
                }

                ClaimPermission permission = claim.getPermission(player.getUniqueId().toString());

                if (!MemberManager.isMemberOfRegion(region, player) && permission != null) {
                    addMemberWithRegionFlags(player, region);
                }
            }

            logImportedRegion(region, owner.getName(), owner.getUniqueId());
        }
    }

    private void importFromLandLord() {
        if (!isInstalled("LandLord")) {
            return;
        }

        ILandLord landlord = (ILandLord) Bukkit.getPluginManager().getPlugin("Landlord");
        Set<IOwnedLand> chunks = landlord.getWGManager().getRegions();

        for (IOwnedLand chunk : chunks) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(chunk.getOwner());

            Region region = RegionManager.getRegionsOwnedByPlayer(owner).isEmpty()
                    ? RegionManager.createRegion(owner.getName(), owner)
                    : RegionManager.getRegionsOwnedByPlayer(owner).getFirst();

            if (!ChunkManager.isChunkClaimed(chunk.getChunk())) {
                ChunkManager.claimChunk(region.getUniqueId(), chunk.getChunk());
            }

            for (UUID friendUuid : chunk.getFriends()) {
                OfflinePlayer friend = Homestead.getInstance().getOfflinePlayerSync(friendUuid);

                if (friend == null || PlayerUtility.equals(friend, owner)) {
                    continue;
                }

                if (!MemberManager.isMemberOfRegion(region, friend)) {
                    addMemberWithRegionFlags(friend, region);
                }
            }

            if (RegionManager.getRegionsOwnedByPlayer(owner).size() == 1) {
                logImportedRegion(region, owner.getName(), owner.getUniqueId());
            }
        }
    }

    private void importFromClaimChunk() {
        if (!isInstalled("ClaimChunk")) {
            return;
        }

        ClaimChunk claimChunk = ClaimChunk.getInstance();

        for (OfflinePlayer offlinePlayer : Homestead.getInstance().getOfflinePlayersSync()) {
            if (offlinePlayer.getName() == null) {
                continue;
            }

            ChunkPos[] chunkPositions = claimChunk.getChunkHandler()
                    .getClaimedChunks(offlinePlayer.getUniqueId());

            if (chunkPositions.length == 0) {
                continue;
            }

            Region region = RegionManager.createRegion(offlinePlayer.getName(), offlinePlayer);

            for (ChunkPos chunkPos : chunkPositions) {
                Chunk chunk = ChunkManager.getFromLocation(Bukkit.getWorld(chunkPos.world()),
                        chunkPos.x(), chunkPos.z());

                if (!ChunkManager.isChunkClaimed(chunk)) {
                    ChunkManager.claimChunk(region.getUniqueId(), chunk);
                }
            }

            logImportedRegion(region, offlinePlayer.getName(), offlinePlayer.getUniqueId());
        }
    }

    private void importFromLands() {
        if (!isInstalled("Lands")) {
            return;
        }

        LandsIntegration landsApi = new LandsIntegration(Homestead.getInstance());

        for (Land land : landsApi.getLands()) {
            OfflinePlayer owner = Homestead.getInstance().getOfflinePlayerSync(land.getOwnerUID());

            if (owner == null) {
                continue;
            }

            Region region = RegionManager.createRegion(owner.getName(), owner);

            for (World world : Bukkit.getWorlds()) {
                for (ChunkCoordinate chunkCoord : Objects.requireNonNull(land.getChunks(world))) {
                    Chunk chunk = ChunkManager.getFromLocation(world, chunkCoord.getX(), chunkCoord.getZ());

                    if (!ChunkManager.isChunkClaimed(chunk)) {
                        ChunkManager.claimChunk(region.getUniqueId(), chunk);
                    }
                }
            }

            for (UUID trustedUuid : land.getTrustedPlayers()) {
                OfflinePlayer trusted = Homestead.getInstance().getOfflinePlayerSync(trustedUuid);

                if (trusted == null || PlayerUtility.equals(trusted, owner)) {
                    continue;
                }

                if (!MemberManager.isMemberOfRegion(region, trusted)) {
                    addMemberWithRegionFlags(trusted, region);
                }
            }

            logImportedRegion(region, owner.getName(), owner.getUniqueId());
        }
    }

    private void importFromHuskClaims() {
        if (!isInstalled("HuskClaims")) {
            return;
        }

        BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();

        for (OfflinePlayer offlinePlayer : Homestead.getInstance().getOfflinePlayersSync()) {
            if (offlinePlayer.getName() == null) {
                continue;
            }

            Region region = RegionManager.createRegion(offlinePlayer.getName(), offlinePlayer);

            for (World world : Bukkit.getWorlds()) {
                net.william278.huskclaims.position.World huskWorld = api.getWorld(world.getName());
                Optional<ClaimWorld> claimWorld = api.getClaimWorld(huskWorld);

                claimWorld.ifPresent(worldClaims -> worldClaims.getClaims().forEach(claim -> {
                    claim.getRegion().getChunks().forEach(chunkPos -> {
                        Chunk chunk = ChunkManager.getFromLocation(world, chunkPos[0], chunkPos[1]);

                        if (!ChunkManager.isChunkClaimed(chunk)) {
                            ChunkManager.claimChunk(region.getUniqueId(), chunk);
                        }
                    });
                }));

                logImportedRegion(region, offlinePlayer.getName(), offlinePlayer.getUniqueId());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(List.of("GriefPrevention", "LandLord", "ClaimChunk", "Lands", "HuskClaims"));
        }

        return suggestions;
    }

    private void addMemberWithRegionFlags(OfflinePlayer player, Region region) {
        MemberManager.addMemberToRegion(player, region);
        MemberManager.getMemberOfRegion(region, player).setPlayerFlags(region.getPlayerFlags());
    }

    private void logImportedRegion(Region region, String ownerName, UUID ownerId) {
        Logger.info(String.format("Imported region: Name=%s, ID=%s, Owner=%s (%s)",
                region.getName(), region.getUniqueId(), ownerName, ownerId));
    }

    private boolean isInstalled(String pluginName) {
        try {
            return IntegrationUtility.isEnabled(pluginName);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}





