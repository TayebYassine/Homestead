package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.MergeRegionSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs merge}) that requests a merge with another region.
 */
public final class MergeRegionSubCmd extends SubCommandBuilder {

    public MergeRegionSubCmd() {
        super("merge");
        setRegionPermission("homestead.actions.regions.merge");
        setUsage("/hs merge [region]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.merge.0");
            return true;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.merge.1");
            return true;
        }

        Region targetRegion = RegionManager.findRegion(args[0]);

        if (targetRegion == null) {
            Messages.send(player, "commands.merge.2", args[0]);
            return true;
        }

        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "commands.merge.3");
            return true;
        }

        if (region.getUniqueId() == targetRegion.getUniqueId()) {
            Messages.send(player, "commands.merge.4");
            return true;
        }

        OfflinePlayer targetOfflineOwner = targetRegion.getOwner();
        Player targetOwner = targetOfflineOwner != null && targetOfflineOwner.isOnline()
                ? targetOfflineOwner.getPlayer()
                : null;

        if (targetOwner == null) {
            Messages.send(player, "commands.merge.5");
            return true;
        }

        if (MergeRegionSession.isFromHaveRequest(region) || MergeRegionSession.isToHaveRequest(targetRegion)) {
            Messages.send(player, "commands.merge.6");
            return true;
        }

        MergeRegionSession.newMergeRequest(region, targetRegion);

        Messages.send(player, "commands.merge.7");
        Messages.send(targetOwner, "commands.merge.8", player.getName(), region.getName());

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
            suggestions.addAll(RegionManager.getRegionNames());
        }

        return suggestions;
    }
}



