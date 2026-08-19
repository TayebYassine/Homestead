package me.tayebyassine.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.resources.Resources;

import java.util.List;

public class ReloadSubCmd extends SubCommandBuilder {
	public ReloadSubCmd() {
		super("reload");
		setPermission(List.of(
				"homestead.commands.homesteadadmin",
				"homestead.commands.homesteadadmin." + getName()
		));
		setUsage("/hsadmin reload");
		setConsoleOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Homestead instance = Homestead.getInstance();

		Logger.info("Please wait...");

		try {
			Resources.load(instance);

			Logger.info("Done. Note that some changes may require a server restart.");
		} catch (Exception e) {
			Logger.error(e);
		}

		return true;
	}
}