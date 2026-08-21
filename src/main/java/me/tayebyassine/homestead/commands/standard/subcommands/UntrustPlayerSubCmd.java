package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerLeftRegionEvent;
import me.tayebyassine.homestead.api.events.RevokePlayerInviteEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.managers.InviteManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionInvite;
import me.tayebyassine.homestead.models.RegionMember;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs untrust}) that revokes a player's invitation or membership
 * in the current region.
 */
public final class UntrustPlayerSubCmd extends SubCommandBuilder {

    public UntrustPlayerSubCmd() {
        super("untrust");
        setRegionPermission("homestead.actions.regions.players.untrust");
        setUsage("/hs untrust [player]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.untrust.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.untrust.1");
            return true;
        }

        if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                ControlFlags.UNTRUST_PLAYERS)) {
            return true;
        }

        String targetName = args[0];

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Messages.send(player, "commands.untrust.3", targetName);
            return true;
        }

        if (InviteManager.isInvited(region, target)) {
            InviteManager.deleteInvitesOfPlayer(region, target);

            Messages.send(player, "commands.untrust.4");

            Homestead.callEvent(new RevokePlayerInviteEvent(region, target));
        } else if (MemberManager.isMemberOfRegion(region, target)) {
            MemberManager.removeMemberFromRegion(target, region);

            Messages.send(player, "commands.untrust.5");

            LogManager.addLog(region, player, LogManager.PredefinedLog.UNTRUST_PLAYER, target.getName());

            Homestead.callEvent(new PlayerLeftRegionEvent(region, target));
        } else {
            Messages.send(player, "commands.untrust.6");
        }

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
            Region region = TargetRegionSession.getRegion(player);

            if (region != null) {
                for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
                    suggestions.add(member.getPlayerName());
                }

                suggestions.addAll(InviteManager.getInvitesOfRegion(region).stream()
                        .map(RegionInvite::getPlayerName).toList());
            }
        }

        return suggestions;
    }
}



