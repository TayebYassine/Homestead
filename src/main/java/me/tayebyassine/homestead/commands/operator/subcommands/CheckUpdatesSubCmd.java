package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.util.https.UpdateChecker;
import org.bukkit.command.CommandSender;

/**
 * Admin sub-command ({@code /hsadmin updates}) that checks for a new plugin version.
 */
public final class CheckUpdatesSubCmd extends SubCommandBuilder {

    public CheckUpdatesSubCmd() {
        super("updates");
        setAdminPermission();
        setUsage("/hsadmin updates");
        setAllowedCommandSenders(CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Homestead.getInstance().runAsyncTask(() -> {
            String newVersion = UpdateChecker.fetch(Homestead.getInstance());

            if (newVersion != null) {
                Logger.warning(Logger.PredefinedMessage.UPDATE_FOUND);
            } else {
                Logger.info(Logger.PredefinedMessage.UPDATE_NOT_FOUND);
            }
        });

        return true;
    }
}





