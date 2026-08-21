package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionMenu;
import me.tayebyassine.homestead.gui.menus.RegionsMenu;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs menu}) that opens the region menu, or the region list
 * when no region is selected.
 */
public final class MenuSubCmd extends SubCommandBuilder {

    public MenuSubCmd() {
        super("menu");
        setRegionPermission();
        setUsage("/hs menu");
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
            new RegionsMenu(player);
        } else {
            new RegionMenu(player, region);
        }

        return true;
    }
}



