package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandBuilder implements CommandExecutor, TabCompleter {
	public final Homestead plugin = Homestead.getInstance();
	private final String name;
	private final String[] aliases;
	private final Map<String, SubCommandBuilder> subCommands = new HashMap<>();
	private String usage = "";

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

	public String getUsage() {
		return usage;
	}

	public CommandBuilder setUsage(String usage) {
		this.usage = usage;

		return this;
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

	public abstract boolean onDefaultExecution(CommandSender sender, String[] args);

	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return getAllSubCommands().stream()
				.filter(sub -> sub.hasPermission(sender, name))
				.map(SubCommandBuilder::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	@Override
	public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			return onDefaultExecution(sender, args);
		}

		SubCommandBuilder subCommand = getSubCommand(args[0]);

		if (subCommand == null) {
			return onDefaultExecution(sender, args);
		}

		if (subCommand.isPlayerOnly() && !(sender instanceof org.bukkit.entity.Player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!subCommand.hasPermission(sender, name)) {
			return onDefaultExecution(sender, args);
		}

		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

		return subCommand.onExecution(sender, subArgs);
	}

	@Override
	public final List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 1) {
			List<String> suggestions = onDefaultTabComplete(sender, args);

			String partial = args[0].toLowerCase();
			return suggestions.stream()
					.filter(s -> s.toLowerCase().startsWith(partial))
					.collect(Collectors.toList());
		}

		SubCommandBuilder subCommand = getSubCommand(args[0]);

		if (subCommand == null) {
			return new ArrayList<>();
		}

		if (!subCommand.hasPermission(sender, name)) {
			return new ArrayList<>();
		}

		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		return subCommand.onTabComplete(sender, subArgs);
	}

	public String getName() {
		return name;
	}

	public String[] getAliases() {
		return aliases;
	}

	protected Player asPlayer(CommandSender sender) {
		return sender instanceof Player ? (Player) sender : null;
	}
}