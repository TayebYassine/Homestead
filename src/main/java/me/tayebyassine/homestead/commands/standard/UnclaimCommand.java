package me.tayebyassine.homestead.commands.standard;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.api.events.ChunkUnclaimEvent;
import me.tayebyassine.homestead.commands.CommandBuilder;
import me.tayebyassine.homestead.cooldown.Cooldown;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkBorder;
import me.tayebyassine.homestead.util.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Player command ({@code /unclaim}) that unclaims the chunk the player is standing on.
 */
public final class UnclaimCommand extends CommandBuilder {

    public UnclaimCommand() {
        super("unclaim");
        setPermission(List.of(
                "homestead.commands.unclaim",
                "homestead.actions.regions.chunks.unclaim"
        ));
        setUsage("/unclaim");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onDefaultExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

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
                ControlFlag.UNCLAIM_CHUNKS.getBitmask())) {
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
}

