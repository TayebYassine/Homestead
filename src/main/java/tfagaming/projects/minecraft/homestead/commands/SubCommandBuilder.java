package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommandBuilder {
	public final Homestead plugin = Homestead.getInstance();
	private final String name;
	private final String permission;
	private final String[] aliases;
	private final boolean playerOnly;

	public SubCommandBuilder(String name) {
		this(name, null, true);
	}

	public SubCommandBuilder(String name, String permission) {
		this(name, permission, true);
	}

	public SubCommandBuilder(String name, String permission, boolean playerOnly) {
		this(name, permission, playerOnly, new String[0]);
	}

	public SubCommandBuilder(String name, String permission, boolean playerOnly, String... aliases) {
		this.name = name.toLowerCase();
		this.permission = permission;
		this.playerOnly = playerOnly;
		this.aliases = aliases;
	}

	public abstract boolean onExecution(CommandSender sender, String[] args);

	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public String getPermission(String parentCommand) {
		return permission != null ? permission : "homestead.commands." + parentCommand + "." + name;
	}

	public String[] getAliases() {
		return aliases;
	}

	public boolean isPlayerOnly() {
		return playerOnly;
	}

	public boolean hasPermission(CommandSender sender, String parentCommand) {
		String perm = getPermission(parentCommand);
		return sender.hasPermission(perm);
	}

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

	protected Player asPlayer(CommandSender sender) {
		return sender instanceof Player ? (Player) sender : null;
	}
}