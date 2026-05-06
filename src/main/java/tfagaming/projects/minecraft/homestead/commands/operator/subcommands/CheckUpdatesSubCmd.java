package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

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