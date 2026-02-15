package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.*;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.List;

public abstract class LegacyCommandBuilder implements CommandExecutor, TabCompleter {
	public final Homestead plugin = Homestead.getInstance();
	private final String name;
	private String[] aliases = {};

	public LegacyCommandBuilder(String name) {
		this.name = name;
	}

	public LegacyCommandBuilder(String name, String... aliases) {
		this.name = name;
		this.aliases = aliases;
	}

	public static void register(LegacyCommandBuilder command) {
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

	public abstract boolean onExecution(CommandSender sender, String[] args);

	public abstract List<String> onAutoComplete(CommandSender sender, String[] args);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return onExecution(sender, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		return onAutoComplete(sender, args);
	}

	public String getName() {
		return name;
	}

	public String[] getAliases() {
		return aliases;
	}
}