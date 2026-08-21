package me.tayebyassine.homestead.commands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.commands.AutoCompleteFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for every top-level Bukkit command.
 *
 * <p>A command may hold any number of {@link SubCommandBuilder sub-commands}. When no sub-command is
 * matched, execution falls back to {@link #onDefaultExecution(CommandSender, String[])}.</p>
 */
public abstract class CommandBuilder implements CommandExecutor, TabCompleter {

    public final Homestead plugin = Homestead.getInstance();

    private final String name;
    private final String[] aliases;
    private final Map<String, SubCommandBuilder> subCommands = new HashMap<>();

    private String usage = "";
    private List<String> permissions = new ArrayList<>();
    private Set<CommandSenderType> allowedCommandSenders = EnumSet.of(CommandSenderType.PLAYER, CommandSenderType.CONSOLE);

    /**
     * Creates a command without aliases.
     *
     * @param name the command name, as declared in {@code plugin.yml}
     */
    public CommandBuilder(String name) {
        this(name, new String[0]);
    }

    /**
     * Creates a command with the given aliases.
     *
     * @param name    the command name, as declared in {@code plugin.yml}
     * @param aliases the command aliases, each also declared in {@code plugin.yml}
     */
    public CommandBuilder(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    /**
     * Registers a sub-command, together with all of its aliases, on this command.
     *
     * @param subCommand the sub-command to register
     */
    protected void registerSubCommand(SubCommandBuilder subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);

        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }

    /**
     * Resolves a sub-command by name or alias.
     *
     * @param name the sub-command name or alias
     * @return the matching sub-command, or {@code null} if none match
     */
    protected SubCommandBuilder getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }

    /**
     * @return a de-duplicated view of all registered sub-commands
     */
    protected Collection<SubCommandBuilder> getAllSubCommands() {
        return new HashSet<>(subCommands.values());
    }

    /**
     * @return a sorted list of the names of all registered sub-commands
     */
    protected List<String> getSubCommandNames() {
        return getAllSubCommands().stream()
                .map(SubCommandBuilder::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Replaces the permission list with a single permission.
     *
     * @param permission the required permission
     * @return this command, for chaining
     */
    public CommandBuilder setPermission(String permission) {
        this.permissions = Collections.singletonList(permission);
        return this;
    }

    /**
     * Replaces the permission list. All permissions must be held by the sender.
     *
     * @param permissions the required permissions
     * @return this command, for chaining
     */
    public CommandBuilder setPermission(List<String> permissions) {
        this.permissions = new ArrayList<>(permissions);
        return this;
    }

    /**
     * Sets the allowed executor types for this command.
     *
     * @param types the allowed executor types
     * @return this command, for chaining
     */
    public CommandBuilder setAllowedCommandSenders(CommandSenderType... types) {
        this.allowedCommandSenders = EnumSet.copyOf(Arrays.asList(types));
        return this;
    }

    /**
     * Restricts this command to players only.
     *
     * @return this command, for chaining
     */
    public CommandBuilder setPlayerOnly() {
        this.allowedCommandSenders = EnumSet.of(CommandSenderType.PLAYER);
        return this;
    }

    /**
     * Restricts this command to the console only.
     *
     * @return this command, for chaining
     */
    public CommandBuilder setConsoleOnly() {
        this.allowedCommandSenders = EnumSet.of(CommandSenderType.CONSOLE);
        return this;
    }

    /**
     * Checks whether the sender holds every required permission.
     *
     * @param sender the command sender
     * @return {@code true} if no permissions are required or the sender holds all of them
     */
    public boolean hasPermission(CommandSender sender) {
        if (permissions.isEmpty()) {
            return true;
        }
        return permissions.stream().allMatch(sender::hasPermission);
    }

    /**
     * Executes the command when no (matching) sub-command is provided.
     *
     * @param sender the command sender
     * @param args   the full command arguments
     * @return {@code true} to signal that the command was handled
     */
    public abstract boolean onDefaultExecution(CommandSender sender, String[] args);

    /**
     * Provides tab-completion suggestions for the default execution of this command.
     *
     * @param sender the command sender
     * @param args   the full command arguments
     * @return the list of suggestions
     */
    public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
        return getAllSubCommands().stream()
                .filter(sub -> sub.hasPermission(sender))
                .filter(sub -> sub.isAllowedCommandSender(sender))
                .map(SubCommandBuilder::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Checks if the given sender is allowed to execute this command.
     *
     * @param sender the command sender
     * @return {@code true} if the sender type is allowed
     */
    public boolean isAllowedCommandSender(CommandSender sender) {
        if (sender instanceof Player) {
            return allowedCommandSenders.contains(CommandSenderType.PLAYER);
        }
        return allowedCommandSenders.contains(CommandSenderType.CONSOLE);
    }

    /**
     * Gets a human-readable description of allowed executors.
     *
     * @return description string
     */
    public String getAllowedCommandSenderDescription() {
        if (allowedCommandSenders.contains(CommandSenderType.PLAYER) && allowedCommandSenders.contains(CommandSenderType.CONSOLE)) {
            return "players and console";
        } else if (allowedCommandSenders.contains(CommandSenderType.PLAYER)) {
            return "players";
        } else if (allowedCommandSenders.contains(CommandSenderType.CONSOLE)) {
            return "console";
        }
        return "no one";
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!hasPermission(sender)) {
            Messages.send(sender, "common.no_permission");
            return true;
        }

        if (!isAllowedCommandSender(sender)) {
            sender.sendMessage("This command can only be used by " + getAllowedCommandSenderDescription() + ".");
            return true;
        }

        if (args.length == 0) {
            return onDefaultExecution(sender, args);
        }

        SubCommandBuilder subCommand = getSubCommand(args[0]);

        if (subCommand == null) {
            return onDefaultExecution(sender, args);
        }

        if (!subCommand.isAllowedCommandSender(sender)) {
            sender.sendMessage("This command can only be used by " + subCommand.getAllowedCommandSenderDescription() + ".");
            return true;
        }

        if (!subCommand.hasPermission(sender)) {
            Messages.send(sender, "common.no_permission");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.onExecution(sender, subArgs);
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!hasPermission(sender)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> suggestions = onDefaultTabComplete(sender, args);
            return AutoCompleteFilter.filter(suggestions, args);
        }

        SubCommandBuilder subCommand = getSubCommand(args[0]);

        if (subCommand == null || !subCommand.hasPermission(sender)) {
            return new ArrayList<>();
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        List<String> suggestions = subCommand.onTabComplete(sender, subArgs);
        return AutoCompleteFilter.filter(suggestions, subArgs);
    }

    /**
     * @return the command name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the command aliases
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * @return the command usage string
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Sets the usage string shown to the player.
     *
     * @param usage the usage string
     * @return this command, for chaining
     */
    public CommandBuilder setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * @return an unmodifiable view of the required permissions
     */
    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    /**
     * @return {@code true} if this command is restricted to players only
     */
    public boolean isPlayerOnly() {
        return allowedCommandSenders.equals(EnumSet.of(CommandSenderType.PLAYER));
    }

    /**
     * @return {@code true} if this command is restricted to the console only
     */
    public boolean isConsoleOnly() {
        return allowedCommandSenders.equals(EnumSet.of(CommandSenderType.CONSOLE));
    }

    /**
     * @return an unmodifiable view of the allowed executor types
     */
    public Set<CommandSenderType> getallowedCommandSenders() {
        return Collections.unmodifiableSet(allowedCommandSenders);
    }

    /**
     * Casts the sender to a {@link Player} when possible.
     *
     * @param sender the command sender
     * @return the player, or {@code null} if the sender is not a player
     */
    protected Player asPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }
}

