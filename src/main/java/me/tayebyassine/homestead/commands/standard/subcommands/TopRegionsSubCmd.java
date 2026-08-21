package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.TopRegionsMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs top}) that opens the top regions menu.
 */
public final class TopRegionsSubCmd extends SubCommandBuilder {

    public TopRegionsSubCmd() {
        super("top");
        setRegionPermission();
        setUsage("/hs top");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        new TopRegionsMenu(player, true);

        return true;
    }
}



