package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs kick}) that kicks a player out of the current region.
 */
public final class KickPlayerSubCmd extends SubCommandBuilder {

    public KickPlayerSubCmd() {
        super("kick");
        setRegionPermission("homestead.actions.regions.kick");
        setUsage("/hs kick [player]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.kick.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.kick.1");
            return true;
        }

        if (!PlayerUtility.hasControlPermissionFlag(region, player, ControlFlag.KICK_PLAYERS, true)) {
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            Messages.send(player, "commands.kick.3", targetName);
            return true;
        }

        if (BanManager.isBanned(region, target)) {
            Messages.send(player, "commands.kick.4");
            return true;
        }

        if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
            Messages.send(player, "commands.kick.5");
            return true;
        }

        SeRent rent = region.getRent();

        if (rent != null && rent.isRenterer(target)) {
            Messages.send(player, "commands.kick.6");
            return true;
        }

        if (!RegionManager.isPlayerInsideRegion(target, region)) {
            Messages.send(player, "commands.kick.7");
            return true;
        }

        Chunk chunk = ChunkUtility.findNearbyUnclaimedChunk(target.getLocation(), 64);

        if (chunk != null) {
            PlayerUtility.teleportPlayerToChunk(target, chunk);
        }

        Messages.send(player, "commands.kick.8", targetName);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Homestead.getInstance().getOnlinePlayerNamesSync());
        }

        return suggestions;
    }
}




