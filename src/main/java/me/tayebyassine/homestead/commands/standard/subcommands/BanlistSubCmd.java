package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionBannedPlayers;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs banlist}) that shows the banned players of the current region,
 * optionally as a GUI ({@code /hs banlist gui}).
 */
public final class BanlistSubCmd extends SubCommandBuilder {

    public BanlistSubCmd() {
        super("banlist");
        setRegionPermission();
        setUsage("/hs banlist");
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
            Messages.send(player, "commands.banlist.0");
            return true;
        }

        if (args.length == 1 && args[0].equals("gui")) {
            new RegionBannedPlayers(player, region);
            return true;
        }

        Messages.send(player, "commands.banlist.1", region.getName(), BanManager.getBanCount(region));

        return true;
    }
}



