package me.tayebyassine.homestead.commands;

import me.tayebyassine.homestead.Homestead;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for every sub-command registered on a {@link CommandBuilder}.
 *
 * <p>Sub-commands are looked up by name or alias (case-insensitive). A sub-command may declare
 * its own permissions, execution context (player only / console only) and usage string.</p>
 */
public abstract class SubCommandBuilder {

    public final Homestead plugin = Homestead.getInstance();

    private final String name;
    private final String[] aliases;

    private String usage = "";
    private List<String> permissions = new ArrayList<>();
    private Set<CommandSenderType> allowedCommandSenders = EnumSet.of(CommandSenderType.PLAYER);

    /**
     * Creates a sub-command without aliases.
     *
     * @param name the sub-command name
     */
    public SubCommandBuilder(String name) {
        this(name, new String[0]);
    }

    /**
     * Creates a sub-command with the given aliases.
     *
     * @param name    the sub-command name
     * @param aliases additional names this sub-command responds to
     */
    public SubCommandBuilder(String name, String... aliases) {
        this.name = name.toLowerCase();
        this.aliases = aliases;
    }

    /**
     * Executes this sub-command.
     *
     * @param sender the command sender
     * @param args   the arguments following the sub-command name
     * @return {@code true} to signal that the command was handled
     */
    public abstract boolean onExecution(CommandSender sender, String[] args);

    /**
     * Provides tab-completion suggestions for this sub-command.
     *
     * @param sender the command sender
     * @param args   the arguments following the sub-command name
     * @return the list of suggestions
     */
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    /**
     * Replaces the permission list with a single permission.
     *
     * @param permission the required permission
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setPermission(String permission) {
        this.permissions = Collections.singletonList(permission);
        return this;
    }

    /**
     * Replaces the permission list. All permissions must be held by the sender.
     *
     * @param permissions the required permissions
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setPermission(List<String> permissions) {
        this.permissions = new ArrayList<>(permissions);
        return this;
    }

    /**
     * Convenience helper that builds the standard {@code region} permission set:
     * {@code homestead.commands.region} and {@code homestead.commands.region.<name>},
     * followed by any extra permissions.
     *
     * @param extraPermissions additional permissions, may be empty
     * @return this sub-command, for chaining
     */
    protected SubCommandBuilder setRegionPermission(String... extraPermissions) {
        return setPermission(buildPermissionSet("homestead.commands.region", extraPermissions));
    }

    /**
     * Convenience helper that builds the standard admin permission set:
     * {@code homestead.commands.homesteadadmin} and
     * {@code homestead.commands.homesteadadmin.<name>}, followed by any extra permissions.
     *
     * @param extraPermissions additional permissions, may be empty
     * @return this sub-command, for chaining
     */
    protected SubCommandBuilder setAdminPermission(String... extraPermissions) {
        return setPermission(buildPermissionSet("homestead.commands.homesteadadmin", extraPermissions));
    }

    private List<String> buildPermissionSet(String base, String... extraPermissions) {
        List<String> permissions = new ArrayList<>(List.of(base, base + "." + name));
        permissions.addAll(Arrays.asList(extraPermissions));
        return permissions;
    }

    /**
     * Sets the allowed executor types for this sub-command.
     *
     * @param types the allowed executor types
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setAllowedCommandSenders(CommandSenderType... types) {
        this.allowedCommandSenders = EnumSet.copyOf(Arrays.asList(types));
        return this;
    }

    /**
     * Restricts this sub-command to players only.
     *
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setPlayerOnly() {
        this.allowedCommandSenders = EnumSet.of(CommandSenderType.PLAYER);
        return this;
    }

    /**
     * Restricts this sub-command to the console only.
     *
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setConsoleOnly() {
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
     * Checks whether this sub-command matches the given input (name or alias).
     *
     * @param input the raw input
     * @return {@code true} if the input matches the name or any alias
     */
    public boolean matches(String input) {
        if (name.equalsIgnoreCase(input)) {
            return true;
        }

        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(input)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return the sub-command name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the sub-command aliases
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * @return the sub-command usage string
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Sets the usage string shown to the player.
     *
     * @param usage the usage string
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setUsage(String usage) {
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
     * @return {@code true} if this sub-command is restricted to players only
     */
    public boolean isPlayerOnly() {
        return allowedCommandSenders.equals(EnumSet.of(CommandSenderType.PLAYER));
    }

    /**
     * Sets whether this sub-command is restricted to players.
     *
     * @param playerOnly {@code true} to restrict to players
     * @return this sub-command, for chaining
     */
    public SubCommandBuilder setPlayerOnly(boolean playerOnly) {
        if (playerOnly) {
            this.allowedCommandSenders = EnumSet.of(CommandSenderType.PLAYER);
        } else {
            this.allowedCommandSenders = EnumSet.of(CommandSenderType.PLAYER, CommandSenderType.CONSOLE);
        }
        return this;
    }

    /**
     * @return {@code true} if this sub-command is restricted to the console only
     */
    public boolean isConsoleOnly() {
        return allowedCommandSenders.equals(EnumSet.of(CommandSenderType.CONSOLE));
    }

    /**
     * Checks if the given sender is allowed to execute this sub-command.
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

