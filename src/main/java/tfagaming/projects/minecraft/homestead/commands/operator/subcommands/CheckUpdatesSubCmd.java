package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class CheckUpdatesSubCmd extends SubCommandBuilder {
	public CheckUpdatesSubCmd() {
		super("updates", null, false);
		setUsage("/hsadmin updates");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Messages.send(sender, 98);

		Homestead.getInstance().runAsyncTask(() -> {
			String newVersion = UpdateChecker.fetch(Homestead.getInstance());

			if (newVersion != null) {
				Messages.send(sender, 97);
			} else {
				Messages.send(sender, 96);
			}
		});

		return true;
	}
}