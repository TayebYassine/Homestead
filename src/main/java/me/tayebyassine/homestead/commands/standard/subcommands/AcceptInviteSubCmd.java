package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerJoinRegionEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionInvite;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs accept}) that lets a player accept a pending region invite.
 */
public final class AcceptInviteSubCmd extends SubCommandBuilder {

    public AcceptInviteSubCmd() {
        super("accept");
        setRegionPermission();
        setUsage("/hs accept [region]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.accept.0");
            return true;
        }

        Region region = RegionManager.findRegion(args[0]);

        if (region == null) {
            Messages.send(player, "commands.accept.1", args[0]);
            return true;
        }

        if (MemberManager.isMemberOfRegion(region, player)) {
            Messages.send(player, "commands.accept.2");
            return true;
        }

        if (!InviteManager.isInvited(region, player)) {
            Messages.send(player, "commands.accept.3");
            return true;
        }

        if (BanManager.isBanned(region, player)) {
            Messages.send(player, "commands.accept.4");
            return true;
        }

        if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
            Messages.send(player, "commands.accept.5");
            return true;
        }

        MemberManager.addMemberToRegion(player, region);
        LogManager.addLog(region, player, LogManager.PredefinedLog.JOIN_REGION);

        Messages.send(player, "commands.accept.6", args[0]);

        Homestead.callEvent(new PlayerJoinRegionEvent(region, player));

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
            suggestions.addAll(InviteManager.getInvitesOfPlayer(player).stream()
                    .map(RegionInvite::getRegionName)
                    .toList());
        }

        return suggestions;
    }
}



