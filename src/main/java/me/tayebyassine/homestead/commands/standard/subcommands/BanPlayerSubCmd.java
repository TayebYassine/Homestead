package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.BanPlayerEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionBan;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.LanguageFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkUtility;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sub-command ({@code /hs ban}) that bans a player from the current region, with an optional reason.
 */
public final class BanPlayerSubCmd extends SubCommandBuilder {

    public BanPlayerSubCmd() {
        super("ban");
        setRegionPermission("homestead.actions.regions.players.ban");
        setUsage("/hs ban [player] (reason)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.ban.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.ban.1");
            return true;
        }

        if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                ControlFlags.BAN_PLAYERS)) {
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Messages.send(player, "commands.ban.3", targetName);
            return true;
        }

        if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
            Messages.send(player, "commands.ban.4");
            return true;
        }

        RegionBan ban = BanManager.getBannedPlayer(region, target);

        if (ban != null) {
            Messages.send(player, "commands.ban.5", targetName, ban.getReason());
            return true;
        }

        SeRent rent = region.getRent();

        if (rent != null && rent.isRenterer(target)) {
            Messages.send(player, "commands.ban.6");
            return true;
        }

        String reason = Resources.<LanguageFile>get(ResourceType.Language).getString("common.default.reason");

        if (args.length > 1) {
            reason = String.join(" ", Arrays.asList(args).subList(1, args.length));
        }

        if (ColorTranslator.containsMiniMessageTag(reason)) {
            Messages.send(player, "commands.ban.7");
            return true;
        }

        Player targetOnline = target.isOnline() ? target.getPlayer() : null;

        if (targetOnline != null && RegionManager.isPlayerInsideRegion(targetOnline, region)) {
            Chunk chunk = ChunkUtility.findNearbyUnclaimedChunk(targetOnline.getLocation(), 64);

            if (chunk != null) {
                PlayerUtility.teleportPlayerToChunk(targetOnline, chunk);
            }
        }

        BanManager.banPlayer(region, target, reason);
        LogManager.addLog(region, player, LogManager.PredefinedLog.BAN_PLAYER, target.getName());

        Messages.send(player, "commands.ban.8", targetName, region.getName(), reason);

        Homestead.callEvent(new BanPlayerEvent(region, target, reason));

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



