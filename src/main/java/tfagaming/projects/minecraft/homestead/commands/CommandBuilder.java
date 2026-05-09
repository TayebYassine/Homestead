package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.commands.AutoCompleteFilter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandBuilder implements CommandExecutor, TabCompleter {
	public final Homestead plugin = Homestead.getInstance();
	private final String name;
	private final String[] aliases;
	private final Map<String, SubCommandBuilder> subCommands = new HashMap<>();
	private String usage = "";
	private List<String> permissions = new ArrayList<>();
	private boolean playerOnly = false;
	private boolean consoleOnly = false;

	public CommandBuilder(String name) {
		this.name = name;
		this.aliases = new String[0];
	}

	public CommandBuilder(String name, String... aliases) {
		this.name = name;
		this.aliases = aliases;
	}

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

	protected void registerSubCommand(SubCommandBuilder subCommand) {
		subCommands.put(subCommand.getName().toLowerCase(), subCommand);

		for (String alias : subCommand.getAliases()) {
			subCommands.put(alias.toLowerCase(), subCommand);
		}
	}

	protected SubCommandBuilder getSubCommand(String name) {
		return subCommands.get(name.toLowerCase());
	}

	protected Collection<SubCommandBuilder> getAllSubCommands() {
		return new HashSet<>(subCommands.values());
	}

	protected List<String> getSubCommandNames() {
		return getAllSubCommands().stream()
				.map(SubCommandBuilder::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	public CommandBuilder setPermission(String permission) {
		this.permissions = Collections.singletonList(permission);
		return this;
	}

	public CommandBuilder setPermission(List<String> permissions) {
		this.permissions = new ArrayList<>(permissions);
		return this;
	}

	public CommandBuilder setPlayerOnly() {
		this.playerOnly = true;
		this.consoleOnly = false;
		return this;
	}

	public CommandBuilder setConsoleOnly() {
		this.consoleOnly = true;
		this.playerOnly = false;
		return this;
	}

	public boolean hasPermission(CommandSender sender) {
		if (permissions.isEmpty()) return true;
		return permissions.stream().allMatch(sender::hasPermission);
	}

	public abstract boolean onDefaultExecution(CommandSender sender, String[] args);

	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return getAllSubCommands().stream()
				.filter(sub -> sub.hasPermission(sender) && sub.isPlayerOnly())
				.map(SubCommandBuilder::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	@Override
	public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!hasPermission(sender)) {
			return onDefaultExecution(sender, args);
		}

		if (playerOnly && !(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (consoleOnly && sender instanceof Player) {
			sender.sendMessage("This command can only be used from the console.");
			return true;
		}

		if (args.length == 0) {
			return onDefaultExecution(sender, args);
		}

		SubCommandBuilder subCommand = getSubCommand(args[0]);

		if (subCommand == null) {
			return onDefaultExecution(sender, args);
		}

		if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (subCommand.isConsoleOnly() && sender instanceof Player) {
			sender.sendMessage("This command can only be used from the console.");
			return true;
		}

		if (!subCommand.hasPermission(sender)) {
			return onDefaultExecution(sender, args);
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

		if (subCommand == null) {
			return new ArrayList<>();
		}

		if (!subCommand.hasPermission(sender)) {
			return new ArrayList<>();
		}

		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		List<String> suggestions = subCommand.onTabComplete(sender, subArgs);
		return AutoCompleteFilter.filter(suggestions, subArgs);
	}

	public String getName() {
		return name;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getUsage() {
		return usage;
	}

	public CommandBuilder setUsage(String usage) {
		this.usage = usage;
		return this;
	}

	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}

	public boolean isPlayerOnly() {
		return playerOnly;
	}

	public boolean isConsoleOnly() {
		return consoleOnly;
	}

	protected Player asPlayer(CommandSender sender) {
		return sender instanceof Player ? (Player) sender : null;
	}

	public void reply(CommandSender sender, String path, Object... args) {
		String message = Resources.<LanguageFile>get(ResourceType.Language).getString("commands." + path, "NULL");

		Messages.send(sender, message, args);
	}
}