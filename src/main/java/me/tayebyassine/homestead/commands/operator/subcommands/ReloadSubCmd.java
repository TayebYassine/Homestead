package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;

/**
 * Admin sub-command ({@code /hsadmin reload}) that reloads the plugin's resource files.
 */
public final class ReloadSubCmd extends SubCommandBuilder {

    public ReloadSubCmd() {
        super("reload");
        setAdminPermission();
        setUsage("/hsadmin reload");
        setAllowedCommandSenders(CommandSenderType.PLAYER, CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Homestead instance = Homestead.getInstance();

        Messages.send(sender, "commands.op_reload.0");

        try {
            Resources.load(instance);

            Messages.send(sender, "commands.op_reload.1");
        } catch (Exception e) {
            Messages.send(sender, "commands.op_reload.2");

            Logger.error(e);
        }

        return true;
    }
}





