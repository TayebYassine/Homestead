package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.util.java.ListUtils;
import org.bukkit.command.CommandSender;

/**
 * Admin sub-command ({@code /hsadmin plugin}) that prints server software and plugin
 * data statistics to the console.
 */
public final class PluginSubCmd extends SubCommandBuilder {

    public PluginSubCmd() {
        super("plugin");
        setAdminPermission();
        setUsage("/hsadmin plugin");
        setAllowedCommandSenders(CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Logger.info("Please wait...");

        ListUtils.printTable(new String[]{"Property", "Value"}, DataStats.combine(DataStats.infoRows(), DataStats.dataRows()));

        return true;
    }
}





