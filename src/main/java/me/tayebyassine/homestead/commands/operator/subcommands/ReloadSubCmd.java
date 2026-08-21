package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.resources.Resources;
import org.bukkit.command.CommandSender;

/**
 * Admin sub-command ({@code /hsadmin reload}) that reloads the plugin's resource files.
 */
public final class ReloadSubCmd extends SubCommandBuilder {

    public ReloadSubCmd() {
        super("reload");
        setAdminPermission();
        setUsage("/hsadmin reload");
        setAllowedCommandSenders(CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Homestead instance = Homestead.getInstance();

        Logger.info("Please wait...");

        try {
            Resources.load(instance);

            Logger.info("Done. Note that some changes may require a server restart.");
        } catch (Exception e) {
            Logger.error(e);
        }

        return true;
    }
}





