package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionClaimedChunks;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs claimlist}) that shows the claimed chunk count of the current region,
 * optionally as a GUI ({@code /hs claimlist gui}).
 */
public final class ClaimlistSubCmd extends SubCommandBuilder {

    public ClaimlistSubCmd() {
        super("claimlist");
        setRegionPermission();
        setUsage("/hs claimlist");
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
            Messages.send(player, "commands.claimlist.0");
            return true;
        }

        if (args.length == 1 && args[0].equals("gui")) {
            new RegionClaimedChunks(player, region);
            return true;
        }

        Messages.send(player, "commands.claimlist.2", region.getName(), ChunkManager.getChunkCount(region));

        return true;
    }
}



