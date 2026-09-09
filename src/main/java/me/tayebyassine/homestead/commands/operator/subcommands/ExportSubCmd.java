package me.tayebyassine.homestead.commands.operator.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.database.Database;
import me.tayebyassine.homestead.database.Driver;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.util.java.ListUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin sub-command ({@code /hsadmin export}) that exports all plugin data from the active
 * database provider into a target provider, asynchronously.
 */
public final class ExportSubCmd extends SubCommandBuilder {

    public ExportSubCmd() {
        super("export");
        setAdminPermission();
        setUsage("/hsadmin export [provider]");
        setAllowedCommandSenders(CommandSenderType.CONSOLE);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Logger.error("Insufficient arguments, usage: ", getUsage());
            return true;
        }

        Driver provider = Driver.parse(args[0]);

        if (provider == null) {
            Logger.error("Incorrect provider provided.");
            return true;
        }

        if (Homestead.database.getProvider() == provider) {
            Logger.error("Provider already in use.");
            return true;
        }

        try {
            Logger.info("Please wait...");

            Logger.warning("-".repeat(70));
            for (int i = 0; i < 5; i++) {
                Logger.warning("THE DATA EXPORTER IS ASYNCHRONOUS, PLEASE DO **NOT** SHUT DOWN YOUR SERVER UNTIL YOU SEE \"Done!\".");
            }
            Logger.warning("-".repeat(70));

            final Database instance = new Database(provider);

            Homestead.getInstance().runAsyncTask(() -> {
                try {
                    instance.exportFromCache();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Logger.info("Done!");

                ListUtils.printTable(new String[]{"Model", "Exported"}, DataStats.dataRows());

                try {
                    instance.closeConnection();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            Logger.error(e);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(List.of("SQLite", "MySQL", "PostgreSQL", "MariaDB"));
        }

        return suggestions;
    }
}





