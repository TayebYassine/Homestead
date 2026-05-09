package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SubCommandBuilder {
	public final Homestead plugin = Homestead.getInstance();
	private final String name;
	private final String[] aliases;
	private String usage = "";
	private List<String> permissions = new ArrayList<>();
	private boolean playerOnly = true;
	private boolean consoleOnly = false;

	public SubCommandBuilder(String name) {
		this(name, new String[0]);
	}

	public SubCommandBuilder(String name, String... aliases) {
		this.name = name.toLowerCase();
		this.aliases = aliases;
	}

	public abstract boolean onExecution(CommandSender sender, String[] args);

	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}

	public SubCommandBuilder setPermission(String permission) {
		this.permissions = Collections.singletonList(permission);
		return this;
	}

	public SubCommandBuilder setPermission(List<String> permissions) {
		this.permissions = new ArrayList<>(permissions);
		return this;
	}

	public SubCommandBuilder setPlayerOnly() {
		this.playerOnly = true;
		this.consoleOnly = false;
		return this;
	}

	public SubCommandBuilder setConsoleOnly() {
		this.consoleOnly = true;
		this.playerOnly = false;
		return this;
	}

	public boolean hasPermission(CommandSender sender) {
		if (permissions.isEmpty() || isConsoleOnly()) return true;
		return permissions.stream().allMatch(sender::hasPermission);
	}

	public boolean matches(String input) {
		if (name.equalsIgnoreCase(input)) return true;

		for (String alias : aliases) {
			if (alias.equalsIgnoreCase(input)) return true;
		}

		return false;
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

	public SubCommandBuilder setUsage(String usage) {
		this.usage = usage;
		return this;
	}

	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}

	public boolean isPlayerOnly() {
		return playerOnly;
	}

	public SubCommandBuilder setPlayerOnly(boolean playerOnly) {
		this.playerOnly = playerOnly;
		if (playerOnly) this.consoleOnly = false;
		return this;
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