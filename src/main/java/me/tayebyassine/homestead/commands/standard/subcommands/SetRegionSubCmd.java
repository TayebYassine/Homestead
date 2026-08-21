package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs set}) that selects a region as the current target.
 */
public final class SetRegionSubCmd extends SubCommandBuilder {

    public SetRegionSubCmd() {
        super("set");
        setRegionPermission();
        setUsage("/hs set [region]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.set.0");
            return true;
        }

        String regionName = args[0];

        Region region = RegionManager.findRegion(regionName);

        if (region == null) {
            Messages.send(player, "commands.set.1", regionName);
            return true;
        }

        if (!PlayerUtility.isOperator(player)
                && !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player))) {
            Messages.send(player, "commands.set.2");
            return true;
        }

        if (TargetRegionSession.hasSession(player)
                && TargetRegionSession.getRegion(player).getUniqueId() == region.getUniqueId()) {
            Messages.send(player, "commands.set.3");
            return true;
        }

        TargetRegionSession.newSession(player, region);

        Messages.send(player, "commands.set.4", regionName);

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
            if (PlayerUtility.isOperator(player)) {
                suggestions.addAll(RegionManager.getRegionNames());
            } else {
                suggestions.addAll(
                        RegionManager.getRegionsOwnedByPlayer(player).stream().map(Region::getName).toList());
                suggestions.addAll(
                        RegionManager.getRegionsHasPlayerAsMember(player).stream().map(Region::getName).toList());
            }
        }

        return suggestions;
    }
}



