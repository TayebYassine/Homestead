package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.MergeRegionSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs mergeaccept}) that accepts a pending region merge request.
 */
public final class MergeAcceptRegionSubCmd extends SubCommandBuilder {

    public MergeAcceptRegionSubCmd() {
        super("mergeaccept");
        setRegionPermission("homestead.actions.regions.merge");
        setUsage("/hs mergeaccept");
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
            Messages.send(player, "commands.mergeaccept.0");
            return true;
        }

        if (!MergeRegionSession.isToHaveRequest(region)) {
            Messages.send(player, "commands.mergeaccept.1");
            return true;
        }

        if (!PlayerUtility.isOperator(player) && !region.isOwner(player)) {
            Messages.send(player, "commands.mergeaccept.2");
            return true;
        }

        Region from = RegionManager.findRegion(MergeRegionSession.getFrom(region));

        if (from == null) {
            Messages.send(player, "commands.mergeaccept.3");
            return true;
        }

        RegionManager.mergeRegions(from, region);

        Messages.send(player, "commands.mergeaccept.4");

        return true;
    }
}



