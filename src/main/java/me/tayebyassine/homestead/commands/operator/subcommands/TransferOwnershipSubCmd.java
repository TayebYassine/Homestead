package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin sub-command ({@code /hsadmin transfer}) that transfers the ownership of a region
 * to another player.
 */
public final class TransferOwnershipSubCmd extends SubCommandBuilder {

    public TransferOwnershipSubCmd() {
        super("transfer");
        setAdminPermission();
        setUsage("/hsadmin transfer [region] [new-owner]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 2) {
            Messages.send(player, "commands.op_transfer.0", getUsage());
            return true;
        }

        Region region = RegionManager.findRegion(args[0]);

        if (region == null) {
            Messages.send(player, "commands.op_transfer.1");
            return true;
        }

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(args[1]);

        if (target == null) {
            Messages.send(player, "commands.op_transfer.2");
            return true;
        }

        if (region.isOwner(target)) {
            Messages.send(player, "commands.op_transfer.3");
            return true;
        }

        BanManager.unbanPlayer(region, target);
        InviteManager.deleteInvitesOfPlayer(region, target);
        MemberManager.removeMemberFromRegion(target, region);

        for (SubArea subArea : SubAreaManager.getSubAreasOfRegion(region)) {
            MemberManager.removeMemberFromSubArea(target, subArea);
        }

        region.setOwner(target);

        Messages.send(player, "commands.op_transfer.4");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(RegionManager.getRegionNames());
        } else if (args.length == 2) {
            suggestions.addAll(Homestead.getInstance().getOfflinePlayerNamesSync());
        }

        return suggestions;
    }
}





