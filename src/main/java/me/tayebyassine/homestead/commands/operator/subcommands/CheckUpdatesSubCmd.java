package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.util.https.UpdateChecker;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;

/**
 * Admin sub-command ({@code /hsadmin updates}) that checks for a new plugin version.
 */
public final class CheckUpdatesSubCmd extends SubCommandBuilder {

    public CheckUpdatesSubCmd() {
        super("updates");
        setAdminPermission();
        setUsage("/hsadmin updates");
        setAllowedCommandSenders(CommandSenderType.PLAYER, CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Homestead.getInstance().runAsyncTask(() -> {
            UpdateChecker.FetchedUpdateData data = UpdateChecker.fetch();

            if (data.errored()) {
                Messages.send(sender, "commands.op_updates.2");
                return;
            }

            if (data.current().equals(data.latest())) {
                Messages.send(sender, "commands.op_updates.0");
            } else {
                Messages.send(sender, "commands.op_updates.1", data.current(), data.latest());
            }
        });

        return true;
    }
}





