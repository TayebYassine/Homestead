package me.tayebyassine.homestead.commands.brigadier.builder;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for a Brigadier command tree registered through <a href="https://github.com/lucko/commodore">Commodore</a>.
 *
 * <p>This only improves tab-completion and argument validation on server software that supports
 * Mojang Brigadier; actual command execution is still handled by the Bukkit
 * {@link me.tayebyassine.homestead.commands.CommandBuilder} system.</p>
 */
public final class BrigadierCommandBuilder {

    private final String commandName;
    private final LiteralArgumentBuilder<Object> rootBuilder;
    private final List<BrigadierSubCommandBuilder> subCommands = new ArrayList<>();

    private BrigadierCommandBuilder(String commandName) {
        this.commandName = commandName;
        this.rootBuilder = LiteralArgumentBuilder.literal(commandName);
    }

    /**
     * Starts a new Brigadier command tree for the given command name.
     *
     * @param commandName the command name
     * @return the builder
     */
    public static BrigadierCommandBuilder create(String commandName) {
        return new BrigadierCommandBuilder(commandName);
    }

    /**
     * Appends a literal sub-command to the root node.
     *
     * @param name the literal name
     * @return the sub-command node
     */
    public BrigadierSubCommandBuilder literalSub(String name) {
        BrigadierSubCommandBuilder sub = new BrigadierSubCommandBuilder(this, name);
        subCommands.add(sub);
        return sub;
    }

    /**
     * Builds the command tree and registers it with Commodore for the plugin command of
     * {@link #commandName}.
     *
     * @param commodore the Commodore instance
     */
    public void register(Commodore commodore) {
        for (BrigadierSubCommandBuilder sub : subCommands) {
            rootBuilder.then(sub.build());
        }

        LiteralCommandNode<?> commandNode = rootBuilder.build();

        PluginCommand pluginCommand = Bukkit.getServer().getPluginCommand(commandName);

        if (pluginCommand != null) {
            commodore.register(pluginCommand, commandNode);
        }
    }
}
