package me.tayebyassine.homestead.commands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.operator.ForceUnclaimCommand;
import me.tayebyassine.homestead.commands.operator.HomesteadAdminCommand;
import me.tayebyassine.homestead.commands.standard.ClaimCommand;
import me.tayebyassine.homestead.commands.standard.RegionCommand;
import me.tayebyassine.homestead.commands.standard.UnclaimCommand;
import org.bukkit.command.PluginCommand;

/**
 * Central registry for all top-level Homestead commands.
 *
 * <p>Every command (and its aliases) is bound to the plugin's {@link org.bukkit.command.CommandExecutor}
 * and {@link org.bukkit.command.TabCompleter} here.</p>
 */
public final class CommandRegistry {

    private CommandRegistry() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Registers every top-level command of the plugin.
     */
    public static void registerAll() {
        register(new RegionCommand());
        register(new ClaimCommand());
        register(new UnclaimCommand());
        register(new HomesteadAdminCommand());
        register(new ForceUnclaimCommand());
    }

    /**
     * Binds a single command, together with all of its aliases, to the plugin.
     *
     * @param command the command to register
     */
    public static void register(CommandBuilder command) {
        PluginCommand bukkitCommand = Homestead.getInstance().getCommand(command.getName());

        if (bukkitCommand != null) {
            bukkitCommand.setExecutor(command);
            bukkitCommand.setTabCompleter(command);
        }

        for (String alias : command.getAliases()) {
            PluginCommand bukkitCommandAlias = Homestead.getInstance().getCommand(alias);

            if (bukkitCommandAlias != null) {
                bukkitCommandAlias.setExecutor(command);
                bukkitCommandAlias.setTabCompleter(command);
            }
        }
    }
}
