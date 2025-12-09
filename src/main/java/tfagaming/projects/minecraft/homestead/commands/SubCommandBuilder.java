package tfagaming.projects.minecraft.homestead.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;

public abstract class SubCommandBuilder implements CommandExecutor {
	public final Homestead plugin = Homestead.getInstance();
	private final String name;

	public SubCommandBuilder(String name) {
		this.name = name;
	}

	public abstract boolean onExecution(CommandSender sender, String[] args);

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return onExecution(sender, args);
	}

	public String getName() {
		return name;
	}
}