package me.tayebyassine.homestead.commands.operator;

import me.tayebyassine.homestead.commands.CommandBuilder;
import me.tayebyassine.homestead.commands.operator.subcommands.*;
import me.tayebyassine.homestead.util.java.StringSimilarity;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;

/**
 * Admin command ({@code /homesteadadmin}, {@code /hsadmin}) used to manage the plugin
 * and perform administrative operations on regions.
 */
public final class HomesteadAdminCommand extends CommandBuilder {

    public HomesteadAdminCommand() {
        super("homesteadadmin", "hsadmin");

        setPermission("homestead.commands.homesteadadmin");
        setUsage("/hsadmin [sub-command]");

        registerSubCommand(new ExportSubCmd());
        registerSubCommand(new PluginSubCmd());
        registerSubCommand(new ReloadSubCmd());
        registerSubCommand(new CheckUpdatesSubCmd());
        registerSubCommand(new ImportSubCmd());
        registerSubCommand(new OverrideFlagSubCmd());
        registerSubCommand(new ClaimSubCmd());
        registerSubCommand(new UnclaimSubCmd());
        registerSubCommand(new TransferOwnershipSubCmd());
    }

    @Override
    public boolean onDefaultExecution(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return true;
        }

        String attempted = args[0].toLowerCase();
        String similarity = String.join(", ", StringSimilarity.find(getSubCommandNames(), attempted));

        Messages.send(sender, "commands.op_hsadmin.0", similarity);

        return true;
    }
}
