package me.tayebyassine.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.util.https.UpdateChecker;

import java.util.List;

public class CheckUpdatesSubCmd extends SubCommandBuilder {
	public CheckUpdatesSubCmd() {
		super("updates");
		setPermission(List.of(
				"homestead.commands.homesteadadmin",
				"homestead.commands.homesteadadmin." + getName()
		));
		setUsage("/hsadmin updates");
		setConsoleOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Homestead.getInstance().runAsyncTask(() -> {
			String newVersion = UpdateChecker.fetch(Homestead.getInstance());

			if (newVersion != null) {
				Logger.warning(Logger.PredefinedMessage.UPDATE_FOUND);
			} else {
				Logger.info(Logger.PredefinedMessage.UPDATE_NOT_FOUND);
			}
		});

		return true;
	}
}